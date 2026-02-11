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

import jakarta.annotation.Nonnull;

import org.jetbrains.annotations.NotNull;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.EventTriggerManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OperationEventTriggerRepository;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.OperationEventTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OperationEventTriggerObjectManager extends EventTriggerObjectManager<OperationEventTrigger>
        implements EventTriggerManager<OperationEventTrigger>, SearchByProjectIdManager<OperationEventTrigger> {

    @Autowired
    public OperationEventTriggerObjectManager(OperationEventTriggerRepository repository) {
        super(OperationEventTrigger.class, repository);
    }

    @Override
    public List<OperationEventTrigger> getAllActive(@Nonnull Operation operation) {
        return ((OperationEventTriggerRepository) repository)
                .getActiveTriggersByOperationNative(toBigInt(operation.getID()));
    }

    public List<BigInteger> getActiveTriggersBySituationIdsNative(List<BigInteger> situationIds) {
        return ((OperationEventTriggerRepository) repository).getActiveTriggersBySituationIdsNative(situationIds);
    }

    @Override
    public List<OperationEventTrigger> getActiveByProject(@NotNull BigInteger projectId) {
        return ((OperationEventTriggerRepository) repository).getActiveTriggersByProject(projectId);
    }

    @Override
    public Collection<OperationEventTrigger> getByProjectId(BigInteger projectId) {
        return ((OperationEventTriggerRepository) repository).findByProject(projectId);
    }

    public OperationEventTrigger getByIdOnly(BigInteger id) {
        return ((OperationEventTriggerRepository)repository).findByIdOnly(id);
    }
}
