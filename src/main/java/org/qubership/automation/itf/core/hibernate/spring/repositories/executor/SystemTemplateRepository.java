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
import org.qubership.automation.itf.core.model.jpa.message.template.SystemTemplate;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
@JaversSpringDataAuditable
public interface SystemTemplateRepository
        extends TemplateRepository<System, SystemTemplate> {
    @Override
    @Query(value = "select systemTemplate from SystemTemplate as systemTemplate "
            + "inner join System as system on system = systemTemplate.parent "
            + "where systemTemplate.name = :name and system.ID = :parentId")
    List<SystemTemplate> findByParentIDAndName(@Param("parentId") Object parentId, @Param("name") String name);

    @Override
    @Query(value = "select template.* from mb_templates template "
            + "where template.type = 'system' and template.parent_system_id = :parentId",
            nativeQuery = true)
    List<SystemTemplate> findByParentID(@Param("parentId") Object parentId);

    @Query(value = "select "
            + " new org.qubership.automation.itf.core.model.IdNamePair"
            + "(systemTemplate.id, systemTemplate.name) "
            + "from SystemTemplate as systemTemplate "
            + "where systemTemplate.parent.id = :parentId")
    List<IdNamePair> findSimpleSystemTemplatesByParentId(@Param("parentId") BigInteger parentId);

    @Override
    @Query(value = "select systemTemplate from SystemTemplate as systemTemplate "
            + "inner join System as system on system = systemTemplate.parent "
            + "where system.name = :name")
    List<SystemTemplate> findByParentName(@Param("name") String name);

    @Query(value = "select t from SystemTemplate as t "
            + "where lower(t.name) like concat('%', lower(:name), '%') "
            + "and t.projectId = :projectId")
    Collection<SystemTemplate> findByPieceOfNameAndProject(@Param("name") String name,
                                                           @Param("projectId") BigInteger projectId);

    @Query(value = "select new org.qubership.automation.itf.core.model.IdNamePair(sysTemplate.id, sysTemplate.name) "
            + "from SystemTemplate as sysTemplate "
            + "where lower(sysTemplate.name) like concat('%', lower(:name), '%') "
            + "and sysTemplate.parent.id = :parentId")
    List<IdNamePair> findByPieceOfNameAndParentId(@Param("name") String name,
                                                  @Param("parentId") BigInteger parentId);

    @Query(value = "select template from SystemTemplate template "
            + "inner join System as system on system = template.parent "
            + "where lower(system.name) like concat('%', lower(:name), '%') "
            + "and system.projectId = :projectId")
    Collection<SystemTemplate> findByParentNameAndProject(@Param("name") String name,
                                                          @Param("projectId") BigInteger projectId);

    @Query(value = "select t from SystemTemplate  as t "
            + "where id = :id")
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "systemTemplateCache")})
    SystemTemplate findByIdOnly(@Param("id") BigInteger id);

}
