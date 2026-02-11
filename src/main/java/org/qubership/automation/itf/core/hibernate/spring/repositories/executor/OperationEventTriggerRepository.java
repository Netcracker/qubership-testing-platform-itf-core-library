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

package org.qubership.automation.itf.core.hibernate.spring.repositories.executor;

import static org.hibernate.jpa.QueryHints.HINT_CACHEABLE;
import static org.hibernate.jpa.QueryHints.HINT_CACHE_REGION;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.QueryHint;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.OperationEventTrigger;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface OperationEventTriggerRepository extends EventTriggerRepository<OperationEventTrigger> {

    @Query(value = "select trigger from OperationEventTrigger trigger "
            + "where trigger.parent in ("
            + "    select situation from Situation situation where situation.parent = :operation"
            + ") order by priority asc")
    Collection<OperationEventTrigger> getTriggersByOperation(@Param("operation") Operation operation);

    @Query(value = "select trigger from OperationEventTrigger trigger "
            + "inner join Situation as situation on situation = trigger.parent "
            + "inner join Operation as operation on operation = situation.parent "
            + "where operation.projectId = :projectId")
    Collection<OperationEventTrigger> findByProject(@Param("projectId") BigInteger projectId);

    @Query(value = "select trg.* from mb_triggers trg "
            + "inner join mb_situation sit on sit.id = trg.oet_parent_id "
            + "inner join mb_operations op on op.id = sit.parent_id "
            + "where trg.parent_type = 'operation' and trg.state = 'ACTIVE' and op.project_id = :projectId",
            nativeQuery = true)
    List<OperationEventTrigger> getActiveTriggersByProject(@Param("projectId") BigInteger projectId);

    @Query(value = "select trg.* from mb_triggers trg "
            + "where trg.oet_parent_id in ("
            + "    select id from mb_situation where parent_id = :operationId"
            + ") and trg.parent_type = 'operation' and trg.state = 'ACTIVE' order by trg.priority asc",
            nativeQuery = true)
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "activeOperationEventTriggersCache")})
    List<OperationEventTrigger> getActiveTriggersByOperationNative(@Param("operationId") BigInteger operationId);

    @Query(value = "select trg.id from mb_triggers trg "
            + "where trg.oet_parent_id in (:situationIds) "
            + "and trg.parent_type = 'operation' and trg.state = 'ACTIVE'",
            nativeQuery = true)
    List<BigInteger> getActiveTriggersBySituationIdsNative(@Param("situationIds") List<BigInteger> situationIds);

    @Query(value = "select trigger from OperationEventTrigger trigger "
            + "where trigger.state = 'ACTIVE' and trigger.parent in ("
            + "    select situation.id from Situation situation where situation.parent = :operation"
            + ") order by priority asc")
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "activeOperationEventTriggersCache")})
    List<OperationEventTrigger> getActiveTriggersByOperation(@Param("operation") Operation operation);

    @Query(value = "select trigger from OperationEventTrigger as trigger where id = :id")
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "operationEventTriggerCache")})
    OperationEventTrigger findByIdOnly(@Param("id") BigInteger id);
}
