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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.SearchRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.util.provider.TemplateProvider;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;

@NoRepositoryBean
public interface TemplateRepository<K extends TemplateProvider, T extends Template<K>>
        extends StorableRepository<T>, QuerydslPredicateExecutor<T>, SearchRepository<T> {

    @Query(value = "select distinct templ_labels.labels "
                 + "from mb_templates as templates "
                 + "inner join mb_templates_labels as templ_labels on templ_labels.id = templates.id "
                 + "where templates.project_id = :projectId",
           nativeQuery = true)
    Set<String> getAllLabels(@Param("projectId") BigInteger projectId);

    @Query(value = "select sys.name as system_name, ser.name as server_name, env.id as env_id, env.name as env_name "
            + "from mb_configuration conf "
            + "inner join mb_systems sys on sys.id = conf.system_id "
            + "inner join mb_servers ser on ser.id = conf.parent_out_server_id "
            + "inner join mb_env_outbound env_outbound "
            + " on (env_outbound.systems = conf.system_id and env_outbound.servers = conf.parent_out_server_id) "
            + "inner join mb_env env on env.id = env_outbound.environment_id "
            + "where conf.type_name = 'org.qubership.automation.itf.transport.diameter.outbound.DiameterOutbound' "
            + "and conf.type = 'outbound' "
            + "and sys.project_id = :projectId "
            + "and ((cast(conf.params as jsonb) @> (cast((concat('{\"DPR\":\"', :templateId, '\"}')) as jsonb))) "
            + "or (cast(conf.params as jsonb) @> (cast((concat('{\"dwa\":\"', :templateId, '\"}')) as jsonb))) "
            + "or (cast(conf.params as jsonb) @> (cast((concat('{\"CER\":\"', :templateId, '\"}')) as jsonb))) "
            + "or (cast(conf.params as jsonb) @> (cast((concat('{\"DPA\":\"', :templateId, '\"}')) as jsonb))))",
            nativeQuery = true)
    List<Map<String, Object>> findUsagesOnOutboundDiameterConfiguration(@Param("templateId") BigInteger templateId,
                                                                        @Param("projectId") BigInteger projectId);
}
