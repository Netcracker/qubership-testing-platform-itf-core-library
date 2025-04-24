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
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface OperationRepository extends StorableRepository<Operation>, QuerydslPredicateExecutor<Operation> {

    @Modifying
    @Query("update Operation o set o.transport = null where o.transport = :configuration")
    void onDeleteTransport(@Param("configuration") TransportConfiguration configuration);

    @Query(value = "select operation from Operation operation "
            + "inner join System as system on system = operation.parent "
            + "where lower(system.name) like concat('%', lower(:name), '%') "
            + "and system.projectId = :projectId")
    Collection<Operation> findByParentNameAndProject(@Param("name") String name,
                                                     @Param("projectId") BigInteger projectId);

    @Query(value = "select operation from Operation operation "
            + "where lower(operation.name) like concat('%', lower(:name), '%') "
            + "and operation.projectId = :projectId")
    Collection<Operation> findByPieceOfNameAndProject(@Param("name") String name,
                                                      @Param("projectId") BigInteger projectId);

    @Query(value = "select operation from Operation operation "
            + "where lower(operation.name) = lower(:name)"
            + "and operation.projectId = :projectId")
    List<Operation> findByNameAndProjectId(@Param("name") String name, @Param("projectId") BigInteger projectId);

    @Query(value = "select id from mb_triggers where set_parent_id in "
            + "(select id from mb_situation where parent_id = :operationId)",
            nativeQuery = true)
    List<BigInteger> getSituationEventTriggersByOperationId(@Param("operationId") BigInteger operationId);

}
