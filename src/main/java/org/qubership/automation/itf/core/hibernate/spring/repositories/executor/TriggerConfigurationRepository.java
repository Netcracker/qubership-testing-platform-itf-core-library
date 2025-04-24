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

import java.math.BigInteger;
import java.util.Collection;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.jpa.environment.TriggerConfiguration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TriggerConfigurationRepository extends StorableRepository<TriggerConfiguration>,
        QuerydslPredicateExecutor<TriggerConfiguration> {

    @Modifying
    @Query("delete from InboundTransportConfiguration i where i.referencedConfiguration = :configuration")
    void onDeleteTrigger(@Param("configuration") TriggerConfiguration configuration);

    @Query(value = "select ms.project_id from mb_configuration mc, mb_servers ms "
            + "where mc.id = (select parent_conf_id from mb_configuration mc2 where mc2.id = :triggerConfigurationId) "
            + "and ms.id = mc.parent_in_server_id" , nativeQuery = true)
    BigInteger getProjectId(@Param("triggerConfigurationId") BigInteger triggerConfigurationId);

    @Query(value = "select trigger.* from mb_configuration trigger "
            + "where \"type\"= 'trigger' "
            + "and trigger_state = 'ACTIVE' "
            + "and parent_conf_id in (select mc.id from mb_configuration mc where mc.parent_in_server_id in "
            + "(select ms.id from mb_servers ms where ms.project_id = :projectId))", nativeQuery = true)
    Collection<TriggerConfiguration> getAllActiveTriggersByProjectId(@Param("projectId") BigInteger projectId);

    @Query(value = "select trigger.* from mb_configuration trigger "
            + "where \"type\"= 'trigger' "
            + "and (trigger_state = 'ACTIVE' or trigger_state = 'ERROR') "
            + "and parent_conf_id in (select mc.id from mb_configuration mc where mc.parent_in_server_id in "
            + "(select ms.id from mb_servers ms where ms.project_id = :projectId))", nativeQuery = true)
    Collection<TriggerConfiguration> getAllActiveAndErrorTriggersByProjectId(@Param("projectId") BigInteger projectId);

    @Query(value = "select trigger.* from mb_configuration trigger "
            + "where \"type\"= 'trigger' "
            + "and parent_conf_id in (select mc.id from mb_configuration mc where mc.parent_in_server_id in "
            + "(select ms.id from mb_servers ms where ms.project_id = :projectId))", nativeQuery = true)
    Collection<TriggerConfiguration> getAllTriggersByProjectId(@Param("projectId") BigInteger projectId);

}
