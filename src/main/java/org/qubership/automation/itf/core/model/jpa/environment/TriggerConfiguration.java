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

package org.qubership.automation.itf.core.model.jpa.environment;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.transport.Configuration;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.constants.TriggerState;
import org.qubership.automation.itf.core.util.ei.deserialize.TriggerStateDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",
                  scope = TriggerConfiguration.class)
public class TriggerConfiguration extends Configuration {
    private static final long serialVersionUID = 20240812L;

    private TriggerState state;

    private String activationErrorMessage;

    public TriggerConfiguration() {
        super();
    }

    /**
     * Should be removed soon.
     */
    @Deprecated
    public TriggerConfiguration(InboundTransportConfiguration parent) {
        setParent(parent);
        setTypeName(parent.getTypeName());
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST",
            justification = "Only InboundTransportConfiguration objects are here")
    public TriggerConfiguration(Storable parent) {
        setParent(parent);
        ((InboundTransportConfiguration) parent).getTriggerConfigurations().add(this);
    }

    public TriggerConfiguration(String typeName) {
        super(typeName);
    }

    @JsonDeserialize(using = TriggerStateDeserializer.class)
    public void setState(TriggerState state) {
        this.state = state;
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public InboundTransportConfiguration getParent() {
        return (InboundTransportConfiguration) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",
                      scope = InboundTransportConfiguration.class)
    public void setParent(InboundTransportConfiguration parent) {
        super.setParent(parent);
    }

    @Override
    public String getTypeName() {
        return getParent() != null ? getParent().getTypeName() : super.getTypeName();
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
    }
}
