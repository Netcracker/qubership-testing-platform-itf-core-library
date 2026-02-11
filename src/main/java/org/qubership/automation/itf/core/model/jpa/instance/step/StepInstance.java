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

package org.qubership.automation.itf.core.model.jpa.instance.step;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.SpContext;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractContainerInstance;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractInstance;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.util.constants.Mep;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@JsonFilter("reportWorkerFilter_StepInstance")
public class StepInstance extends AbstractInstance {
    private static final long serialVersionUID = 20240812L;

    private BigInteger stepId;
    private Step step;

    private int currentValidAttemptValue;
    private int currentCondAttemptValue;
    private boolean isRetryStep;

    public StepInstance() {
        getContext().setSP(new SpContext(this));
    }

    @Override
    public Storable getSource() {
        return step;
    }

    /**
     * Init stepInstance and the corresponding SpContext from step.
     */
    public void init(Step step) {
        this.stepId = (BigInteger) step.getID();
        this.step = step;
        setName(step.getName());
        getContext().setSP(new SpContext(this));
    }

    /**
     * Init stepInstance and the corresponding SpContext from step, InstanceContext and SpContext.
     */
    public void init(Step step, InstanceContext context, SpContext spContext) {
        this.stepId = (BigInteger) step.getID();
        this.step = step;
        getContext().putAll(context);
        if (spContext == null) {
            spContext = new SpContext(this);
        }
        getContext().setSP(spContext);
        getContext().sp().setParent(getContext());
        getContext().setProjectUuid(context.getProjectUuid());
    }

    @Transient
    @JsonIgnore
    public Step getStep() {
        return step;
    }

    public void setStep(Step step) {
        this.step = step;
    }

    public void setStepId(BigInteger stepId) {
        this.stepId = stepId;
    }

    //@JsonRef
    public BigInteger getStepId() {
        return stepId;
    }

    @Override
    public AbstractContainerInstance getParent() {
        return (AbstractContainerInstance) super.getParent();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof StepInstance && (obj == this || ((StepInstance) obj).getParent() == getParent()
                && ((StepInstance) obj).getStepId().equals(stepId));
    }

    @Override
    public int hashCode() {
        return ("StepInstance" + getParent().getID() + stepId).hashCode();
    }

    @JsonIgnore
    public Mep getMep() {
        return getStep() == null ? null : getStep().getMep();
    }

    @JsonIgnore
    public Message getIncomingMessage() {
        return getContext().sp().getIncomingMessage();
    }

    public void setIncomingMessage(Message incomingMessage) {
        getContext().sp().setIncomingMessage(incomingMessage);
    }

    @JsonIgnore
    public Message getOutgoingMessage() {
        return getContext().sp().getOutgoingMessage();
    }

    public void setOutgoingMessage(Message outgoingMessage) {
        getContext().sp().setOutgoingMessage(outgoingMessage);
    }

    @JsonIgnore
    public int getCurrentValidAttemptValue() {
        return currentValidAttemptValue;
    }

    public void setCurrentValidAttemptValue(int currentValidAttemptValue) {
        this.currentValidAttemptValue = currentValidAttemptValue;
    }

    @JsonIgnore
    public int getCurrentCondAttemptValue() {
        return currentCondAttemptValue;
    }

    public void setCurrentCondAttemptValue(int currentCondAttemptValue) {
        this.currentCondAttemptValue = currentCondAttemptValue;
    }

    @JsonIgnore
    public boolean isRetryStep() {
        return isRetryStep;
    }

    public void setIsRetryStep(boolean isRetryStep) {
        this.isRetryStep = isRetryStep;
    }

    @Override
    public Map<String, String> getTransportConfiguration() {
        HashMap<String, String> transportConfigurationMap = new HashMap<>();
        if (this.getStep() != null && ((IntegrationStep) this.getStep()).getOperation() != null) {
            TransportConfiguration transportConfiguration = ((IntegrationStep) this.getStep())
                    .getOperation().getTransport();
            if (transportConfiguration != null) {
                transportConfigurationMap.put("transportName", transportConfiguration.getName());
                transportConfigurationMap.put("transportType", transportConfiguration.getTypeName()
                        .replaceAll("\\w+\\.", ""));
                transportConfigurationMap.put("transportMEP", transportConfiguration.getMep().toString());
            }
        }
        return transportConfigurationMap;
    }
}
