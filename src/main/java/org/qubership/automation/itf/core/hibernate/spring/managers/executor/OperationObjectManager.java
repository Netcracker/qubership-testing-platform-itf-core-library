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

import org.apache.commons.lang3.NotImplementedException;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.ByProject;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OperationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.copier.OriginalCopyMap;
import org.qubership.automation.itf.core.util.copier.StorableCopier;
import org.qubership.automation.itf.core.util.exception.CopyException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
public class OperationObjectManager extends AbstractObjectManager<Operation, Operation>
        implements ByProject<Operation> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OperationObjectManager.class);
    private final StepRepository stepRepository;

    @Autowired
    public OperationObjectManager(OperationRepository repository, StepRepository stepRepository) {
        super(Operation.class, repository);
        this.stepRepository = stepRepository;
    }

    @Override
    public void protectedOnRemove(Operation object) {
        stepRepository.onDeleteOperation(object);
    }

    @Override
    public Collection<UsageInfo> findUsages(Storable storable) {
        Collection<UsageInfo> result = Lists.newArrayListWithExpectedSize(500);
        Iterable<Step> all = storable instanceof Operation
                ? stepRepository.getIntegrationStepsByOperation((Operation)storable) : new ArrayList<>();
        addToUsages(result, "operation", all);
        return result;
    }

    /**
     * Find so-called 'important children' for the Operation object.
     * 'important children' are object types preventing deletion of the object (by some decision).
     * For Operation: "An operation can't be deleted if it has SituationEvent triggers".
     *
     * @param storable Operation object to find 'important children', as a rule, checked before deletion.
     * @return Map of objects found. Map key is String object type, value is a list of object ids.
     */
    @Override
    public Map<String, List<BigInteger>> findImportantChildren(Storable storable) {
        if (storable instanceof Operation) {
            List<BigInteger> seTriggerIds = ((OperationRepository) repository)
                    .getSituationEventTriggersByOperationId(toBigInt(storable.getID()));
            Map<String, List<BigInteger>> result = new HashMap<>();
            result.put("SituationEventTriggers", seTriggerIds);
            return result;
        }
        return super.findImportantChildren(storable);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Operation objects are here")
    @Override
    public void additionalMoveActions(Storable operation, String sessionId) {
        TransportConfiguration transport = ((Operation) operation).getTransport();
        if (transport != null) {
            Storable cachedTransportCopy = OriginalCopyMap.getInstance().get(sessionId, transport.getID());
            if (cachedTransportCopy == null) {
                try {
                    cachedTransportCopy = new StorableCopier(sessionId).copy(transport, operation.getParent(),
                            ((Folder<System>) operation.getParent().getParent()).getProject().getID().toString(),
                            "copy");
                    OriginalCopyMap.getInstance().put(sessionId, transport.getID(), cachedTransportCopy);
                    cachedTransportCopy.store();

                    ((Operation) operation).setTransport((TransportConfiguration) cachedTransportCopy);
                } catch (CopyException e) {
                    LOGGER.error(
                            "Can't create the copy of {} transport for moved {} operation. Please, fill the "
                                    + "transport manualy.",
                            transport.getName(), operation.getName());
                }
            }
        }

        for (Situation situation : ((Operation) operation).getSituations()) {
            CoreObjectManager.getInstance().getManager(Situation.class).additionalMoveActions(situation, sessionId);
        }
        operation.store();
    }

    @Override
    public Collection<Operation> getAllByProject(Object projectId) {
        throw new NotImplementedException("Not implemented for project");
    }

    @Override
    public Collection<Operation> getByPieceOfNameAndProject(String name, Object projectId) {
        return ((OperationRepository) repository).findByPieceOfNameAndProject(name, toBigInt(projectId));
    }

    @Override
    public List<Operation> getByNameAndProjectId(String name, BigInteger projectId) {
        return ((OperationRepository) repository).findByNameAndProjectId(name, toBigInt(projectId));
    }

    @Override
    public Collection<Operation> getByParentNameAndProject(String name, Object projectId) {
        return ((OperationRepository) repository).findByParentNameAndProject(name, toBigInt(projectId));
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Operation objects are here")
    @Override
    public void afterDelete(Storable object) {
        if (object.getParent() instanceof System) {
            synchronized (object.getParent()) {
                ((System) object.getParent()).getOperations().remove((Operation) object);
            }
        }
    }
}
