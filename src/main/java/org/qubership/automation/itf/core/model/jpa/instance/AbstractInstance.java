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

package org.qubership.automation.itf.core.model.jpa.instance;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Entity;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.extension.ExtendableImpl;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.instance.chain.CallChainInstance;
import org.qubership.automation.itf.core.model.jpa.instance.step.StepInstance;
import org.qubership.automation.itf.core.util.annotation.JsonRef;
import org.qubership.automation.itf.core.util.constants.Status;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = SituationInstance.class, name = "SituationInstance"),
        @JsonSubTypes.Type(value = CallChainInstance.class, name = "CallChainInstance"),
        @JsonSubTypes.Type(value = StepInstance.class, name = "StepInstance")
})
public abstract class AbstractInstance extends ExtendableImpl {

    private Status status;
    private Date startTime;
    private Date endTime;
    private InstanceContext context = new InstanceContext(this);
    private Throwable error;
    private String errorName;
    private String errorMessage;
    private AbstractInstance parent;
    private Integer partNum;

    public AbstractInstance() {
        status = Status.NOT_STARTED;
    }

    @JsonIgnore
    public Throwable getError() {
        return error;
    }

    /**
     * Fill error, errorName and errorMessage with error info.
     */
    public void setError(Throwable error) {
        this.error = error;
        this.errorName = error.getMessage();
        this.errorMessage = ExceptionUtils.getStackTrace(error);
    }

    public boolean isRunning() {
        return Status.IN_PROGRESS.equals(status);
    }

    public boolean isFinished() {
        return Status.FAILED.equals(status) || Status.PASSED.equals(status)
                || Status.STOPPED.equals(status) || Status.FAILED_BY_TIMEOUT.equals(status);
    }

    @JsonRef
    public abstract Storable getSource();

    public Map<String, String> getTransportConfiguration() {
        return new HashMap<>();
    }

    @Override
    public String toString() {
        return String.format("Instance{%s}", getSource());
    }

    public void destroy() {
        context = null;
        protectedDestroy();
    }

    protected void protectedDestroy() {
    }
}
