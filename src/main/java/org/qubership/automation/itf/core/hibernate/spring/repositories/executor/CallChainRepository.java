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
import java.util.Set;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.SearchRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@JaversSpringDataAuditable
@Repository
public interface CallChainRepository extends SearchRepository<CallChain>, QuerydslPredicateExecutor<Operation>,
        StorableRepository<CallChain> {
    @Query(value = "select new org.qubership.automation.itf.core.model.IdNamePair(chain.id, chain.name) "
            + "from CallChain chain "
            + "where chain.projectId = :projectId")
    List<IdNamePair> getSimpleListByProject(@Param("projectId") BigInteger projectId);

    @Query(value = "select ch.id from mb_chain ch "
            + "inner join mb_chain_labels lbl on ch.id = lbl.id "
            + "where ch.project_id = :projectId and lbl.labels = :label",
            nativeQuery = true)
    Collection<BigInteger> getCallchainIdsByLabel(@Param("label") String label,
                                                  @Param("projectId") BigInteger projectId);

    @Query(value = "select distinct ch_labels.labels "
            + "from mb_chain as ch "
            + "inner join mb_chain_labels as ch_labels on ch_labels.id = ch.id "
            + "where project_id = :projectId "
            + "union "
            + "select distinct mfl.labels as labels "
            + "from mb_folders as mf "
            + "inner join mb_folders_labels as mfl on mfl.id =mf.id "
            + "where mf.\"type\" = 'chains' and mf.project_id = :projectId",
           nativeQuery = true)
    Set<String> getAllLabels(@Param("projectId") BigInteger projectId);

    @Query(value = "select count(*) from mb_bv_cases bvc where bvc.bv_tcid = :bvCaseId", nativeQuery = true)
    int countBvCaseUsages(@Param("bvCaseId") String bvCaseId);

    @Query(value = "select distinct parent_id from mb_steps where chain_id = :chainId", nativeQuery = true)
    List<BigInteger> getIdsCallchains(@Param("chainId") BigInteger chainId);

    @Query(value = "select callChain from CallChain callChain "
            + "where lower(callChain.name) like concat('%', lower(:name), '%') "
            + "and callChain.projectId = :projectId")
    List<CallChain> findByPieceOfNameAndProjectId(@Param("name") String name, @Param("projectId") BigInteger projectId);

    List<CallChain> findByNameAndProjectId(@Param("name") String name, @Param("projectId") BigInteger projectId);

    @Query(value = "select distinct callchain_id from mb_bv_cases "
            + "inner join mb_chain ch on callchain_id=ch.id "
            + "where ch.project_id = :projectId", nativeQuery = true)
    Collection<BigInteger> getCallChainsWithBvLinks(@Param("projectId") BigInteger projectId);
}
