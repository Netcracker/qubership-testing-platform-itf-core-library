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

package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.StorableInFolderObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.EnvConfigurationManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByUrlAndProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.EnvironmentRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.InboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OutboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.ServerRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.environment.OutboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.environment.QEnvironment;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.project.StubContainer;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.server.ServerHB;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
public class ServerObjectManager extends AbstractObjectManager<Server, ServerHB>
        implements EnvConfigurationManager<Server>, SearchByUrlAndProjectIdManager<Server>,
        StorableInFolderObjectManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerObjectManager.class);
    public static ServerObjectManager INSTANCE;
    private final InboundTransportConfigurationRepository inboundTransportRepository;
    private final OutboundTransportConfigurationRepository outboundTransportRepository;
    private final EnvironmentRepository environmentRepository;
    private final ServerRepository serverRepository;

    /**
     * Constructor with related repositories.
     */
    @Autowired
    public ServerObjectManager(ServerRepository repository, InboundTransportConfigurationRepository inbRep,
                               OutboundTransportConfigurationRepository outbRep,
                               EnvironmentRepository environmentRepository) {
        super(Server.class, repository);
        this.serverRepository = repository;
        this.inboundTransportRepository = inbRep;
        this.outboundTransportRepository = outbRep;
        this.environmentRepository = environmentRepository;
        INSTANCE = this;
    }

    @Override
    public void protectedOnRemove(Server object) {
        Iterable<Environment> environments =
                environmentRepository.findAll(QEnvironment.environment.outbound.containsValue(object));
        for (Environment environment : environments) {
            Collection<System> toRemove = Sets.newHashSetWithExpectedSize(environment.getOutbound().size());
            for (Map.Entry<System, Server> entry : environment.getOutbound().entrySet()) {
                if (entry.getValue().getID().equals(object.getID())) {
                    toRemove.add(entry.getKey());
                }
            }
            for (System system : toRemove) {
                environment.getOutbound().remove(system);
            }
            environment.store();
        }
        environments = environmentRepository.findAll(QEnvironment.environment.inbound.containsValue(object));
        for (Environment environment : environments) {
            Collection<System> toRemove = Sets.newHashSetWithExpectedSize(environment.getInbound().size());
            for (Map.Entry<System, Server> entry : environment.getInbound().entrySet()) {
                if (entry.getValue().getID().equals(object.getID())) {
                    toRemove.add(entry.getKey());
                }
            }
            for (System system : toRemove) {
                environment.getInbound().remove(system);
            }
            environment.store();
        }
    }

    @Override
    public Server create(Storable parent) {
        Folder<Server> actualParent = null;
        if (parent instanceof StubContainer) {
            actualParent = ((StubContainer) parent).getServers();
        } else if (parent instanceof Folder) {
            Optional<Folder<Server>> serverFolder = ((Folder<? extends Storable>) parent).of(Server.class);
            if (serverFolder.isPresent()) {
                actualParent = serverFolder.get();
            }
        }
        if (actualParent == null) {
            throw new RuntimeException("ER: ServerFolder or StubContainer; AR: " + parent);
        }
        ServerHB result = new ServerHB();
        result.setParent(actualParent);
        result.setProjectId(actualParent.getProjectId());
        actualParent.getObjects().add(result);
        return repository.save(result);
    }

    @Override
    public Server create() {
        ServerHB result = new ServerHB();
        return repository.save(result);
    }

    /**
     * Get {@link OutboundTransportConfiguration} by system-server ids and transport type name.
     *
     * @param server   {@link Server} object.
     * @param system   {@link System} object.
     * @param type string transport type name
     *                 (transport class path,
     *                 e.g. org.qubership.automation.itf.transport.rest.outbound.RESTOutboundTransport).
     * @return {@link OutboundTransportConfiguration}
     */
    public OutboundTransportConfiguration getOutbound(Server server, System system, String type) {
        try {
            return outboundTransportRepository.findOne((BigInteger) system.getID(), (BigInteger) server.getID(), type);
        } catch (Exception e) {
            LOGGER.error("Can't get outbound configuration for Server: {}, System {} and type '{}'", server, system,
                    type, e);
            throw e;
        }
    }

    /**
     * Find outbound transport configurations under System + Server pair.
     */
    public Iterable<OutboundTransportConfiguration> getOutbounds(Server server, System system) {
        return outboundTransportRepository.findAll((BigInteger) system.getID(), (BigInteger) server.getID());
    }

    /**
     * Find inbound configuration under Server + TransportConfiguration.
     */
    public InboundTransportConfiguration getInbound(Server server,
                                                    TransportConfiguration configuration) {
        return inboundTransportRepository.findOne((BigInteger) server.getID(), (BigInteger) configuration.getID());
    }

    /**
     * Find inbound transport configurations under System + Server pair.
     */
    public Iterable<InboundTransportConfiguration> getInbounds(Server server, System system) {
        return inboundTransportRepository.findAll((BigInteger) server.getID(), (BigInteger) system.getID());
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Server objects are here")
    @Override
    public Collection<UsageInfo> findUsages(Storable storable) {
        Collection<UsageInfo> result = Lists.newArrayListWithExpectedSize(20);
        Iterable<Environment> environments =
                environmentRepository.findAll(QEnvironment.environment.outbound.containsValue((Server) storable));
        addToUsages(result, "outbound", environments);
        environments = environmentRepository.findAll(QEnvironment.environment.inbound.containsValue((Server) storable));
        addToUsages(result, "inbound", environments);
        return result;
    }

    /**
     * Find so-called 'important children' for the Server object.
     * 'important children' are object types preventing deletion of the object (by some decision).
     * For Server: "A Server can't be deleted if it has Transport Triggers".
     *
     * @param storable Server object to find 'important children', as a rule, checked before deletion.
     * @return Map of objects found. Map key is String object type, value is a list of object ids.
     */
    @Override
    public Map<String, List<BigInteger>> findImportantChildren(Storable storable) {
        if (storable instanceof Server) {
            List<BigInteger> stubTriggerIds = serverRepository
                    .getTransportTriggersByServerId(toBigInt(storable.getID()));
            Map<String, List<BigInteger>> result = new HashMap<>();
            result.put("TransportTriggers", stubTriggerIds);
            return result;
        }
        return super.findImportantChildren(storable);
    }

    /**
     * Find Server by ecId + Server name (objects[0]) + Server url (objects[1]).
     */
    public Server getByEcId(@Nonnull String ecId, Object... objects) {
        return TxExecutor.executeUnchecked(() -> serverRepository
                .findByEcId(ecId, (String) objects[0], (String) objects[1]), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<Server> getByEcProjectId(String ecProjectId) {
        return TxExecutor.executeUnchecked(() -> serverRepository.getByEcProject(ecProjectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<String> getEcProjectIds(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> serverRepository.getEcProjectIds(projectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public void unbindByEcProject(String ecProjectId) {
        TxExecutor.executeUnchecked(() -> serverRepository.unbindByEcProject(ecProjectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Server findByEcLabel(String ecLabel, BigInteger projectId) {
        return null;
    }

    @Override
    public List<Server> getByUrlAndProjectId(String url, BigInteger projectId) {
        List<Server> result = Lists.newArrayList();
        result.addAll(serverRepository.findByUrlAndProjectId(url, projectId));
        return result;
    }

    @Override
    public List<Server> getByUrlSlashedAndProjectId(String url, BigInteger projectId) {
        List<BigInteger> ids = serverRepository
                .getServersByProjectAndUrlSlashed(url.endsWith("/") ? url : url + "/", projectId);
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Server> servers = new ArrayList<>();
        for (BigInteger id : ids) {
            if (id != null) {
                Server server = serverRepository.getOne(id);
                servers.add(server);
            }
        }
        return servers;
    }

    @Override
    public List<Server> getByNameAndProjectId(String name, BigInteger projectId) {
        List<Server> result = Lists.newArrayList();
        result.addAll(serverRepository.findByNameAndProjectId(name, projectId));
        return result;
    }

    @Override
    public List<Server> getByProjectId(BigInteger projectId) {
        List<Server> result = Lists.newArrayList();
        result.addAll(serverRepository.findByProjectId(projectId));
        return result;
    }

    @Override
    public void afterDelete(Storable object) {
        afterDeleteFromFolder(object);
    }

    public int deleteUnusedOutboundConfigurations() {
        return ((ServerRepository) repository).deleteUnusedOutboundConfigurations();
    }

    public int deleteUnusedOutboundConfigurationsByProjectId(BigInteger projectId) {
        return ((ServerRepository) repository).deleteUnusedOutboundConfigurationsByProjectId(projectId);
    }
}
