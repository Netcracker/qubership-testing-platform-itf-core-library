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

import javax.persistence.QueryHint;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface SituationEventTriggerRepository extends EventTriggerRepository<SituationEventTrigger> {

    @Modifying
    @Query("update SituationEventTrigger t set t.situation = null where t.situation = :situation")
    void onDeleteSituation(@Param("situation") Situation situation);

    @Query("select trigger from SituationEventTrigger trigger where trigger.situation = :situation")
    Iterable<SituationEventTrigger> getTriggersBySituation(@Param("situation") Situation situation);

    @Query(value = "select trigger from SituationEventTrigger trigger "
            + "inner join Situation as situation on situation = trigger.parent "
            + "inner join Operation as operation on operation = situation.parent "
            + "where operation.projectId = :projectId")
    Collection<SituationEventTrigger> findByProject(@Param("projectId") BigInteger projectId);

    @Query(value = "select trg.* from mb_triggers trg "
            + "inner join mb_situation sit on sit.id = trg.set_parent_id "
            + "inner join mb_operations op on op.id = sit.parent_id "
            + "where trg.parent_type = 'situation' and trg.state = 'ACTIVE' and op.project_id = :projectId",
            nativeQuery = true)
    List<SituationEventTrigger> getActiveTriggersByProject(@Param("projectId") BigInteger projectId);

    @Query(value = "select trg.* from mb_triggers trg \n"
            + "inner join mb_situation sit on sit.id = trg.set_parent_id \n"
            + "inner join mb_operations op on op.id = sit.parent_id \n"
            + "where trg.parent_type = 'situation' and op.parent_id = :systemId",
            nativeQuery = true)
    List<SituationEventTrigger> getTriggersBySystemId(@Param("systemId") BigInteger systemId);

    @Query(value = "select trg.id, trg.state from mb_triggers trg \n"
            + "inner join mb_situation sit on sit.id = trg.set_parent_id \n"
            + "inner join mb_operations op on op.id = sit.parent_id \n"
            + "where op.parent_id = :systemId",
            nativeQuery = true)
    List<Object[]> getTriggersBriefInfoBySystemId(@Param("systemId") BigInteger systemId);

    @Query(value = "select trigger from SituationEventTrigger as trigger "
            + "where id = :id")
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "situationEventTriggerCache")})
    SituationEventTrigger findByIdOnly(@Param("id") BigInteger id);
}
