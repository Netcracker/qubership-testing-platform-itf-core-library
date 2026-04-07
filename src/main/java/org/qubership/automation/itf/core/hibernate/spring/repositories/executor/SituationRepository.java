/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.FastStubsCandidate;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface SituationRepository extends StorableRepository<Situation> {
    @NativeQuery("select count(*) from mb_situation where bv_tcid = :bvCaseId")
    int countBvCaseUsages(@Param("bvCaseId") String bvCaseId);

    @NativeQuery("select distinct msl.labels "
            + "from mb_situation as ms "
            + "inner join mb_operations as mo on mo.id = ms.parent_id "
            + "inner join mb_situation_labels as msl on msl.id = ms.id "
            + "where mo.project_id = :projectId ")
    Set<String> getAllLabels(@Param("projectId") BigInteger projectId);

    @Query(value = "select new org.qubership.automation.itf.core.model.IdNamePair(situation.id, situation.name) "
            + "from Situation as situation "
            + "inner join Operation as operation on operation = situation.parent "
            + "where operation.projectId = :projectId")
    Collection<IdNamePair> findAllByProjectIdOfNameAndId(@Param("projectId") BigInteger projectId);

        @Query(value = "select situation from Situation as situation "
            + "inner join Operation as operation on operation = situation.parent "
            + "where operation.projectId = :projectId")
    Collection<Situation> findAllByProjectId(@Param("projectId") BigInteger projectId);

    @Query(value = "select situation from Situation as situation "
            + "inner join Operation as operation on operation = situation.parent "
            + "where lower(situation.name) like concat('%', lower(:name), '%')  "
            + "and operation.projectId = :projectId")
    Collection<Situation> findByPieceOfNameAndProject(@Param("name") String name,
                                                      @Param("projectId") BigInteger projectId);

    @Query(value = "select situation from Situation as situation "
            + "inner join Operation as operation on operation = situation.parent "
            + "where lower(situation.name) = lower(:name)"
            + "and operation.projectId = :projectId")
    List<Situation> findByNameAndProjectId(@Param("name") String name, @Param("projectId") BigInteger projectId);

    @NativeQuery("""
                    select sit.id, sit.parent_id as operation_id, sit.bv_tcid, sit.validate_incoming,
                    st."name" as situation_name,
                    op."name" as operation_name,
                    op.parent_id as system_id,
                    sys."name" as system_name
                    from mb_situation sit
                    inner join mb_step_container st on sit.id=st.id
                    inner join mb_operations op on sit.parent_id=op.id
                    inner join mb_systems as sys on sys.id = op.parent_id
                    where sit.bv_tcid is not null\s
                    and sit.bv_tcid != ''\s
                    and sys.project_id = :projectId""")
    List<Object[]> getSituationsWithBvLinks(@Param("projectId") BigInteger projectId);
    @NativeQuery("""
            with
            common as (select
            sys.id as systemId,
            operation.id as operationId,
            operation."name" as operationName,
            situation.id as situationId,
            step."name" as situationName,
            case when transport.type_name = \
            'org.qubership.automation.itf.transport.rest.inbound.RESTInboundTransport' then 'REST' \
             when transport.type_name = \
            'org.qubership.automation.itf.transport.soap.http.inbound.SOAPOverHTTPInboundTransport' then 'SOAP' \
            else 'UNKNOWN' end as transportType,\s
            transport.params\\:\\:json#>>'{endpoint}' as endpoint,
            case when situation.validate_incoming is not null and situation.validate_incoming != 'NO' then 1 \
            else 0 end as bv_validation,
            count(case when templ."text" ~ \
            '.*(#get_config|#get_ds_file|#load_part|#next_index|#set_config|#set_userdata).*' then 1 end) \
            over (partition by situation.id) as template_script,
            count(case when situation.pre_script ~ \
            '.*(#get_config|#get_ds_file|#load_part|#next_index|#set_config|#set_userdata).*' then 1 end) \
            over (partition by situation.id) as pre_script,
            count(case when situation.post_script ~ \
            '.*(#get_config|#get_ds_file|#load_part|#next_index|#set_config|#set_userdata).*' then 1 end) \
            over (partition by situation.id) as post_script,
            count(case when situation.pre_validation_script ~ \
            '.*(#get_config|#get_ds_file|#load_part|#next_index|#set_config|#set_userdata).*' then 1 end) \
            over (partition by situation.id) as prevalid_script,
            case when triggers.id is not null then 1 else 0 end as start_finish_triggers
            from mb_situation situation
            left outer join mb_triggers triggers on (triggers.situation_id = situation.id),
            mb_operations operation,
            mb_systems sys,
            mb_configuration transport,
            mb_step_container step,
            mb_projects project,
            mb_templates templ,
            mb_steps steps
            where operation.project_id = project.id
            and project."uuid" = :projectUuid\s
            and situation.parent_id = operation.id
            and step.id = situation.id
            and operation.parent_id = sys.id
            and operation.transport_id = transport.id
            and transport."type" = 'transport'
            and transport.type_name in ('org.qubership.automation.itf.transport.rest.inbound.RESTInboundTransport',\
            'org.qubership.automation.itf.transport.soap.http.inbound.SOAPOverHTTPInboundTransport')
            and steps.parent_id = situation.id \
            and templ.id = coalesce(steps.sys_template_id, steps.op_template_id)
            and (transport.params\\:\\:json#>>'{isStub}' = 'Yes' \
            or transport.params\\:\\:json#>>'{isStub}' is null)
            and operation.id in ( :operationIds )
            ),
            common_with_details as (
            select
            common_info.systemId,
            common_info.operationId,
            common_info.operationName,
            common_info.situationId,
            common_info.situationName,
            common_info.transportType,
            common_info.endpoint,
            (common_info.bv_validation*1 + common_info.template_script*2 + common_info.pre_script*4 + \
            common_info.post_script*8 + common_info.prevalid_script*16 + common_info.start_finish_triggers*32) \
            as details
            from common common_info
            )
            select d.* from common_with_details d""")
    Optional<List<FastStubsCandidate>> getFastStubsCandidates(@Param("projectUuid") UUID projectUuid,
                                                              @Param("operationIds") List<BigInteger> operationIds);
}
