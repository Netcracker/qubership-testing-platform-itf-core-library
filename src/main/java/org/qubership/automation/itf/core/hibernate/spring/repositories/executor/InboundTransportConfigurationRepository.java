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
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InboundTransportConfigurationRepository extends StorableRepository<InboundTransportConfiguration>,
        QuerydslPredicateExecutor<InboundTransportConfiguration> {

    @Modifying
    @Query("delete from InboundTransportConfiguration i where i.referencedConfiguration = :configuration")
    void onDeleteTransport(@Param("configuration") TransportConfiguration configuration);

    @Query("select i from TransportConfiguration i where i.id = :id")
    TransportConfiguration findFirstTransport(@Param("id") BigInteger id);

    // TODO: query text and method name contradict each other. Need to check what is wrong, and fix.
    @Query(value = "select i.transport_id from mb_configuration i where i.id =  :parent",
            nativeQuery = true)
    Object getIdByTransport(@Param("parent") BigInteger parent);

    @Query(value = "select itc from InboundTransportConfiguration itc where itc.ecId = :ecId")
    InboundTransportConfiguration findByEcId(@Param("ecId") String ecId);

    @Query(value = "select ec_project_id from mb_configuration "
            + "where type='inbound' and ec_project_id is not null "
            + "and parent_in_server_id in (select id from mb_servers where project_id = :projectId) "
            + "group by ec_project_id", nativeQuery = true)
    Collection<String> getEcProjectIds(@Param("projectId") BigInteger projectId);

    @Query(value = "select itc from InboundTransportConfiguration itc where itc.ecProjectId = :ecProjectId")
    Collection<InboundTransportConfiguration> getByEcProject(@Param("ecProjectId") String ecProjectId);

    @Modifying
    @Query(value = "update mb_configuration "
            + "set ec_project_id = null, ec_id = null "
            + "where ec_project_id = :ecProjectId and type='inbound'", nativeQuery = true)
    void unbindByEcProject(@Param("ecProjectId") String ecProjectId);

    @Query(value = "select inb_conf.*  from mb_configuration inb_conf "
            + "where inb_conf.type = 'inbound' "
            + "and inb_conf.parent_in_server_id = :serverId "
            + "and inb_conf.transport_id = :transportId", nativeQuery = true)
    InboundTransportConfiguration findOne(@Param("serverId") BigInteger serverId,
                                          @Param("transportId") BigInteger transportId);

    @Query(value = "select inb_conf.*  "
            + "from mb_configuration inb_conf, mb_configuration tr_conf "
            + "where inb_conf.type = 'inbound' "
            + "and inb_conf.parent_in_server_id = :serverId "
            + "and tr_conf.id = inb_conf.transport_id "
            + "and tr_conf.parent_system_id = :systemId", nativeQuery = true)
    Iterable<InboundTransportConfiguration> findAll(@Param("serverId") BigInteger serverId,
                                                    @Param("systemId") BigInteger systemId);
}
