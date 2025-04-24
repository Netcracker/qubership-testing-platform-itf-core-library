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

package org.qubership.automation.itf.core.hibernate.spring.managers.reports;

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.ContextManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.reports.TcContextBriefInfoRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.reports.TcContextRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TcContextObjectManager extends AbstractObjectManager<TcContext, TcContext> implements ContextManager,
        SearchByProjectIdManager<TcContext> {

    private final TcContextRepository tcContextRepository;
    private final TcContextBriefInfoRepository tcContextBriefInfoRepository;

    /**
     * TODO: Add JavaDoc.
     */
    @Autowired
    public TcContextObjectManager(TcContextRepository repository,
                                  TcContextBriefInfoRepository tcContextBriefInfoRepository) {
        super(TcContext.class, repository);
        this.tcContextRepository = repository;
        this.tcContextBriefInfoRepository = tcContextBriefInfoRepository;
    }

    @Override
    public void store(final Storable storable) {
        TxExecutor.executeUnchecked(() -> {
            TcContext tcContext = tcContextRepository.save((TcContext) storable);
            storable.setVersion(tcContext.getVersion());
            storable.setID(tcContext.getID());
            return tcContext;
        }, TxExecutor.nestedWritableTransaction());
    }

    @Override
    public TcContext create(Storable parent, String type, Map parameters) {
        return create();
    }

    @Override
    public TcContext create() {
        return TxExecutor.executeUnchecked((Callable<TcContext>) TcContextObjectManager.super::create,
                TxExecutor.nestedWritableTransaction());
    }

    @Nonnull
    @Override
    public Storable copy(Storable dst, Storable obj, String projectId, String sessionId) {
        throw new UnsupportedOperationException("Cannot copy TcContext");
    }

    @Override
    public void move(Storable dst, Storable obj, String sessionId) {
        throw new UnsupportedOperationException("Cannot move TcContext");
    }

    @Override
    public String acceptsTo(Storable storable) {
        throw new UnsupportedOperationException("TcContext cannot accept copy/move of other objects");
    }

    @Override
    public void protectedOnRemove(TcContext object) {
    }

    @Transactional
    public int updateStatusContextWithStatusInProgress() {
        return tcContextRepository.updateStatusContextWithStatusInProgress();
    }

    @Override
    public String clearMonitoringData(int clearHours) {
        return tcContextRepository.clearMonitoringData(clearHours);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void deleteById(String id, Integer partNum) {
        if (!StringUtils.isBlank(id)) {
            if (partNum == null) {
                tcContextBriefInfoRepository.deleteFromUIWithoutPartNum(toBigInt(id));
            } else {
                tcContextBriefInfoRepository.deleteFromUIWithPartNum(toBigInt(id), partNum);
            }
        }
    }

    @Override
    public Collection<TcContext> getByProjectId(BigInteger projectId) {
        return tcContextRepository.findByProjectId(projectId);
    }
}
