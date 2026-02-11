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
import java.util.Set;

import jakarta.persistence.QueryHint;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SystemRepository extends StorableRepository<System> {

    @Query(value = "select oper from Operation oper "
            + "where parent_id = :parentId and definition_key = :key "
    )
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "operationByDefinitionKeyCache")})
    Operation findFirstByDefineOperation(@Param("parentId") BigInteger parentId, @Param("key") String key);

    @Query(value = "select distinct s_labels.labels as labels "
            + "from mb_systems as s "
            + "inner join mb_systems_labels as s_labels on s_labels.id = s.id "
            + "where project_id = :projectId "
            + "union "
            + "select distinct mfl.labels as labels "
            + "from mb_folders as mf "
            + "inner join mb_folders_labels as mfl on mfl.id =mf.id "
            + "where mf.\"type\" = 'systems' and mf.project_id = :projectId",
           nativeQuery = true)
    Set<String> getAllLabels(@Param("projectId") BigInteger projectId);

    @Query(value = "select sys from System sys where sys.ecId = :ecId")
    System findSystemByEcId(@Param("ecId") String ecId);

    @Query(value = "select ec_project_id from mb_systems "
            + "where ec_project_id is not null and project_id = :projectId "
            + "group by ec_project_id", nativeQuery = true)
    Collection<String> getEcProjectIds(@Param("projectId") BigInteger projectId);

    @Query(value = "select system from System system where system.ecProjectId = :ecProjectId")
    Collection<System> getSystemsByEcProject(@Param("ecProjectId") String ecProjectId);

    @Modifying
    @Query(value = "update mb_systems set ec_project_id = null, ec_id = null, ec_label = null "
            + "where ec_project_id = :ecProjectId", nativeQuery = true)
    void unbindByEcProject(@Param("ecProjectId") String ecProjectId);

    @Query(value = "select sys from System sys where sys.ecLabel = :ecLabel and sys.projectId = :projectId "
            + "and sys.ecId is not null")
    System findByEcLabel(@Param("ecLabel") String ecLabel, @Param("projectId") BigInteger projectId);

    @Query(value = "select system from System system "
            + "where lower(system.name) like concat('%', lower(:name), '%') "
            + "and system.projectId = :projectId")
    Collection<System> findByPieceOfNameAndProject(@Param("name") String name,
                                                   @Param("projectId") BigInteger projectId);

    @Query(value = "select system from System system "
            + "where system.projectId = :projectId")
    Collection<System> findByProject(@Param("projectId") BigInteger projectId);

    @Query(value = "select distinct receiver_id "
            + "FROM mb_steps "
            + "WHERE parent_id in "
            + "( "
            + "    SELECT distinct situation_id "
            + "    FROM mb_steps "
            + "    WHERE parent_id in ( "
            + "        WITH RECURSIVE embedded_steps (chain_id) AS ( "
            + "        SELECT distinct chain_id "
            + "        FROM mb_steps "
            + "        WHERE parent_id = :chainId "
            + "            and enabled and \"type\"='embedded' and chain_id is not null "
            + "        UNION "
            + "        SELECT distinct p.chain_id "
            + "        FROM mb_steps p "
            + "        INNER JOIN embedded_steps ON embedded_steps.chain_id = p.parent_id  "
            + "            and p.enabled and p.\"type\"='embedded' and p.chain_id is not null "
            + "        ) "
            + "        select * from embedded_steps "
            + "        union "
            + "        select :chainId "
            + "    ) "
            + "    and enabled and \"type\"='situation' and situation_id is not null "
            + ")", nativeQuery = true)
    List<BigInteger> getReceiverSystemIdsFromCallChainSteps(@Param("chainId") BigInteger chainId);

    @Query(value = "select new org.qubership.automation.itf.core.model.IdNamePair(system.id, system.name) "
            + "from System as system "
            + "where system.projectId = :projectId")
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "simpleSystemListByProjectCache")})
    List<IdNamePair> getSimpleListByProject(@Param("projectId") BigInteger projectId);

    @Query(value = "select id from mb_configuration where parent_conf_id in "
            + "(select id from mb_configuration where transport_id in "
            + "  (select id from mb_configuration where parent_system_id = :systemId))",
            nativeQuery = true)
    List<BigInteger> getTransportTriggersBySystemId(@Param("systemId") BigInteger systemId);

    @Query(value = "select id from mb_triggers where set_parent_id in "
            + "(select id from mb_situation where parent_id in "
            + "  (select id from mb_operations where parent_id = :systemId))",
            nativeQuery = true)
    List<BigInteger> getSituationEventTriggersBySystemId(@Param("systemId") BigInteger systemId);

}
