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
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.TriggerConfigurationRepository;
import org.qubership.automation.itf.core.model.jpa.environment.TriggerConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TriggerConfigurationObjectManager
        extends AbstractObjectManager<TriggerConfiguration, TriggerConfiguration> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TriggerConfigurationObjectManager.class);
    private final TriggerConfigurationRepository triggerConfigurationRepository;

    @Autowired
    public TriggerConfigurationObjectManager(TriggerConfigurationRepository repository,
                                             TriggerConfigurationRepository triggerConfigurationRepository) {
        super(TriggerConfiguration.class, repository);
        this.triggerConfigurationRepository = triggerConfigurationRepository;
    }

    @Transactional(readOnly = true)
    public BigInteger getProjectId(BigInteger triggerConfigurationId) {
        return triggerConfigurationRepository.getProjectId(triggerConfigurationId);
    }

    public Collection<TriggerConfiguration> getAllActiveTriggersByProjectId(BigInteger projectId) {
        return triggerConfigurationRepository.getAllActiveTriggersByProjectId(projectId);
    }

    public Collection<TriggerConfiguration> getAllTriggersByProjectId(BigInteger projectId) {
        return triggerConfigurationRepository.getAllTriggersByProjectId(projectId);
    }

    public Collection<TriggerConfiguration> getAllActiveAndErrorTriggersByProjectId(BigInteger projectId) {
        return triggerConfigurationRepository.getAllActiveAndErrorTriggersByProjectId(projectId);
    }
}
