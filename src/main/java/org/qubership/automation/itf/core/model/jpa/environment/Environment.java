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
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.EnvironmentManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.eci.AbstractEciConfigurable;
import org.qubership.automation.itf.core.model.eci.EciConfigurable;
import org.qubership.automation.itf.core.model.jpa.folder.EnvFolder;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.project.StubContainer;
import org.qubership.automation.itf.core.model.jpa.report.LinkCollectorConfiguration;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.constants.TriggerState;
import org.qubership.automation.itf.core.util.ei.deserialize.EnvFolderDeserializer;
import org.qubership.automation.itf.core.util.ei.deserialize.SystemServerMapDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.ei.serialize.StorablesMapSerializer;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Environment.class)
public class Environment extends AbstractEciConfigurable {
    private static final long serialVersionUID = 20240812L;

    @JsonSerialize(using = StorablesMapSerializer.class)
    @JsonDeserialize(using = SystemServerMapDeserializer.class)
    private Map<System, Server> outbound = Maps.newHashMapWithExpectedSize(10);
    @JsonSerialize(using = StorablesMapSerializer.class)
    @JsonDeserialize(using = SystemServerMapDeserializer.class)
    private Map<System, Server> inbound = Maps.newHashMapWithExpectedSize(10);
    private Set<LinkCollectorConfiguration> reportCollectors = Sets.newHashSetWithExpectedSize(5);
    private TriggerState environmentState;
    private BigInteger projectId;

    public Environment() {
    }

    /**
     * Constructor of a new Environment under given parent.
     */
    public Environment(Storable parent) {
        Folder<Environment> actualParent = null;
        if (parent instanceof StubContainer) {
            actualParent = ((StubContainer) parent).getEnvironments();
        } else if (parent instanceof Folder) {
            Optional<Folder<Environment>> environmentFolder = ((Folder<? extends Storable>) parent)
                    .of(Environment.class);
            if (environmentFolder.isPresent()) {
                actualParent = environmentFolder.get();
            }
        }
        if (actualParent == null) {
            throw new RuntimeException("EnvironmentFolder or StubContainer are expected, but given: " + parent);
        }
        setParent(actualParent);
        actualParent.getObjects().add(this);
    }

    public TriggerState getEnvironmentState() {
        return environmentState;
    }

    public void setEnvironmentState(TriggerState environmentState) {
        this.environmentState = environmentState;
    }

    public Map<System, Server> getOutbound() {
        return outbound;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setOutbound(Map<System, Server> outbound) {
        this.outbound = outbound;
    }

    public void fillOutbound(Map<System, Server> outbound) {
        StorableUtils.fillMap(getOutbound(), outbound);
    }

    public Map<System, Server> getInbound() {
        return inbound;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setInbound(Map<System, Server> inbound) {
        this.inbound = inbound;
    }

    public void fillInbound(Map<System, Server> inbound) {
        StorableUtils.fillMap(getInbound(), inbound);
    }

    public Set<LinkCollectorConfiguration> getReportCollectors() {
        return reportCollectors;
    }

    protected void setReportCollectors(Set<LinkCollectorConfiguration> reportCollectors) {
        this.reportCollectors = reportCollectors;
    }

    public void fillReportCollectors(Set<LinkCollectorConfiguration> reportCollectors) {
        StorableUtils.fillCollection(this.reportCollectors, reportCollectors);
    }

    public BigInteger getProjectId() {
        return projectId;
    }

    public void setProjectId(BigInteger projectId) {
        this.projectId = projectId;
    }

    @Override
    @JsonSerialize(using = IdSerializer.class)
    public EnvFolder getParent() {
        return (EnvFolder) super.getParent();
    }

    @JsonDeserialize(using = EnvFolderDeserializer.class)
    public void setParent(EnvFolder parent) {
        super.setParent(parent);
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        setProjectId(projectId);
        Set<System> importedSystems = Sets.newHashSet();
        Set<Server> importedServers = Sets.newHashSet();
        getSystemsAndServersFromImport(getInbound(), importedSystems, importedServers);
        getSystemsAndServersFromImport(getOutbound(), importedSystems, importedServers);

        for (System system : importedSystems) {
            system.performPostImportActions(projectId, sessionId);
        }
        for (Server server : importedServers) {
            server.performPostImportActions(projectId, sessionId);
        }
        reportLinkPostActions();
    }

    private void getSystemsAndServersFromImport(Map<System, Server> systemServerMap,
                                                Set<System> importedSystems, Set<Server> importedServers) {
        for (Map.Entry<System, Server> entry : systemServerMap.entrySet()) {
            importedSystems.add(entry.getKey());
            if (entry.getValue() != null) {
                importedServers.add(entry.getValue());
            }
        }
    }

    private void reportLinkPostActions() {
        for (LinkCollectorConfiguration linkCollectorConfiguration : getReportCollectors()) {
            Map<String, String> configuration = linkCollectorConfiguration.getConfiguration();
            if (configuration != null) {
                String systemId = configuration.get("system");
                if (StringUtils.isNotEmpty(systemId)) {
                    System linkedSystem = CoreObjectManager.getInstance().getManager(System.class).getById(systemId);
                    if (linkedSystem == null) {
                        configuration.remove("system");
                    }
                }
            }
        }
    }

    @Override
    public void unbindEntityWithHierarchy() {
        Pair<Set<EciConfigurable>, Set<EciConfigurable>> systemsAndServers = getSystemsAndServersForUnbind();
        unbindEntities(systemsAndServers.getLeft());
        unbindEntities(systemsAndServers.getRight());
        setEciParameters(null, null);
    }

    private Pair<Set<EciConfigurable>, Set<EciConfigurable>> getSystemsAndServersForUnbind() {
        Set<EciConfigurable> systems = Sets.newHashSet();
        Set<EciConfigurable> servers = Sets.newHashSet();
        for (Map.Entry<System, Server> entry : getOutbound().entrySet()) {
            systems.add(entry.getKey());
            if (entry.getValue() != null) {
                servers.add(entry.getValue());
            }
        }
        for (Map.Entry<System, Server> entry : getInbound().entrySet()) {
            systems.add(entry.getKey());
            if (entry.getValue() != null) {
                servers.add(entry.getValue());
            }
        }
        return new ImmutablePair<>(systems, servers);
    }

    private void unbindEntities(Set<EciConfigurable> entitiesForUnbind) {
        EnvironmentManager environmentManager = CoreObjectManager.getInstance()
                .getSpecialManager(Environment.class, EnvironmentManager.class);
        for (EciConfigurable entity : entitiesForUnbind) {
            if (entity.getEcId() != null) {
                Collection<String> environmentEcIds
                        = entity instanceof System
                              ? environmentManager.findEnvironmentEcIdsForSystem((BigInteger) entity.getID())
                              : environmentManager.findEnvironmentEcIdsForServer((BigInteger) entity.getID());
                if (environmentEcIds != null && environmentEcIds.size() == 1) {
                    entity.unbindEntityWithHierarchy();
                }
            }
        }
    }

    /**
     * Object's version is increased for all systems and servers used under the Environment
     * and system children and transport configurations under servers.
     */
    public void upStorableVersion() {
        super.upStorableVersion();
        Set<System> importedSystems = Sets.newHashSet();
        Set<Server> importedServers = Sets.newHashSet();
        getSystemsAndServersFromImport(getInbound(), importedSystems, importedServers);
        getSystemsAndServersFromImport(getOutbound(), importedSystems, importedServers);
        for (System system : importedSystems) {
            system.upStorableVersion();
        }
        for (Server server : importedServers) {
            server.upStorableVersion();
        }
        reportLinkPostActions();
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId,
            UUID projectUuid,
            boolean needToUpdateProjectId,
            boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(replacementMap, projectId, projectUuid, needToUpdateProjectId,
                needToGenerateNewId);
        outbound.forEach((key, value) ->
                processServer(replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId, value)
        );
        inbound.forEach((key, value) ->
                processServer(replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId, value)
        );
    }

    private static void processServer(Map<BigInteger, BigInteger> replacementMap,
                                      BigInteger projectId, UUID projectUuid,
                                      boolean needToUpdateProjectId,
                                      boolean needToGenerateNewId,
                                      Server value) {
        if (value != null) {
            value.performActionsForImportIntoAnotherProject(
                    replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId);
        }
    }
}
