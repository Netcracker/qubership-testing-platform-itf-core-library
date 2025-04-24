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
import java.util.Set;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.NotImplementedException;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.StorableInFolderObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.EnvConfigurationManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.LabeledObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.NativeManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.EnvironmentRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SystemRepository;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.environment.QEnvironment;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class SystemObjectManager extends AbstractObjectManager<System, System> implements NativeManager<System>,
        LabeledObjectManager<System>, EnvConfigurationManager<System>, SearchByProjectIdManager<System>,
        StorableInFolderObjectManager {

    private final StepRepository stepRepository;
    private final EnvironmentRepository environmentRepository;
    private final SystemRepository systemRepository;

    /**
     * Constructor.
     */
    @Autowired
    public SystemObjectManager(SystemRepository repository, StepRepository stepRepository,
                               EnvironmentRepository environmentRepository) {
        super(System.class, repository);
        this.systemRepository = repository;
        this.stepRepository = stepRepository;
        this.environmentRepository = environmentRepository;
    }

    @Override
    public void protectedOnRemove(System object) {
        stepRepository.onDeleteSystemSender(object);
        stepRepository.onDeleteSystemReceiver(object);
        Iterable<Environment> environments =
                environmentRepository.findAll(QEnvironment.environment.outbound.containsKey(object));
        for (Environment environment : environments) {
            environment.getOutbound().remove(object);
            environment.store();
        }
        environments = environmentRepository.findAll(QEnvironment.environment.inbound.containsKey(object));
        for (Environment environment : environments) {
            environment.getInbound().remove(object);
            environment.store();
        }
    }

    @Override
    public Collection<UsageInfo> findUsages(Storable storable) {
        Collection<UsageInfo> result = Lists.newArrayListWithExpectedSize(500);
        if (storable instanceof System) {
            System system = (System) storable;
            Iterable<Step> stepsWithSender = stepRepository.getIntegrationStepsBySender(system);
            addToUsages(result, "Sender", stepsWithSender);
            Iterable<Step> stepsWithReceiver = stepRepository.getIntegrationStepsByReceiver(system);
            addToUsages(result, "Receiver", stepsWithReceiver);
            Iterable<Environment> envsOut =
                    environmentRepository.findAll(QEnvironment.environment.outbound.containsKey(system));
            addToUsages(result, "Outbound", envsOut);
            Iterable<Environment> envsIn =
                    environmentRepository.findAll(QEnvironment.environment.inbound.containsKey(system));
            addToUsages(result, "Inbound", envsIn);
        }
        return result;
    }

    /**
     * Find so-called 'important children' for the System object.
     * 'important children' are object types preventing deletion of the object (by some decision).
     * For system: "A system can't be deleted if it has transport triggers or SituationEvent triggers".
     *
     * @param storable System object to find 'important children', as a rule, checked before deletion.
     * @return Map of objects found. Map key is String object type, value is a list of object ids.
     */
    @Override
    public Map<String, List<BigInteger>> findImportantChildren(Storable storable) {
        if (storable instanceof System) {
            BigInteger id = toBigInt(storable.getID());
            List<BigInteger> stubTriggerIds = systemRepository.getTransportTriggersBySystemId(id);
            List<BigInteger> seTriggerIds = systemRepository.getSituationEventTriggersBySystemId(id);
            Map<String, List<BigInteger>> result = new HashMap<>();
            result.put("TransportTriggers", stubTriggerIds);
            result.put("SituationEventTriggers", seTriggerIds);
            return result;
        }
        return super.findImportantChildren(storable);
    }

    @Override
    public Storable getChildByClass(System parent, Class childrenClass, Object... param) {
        if (childrenClass.getName().equals(Operation.class.getName()) && param[0] != null) {
            return systemRepository.findFirstByDefineOperation(toBigInt(parent.getID()), String.valueOf(param[0]));
        }
        throw new NotImplementedException("Not implemented for classes other than " + Operation.class.getName());
    }

    @Override
    public Collection<? extends Storable> getChildrenByClass(System parent, Class childrenClass, Object... param) {
        throw new NotImplementedException("");
    }

    @Override
    public Collection<System> getByLabel(String label) {
        throw new NotImplementedException("Method getByLabel is not implemented");
    }

    @Override
    public Collection<System> getByLabel(String label, BigInteger projectId) {
        throw new NotImplementedException("Method getByLabel is not implemented");
    }

    @Override
    public Set<String> getAllLabels(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> systemRepository.getAllLabels(projectId),
                TxExecutor.readOnlyTransaction());
    }

    @Override
    public System getByEcId(@Nonnull String ecId, Object... objects) {
        return TxExecutor.executeUnchecked(() -> systemRepository.findSystemByEcId(ecId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<System> getByEcProjectId(String ecProjectId) {
        return TxExecutor.executeUnchecked(() -> systemRepository.getSystemsByEcProject(ecProjectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<String> getEcProjectIds(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> systemRepository.getEcProjectIds(projectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public void unbindByEcProject(String ecProjectId) {
        TxExecutor.executeUnchecked(() -> systemRepository.unbindByEcProject(ecProjectId),
                TxExecutor.defaultWritableTransaction());
    }

    @Override
    public System findByEcLabel(String ecLabel, BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> systemRepository.findByEcLabel(ecLabel, projectId),
                TxExecutor.defaultWritableTransaction());
    }

    public Collection<System> getByPieceOfNameAndProject(String name, Object projectId) {
        return systemRepository.findByPieceOfNameAndProject(name, toBigInt(projectId));
    }

    public Collection<System> getByProjectId(BigInteger projectId) {
        return systemRepository.findByProject(projectId);
    }

    public List<IdNamePair> getSimpleListByProject(Object projectId) {
        return ((SystemRepository) repository).getSimpleListByProject(toBigInt(projectId));
    }

    /**
     * Method returns 'Receiver' Systems List from the call chain identified by chainId.
     * It invokes systemRepository.getReceiverSystemIdsFromCallChainSteps method executing native hierarchical query.
     * Then System Objects List is populated, for each systemId received.
     */
    @Override
    public List<System> getReceiverSystemsFromCallChainSteps(Object chainId) {
        List<BigInteger> ids = systemRepository.getReceiverSystemIdsFromCallChainSteps(toBigInt(chainId));
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<System> systems = new ArrayList<>();
        for (BigInteger id : ids) {
            if (id != null) {
                System system = systemRepository.getOne(id);
                if (system != null) {
                    systems.add(system);
                }
            }
        }
        return systems;
    }

    @Override
    public void afterDelete(Storable object) {
        afterDeleteFromFolder(object);
    }
}
