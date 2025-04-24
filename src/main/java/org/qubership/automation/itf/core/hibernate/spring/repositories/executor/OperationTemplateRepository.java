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
import java.util.List;

import javax.persistence.QueryHint;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.jpa.message.template.OperationTemplate;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface OperationTemplateRepository
        extends TemplateRepository<Operation, OperationTemplate> {

    @Override
    @Query(value = "select operationTemplate from OperationTemplate as operationTemplate "
            + "inner join Operation as operation on operation = operationTemplate.parent "
            + "where operationTemplate.name = :name and operation.ID = :parentId")
    List<OperationTemplate> findByParentIDAndName(@Param("parentId") Object parentId, @Param("name") String name);

    @Override
    @Query(value = "select template.* from mb_templates template "
            + "where template.type = 'operation' and template.parent_operation_id = :parentId",
            nativeQuery = true)
    List<OperationTemplate> findByParentID(@Param("parentId") Object parentId);

    @Override
    @Query(value = "select operationTemplate from OperationTemplate as operationTemplate "
            + "inner join Operation as operation on operation = operationTemplate.parent "
            + "where operation.name = :name")
    List<OperationTemplate> findByParentName(@Param("name") String name);

    @Query(value = "select template from OperationTemplate template "
            + "inner join Operation as operation on operation = template.parent "
            + "where lower(operation.name) like concat('%', lower(:name), '%') "
            + "and operation.projectId = :projectId")
    Collection<OperationTemplate> findByParentNameAndProject(@Param("name") String name,
                                                             @Param("projectId") BigInteger projectId);

    @Query(value = "select t from OperationTemplate as t "
            + "where lower(t.name) like concat('%', lower(:name), '%') "
            + "and t.projectId = :projectId")
    Collection<OperationTemplate> findByPieceOfNameAndProject(@Param("name") String name,
                                                              @Param("projectId") BigInteger projectId);

    @Query(value = "select new org.qubership.automation.itf.core.model.IdNamePair(opTemplate.id, opTemplate.name) "
            + "from OperationTemplate opTemplate "
            + "where lower(opTemplate.name) like concat('%', lower(:name), '%') "
            + "and opTemplate.parent.id = :parentId")
    List<IdNamePair> findByPieceOfNameAndParentId(@Param("name") String name,
                                                  @Param("parentId") BigInteger parentId);

    @Query(value = "select t.id, t.name "
            + "from mb_templates t where t.project_id = :projectId", nativeQuery = true)
    List<Object[]> findIdAndNameByProjectId(@Param("projectId") BigInteger projectId);

    @Query(value = "select t from OperationTemplate  as t "
            + "where id = :id")
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
    @QueryHint(name = HINT_CACHE_REGION, value = "operationTemplateCache")})
    OperationTemplate findByIdOnly(@Param("id") BigInteger id);
}
