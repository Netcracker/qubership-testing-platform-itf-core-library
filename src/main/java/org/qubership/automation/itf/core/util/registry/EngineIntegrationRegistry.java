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

package org.qubership.automation.itf.core.util.registry;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.itf.core.util.annotation.UserName;
import org.qubership.automation.itf.core.util.descriptor.Extractor;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.engine.EngineIntegration;
import org.qubership.automation.itf.core.util.helper.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

public class EngineIntegrationRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(EngineIntegrationRegistry.class);

    private static EngineIntegrationRegistry INSTANCE = new EngineIntegrationRegistry();

    private Map<String, EngineIntegration> engines = Maps.newHashMapWithExpectedSize(50);
    private Map<String, List<PropertyDescriptor>> properties = Maps.newHashMapWithExpectedSize(50);

    private EngineIntegrationRegistry() {
        init();
    }

    public static EngineIntegrationRegistry getInstance() {
        return INSTANCE;
    }

    /**
     * Registry init.
     */
    public void init() {
        Set<Class<? extends EngineIntegration>> classes =
                Reflection.getReflections().getSubTypesOf(EngineIntegration.class);
        for (Class<?> clazz : classes) {
            if (!clazz.isInterface()) {
                try {
                    EngineIntegration integration = (EngineIntegration) clazz.newInstance();
                    UserName userName = clazz.getAnnotation(UserName.class);
                    String name = userName != null ? userName.value() : clazz.getSimpleName();
                    engines.put(name, integration);
                    List<PropertyDescriptor> descriptors = Extractor.extractProperties(integration);
                    properties.put(name, descriptors);
                    LOGGER.info("Class {} is loaded successfully", clazz.getSimpleName());
                } catch (InstantiationException | IllegalAccessException ex) {
                    LOGGER.error("Error in initializer of {}", clazz.getName(), ex);
                }
            }
        }
    }

    public EngineIntegration find(String engineName) {
        return engines.get(engineName);
    }

    public List<PropertyDescriptor> getProperties(String engineName) {
        return properties.get(engineName);
    }

    public Collection<String> getAvailableIntegrations() {
        return Sets.newHashSet(engines.keySet());
    }

    public Map<String, EngineIntegration> getAvailableEngines() {
        return engines;
    }

}
