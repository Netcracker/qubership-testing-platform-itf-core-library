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

import java.math.BigInteger;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.ei.deserialize.NestedChainDeserializer;
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
public class EmbeddedStep extends AbstractCallChainStep {
    private static final long serialVersionUID = 20240812L;

    @JsonProperty(value = "type")
    public static final String TYPE = "embeddedChainStep";

    private CallChain chain;

    public EmbeddedStep() {
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only StepContainer objects are here")
    public EmbeddedStep(Storable parent) {
        setParent(parent);
        ((StepContainer) parent).getSteps().add(this);
    }

    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public CallChain getChain() {
        return chain;
    }

    @JsonDeserialize(using = NestedChainDeserializer.class)
    public void setChain(CallChain chain) {
        this.chain = chain;
    }

    @Override
    @JsonIgnore
    public Mep getMep() {
        return Mep.OUTBOUND_REQUEST_ASYNCHRONOUS;
    }

    @Override
    public String getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "EmbeddedStep{chain=" + chain + '}';
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        setLastOrderToImportedStep(getParent());
        super.performPostImportActions(projectId, sessionId);
        CallChain embeddedCallchain = getChain();
        if (embeddedCallchain != null) {
            embeddedCallchain.performPostImportActions(projectId, sessionId);
        }
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
}
