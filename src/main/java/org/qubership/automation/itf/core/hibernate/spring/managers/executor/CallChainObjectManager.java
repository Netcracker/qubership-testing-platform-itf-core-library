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

package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.StorableInFolderObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.BvCaseContainingObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.LabeledObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByParameterAndProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.CallChainRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;

@Service
public class CallChainObjectManager extends AbstractObjectManager<CallChain, CallChain>
        implements LabeledObjectManager<CallChain>, BvCaseContainingObjectManager<CallChain>,
        SearchByParameterAndProjectIdManager<CallChain>, StorableInFolderObjectManager {

    private final StepRepository stepRepository;
    private final CallChainRepository callChainRepository;

    /**
     * Constructor.
     */
    @Autowired
    public CallChainObjectManager(CallChainRepository repository, StepRepository stepRepository,
                                  CallChainRepository callChainRepository) {
        super(CallChain.class, repository);
        this.stepRepository = stepRepository;
        this.callChainRepository = callChainRepository;
    }

    @Override
    public void protectedOnRemove(CallChain object) {
        stepRepository.onDeleteCallChain(object);
    }

    /**
     * Find usages of storable object.
     * @param storable - object,
     * @return Collection of UsageInfo objects.
     */
    @Override
    public Collection<UsageInfo> findUsages(Storable storable) {
        Collection<UsageInfo> result = Lists.newArrayListWithExpectedSize(50);
        addToUsages(result, "callChains", getCallChains((BigInteger) storable.getID()));
        return result;
    }

    private Iterable<CallChain> getCallChains(BigInteger id) {
        List<BigInteger> callChainsIds = callChainRepository.getIdsCallchains(id);
        List<CallChain> callChains = Lists.newArrayListWithCapacity(callChainsIds.size());
        if (!callChainsIds.isEmpty()) {
            for (BigInteger callChainId : callChainsIds) {
                CallChain callChain = callChainRepository.getOne(callChainId);
                callChains.add(callChain);
            }
        }
        return callChains;
    }

    @Override
    @Deprecated
    public Collection<? extends CallChain> getByLabel(String label) {
        return null;
    }

    @Override
    public Collection<CallChain> getByLabel(String label, BigInteger projectId) {
        Collection<BigInteger> ids =
                TxExecutor.executeUnchecked(() -> callChainRepository.getCallchainIdsByLabel(label, projectId),
                        TxExecutor.readOnlyTransaction());
        List<CallChain> callChainsByLabel = Lists.newArrayList();
        if (ids != null) {
            for (BigInteger id : ids) {
                callChainsByLabel.add(callChainRepository.getOne(id));
            }
        }
        return callChainsByLabel;
    }

    /**
     * Get callchains list by projectId (only id and name are retrieved).
     * @param projectId - id of project.
     * @return list of {id, name} pairs for all callchains found.
     */
    public List<IdNamePair> getSimpleListByProject(BigInteger projectId) {
        // TODO: check if enclosing into TxExecutor is really necessary here
        return TxExecutor.executeUnchecked(() -> callChainRepository.getSimpleListByProject(projectId),
                TxExecutor.readOnlyTransaction());
    }

    /**
     * Count usages of BV testcase.
     * @param bvCaseId - testcase id,
     * @return count of usages.
     */
    public int countBvCaseUsages(String bvCaseId) {
        // TODO: check if enclosing into TxExecutor is really necessary here
        return TxExecutor.executeUnchecked(() -> callChainRepository.countBvCaseUsages(bvCaseId),
                TxExecutor.readOnlyTransaction());
    }

    @Override
    public Set<String> getAllLabels(BigInteger projectId) {
        // TODO: check if enclosing into TxExecutor is really necessary here
        return TxExecutor.executeUnchecked(() -> callChainRepository.getAllLabels(projectId),
                TxExecutor.readOnlyTransaction());
    }

    @Override
    public List<CallChain> getByNameAndProjectId(String name, BigInteger projectId) {
        return callChainRepository.findByNameAndProjectId(name, projectId);
    }

    public List<CallChain> getByPieceOfNameAndProjectId(String name, BigInteger projectId) {
        return callChainRepository.findByPieceOfNameAndProjectId(name, projectId);
    }

    @Override
    public Collection<CallChain> getByProjectId(BigInteger projectId) {
        return callChainRepository.findByProjectId(projectId);
    }

    // TODO: this method and the query should be modified like SituationRepository#getSituationsWithBvLinks
    @Override
    public List<Object[]> getObjectsWithBvLinks(BigInteger projectId) {
        /*
        Collection<BigInteger> ids = TxExecutor.executeUnchecked(() -> callChainRepository
                .getCallChainsWithBvLinks(projectId), TxExecutor.readOnlyTransaction());
        if (ids == null || ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Storable> callChains = new ArrayList<>();
        for (BigInteger id : ids) {
            callChains.add(callChainRepository.getOne(id));
        }
         */
        return new ArrayList<>();
    }

    @Override
    public List<?> getReceiverSystemsFromCallChainSteps(Object chainId) {
        throw new NotImplementedException("Method getReceiverSystemsFromCallChainSteps is not implemented");
    }

    public List<Object[]> getAllIdsAndNamesByProjectId(BigInteger projectId) {
        return stepRepository.findIdAndNameByProjectId(toBigInt(projectId));
    }

    @Override
    public void afterDelete(Storable object) {
       afterDeleteFromFolder(object);
    }
}
