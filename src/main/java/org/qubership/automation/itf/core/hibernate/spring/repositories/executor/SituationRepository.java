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
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.FastStubsCandidate;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface SituationRepository extends StorableRepository<Situation> {
    @Query(value = "select count(*) from mb_situation where bv_tcid = :bvCaseId", nativeQuery = true)
    int countBvCaseUsages(@Param("bvCaseId") String bvCaseId);

    @Query(value = "select distinct msl.labels "
            + "from mb_situation as ms "
            + "inner join mb_operations as mo on mo.id = ms.parent_id "
            + "inner join mb_situation_labels as msl on msl.id = ms.id "
            + "where mo.project_id = :projectId ",
           nativeQuery = true)
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

    @Query(value = "select sit.id, sit.parent_id as operation_id, sit.bv_tcid, sit.validate_incoming,\n"
                    + "st.\"name\" as situation_name,\n"
                    + "op.\"name\" as operation_name,\n"
                    + "op.parent_id as system_id,\n"
                    + "sys.\"name\" as system_name\n"
                    + "from mb_situation sit\n"
                    + "inner join mb_step_container st on sit.id=st.id\n"
                    + "inner join mb_operations op on sit.parent_id=op.id\n"
                    + "inner join mb_systems as sys on sys.id = op.parent_id\n"
                    + "where sit.bv_tcid is not null \n"
                    + "and sit.bv_tcid != '' \n"
                    + "and sys.project_id = :projectId", nativeQuery = true)
    List<Object[]> getSituationsWithBvLinks(@Param("projectId") BigInteger projectId);
    @Query(value = "with\n"
            + "common as (select\n"
            + "sys.id as systemId,\n"
            + "operation.id as operationId,\n"
            + "operation.\"name\" as operationName,\n"
            + "situation.id as situationId,\n"
            + "step.\"name\" as situationName,\n"
            + "case when transport.type_name = "
            + "'org.qubership.automation.itf.transport.rest.inbound.RESTInboundTransport' then 'REST' "
            + " when transport.type_name = "
            + "'org.qubership.automation.itf.transport.soap.http.inbound.SOAPOverHTTPInboundTransport' then 'SOAP' "
            + "else 'UNKNOWN' end as transportType, \n"
            + "transport.params\\:\\:json#>>'{endpoint}' as endpoint,\n"
            + "case when situation.validate_incoming is not null and situation.validate_incoming != 'NO' then 1 "
            + "else 0 end as bv_validation,\n"
            + "count(case when templ.\"text\" ~ "
            + "'.*(#get_config|#get_ds_file|#load_part|#next_index|#set_config|#set_userdata).*' then 1 end) "
            + "over (partition by situation.id) as template_script,\n"
            + "count(case when situation.pre_script ~ "
            + "'.*(#get_config|#get_ds_file|#load_part|#next_index|#set_config|#set_userdata).*' then 1 end) "
            + "over (partition by situation.id) as pre_script,\n"
            + "count(case when situation.post_script ~ "
            + "'.*(#get_config|#get_ds_file|#load_part|#next_index|#set_config|#set_userdata).*' then 1 end) "
            + "over (partition by situation.id) as post_script,\n"
            + "count(case when situation.pre_validation_script ~ "
            + "'.*(#get_config|#get_ds_file|#load_part|#next_index|#set_config|#set_userdata).*' then 1 end) "
            + "over (partition by situation.id) as prevalid_script,\n"
            + "case when triggers.id is not null then 1 else 0 end as start_finish_triggers\n"
            + "from mb_situation situation\n"
            + "left outer join mb_triggers triggers on (triggers.situation_id = situation.id),\n"
            + "mb_operations operation,\n"
            + "mb_systems sys,\n"
            + "mb_configuration transport,\n"
            + "mb_step_container step,\n"
            + "mb_projects project,\n"
            + "mb_templates templ,\n"
            + "mb_steps steps\n"
            + "where operation.project_id = project.id\n"
            + "and project.\"uuid\" = :projectUuid \n"
            + "and situation.parent_id = operation.id\n"
            + "and step.id = situation.id\n"
            + "and operation.parent_id = sys.id\n"
            + "and operation.transport_id = transport.id\n"
            + "and transport.\"type\" = 'transport'\n"
            + "and transport.type_name in ('org.qubership.automation.itf.transport.rest.inbound.RESTInboundTransport',"
            + "'org.qubership.automation.itf.transport.soap.http.inbound.SOAPOverHTTPInboundTransport')\n"
            + "and steps.parent_id = situation.id "
            + "and templ.id = coalesce(steps.sys_template_id, steps.op_template_id)\n"
            + "and (transport.params\\:\\:json#>>'{isStub}' = 'Yes' "
            + "or transport.params\\:\\:json#>>'{isStub}' is null)\n"
            + "and operation.id in ( :operationIds )\n"
            + "),\n"
            + "common_with_details as (\n"
            + "select\n"
            + "common_info.systemId,\n"
            + "common_info.operationId,\n"
            + "common_info.operationName,\n"
            + "common_info.situationId,\n"
            + "common_info.situationName,\n"
            + "common_info.transportType,\n"
            + "common_info.endpoint,\n"
            + "(common_info.bv_validation*1 + common_info.template_script*2 + common_info.pre_script*4 + "
            + "common_info.post_script*8 + common_info.prevalid_script*16 + common_info.start_finish_triggers*32) "
            + "as details\n"
            + "from common common_info\n"
            + ")\n"
            + "select d.* from common_with_details d", nativeQuery = true)
    Optional<List<FastStubsCandidate>> getFastStubsCandidates(@Param("projectUuid") UUID projectUuid,
                                                              @Param("operationIds") List<BigInteger> operationIds);
}
