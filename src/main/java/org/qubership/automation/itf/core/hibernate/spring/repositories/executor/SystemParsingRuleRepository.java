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
import java.util.List;

import javax.persistence.QueryHint;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@JaversSpringDataAuditable
@Repository
public interface SystemParsingRuleRepository
        extends ParsingRuleRepository<System, SystemParsingRule> {

    @Override
    @Query(value = "select parsingRule from SystemParsingRule as parsingRule "
            + "inner join System as system on system = parsingRule.parent "
            + "where parsingRule.name = :name and system.ID = :parentId")
    List<SystemParsingRule> findByParentIDAndName(@Param("parentId") Object parentId, @Param("name") String name);

    @Override
    @Query(value = "select parsingRule.* from mb_parsing_rules parsingRule "
            + "where parsingRule.type = 'system' and parsingRule.parent_system_id = :parentId",
            nativeQuery = true)
    List<SystemParsingRule> findByParentID(@Param("parentId") Object parentId);

    @Override
    @Query(value = "select parsingRule from SystemParsingRule as parsingRule "
            + "inner join System as system on system = parsingRule.parent "
            + "where system.name = :name")
    List<SystemParsingRule> findByParentName(@Param("name") String name);

    @Query(value = "select parsingRule from SystemParsingRule as parsingRule "
            + "where id = :id")
    @QueryHints(value = {@QueryHint(name = HINT_CACHEABLE, value = "true"),
            @QueryHint(name = HINT_CACHE_REGION, value = "systemParsingRulesCache")})
    SystemParsingRule findByIdOnly(@Param("id") BigInteger id);

}
