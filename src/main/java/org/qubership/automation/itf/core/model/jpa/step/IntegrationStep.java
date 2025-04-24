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

package org.qubership.automation.itf.core.model.jpa.step;

import java.beans.Transient;
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.jpa.message.template.OperationTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.SystemTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.util.annotation.OperationRefCopyAsNewObject;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.annotation.TemplateRefCopyAsNewObject;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.ei.deserialize.SystemDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = IntegrationStep.class)
public class IntegrationStep extends AbstractStep {
    private static final long serialVersionUID = 20240812L;

    @JsonProperty(value = "type")
    public static final String TYPE = "integrationStep";

    private System sender;

    private System receiver;

    private Operation operation;

    private SystemTemplate systemTemplate;

    private OperationTemplate operationTemplate;

    private boolean retryOnFail = false;
    private long retryTimeout;
    private String retryTimeoutUnit = TimeUnit.SECONDS.toString();
    private int validationMaxAttempts;
    private long validationMaxTime;
    private String validationUnitMaxTime = TimeUnit.SECONDS.toString();

    public IntegrationStep() {
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only StepContainer objects are here")
    public IntegrationStep(Storable parent) {
        setParent(parent);
        ((StepContainer) parent).getSteps().add(this);
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Situation getParent() {
        return (Situation) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Situation.class)
    public void setParent(Situation parent) {
        super.setParent(parent);
    }

    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public System getSender() {
        return sender;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = System.class)
    public void setSender(System sender) {
        this.sender = sender;
    }

    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public System getReceiver() {
        return receiver;
    }

    @JsonDeserialize(using = SystemDeserializer.class)
    public void setReceiver(System receiver) {
        this.receiver = receiver;
    }

    @TemplateRefCopyAsNewObject
    @JsonSerialize(using = IdSerializer.class)
    public SystemTemplate getSystemTemplate() {
        return systemTemplate;
    }

    @TemplateRefCopyAsNewObject
    @JsonSerialize(using = IdSerializer.class)
    public OperationTemplate getOperationTemplate() {
        return operationTemplate;
    }

    @JsonIgnore
    public Template returnStepTemplate() {
        return systemTemplate != null ? systemTemplate : operationTemplate;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id", scope = SystemTemplate.class)
    public void setSystemTemplate(SystemTemplate systemTemplate) {
        this.systemTemplate = systemTemplate;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id", scope = OperationTemplate.class)
    public void setOperationTemplate(OperationTemplate operationTemplate) {
        this.operationTemplate = operationTemplate;
    }

    /**
     * Set Template.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only OperationTemplate objects are here")
    @JsonIgnore
    public void setTemplate(Template template) {
        if (template instanceof SystemTemplate) {
            if (getOperationTemplate() != null) {
                setOperationTemplate(null);
            }
            setSystemTemplate((SystemTemplate) template);
        } else {
            if (getSystemTemplate() != null) {
                setSystemTemplate(null);
            }
            setOperationTemplate((OperationTemplate) template);
        }
    }

    @OperationRefCopyAsNewObject
    @JsonSerialize(using = IdSerializer.class)
    public Operation getOperation() {
        return this.operation;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Operation.class)
    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    @SuppressWarnings("Duplicates")
    public boolean isRetryOnFail() {
        return retryOnFail;
    }

    @SuppressWarnings("Duplicates")
    public void setRetryOnFail(boolean retryOnFail) {
        this.retryOnFail = retryOnFail;
    }

    @SuppressWarnings("Duplicates")
    public long getRetryTimeout() {
        return retryTimeout;
    }

    @SuppressWarnings("Duplicates")
    public void setRetryTimeout(long retryTimeout) {
        this.retryTimeout = retryTimeout;
    }

    @SuppressWarnings("Duplicates")
    public String getRetryTimeoutUnit() {
        return retryTimeoutUnit;
    }

    @SuppressWarnings("Duplicates")
    public void setRetryTimeoutUnit(String retryTimeoutUnit) {
        this.retryTimeoutUnit = checkUnit(retryTimeoutUnit);
    }

    @SuppressWarnings("Duplicates")
    @Transient
    @JsonIgnore
    public TimeUnit retrieveRetryTimeoutUnit() {
        return convertToTimeUnit(retryTimeoutUnit);
    }

    @SuppressWarnings("Duplicates")
    public int getValidationMaxAttempts() {
        return validationMaxAttempts;
    }

    @SuppressWarnings("Duplicates")
    public void setValidationMaxAttempts(int validationMaxAttempts) {
        this.validationMaxAttempts = validationMaxAttempts;
    }

    @SuppressWarnings("Duplicates")
    public long getValidationMaxTime() {
        return validationMaxTime;
    }

    @SuppressWarnings("Duplicates")
    public void setValidationMaxTime(long validationMaxTime) {
        this.validationMaxTime = validationMaxTime;
    }

    @SuppressWarnings("Duplicates")
    public String getValidationUnitMaxTime() {
        return validationUnitMaxTime;
    }

    @SuppressWarnings("Duplicates")
    public void setValidationUnitMaxTime(String validationUnitMaxTime) {
        this.validationUnitMaxTime = checkUnit(validationUnitMaxTime);
    }

    @SuppressWarnings("Duplicates")
    @Transient
    @JsonIgnore
    public TimeUnit retrieveValidationUnitMaxTime() {
        return convertToTimeUnit(validationUnitMaxTime);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Mep getMep() {
        if (operation != null) {
            return operation.getMep();
        } else {
            return Mep.OUTBOUND_REQUEST_ASYNCHRONOUS;
        }
    }

    @Override
    @JsonIgnore
    public String getType() {
        return TYPE;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof IntegrationStep)) {
            return false;
        } else {
            if (obj == this) {
                return true;
            } else {
                if (((IntegrationStep) obj).getOperation() == null) {
                    return operation == null;
                } else {
                    return ((IntegrationStep) obj).getOperation().equals(operation);
                }
            }
        }
    }

    @Override
    public int hashCode() {
        return getID().hashCode();
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {

    }
}
