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

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.message.template.OperationTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.SystemTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@JaversSpringDataAuditable
@Repository
public interface StepRepository extends StorableRepository<Step>, QuerydslPredicateExecutor<Step> {

    @Modifying
    @Query("update IntegrationStep step set step.systemTemplate = null where step.systemTemplate = :template")
    void onDeleteSystemTemplate(@Param("template") Template template);

    @Modifying
    @Query("update IntegrationStep step set step.operationTemplate = null where step.operationTemplate = :template")
    void onDeleteOperationTemplate(@Param("template") Template template);

    @Modifying
    @Query("update IntegrationStep step set step.sender = null where step.sender = :system")
    void onDeleteSystemSender(@Param("system") System system);

    @Modifying
    @Query("update IntegrationStep step set step.receiver = null where step.receiver = :system")
    void onDeleteSystemReceiver(@Param("system") System system);

    @Modifying
    @Query("update IntegrationStep step set step.operation = null where step.operation = :operation")
    void onDeleteOperation(@Param("operation") Operation operation);

    @Modifying
    @Query("update EmbeddedStep step set step.chain = null where step.chain = :callChain")
    void onDeleteCallChain(@Param("callChain") CallChain callChain);

    @Modifying
    @Query("update SituationStep step set step.situation = null where step.situation = :situation")
    void onDeleteSituation(@Param("situation") Situation situation);

    @Query("select step from IntegrationStep step where step.systemTemplate = :template")
    Iterable<Step> getIntegrationStepsBySystemTemplate(@Param("template") SystemTemplate template);

    @Query("select step from IntegrationStep step where step.operationTemplate = :template")
    Iterable<Step> getIntegrationStepsByOperationTemplate(@Param("template") OperationTemplate template);

    @Query("select step from IntegrationStep step where step.operation = :operation")
    Iterable<Step> getIntegrationStepsByOperation(@Param("operation") Operation operation);

    @Query("select step from IntegrationStep step where step.sender = :system")
    Iterable<Step> getIntegrationStepsBySender(@Param("system") System system);

    @Query("select step from IntegrationStep step where step.receiver = :system")
    Iterable<Step> getIntegrationStepsByReceiver(@Param("system") System system);

    @Query(value = "select id from mb_steps where situation_id = :situationId "
            + "union select step_id from mb_end_situations where situation_id = :situationId "
            + "union select step_id from mb_exceptional_situation where situation_id = :situationId",
            nativeQuery = true)
    List<BigInteger> getIdsSteps(@Param("situationId") BigInteger situationId);

    @Query(value = "SELECT id, name from mb_step_container "
            + "where id in (select id from mb_chain where project_id = :projectId) "
            + "order by name",
            nativeQuery = true)
    List<Object[]> findIdAndNameByProjectId(@Param("projectId") BigInteger projectId);
}
