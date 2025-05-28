/*
 *  Copyright 2024-2025 NetCracker Technology Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.qubership.automation.itf.core.model.jpa.context;

import java.math.BigInteger;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;
import javax.persistence.Transient;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractContainerInstance;
import org.qubership.automation.itf.core.model.jpa.instance.SituationInstance;
import org.qubership.automation.itf.core.util.annotation.JsonRef;
import org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants;
import org.qubership.automation.itf.core.util.constants.StartedFrom;
import org.qubership.automation.itf.core.util.constants.Status;
import org.qubership.automation.itf.core.util.helper.FailTimeOut;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.services.CoreServices;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

@JsonInclude()
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Entity
@JsonFilter("reportWorkerFilter_TCContext")
public class TcContext extends JsonStorable {
    private static final long serialVersionUID = 20240812L;

    public static final String TC = "tc";
    private static final transient DateTimeFormatter dateTimeFormatter = DateTimeFormatter
            .ofPattern("dd.MM.yy HH:mm:ss.SSS").withZone(ZoneId.systemDefault()).withLocale(Locale.ENGLISH);
    @Getter
    @Setter
    BigInteger environmentId;
    @Getter
    @Setter
    private String environmentName;

    private List<AbstractContainerInstance> instances = Lists.newArrayListWithExpectedSize(20);
    private AbstractContainerInstance initiator;

    @Setter
    @Getter
    private BigInteger projectId;

    @Setter
    @Getter
    private UUID projectUuid;

    @Setter
    @Getter
    private Status status = Status.NOT_STARTED;

    @Setter(lombok.AccessLevel.PROTECTED)
    @Getter
    private Set<String> bindingKeys = Sets.newHashSet();

    @Setter(lombok.AccessLevel.PROTECTED)
    @Getter
    private Map<String, String> reportLinks = Maps.newHashMapWithExpectedSize(5);

    private boolean startedByAtp;
    private boolean needToReportToAtp;
    private boolean needToReportToItf;
    private boolean startValidation;
    private StartedFrom startedFrom;
    private boolean runStepByStep;

    @Setter
    @Getter
    private String client;

    private boolean validationFailed;

    @Setter
    @Getter
    private Date endTime;
    private transient boolean isFailEventSent;
    private transient boolean isFinishEventSent;
    private transient boolean isNotified;

    @Getter
    @Setter
    private long lastUpdateTime = 0L;
    @Getter
    @Setter
    private long timeToLive = 0L;
    @Getter
    @Setter
    private String podName = null;

    @Getter
    @Setter
    private int partNum;

    public void setTimeToLive() {
        this.timeToLive = FailTimeOut.getTimeout(projectId);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public TcContext() {
        super();
        Date crDate = new Date();
        // Name pattern is changed from "Context started..." - it's more accurate. And date format is set to Russian
        setName("Context created at " + crDate.toInstant().atZone(ZoneId.systemDefault()).format(dateTimeFormatter));
        // May be, it's not so accurate, but... Context rows (without startTime) in the monitoring table are missable
        // and not sortable by date
        setStartTime(crDate);
        this.isFailEventSent = false;
        this.isFinishEventSent = false;
        this.isNotified = false;
    }

    public TcContext(Storable parent, Map parameters) {
        this();
    }

    public void fillBindingKeys(Set<String> renderendContextKeys) {
        StorableUtils.fillCollection(getBindingKeys(), renderendContextKeys);
    }

    public void fillReportLinks(Map<String, String> reportLinks) {
        StorableUtils.fillMap(getReportLinks(), reportLinks);
    }

    public boolean isRunning() {
        return Status.IN_PROGRESS.equals(status);
    }

    /**
     * Method returns true if state change from the current state to IN_PROGRESS is valid.
     * Scope of the method:
     * - To control state manipulations while context execution, e.g. in the SituationExecutor.
     *
     * <p>So, direct state manipulations made by a user from UI (re-Run Failed/Stopped context via Context popup or
     * Pause/Resume) are not controlled by this method.
     */
    public boolean isRunnable() {
        return Status.NOT_STARTED.equals(status) || Status.PAUSED.equals(status);
    }

    public boolean isFinished() {
        return Status.PASSED.equals(status) || Status.FAILED.equals(status) || Status.STOPPED
                .equals(status) || Status.FAILED_BY_TIMEOUT.equals(status);
    }

    @JsonIgnore
    public List<AbstractContainerInstance> getInstances() {
        return instances;
    }

    protected void setInstances(List<AbstractContainerInstance> instances) {
        this.instances = instances;
    }

    public void fillInstances(List<AbstractContainerInstance> instances) {
        StorableUtils.fillCollection(getInstances(), instances);
    }

    @JsonRef
    public AbstractContainerInstance getInitiator() {
        return initiator;
    }

    public void setInitiator(AbstractContainerInstance initiator) {
        this.initiator = initiator;
    }

    /**
     * Compute duration of the context execution.
     *
     * @return computed duration as Date object. In case endTime or startTime are null, return current system Date
     */
    @Transient
    @JsonIgnore
    public Date getDuration() {
        /* @JsonIgnore annotation is added in order to avoid strange exceptions
        while object serialization in ReportWorker#run
            Exception was NullPointerException at TcContext["duration"] */
        return new Date(endTime == null || startTime == null
                ? System.currentTimeMillis() : endTime.getTime() - startTime.getTime());
    }

    /**
     * Compute duration of the context execution.
     *
     * @return Int - computed duration in minutes.
     *     In case startTime is null, return 0;
     *     Otherwize, in case endTime is null, System.currentTimeMillis() is used in calculations.
     *     Calculated value is increased by 1.
     */
    @Transient
    @JsonIgnore
    public int getDurationMinutes() {
        if (startTime == null) {
            return 0;
        } else if (endTime == null) {
            return (int) ((System.currentTimeMillis() - startTime.getTime()) / (1000 * 60) + 1);
        } else {
            return (int) ((endTime.getTime() - startTime.getTime()) / (1000 * 60) + 1);
        }
    }

    @Transient
    @JsonIgnore
    public boolean isStartValidation() {
        return startValidation;
    }

    public void setStartValidation(boolean startValidation) {
        this.startValidation = startValidation;
    }

    @Transient
    @JsonIgnore
    public Environment getEnvironmentById() {
        return CoreObjectManager.getInstance().getManager(Environment.class).getById(getEnvironmentId());
    }

    @Transient
    @JsonIgnore
    public boolean getStartedByAtp() {
        return startedByAtp;
    }

    public void setStartedByAtp(boolean startedByAtp) {
        this.startedByAtp = startedByAtp;
    }

    public boolean isNeedToReportToAtp() {
        return needToReportToAtp;
    }

    public void setNeedToReportToAtp(boolean needToReportToAtp) {
        this.needToReportToAtp = needToReportToAtp;
    }

    @Transient
    @JsonIgnore
    public boolean isNeedToReportToItf() {
        return needToReportToItf;
    }

    public void setNeedToReportToItf(boolean needToReportToItf) {
        this.needToReportToItf = needToReportToItf;
    }

    /**
     * Calculate and set needToReportToItf field.
     */
    public void setAndCalculateNeedToReportToItf() {
        /*
         * If context initiator instanceof SituationInstance:
         *  - this is stub context, so itf reporting is enabled, (needToReportToItf = true).
         *      - project setting "enable.itf.reporting" doesn't affect result.
         * Otherwise, if project setting "enable.itf.reporting" = false and startedFrom = ATP or RAM2:
         *  - itf reporting is disabled (needToReportToItf = false).
         * Otherwise - itf reporting is enabled (needToReportToItf = true).
         */
        if (getInitiator() instanceof SituationInstance) {
            setNeedToReportToItf(true);
        } else if (StartedFrom.RAM2.equals(getStartedFrom()) || StartedFrom.ATP.equals(getStartedFrom())
                || getStartedByAtp()) {
            setNeedToReportToItf(Boolean.parseBoolean(CoreServices.getProjectSettingsService().get(getProjectId(),
                    ProjectSettingsConstants.ENABLE_ITF_REPORTING,
                    ProjectSettingsConstants.ENABLE_ITF_REPORTING_DEFAULT_VALUE)));
        } else {
            setNeedToReportToItf(true);
        }
    }

    @Transient
    @JsonIgnore
    public StartedFrom getStartedFrom() {
        return startedFrom;
    }

    public void setStartedFrom(StartedFrom startedFrom) {
        this.startedFrom = startedFrom;
    }

    @Transient
    @JsonIgnore
    public Boolean isFailEventSent() {
        return isFailEventSent;
    }

    public void setFailEventAsSent() {
        isFailEventSent = true;
    }

    @Transient
    @JsonIgnore
    public Boolean isFinishEventSent() {
        return isFinishEventSent;
    }

    public void setFinishEventAsSent() {
        isFinishEventSent = true;
    }

    @Transient
    @JsonIgnore
    public boolean isNotified() {
        return isNotified;
    }

    public void setNotified(boolean notified) {
        isNotified = notified;
    }

    @Override
    public boolean equals(Object anObject) {
        if (anObject instanceof TcContext) {
            return this.getID().toString().equals(((TcContext) anObject).getID().toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(getID());
    }

    public boolean isRunStepByStep() {
        return runStepByStep;
    }

    public void setRunStepByStep(boolean runStepByStep) {
        this.runStepByStep = runStepByStep;
    }

    public boolean isValidationFailed() {
        return validationFailed;
    }

    public void setValidationFailed(boolean validationFailed) {
        this.validationFailed = validationFailed;
    }
}
