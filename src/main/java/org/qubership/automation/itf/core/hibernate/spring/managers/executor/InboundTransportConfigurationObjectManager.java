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
import java.util.Collection;

import org.apache.commons.lang3.NotImplementedException;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.EnvConfigurationManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.NativeManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.InboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InboundTransportConfigurationObjectManager extends AbstractObjectManager<InboundTransportConfiguration,
        InboundTransportConfiguration> implements NativeManager<InboundTransportConfiguration>,
        EnvConfigurationManager<InboundTransportConfiguration> {

    @Autowired
    public InboundTransportConfigurationObjectManager(InboundTransportConfigurationRepository repository) {
        super(InboundTransportConfiguration.class, repository);
    }

    @Override
    public Storable getChildByClass(InboundTransportConfiguration parent, Class childrenClass, Object... param) {
        if (childrenClass.getName().equals(TransportConfiguration.class.getName())) {
            Object id =
                    ((InboundTransportConfigurationRepository) repository).getIdByTransport(toBigInt(parent.getID()));
            return ((InboundTransportConfigurationRepository) repository).findFirstTransport(toBigInt(id));
        }
        throw new NotImplementedException("Not implemented for classes other than "
                + TransportConfiguration.class.getName());
    }

    @Override
    public Collection<? extends Storable> getChildrenByClass(InboundTransportConfiguration parent,
                                                             Class childrenClass, Object... param) {
        throw new NotImplementedException("");
    }

    @Override
    public InboundTransportConfiguration getByEcId(String ecId, Object... objects) {
        return TxExecutor.executeUnchecked(() -> ((InboundTransportConfigurationRepository) repository)
                .findByEcId(ecId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<InboundTransportConfiguration> getByEcProjectId(String ecProjectId) {
        return TxExecutor.executeUnchecked(() -> ((InboundTransportConfigurationRepository) repository)
                .getByEcProject(ecProjectId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<String> getEcProjectIds(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> ((InboundTransportConfigurationRepository) repository)
                        .getEcProjectIds(projectId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public void unbindByEcProject(String ecProjectId) {
        TxExecutor.executeUnchecked(() -> ((InboundTransportConfigurationRepository) repository)
                .unbindByEcProject(ecProjectId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public InboundTransportConfiguration findByEcLabel(String ecLabel, BigInteger projectId) {
        return null;
    }

    public Collection<InboundTransportConfiguration> getConfigurationsByTransportId(BigInteger transportId) {
        return ((InboundTransportConfigurationRepository) repository)
                .getConfigurationsByTransportId(transportId);
    }
}
