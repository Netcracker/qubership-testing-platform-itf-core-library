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

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.eci.EciConfigurable;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.server.ServerHB;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.transport.EciConfiguration;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.ei.deserialize.SystemDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",
                  scope = OutboundTransportConfiguration.class)
public class OutboundTransportConfiguration extends EciConfiguration implements EciConfigurable {
    private static final long serialVersionUID = 20240812L;

    private System system;
    //Need for QueryDSL
    private String typeName;

    public OutboundTransportConfiguration() {
        super();
    }

    /**
     * Constructor from type, parent and system parameters.
     */
    public OutboundTransportConfiguration(String type, Server parent, System system) {
        super();
        setParent((ServerHB) parent);
        setTypeName(type);
        setSystem(system);
    }

    @JsonSerialize(using = IdSerializer.class)
    public System getSystem() {
        return system;
    }

    @JsonDeserialize(using = SystemDeserializer.class)
    public void setSystem(System system) {
        this.system = system;
    }

    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public ServerHB getParent() {
        return (ServerHB) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ServerHB.class)
    public void setParent(ServerHB parent) {
        super.setParent(parent);
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        getSystem().performPostImportActions(projectId, sessionId);
    }

    @Override
    public void unbindEntityWithHierarchy() {
        setEciParameters(null, null);
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
