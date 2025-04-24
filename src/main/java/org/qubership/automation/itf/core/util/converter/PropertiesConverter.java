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

package org.qubership.automation.itf.core.util.converter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.model.transport.ConnectionProperties;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.engine.TemplateEngineFactory;
import org.qubership.automation.itf.core.util.exception.TransportException;
import org.qubership.automation.itf.core.util.transport.manager.TransportRegistryManager;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

public class PropertiesConverter {

    @SafeVarargs
    public static ConnectionProperties convert(String typeName, Map<String, String>... configurations)
            throws TransportException {
        return convert(null, typeName,
                Arrays.stream(configurations).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    @SafeVarargs
    public static ConnectionProperties convert(InstanceContext instanceContext,
                                               String typeName,
                                               Map<String, String>... configurations)
            throws TransportException {
        return convert(instanceContext, typeName,
                Arrays.stream(configurations).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    private static ConnectionProperties convert(InstanceContext instanceContext, String typeName,
                                                Collection<? extends Map<String, String>> configurations)
            throws  TransportException {
        Map<String, String> merged = Maps.newHashMap();
        for (Map<String, String> configuration : configurations) {
            for (Map.Entry<String, String> entry : configuration.entrySet()) {
                String value = entry.getValue();
                if (StringUtils.isNotBlank(value)) {
                    merged.put(entry.getKey(), value);
                }
            }
        }
        return convert(instanceContext, typeName, merged);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static ConnectionProperties convert(InstanceContext instanceContext,
                                               String typeName,
                                               Map<String, String> configuration)
            throws TransportException {
        Map<String, PropertyDescriptor> properties = TransportRegistryManager.getInstance().getProperties(typeName);
        if (properties == null) {
            throw new IllegalStateException(String.format("Transport %s did not export any properties. "
                    + "Configuration is %s", typeName, configuration));
        }
        ConnectionProperties toReturn = new ConnectionProperties();
        for (Map.Entry<String, PropertyDescriptor> entry : properties.entrySet()) {
            String stringValue = configuration.get(entry.getKey());
            if (!Strings.isNullOrEmpty(stringValue)) {
                PropertyDescriptor descriptor = entry.getValue();
                Object convert = descriptor.convert(descriptor.isMap()
                        ? preProcess(stringValue, descriptor, instanceContext) : stringValue);
                if (convert != null) {
                    toReturn.put(entry.getKey(), convert);
                }
            }
        }
        return toReturn;
    }

    private static String preProcess(String stringValue,
                                     PropertyDescriptor descriptor,
                                     InstanceContext instanceContext) {
        if (descriptor.isMap() && (stringValue.startsWith("$") || stringValue.startsWith("#"))) {
            return TemplateEngineFactory.get().process(
                    (Storable) null,
                    stringValue,
                    (instanceContext == null) ? new JsonContext() : instanceContext);
        } else {
            return stringValue;
        }
    }
}
