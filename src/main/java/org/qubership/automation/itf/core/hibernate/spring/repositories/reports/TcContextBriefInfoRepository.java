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

package org.qubership.automation.itf.core.hibernate.spring.repositories.reports;

import java.math.BigInteger;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.RootRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.SearchRepository;
import org.qubership.automation.itf.core.model.jpa.context.TcContextBriefInfo;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TcContextBriefInfoRepository extends RootRepository<TcContextBriefInfo>,
        QuerydslPredicateExecutor<TcContextBriefInfo>, SearchRepository<TcContextBriefInfo> {

    @Modifying
    @Query(value = "delete from mb_tccontext where mb_tccontext.id= :contextId and part_num = :partNum",
            nativeQuery = true)
    void deleteFromUIWithPartNum(@Param("contextId") BigInteger contextId, @Param("partNum") Integer partNum);

    @Modifying
    @Query(value = "delete from mb_tccontext where mb_tccontext.id= :contextId", nativeQuery = true)
    void deleteFromUIWithoutPartNum(@Param("contextId") BigInteger contextId);

    TcContextBriefInfo findByIDAndPartNum(Object id, Integer partNum);

    @Query(value = "select current_partition_number() as current_part_num", nativeQuery = true)
    int getCurrentPartitionNumber();
}

