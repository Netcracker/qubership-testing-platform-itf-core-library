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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.ObjectCreationByTypeManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.IntegrationConfigRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.project.IntegrationConfig;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.util.config.ApplicationConfig;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.registry.EngineIntegrationRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
public class IntegrationConfigObjectManager
        extends AbstractObjectManager<IntegrationConfig, IntegrationConfig>
        implements ObjectCreationByTypeManager<IntegrationConfig> {

    @Autowired
    public IntegrationConfigObjectManager(IntegrationConfigRepository repository) {
        super(IntegrationConfig.class, repository);
    }

    @Override
    public IntegrationConfig create(Storable parent, String type) {
        IntegrationConfig result = new IntegrationConfig();
        result.setParent(parent);
        result.setName(type);
        result.setTypeName(type);
        repository.save(result);
        addPropertiesToConfig(result);
        return result;
    }

    @Override
    public IntegrationConfig create(Storable parent, String name, String type, Map parameters) {
        IntegrationConfig integrationConfig = new IntegrationConfig(parent, parameters);
        integrationConfig.setName(name);
        integrationConfig.setTypeName(type);
        return repository.save(integrationConfig);
    }

    private void addPropertiesToConfig(IntegrationConfig config) {
        for (PropertyDescriptor property : EngineIntegrationRegistry.getInstance().getProperties(config.getName())) {
            String propertyValue = ApplicationConfig.env.getProperty(property.getShortName(), "");
            if (StringUtils.isNotEmpty(propertyValue)) {
                config.put(property.getShortName(), propertyValue);
            }
        }
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only IntegrationConfig objects are here")
    @Override
    public void afterDelete(Storable object) {
        if (object.getParent() instanceof StubProject) {
            synchronized (object.getParent()) {
                ((StubProject) object.getParent()).getIntegrationConfs().remove((IntegrationConfig) object);
            }
        }
    }
}
