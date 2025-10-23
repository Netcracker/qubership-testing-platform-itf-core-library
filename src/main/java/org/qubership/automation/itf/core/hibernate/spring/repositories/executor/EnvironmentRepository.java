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
import java.util.List;
import java.util.Set;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.SearchRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.report.LinkCollectorConfiguration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EnvironmentRepository
        extends SearchRepository<Environment>, QuerydslPredicateExecutor<Environment>, StorableRepository<Environment> {
    /* Explicit commit is necessary in case autocommit=off database setting.
        Please take it in mind for native @Modifying queries.
        Without it (PostgreSQL 9.6) some sessions remains in the 'idle in transaction' status
        and holds transaction locks preventing further normal operations.
     */
    @Modifying
    @Query(value = "update mb_env set state = case "
            + "when id in (select distinct get_env(id) from mb_configuration where \"type\"='trigger') "
            + "then 'INACTIVE' else 'EMPTY' end; commit;", nativeQuery = true)
    void setInitialEnvStateInactiveOrEmpty();

    @Modifying
    @Query(value =
            "update mb_configuration set trigger_state = 'INACTIVE', activation_error_message = null "
                    + "where \"type\"='trigger' "
                    + "and id not in (select get_all_env_triggers(id) from mb_env); commit;", nativeQuery = true)
    void turnOffLostTriggers();

    @Query(value = "select state from mb_env where id = :envId", nativeQuery = true)
    String getEnvironmentState(@Param("envId") BigInteger envId);

    @Query(value = "select env from Environment env where env.ecId = :ecId")
    Environment findEnvironmentByEcId(@Param("ecId") String ecId);

    @Query(value = "select ec_project_id from mb_env where ec_project_id is not null and project_id = :projectId "
            + "group by ec_project_id", nativeQuery = true)
    Collection<String> getEcProjectIds(@Param("projectId") BigInteger projectId);

    @Query(value = "select environment from Environment environment where environment.ecProjectId = :ecProjectId")
    Collection<Environment> getEnvironmentsByEcProject(@Param("ecProjectId") String ecProjectId);

    @Modifying
    @Query(value = "update mb_env set ec_project_id = null, ec_id = null where ec_project_id = :ecProjectId",
            nativeQuery = true)
    void unbindByEcProject(@Param("ecProjectId") String ecProjectId);

    @Query(value = "select environment_id, mb_env.\"name\" from mb_env_inbound "
            + "  inner join mb_env on environment_id=mb_env.id "
            + "  where systems = :systemId and servers = :serverId",
            nativeQuery = true)
    List<Object[]> findEnvironmentByServerAndSystemIdPair(
            @Param("systemId") BigInteger systemId, @Param("serverId") BigInteger serverId);

    @Query(value = "with system_info as (select cast(:systemId as BIGINT) as id)\n"
            + "select distinct on (t.environment_id) e.ec_id "
            + "from ("
            + "  select environment_id "
            + "  from mb_env_inbound "
            + "  where systems in (select id from system_info) "
            + "  union all "
            + "  select environment_id "
            + "  from mb_env_outbound "
            + "  where systems in (select id from system_info) "
            + ") t "
            + "join mb_env e on t.environment_id = e.id "
            + "where e.ec_id is not null", nativeQuery = true)
    Collection<String> findEnvironmentEcIdsForSystem(@Param("systemId") BigInteger systemId);

    @Query(value = "with server_info as (select cast(:serverId as BIGINT) as id) "
            + "select distinct on (t.environment_id) e.ec_id "
            + "from ( "
            + "  select environment_id "
            + "  from mb_env_inbound "
            + "  where servers in (select id from server_info) "
            + "  union all "
            + "  select environment_id "
            + "  from mb_env_outbound "
            + "  where servers in (select id from server_info) "
            + ") t "
            + "join mb_env e on t.environment_id = e.id "
            + "where e.ec_id is not null", nativeQuery = true)
    Collection<String> findEnvironmentEcIdsForServer(@Param("serverId") BigInteger serverId);

    @Query(value = "select env from Environment env "
            + "where lower(env.name) like concat('%', lower(:name), '%')  "
            + "and env.projectId = :projectId")
    List<Environment> findByPieceOfNameAndProjectId(@Param("name") String name,
                                                    @Param("projectId") BigInteger projectId);

    List<Environment> findByNameAndProjectId(@Param("name") String name, @Param("projectId") BigInteger projectId);

    @Query(value = "select environment_id from mb_env_outbound "
            + "where servers=:serverId "
            + "and systems in :systemIds",
            nativeQuery = true)
    List<BigInteger> findByServerAndSystems(@Param("serverId") BigInteger serverId,
                                            @Param("systemIds") Collection<BigInteger> systemIds);

    @Query(value = "select jsonb_build_object( "
            + "'system_server', t.system_server, 'transports', json_agg(t.transports_info) "
            + ")\\:\\:text "
            + "from  ( "
            + "select  systems.name||' / '||servers.name as system_server, "
            + "jsonb_build_object( 'transport_name', transport.name, 'triggers', "
            + "json_agg( "
            + "jsonb_build_object( 'trigger_id', transport_trigger.id, 'trigger_name', "
            + "case when transport_trigger.name is not null "
            + "then transport_trigger.name "
            + "else '[ID='||transport_trigger.id||']' "
            + "end, "
            + "'trigger_state', "
            + "transport_trigger.trigger_state "
            + ") "
            + ") "
            + ") as transports_info "
            + "from  mb_env_inbound env_inbound "
            + "inner join  mb_systems systems on systems.id = env_inbound.systems "
            + "inner join  mb_servers servers on servers.id = env_inbound.servers "
            + "inner join  mb_configuration inbound_transport_config "
            + "on  inbound_transport_config.parent_in_server_id = env_inbound.servers "
            + "inner join  mb_configuration transport "
            + "on  transport.id = inbound_transport_config.transport_id "
            + "and  transport.parent_system_id = env_inbound.systems "
            + "left join  mb_configuration transport_trigger "
            + "on  transport_trigger.parent_conf_id = inbound_transport_config.id "
            + "where  env_inbound.environment_id = :envId "
            + "group by  transport.id, systems.name||' / '||servers.name "
            + ") t group by t.system_server",
            nativeQuery = true)
    List<String> getInboundInfo(@Param("envId") BigInteger envId);

    @Query(value = "select env.reportCollectors from Environment env where env.id = :envId")
    Set<LinkCollectorConfiguration> getLinkCollectorsByEnvId(@Param("envId") BigInteger envId);

    @Query(value = "select " +
            "mb_env_inbound.environment_id as environment_id, mb_env.name as environment_name, " +
            "mb_systems.id as system_id, mb_systems.name as system_name, " +
            "mb_servers.id as server_id, mb_servers.name as server_name " +
            "from mb_env_inbound " +
            "join mb_env on mb_env.id = mb_env_inbound.environment_id " +
            "join mb_servers on mb_env_inbound.servers = mb_servers.id " +
            "join mb_systems on mb_env_inbound.systems = mb_systems.id " +
            "where (mb_env_inbound.systems, mb_env_inbound.servers) in ( " +
            "select systems, servers from mb_env_inbound) " +
            "and mb_env.project_id = :projectId " +
            "group by mb_env_inbound.environment_id,  mb_env.name, mb_systems.id, mb_systems.name, " +
            "mb_servers.id, mb_servers.name " +
            "having count(environment_id) > 1", nativeQuery = true)
    List<Object[]> findDuplicateConfigurationBySystemServer(@Param("projectId") BigInteger projectId);

}
