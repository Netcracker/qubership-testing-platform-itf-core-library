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

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface TransportConfigurationRepository extends StorableRepository<TransportConfiguration> {

    @Query(value = "select transport from TransportConfiguration transport where transport.ecId = :ecId")
    TransportConfiguration findTransportByEcId(@Param("ecId") String ecId);

    @Query(value = "select conf.ec_project_id from mb_configuration as conf "
            + "inner join mb_systems as sys on sys.id = conf.parent_system_id "
            + "where conf.ec_project_id is not null and type = 'transport' AND sys.project_id = :projectId "
            + "group by conf.ec_project_id", nativeQuery = true)
    Collection<String> getEcProjectIds(@Param("projectId") BigInteger projectId);

    @Modifying
    @Query(value = "update mb_configuration set ec_project_id = null, ec_id = null where ec_project_id = :ecProjectId "
            + "and type = 'transport'", nativeQuery = true)
    void unbindByEcProject(@Param("ecProjectId") String ecProjectId);

    @Query(value = "select transport from TransportConfiguration transport where transport.ecProjectId = :ecProjectId")
    Collection<TransportConfiguration> getTransportsByEcProject(@Param("ecProjectId") String ecProjectId);

    @Query(value = "select configuration from TransportConfiguration configuration "
            + "inner join System as system on system = configuration.parent "
            + "where system.projectId = :projectId")
    Collection<TransportConfiguration> findByProjectId(@Param("projectId") BigInteger projectId);

    @Query(value = "select id from mb_configuration where parent_conf_id in "
            + "(select id from mb_configuration where transport_id = :transportId)",
            nativeQuery = true)
    List<BigInteger> getTransportTriggersByTransportConfigurationId(@Param("transportId") BigInteger transportId);

    @Query(value = "select conf.* from mb_configuration conf "
            + "where type_name = 'org.qubership.automation.itf.transport.diameter.outbound.DiameterOutbound' "
            + "and type = 'transport' "
            + "and ((cast(conf.params as jsonb) @> (cast((concat('{\"DPR\":\"', :templateId, '\"}')) as jsonb))) "
            + "or (cast(conf.params as jsonb) @> (cast((concat('{\"dwa\":\"', :templateId, '\"}')) as jsonb))) "
            + "or (cast(conf.params as jsonb) @> (cast((concat('{\"CER\":\"', :templateId, '\"}')) as jsonb))) "
            + "or (cast(conf.params as jsonb) @> (cast((concat('{\"DPA\":\"', :templateId, '\"}')) as jsonb))))",
            nativeQuery = true)
    Collection<TransportConfiguration> findUsagesTemplateOnTransport(@Param("templateId") BigInteger templateId);

}
