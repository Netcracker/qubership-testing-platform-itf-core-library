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

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.ServerSearchRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.server.ServerHB;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ServerRepository extends ServerSearchRepository<ServerHB>, StorableRepository<ServerHB> {

    @Query(value = "select server from ServerHB server "
            + "where server.ecId = :ecId or (server.name = :name and server.url = :url)")
    Server findByEcId(@Param("ecId") String ecId, @Param("name") String name, @Param("url") String url);

    @Query(value = "select ec_project_id from mb_servers "
            + "where project_id = :projectId and ec_project_id is not null "
            + "group by ec_project_id", nativeQuery = true)
    Collection<String> getEcProjectIds(@Param("projectId") BigInteger projectId);

    @Query(value = "select server from ServerHB server where server.ecProjectId = :ecProjectId")
    Collection<Server> getByEcProject(@Param("ecProjectId") String ecProjectId);

    @Modifying
    @Query(value = "update mb_servers "
            + "set ec_project_id = null, ec_id = null "
            + "where ec_project_id = :ecProjectId", nativeQuery = true)
    void unbindByEcProject(@Param("ecProjectId") String ecProjectId);

    @Query(value = "select id from mb_servers "
            + " where project_id = :projectId "
            + " and case when right(url,1)='/' then url else url||'/' end = :url",
            nativeQuery = true)
    List<BigInteger> getServersByProjectAndUrlSlashed(
            @Param("url") String url,
            @Param("projectId") BigInteger projectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM mb_configuration "
            + "WHERE type = 'outbound' "
            + "AND (parent_out_server_id, system_id) IN ( "
            + "SELECT mc.parent_out_server_id, mc.system_id "
            + "FROM mb_configuration mc "
            + "LEFT JOIN mb_env_outbound eo ON mc.parent_out_server_id = eo.servers AND mc.system_id = eo.systems "
            + "WHERE eo.servers IS NULL AND eo.systems IS NULL)", nativeQuery = true)
    int deleteUnusedOutboundConfigurations();

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "DELETE FROM mb_configuration "
            + "WHERE type = 'outbound' "
            + "AND parent_out_server_id IN (SELECT id FROM mb_servers WHERE project_id = :projectId) "
            + "AND (parent_out_server_id, system_id) IN ("
            + "SELECT mc.parent_out_server_id, mc.system_id "
            + "FROM mb_configuration mc "
            + "LEFT JOIN mb_env_outbound eo ON mc.parent_out_server_id = eo.servers AND mc.system_id = eo.systems "
            + "WHERE eo.servers IS NULL AND eo.systems IS null and mc.parent_out_server_id in "
            + "(SELECT id FROM mb_servers WHERE project_id = :projectId))", nativeQuery = true)
    int deleteUnusedOutboundConfigurationsByProjectId(@Param("projectId") BigInteger projectId);

    @Query(value = "select id from mb_configuration where parent_conf_id in "
            + "(select id from mb_configuration where parent_in_server_id = :serverId)",
            nativeQuery = true)
    List<BigInteger> getTransportTriggersByServerId(@Param("serverId") BigInteger serverId);
}
