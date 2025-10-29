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
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.NotImplementedException;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.StorableInFolderObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.ByProject;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.EnvConfigurationManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.EnvironmentManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByParameterAndProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.EnvironmentRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.report.LinkCollectorConfiguration;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class EnvironmentObjectManager extends AbstractObjectManager<Environment, Environment>
        implements EnvironmentManager, EnvConfigurationManager<Environment>, ByProject<Environment>,
        SearchByParameterAndProjectIdManager<Environment>, StorableInFolderObjectManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentObjectManager.class);
    private final EnvironmentRepository environmentRepository;

    @Autowired
    public EnvironmentObjectManager(EnvironmentRepository repository) {
        super(Environment.class, repository);
        this.environmentRepository = repository;
    }

    /**
     * To Set initial state of Environments when ITF is started.
     */
    public void updateInitialEnvState() {
        LOGGER.info("Triggers' and Environments' statuses initial setup is started.");
        /*
                setInitialEnvStateInactiveOrEmpty() method commented after discussion.
                It looks unneeded in the new ITF architecture.
             */
        // environmentRepository.setInitialEnvStateInactiveOrEmpty();
        TxExecutor.executeUnchecked(environmentRepository::turnOffLostTriggers,
                TxExecutor.defaultWritableTransaction());
        LOGGER.info("Triggers' and Environments' statuses initial setup is done.");
    }

    public Environment getByEcId(@Nonnull String ecId, Object... objects) {
        return TxExecutor.executeUnchecked(() -> environmentRepository.findEnvironmentByEcId(ecId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<Environment> getByEcProjectId(String ecProjectId) {
        return TxExecutor.executeUnchecked(() -> environmentRepository.getEnvironmentsByEcProject(ecProjectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<String> getEcProjectIds(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> environmentRepository.getEcProjectIds(projectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public void unbindByEcProject(String ecProjectId) {
        TxExecutor.executeUnchecked(() -> environmentRepository.unbindByEcProject(ecProjectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Environment findByEcLabel(String ecLabel, BigInteger projectId) {
        return null;
    }

    @Override
    public List<Environment> getByNameAndProjectId(String name, BigInteger projectId) {
        return environmentRepository.findByNameAndProjectId(name, projectId);
    }

    public List<Environment> getByPieceOfNameAndProjectId(String name, BigInteger projectId) {
        return environmentRepository.findByPieceOfNameAndProjectId(name, projectId);
    }

    /**
     * Search Environments under the project specified by projectId.
     *
     * @return Collection of Environments
     */
    @Override
    public Collection<Environment> getByProjectId(BigInteger projectId) {
        return environmentRepository.findByProjectId(projectId);
    }

    /**
     * Search Environments having System + Server pair specified by object ids.
     *
     * @return List of Environment [id, name]
     */
    @Override
    public List<Object[]> getByServerAndSystemIdPair(
            @Nonnull BigInteger systemId, @Nonnull BigInteger serverId) {
        return TxExecutor.executeUnchecked(() -> environmentRepository
                        .findEnvironmentByServerAndSystemIdPair(systemId, serverId),
        TxExecutor.defaultWritableTransaction());
    }

    public Collection<String> findEnvironmentEcIdsForSystem(BigInteger systemId) {
        return TxExecutor.executeUnchecked(() -> environmentRepository.findEnvironmentEcIdsForSystem(systemId),
                TxExecutor.readOnlyTransaction());
    }

    public Collection<String> findEnvironmentEcIdsForServer(BigInteger serverId) {
        return TxExecutor.executeUnchecked(() -> environmentRepository.findEnvironmentEcIdsForServer(serverId),
                TxExecutor.readOnlyTransaction());
    }

    @Override
    public Environment findByServerAndSystems(BigInteger serverId, Collection<BigInteger> systemIds) {
        List<BigInteger> envIds = environmentRepository.findByServerAndSystems(serverId, systemIds);
        return envIds == null || envIds.isEmpty() ? null : environmentRepository.findById(envIds.get(0)).get();
    }

    @Override
    public Collection<Environment> getAllByProject(Object projectId) {
        throw new NotImplementedException("Not implemented for project");
    }

    @Override
    public Collection<Environment> getByPieceOfNameAndProject(String name, Object projectId) {
        throw new NotImplementedException("Not implemented method");
    }

    @Override
    public Collection<Environment> getByParentNameAndProject(String name, Object projectId) {
        throw new NotImplementedException("Not implemented method");
    }

    public String getEnvironmentStateById(String environmentId) {
        return environmentRepository.getEnvironmentState(toBigInt(environmentId));
    }

    public Set<LinkCollectorConfiguration> getLinkCollectorsByEnvId(BigInteger environmentId) {
        return environmentRepository.getLinkCollectorsByEnvId(environmentId);
    }

    @Override
    public List<String> getInboundInfo(BigInteger envId) {
        return environmentRepository.getInboundInfo(envId);
    }

    @Override
    public List<Object[]> findDuplicateConfigurationBySystemServer(BigInteger projectId) {
        return environmentRepository.findDuplicateConfigurationBySystemServer(projectId);
    }

    @Override
    public void afterDelete(Storable object) {
        afterDeleteFromFolder(object);
    }
}
