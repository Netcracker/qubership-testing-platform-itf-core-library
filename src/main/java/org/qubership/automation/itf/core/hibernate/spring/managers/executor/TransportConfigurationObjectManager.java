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
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.EnvConfigurationManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.ObjectCreationByTypeManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.InboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OperationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.TransportConfigurationRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.QOperation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
public class TransportConfigurationObjectManager extends AbstractObjectManager<TransportConfiguration,
        TransportConfiguration> implements EnvConfigurationManager<TransportConfiguration>,
        ObjectCreationByTypeManager<TransportConfiguration>, SearchByProjectIdManager<TransportConfiguration> {

    private final OperationRepository operationRepository;
    private final InboundTransportConfigurationRepository inboundTransportConfigurationRepository;
    private final TransportConfigurationRepository transportConfigurationRepository;

    /**
     * Constructor.
     */
    @Autowired
    public TransportConfigurationObjectManager(TransportConfigurationRepository repository,
                                               OperationRepository operationRepository,
                                               InboundTransportConfigurationRepository
                                                           inboundTransportConfigurationRepository) {
        super(TransportConfiguration.class, repository);
        this.operationRepository = operationRepository;
        this.inboundTransportConfigurationRepository = inboundTransportConfigurationRepository;
        this.transportConfigurationRepository = repository;
    }

    @Override
    public void protectedOnRemove(TransportConfiguration transportConfiguration) {
        operationRepository.onDeleteTransport(transportConfiguration);
        inboundTransportConfigurationRepository.onDeleteTransport(transportConfiguration);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only TransportConfiguration objects are here")
    @Override
    public Collection<UsageInfo> findUsages(Storable storable) {
        Collection<UsageInfo> result = Sets.newHashSet();
        addToUsages(result, "transport",
                operationRepository.findAll(QOperation.operation.transport.eq((TransportConfiguration) storable)));
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
        if (storable instanceof TransportConfiguration) {
            List<BigInteger> stubTriggerIds = transportConfigurationRepository
                    .getTransportTriggersByTransportConfigurationId(toBigInt(storable.getID()));
            Map<String, List<BigInteger>> result = new HashMap<>();
            result.put("TransportTriggers", stubTriggerIds);
            return result;
        }
        return super.findImportantChildren(storable);
    }

    /** create method.
     * @param system     - System object instance of {@link org.qubership.automation.itf.core.model.jpa.system.System}
     * @param name       - Name of Transport.
     * @param type       - Type of Transport. It's necessary for lookup transport Mep
     * @param parameters - transport configuration
     * @return - transport configuration object
     */
    @Override
    public TransportConfiguration create(Storable system, @Nonnull String name, @Nonnull String type, Map parameters) {
        TransportConfiguration transportConfiguration = new TransportConfiguration(system, parameters);
        transportConfiguration.setName(name);
        transportConfiguration.setTypeName(type);
        repository.save(transportConfiguration);
        return transportConfiguration;
    }

    @Override
    public TransportConfiguration getByEcId(String ecId, Object... objects) {
        return TxExecutor.executeUnchecked(() -> transportConfigurationRepository.findTransportByEcId(ecId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<TransportConfiguration> getByEcProjectId(String ecProjectId) {
        return TxExecutor.executeUnchecked(() -> transportConfigurationRepository
                .getTransportsByEcProject(ecProjectId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<String> getEcProjectIds(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> transportConfigurationRepository.getEcProjectIds(projectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public void unbindByEcProject(String ecProjectId) {
        TxExecutor.executeUnchecked(() -> transportConfigurationRepository.unbindByEcProject(ecProjectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public TransportConfiguration findByEcLabel(String ecLabel, BigInteger projectId) {
        return null;
    }

    @Override
    public Collection<TransportConfiguration> getByProjectId(BigInteger projectId) {
        return transportConfigurationRepository.findByProjectId(projectId);
    }

    /**
     * Find usages of storable (template) by id in diameter transport configurations under Systems.
     * Please note: the search is without project condition. Execution plan uses special index.
     */
    public Collection<TransportConfiguration> findUsagesTemplateOnTransport(BigInteger templateId) {
        return transportConfigurationRepository.findUsagesTemplateOnTransport(templateId);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only TransportConfiguration objects are here")
    @Override
    public void afterDelete(Storable object) {
        if (object.getParent() instanceof System) {
            synchronized (object.getParent()) {
                ((System) object.getParent()).getTransports().remove((TransportConfiguration) object);
            }
        }
    }
}
