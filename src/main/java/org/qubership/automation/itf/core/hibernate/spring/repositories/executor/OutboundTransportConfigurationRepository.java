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

import javax.persistence.QueryHint;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.jpa.environment.OutboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.template.OutboundTemplateTransportConfiguration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface OutboundTransportConfigurationRepository extends StorableRepository<OutboundTransportConfiguration>,
        QuerydslPredicateExecutor<OutboundTransportConfiguration> {

    @Query(value = "select otc from OutboundTransportConfiguration otc where otc.ecId = :ecId")
    OutboundTransportConfiguration findByEcId(@Param("ecId") String ecId);

    @Query(value = "select ec_project_id from mb_configuration "
            + "where type='outbound' and ec_project_id is not null "
            + "and parent_out_server_id in (select id from mb_servers where project_id = :projectId) "
            + "group by ec_project_id", nativeQuery = true)
    Collection<String> getEcProjectIds(@Param("projectId") BigInteger projectId);

    @Query(value = "select otc from OutboundTransportConfiguration otc where otc.ecProjectId = :ecProjectId")
    Collection<OutboundTransportConfiguration> getByEcProject(@Param("ecProjectId") String ecProjectId);

    @Modifying
    @Query(value = "update mb_configuration "
            + "set ec_project_id = null, ec_id = null "
            + "where ec_project_id = :ecProjectId and type='outbound'", nativeQuery = true)
    void unbindByEcProject(@Param("ecProjectId") String ecProjectId);

    @Query(value = "select conf from OutboundTemplateTransportConfiguration conf "
            + "where parent_template_id = :templateId")
    Collection<OutboundTemplateTransportConfiguration> findAllCfgByTemplate(@Param("templateId") BigInteger templateId);

    @Query(value = "select conf from OutboundTemplateTransportConfiguration conf "
            + "where parent_template_id = :templateId and type_name = :typeName")
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "outboundTemplateTransportConfigurationsCollectionCache")})
    Collection<OutboundTemplateTransportConfiguration> findCfgByTemplateAndType(
            @Param("templateId") BigInteger templateId, @Param("typeName") String typeName);

    @Query(value = "select otc.* from mb_configuration otc "
            + "where otc.parent_out_server_id = :serverId "
            + "and otc.system_id = :systemId "
            + "and otc.type_name = :typeName", nativeQuery = true)
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "outboundTransportConfigurationCache")})
    OutboundTransportConfiguration findOne(@Param("systemId") BigInteger systemId,
                                           @Param("serverId") BigInteger serverId,
                                           @Param("typeName") String typeName);

    @Query(value = "select otc.* from mb_configuration otc "
            + "where otc.parent_out_server_id = :serverId "
            + "and otc.system_id = :systemId", nativeQuery = true)
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "outboundTransportConfigurationsCollectionCache")})
    Iterable<OutboundTransportConfiguration> findAll(@Param("systemId") BigInteger systemId,
                                                     @Param("serverId") BigInteger serverId);
}
