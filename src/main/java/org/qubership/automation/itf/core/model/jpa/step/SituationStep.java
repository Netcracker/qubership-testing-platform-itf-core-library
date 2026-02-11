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
import java.util.Set;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.regenerator.KeysRegeneratable;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.ei.deserialize.DeserializedEntitiesCache;
import org.qubership.automation.itf.core.util.ei.deserialize.SituationDeserializer;
import org.qubership.automation.itf.core.util.ei.deserialize.SituationsSetDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdsListSerializer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
public class SituationStep extends AbstractCallChainStep implements KeysRegeneratable {
    private static final long serialVersionUID = 20240812L;

    @JsonProperty(value = "type")
    public static final String TYPE = "situationStep";
    private Situation situation;
    private Set<Situation> endSituations = Sets.newHashSetWithExpectedSize(2);
    private Set<Situation> exceptionalSituations = Sets.newHashSetWithExpectedSize(5);
    private boolean waitAllEndSituations;
    private boolean retryOnFail = false;
    private long retryTimeout;
    private String retryTimeoutUnit = TimeUnit.SECONDS.toString();
    private int validationMaxAttempts;
    private long validationMaxTime;
    private String validationUnitMaxTime = TimeUnit.SECONDS.toString();

    public SituationStep() {
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only StepContainer objects are here")
    public SituationStep(Storable parent) {
        setParent(parent);
        ((StepContainer) parent).getSteps().add(this);
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public CallChain getParent() {
        return (CallChain) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = CallChain.class)
    public void setParent(CallChain parent) {
        super.setParent(parent);
    }

    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public Situation getSituation() {
        return situation;
    }

    @JsonDeserialize(using = SituationDeserializer.class)
    public void setSituation(Situation situation) {
        this.situation = situation;
    }

    @RefCopy
    @JsonSerialize(using = IdsListSerializer.class)
    public Set<Situation> getEndSituations() {
        return endSituations;
    }

    @JsonDeserialize(using = SituationsSetDeserializer.class)
    public void setEndSituations(Set<Situation> endSituations) {
        this.endSituations = endSituations;
    }

    @RefCopy
    @JsonSerialize(using = IdsListSerializer.class)
    public Set<Situation> getExceptionalSituations() {
        return exceptionalSituations;
    }

    @JsonDeserialize(using = SituationsSetDeserializer.class)
    public void setExceptionalSituations(Set<Situation> exceptionalSituations) {
        this.exceptionalSituations = exceptionalSituations;
    }

    @RefCopy
    public boolean getWaitAllEndSituations() {
        return waitAllEndSituations;
    }

    public void setWaitAllEndSituations(boolean waitAllEndSituations) {
        this.waitAllEndSituations = waitAllEndSituations;
    }

    @Override
    public Mep getMep() {
        return situation != null ? situation.getMep() : null;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("SituationStep{situation=").append(situation);
        for (Situation situation : endSituations) {
            builder.append(", endSituation=").append(situation);
        }
        return builder.append('}').toString();
    }

    @Override
    public String getType() {
        return TYPE;
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

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        setLastOrderToImportedStep((CallChain) DeserializedEntitiesCache.getInstance().getCacheBySessionId(sessionId)
                .getById((BigInteger) getParent().getID()));
        super.performPostImportActions(projectId, sessionId);
        if (getSituation() != null) {
            getSituation().performPostImportActions(projectId, sessionId);
        }
        for (Situation endSituation : getEndSituations()) {
            endSituation.performPostImportActions(projectId, sessionId);
        }
        for (Situation exceptionalSituation : getExceptionalSituations()) {
            exceptionalSituation.performPostImportActions(projectId, sessionId);
        }
    }
}
