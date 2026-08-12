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

package org.qubership.automation.itf.core.hibernate.spring.repositories.reports;

import java.math.BigInteger;
import java.util.List;
import java.util.Set;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.RootRepository;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface InstanceContextRepository extends RootRepository<InstanceContext> {

    @NativeQuery("select "
            + "    mp.param_name, "
            + "    case "
            + "        when cardinality(mpmv.arr) = 1 then array_to_string(mpmv.arr, '') "
            + "        else '[' || array_to_string(mpmv.arr, ', ') || ']' "
            + "    end "
            + "from "
            + "    mb_message_param mp "
            + "    left join lateral ( "
            + "        select array_agg(mpmv.value) as arr "
            + "        from mb_message_param_multiple_value mpmv "
            + "        where mpmv.message_param_id = mp.id and mpmv.part_num = :partNum "
            + "    ) mpmv on true "
            + "where "
            + "    mp.context_id = cast(:spcontext_id as int8) "
            + "    and mp.part_num = :partNum")
    List<Object[]> getSpMessageParameters(@Param("spcontext_id") BigInteger spcontextId,
                                          @Param("partNum") Integer partNum);

    @NativeQuery("select error_name, error_message from mb_instance "
            + "where id = cast(:instance_id as int8) "
            + "and part_num = :partNum")
    List<Object[]> stepInstanceError(@Param("instance_id") BigInteger stepInstanceId,
                                     @Param("partNum") Integer partNum);

    /*  One more select is added, combined via 'union'.
     *  Performance degradation is possible in case big monitoring tables - should be checked
     */
    @NativeQuery("""
            select error_name, error_message, "name", id \
            from mb_instance \
            where context_id = cast(:tcContextId as int8) \
            and (error_name is not null or error_message is not null) \
            union\s
            select error_name, error_message, "name", id\s
            from mb_instance\s
            where id=(select initiator_id from mb_tccontext where id = cast(:tcContextId as int8))
            and (error_name is not null or error_message is not null)
            and part_num = :partNum
            order by id""")
    List<Object[]> allTcContextInstancesErrors(@Param("tcContextId") BigInteger tcContextId,
                                               @Param("partNum") Integer partNum);

    @NativeQuery("select "
            + " case  when \"ID\"  is null then 0 else \"ID\" end as id,"
            + " case  when \"PARENT\"  is null then 0 else \"PARENT\" end as parent,"
            + " case  when \"TYPE\"  is null then 'NULL' else \"TYPE\" end as type,"
            + " case  when \"DESCRIPTION\"   is null then 'NULL' else \"DESCRIPTION\"  end as description, "
            + " case  when \"PATH\"  is null then 'NULL' else \"PATH\"  end as path,"
            + " case  when \"LEVEL\"  is null then 0 else \"LEVEL\" end  as level,"
            + " case  when \"STATUS\"  is null then 'NULL' else \"STATUS\"  end as status,"
            + " case  when \"DURATION\"   is null then 'NULL' else \"DURATION\" end as duration,"
            + " case  when \"START_TIME\"   is null then 'NULL' else \"START_TIME\" end as start_time,"
            + " case  when \"END_TIME\"   is null then 'NULL' else \"END_TIME\" end as end_time"
            + " from  get_messages_from_tc_context_no_message(cast(:tcContextId as int8), cast(:partNum as smallint))"
            + " order by start_time, type ")
    List<Object[]> getTcContextTree(@Param("tcContextId") BigInteger tcContextId,
                                    @Param("partNum") Integer partNum);

    @NativeQuery("select sp_ctx.id, sp_ctx.incoming_message_id, sp_ctx.outgoing_message_id, sp_ctx.json_string "
            + "from mb_context some_ctx "
            + "inner join mb_context sp_ctx on sp_ctx.parent_ctx_id = some_ctx.id "
            + "  and sp_ctx.part_num = some_ctx.part_num "
            + "where some_ctx.\"instance\" = cast(:stepinstanceid as int8) "
            + "and some_ctx.part_num = :partNum")
    List<Object[]> getMessageIds(@Param("stepinstanceid") BigInteger stepInstanceId,
                                 @Param("partNum") Integer partNum);

    @NativeQuery("select text from mb_message where id = cast(:message_id as int8) "
            + "and part_num = :partNum")
    String getMessageText(@Param("message_id") BigInteger messageId,
                          @Param("partNum") Integer partNum);

    @NativeQuery("select key, value from mb_message_headers "
            + "where parent_id = cast(:message_id as int8) "
            + "and part_num = :partNum")
    List<Object[]> getMessageHeaders(@Param("message_id") BigInteger messageId,
                                     @Param("partNum") Integer partNum);

    @NativeQuery("select key, value from mb_message_connection_properties "
            + "where parent_id = cast(:message_id as int8) "
            + "and part_num = :partNum")
    List<Object[]> getMessageConnectionProperties(@Param("message_id") BigInteger messageId,
                                                  @Param("partNum") Integer partNum);

    @NativeQuery("select key, value from mb_context_report_links "
            + "where parent_id = cast(:tcContextId as int8) "
            + "and part_num = :partNum")
    List<Object[]> getTcContextReportLinks(@Param("tcContextId") BigInteger tcContextId,
                                           @Param("partNum") Integer partNum);

    @NativeQuery("select key from mb_context_binding_keys "
            + "where id = cast(:tcContextId as int8) "
            + "and part_num = :partNum")
    Set<String> getTcContextBindingKeys(@Param("tcContextId") BigInteger tcContextId,
                                        @Param("partNum") Integer partNum);

    @NativeQuery("select key from mb_context_binding_keys "
            + "where id = cast(:tcContextId as int8)")
    Set<String> getTcContextBindingKeys(@Param("tcContextId") BigInteger tcContextId);

    /* It's very strange but 'getTCContextInformation' query sometimes is very slow on the Openshift.
     *   Explain plan shows index scan plans on the mb_context and mb_instance tables but
     *   the query is sometimes much slower than separate queries.
     *
     *   So, below there are three separate queries to replace 'getTCContextInformation' query
     */
    @NativeQuery("select "
            + " ctx.id, ctx.name, ctx.initiator_id, ctx.environment_id, "
            + " ctx.status, ctx.start_time, ctx.end_time, ctx.json_string, ctx.project_id "
            + "from mb_context ctx "
            + "where ctx.id = cast(:tcContextId as int8)")
    List<Object[]> getTcContextInfo(@Param("tcContextId") BigInteger tcContextId);

    @NativeQuery("select ctx.json_string from mb_context ctx "
            + "where ctx.id = cast(:tcContextId as int8) "
            + "and ctx.part_num = :partNum")
    String getContextVariables(@Param("tcContextId") BigInteger tcContextId,
                               @Param("partNum") Integer partNum);

    @NativeQuery("select ctx.json_string from mb_context ctx "
            + "where ctx.id = cast(:tcContextId as int8)")
    String getContextVariables(@Param("tcContextId") BigInteger tcContextId);

    @NativeQuery("select ini.name, ini.type, ini.situation_id, ini.chain_id, ini.callchain_execution_data, "
            + "ini.operation_name, ini.system_name, ini.system_id "
            + "from mb_instance ini "
            + "where ini.id = cast(:initiator_id as int8) "
            + "and ini.part_num = :partNum")
    List<Object[]> getTcContextInitiatorInfo(@Param("initiator_id") BigInteger initiatorId,
                                             @Param("partNum") Integer partNum);

    @NativeQuery("select validation_results from mb_context "
            + "where id = cast(:spcontext_id as int8) "
            + "and part_num = :partNum")
    String getValidationResults(@Param("spcontext_id") BigInteger spcontextId,
                                @Param("partNum") Integer partNum);

    @NativeQuery("select id, situation_id from mb_instance "
            + "where context_id = cast(:tcContextId as int8) "
            + "and situation_id is not null "
            + "and part_num = :partNum")
    List<Object[]> getTcContextStepsSituations(@Param("tcContextId") BigInteger tcContextId,
                                               @Param("partNum") Integer partNum);
}
