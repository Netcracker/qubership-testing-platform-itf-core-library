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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.jpa.instance.step.StepInstance;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.parser.MessageParameter;
import org.qubership.automation.itf.core.util.annotation.JsonRef;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.google.common.collect.Lists;

@Entity
@JsonFilter("reportWorkerFilter_SPContext")
public class SpContext extends JsonStorable {
    private static final long serialVersionUID = 20240812L;

    public static final String SP = "sp";

    private List<MessageParameter> messageParameters;
    private StepInstance step;
    private Message incomingMessage;
    private Message outgoingMessage;
    private String validationResults;

    /**
     * Constructor of SpContext for step parameter.
     */
    public SpContext(StepInstance step) {
        this.step = step;
        if (step != null) {
            setParent(step.getContext());
        }
    }

    public SpContext() {
        setStartTime(new Date());
    }

    public List<MessageParameter> getMessageParameters() {
        return messageParameters;
    }

    protected void setMessageParameters(List<MessageParameter> messageParameters) {
        this.messageParameters = messageParameters;
    }

    /**
     * Put all message parameters into the context.
     */
    public void putMessageParameters(Collection<MessageParameter> messageParameters) {
        if (getMessageParameters() == null) {
            setMessageParameters(Lists.newArrayListWithExpectedSize(messageParameters.size() * 2));
        }
        getMessageParameters().addAll(messageParameters);
        for (MessageParameter parameter : messageParameters) {
            parameter.setParent(this);
            put(parameter.getParamName(),
                    parameter.isMultiple() ? parameter.getMultipleValue() : parameter.getSingleValue());
        }
    }

    @JsonRef
    public StepInstance getStep() {
        return step;
    }

    public void setStep(StepInstance step) {
        this.step = step;
    }

    public Message getIncomingMessage() {
        return incomingMessage;
    }

    public void setIncomingMessage(Message incomingMessage) {
        this.incomingMessage = incomingMessage;
    }

    public Message getOutgoingMessage() {
        return outgoingMessage;
    }

    public void setOutgoingMessage(Message outgoingMessage) {
        this.outgoingMessage = outgoingMessage;
    }

    public String getValidationResults() {
        return validationResults;
    }

    public void setValidationResults(String validationResults) {
        this.validationResults = validationResults;
    }
}
