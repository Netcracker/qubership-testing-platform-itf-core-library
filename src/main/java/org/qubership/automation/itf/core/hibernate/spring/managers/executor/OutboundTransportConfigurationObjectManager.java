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

import java.math.BigInteger;
import java.util.Collection;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.EnvConfigurationManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OutboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.model.jpa.environment.OutboundTransportConfiguration;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class OutboundTransportConfigurationObjectManager
        extends AbstractObjectManager<OutboundTransportConfiguration, OutboundTransportConfiguration>
        implements EnvConfigurationManager<OutboundTransportConfiguration> {
    @Autowired
    public OutboundTransportConfigurationObjectManager(OutboundTransportConfigurationRepository repository) {
        super(OutboundTransportConfiguration.class, repository);
    }

    @Override
    protected void protectedOnRemove(OutboundTransportConfiguration object) {
    }

    @Override
    public OutboundTransportConfiguration getByEcId(String ecId, Object... objects) {
        return TxExecutor.executeUnchecked(() -> ((OutboundTransportConfigurationRepository) repository)
                .findByEcId(ecId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<OutboundTransportConfiguration> getByEcProjectId(String ecProjectId) {
        return TxExecutor.executeUnchecked(() -> ((OutboundTransportConfigurationRepository) repository)
                .getByEcProject(ecProjectId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public Collection<String> getEcProjectIds(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> ((OutboundTransportConfigurationRepository) repository)
                        .getEcProjectIds(projectId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public void unbindByEcProject(String ecProjectId) {
        TxExecutor.executeUnchecked(() -> ((OutboundTransportConfigurationRepository) repository)
                .unbindByEcProject(ecProjectId), TxExecutor.defaultWritableTransaction());
    }

    @Override
    public OutboundTransportConfiguration findByEcLabel(String ecLabel, BigInteger projectId) {
        return null;
    }
}
