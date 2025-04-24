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

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.NotNull;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.EventTriggerManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SituationEventTriggerRepository;
import org.qubership.automation.itf.core.model.communication.EventTriggerBriefInfo;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SituationEventTriggerObjectManager extends EventTriggerObjectManager<SituationEventTrigger>
        implements EventTriggerManager<SituationEventTrigger>, SearchByProjectIdManager<SituationEventTrigger> {

    @Autowired
    public SituationEventTriggerObjectManager(SituationEventTriggerRepository repository) {
        super(SituationEventTrigger.class, repository);
    }

    @Override
    public Collection<SituationEventTrigger> getByProjectId(BigInteger projectId) {
        return ((SituationEventTriggerRepository) repository).findByProject(projectId);
    }

    @Override
    public List<SituationEventTrigger> getAllActive(@NotNull Operation operation) {
        return new ArrayList<>(); // no use case to retrieve it currently, so no repository method
    }

    @Override
    public List<SituationEventTrigger> getActiveByProject(@NotNull BigInteger projectId) {
        return ((SituationEventTriggerRepository) repository).getActiveTriggersByProject(projectId);
    }

    public List<SituationEventTrigger> getTriggersBySystem(@NotNull BigInteger systemId) {
        return ((SituationEventTriggerRepository) repository).getTriggersBySystemId(systemId);
    }

    /**
     * Get map of EventTriggerBriefInfo (to deactivate or to reactivate) under the system.
     *
     * @param systemId System Id,
     * @return map of EventTriggerBriefInfo.
     */
    public Map<String, List<EventTriggerBriefInfo>> getTriggersBriefInfoBySystem(@NotNull BigInteger systemId) {
        List<Object[]> objs = ((SituationEventTriggerRepository) repository).getTriggersBriefInfoBySystemId(systemId);
        List<EventTriggerBriefInfo> triggersToDeactivate = new ArrayList<>();
        List<EventTriggerBriefInfo> triggersToReactivate = new ArrayList<>();
        if (objs != null && !objs.isEmpty()) {
            for (Object[] entry : objs) {
                if (entry.length < 2) {
                    continue;
                }
                EventTriggerBriefInfo briefInfo = new EventTriggerBriefInfo(
                        (BigInteger) entry[0],
                        SituationEventTrigger.TYPE);
                if ("ACTIVE".equals(entry[1].toString())) {
                    triggersToReactivate.add(briefInfo);
                } else {
                    triggersToDeactivate.add(briefInfo);
                }
            }
        }
        Map<String, List<EventTriggerBriefInfo>> triggersToSync = new HashMap<>();
        triggersToSync.put("ToDeactivate", triggersToDeactivate);
        triggersToSync.put("ToReactivate", triggersToReactivate);
        return triggersToSync;
    }

    public SituationEventTrigger getByIdOnly(BigInteger id) {
        return ((SituationEventTriggerRepository)repository).findByIdOnly(id);
    }
}
