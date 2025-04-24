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

package org.qubership.automation.itf.core.util.descriptor;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.qubership.automation.itf.core.util.annotation.DefaultValue;
import org.qubership.automation.itf.core.util.annotation.Options;
import org.qubership.automation.itf.core.util.annotation.Parameter;
import org.qubership.automation.itf.core.util.helper.Reflection;
import org.qubership.automation.itf.core.util.provider.PropertyProvider;

import com.google.common.collect.Lists;

public class Extractor {

    private Extractor() {
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static List<PropertyDescriptor> extractProperties(PropertyProvider object) {
        Set<Field> fieldSet = getFields(object.getClass());
        appendWithParentFields(fieldSet, object.getClass().getSuperclass());
        List<PropertyDescriptor> properties = Lists.newArrayListWithCapacity(fieldSet.size());
        fieldSet.forEach(field -> {
            Parameter parameter = field.getAnnotation(Parameter.class);
            Options options = field.getAnnotation(Options.class);
            PropertyDescriptor descriptor = new PropertyDescriptor(parameter.shortName(), parameter.longName(),
                    field.getType().getName(), parameter.description(), parameter.optional(), parameter.forTemplate(),
                    parameter.fromServer(), parameter.forServer(), parameter.forTrigger(), options != null,
                    options != null ? options.value() : null, parameter.isDynamic(), parameter.isRedefined(),
                    parameter.loadTemplate(), parameter.userSettings(), parameter.order(),
                    parameter.fileDirectoryType(), parameter.uiCategory(), parameter.validatePattern()
            );
            DefaultValue defaultValue = field.getAnnotation(DefaultValue.class);
            if (defaultValue != null) {
                descriptor.setDefaultValue(defaultValue.value());
            }
            properties.add(descriptor);
        });
        return properties;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static List<String> extractMandatory(List<PropertyDescriptor> properties) {
        List<String> mandatoryProperties = Lists.newArrayListWithCapacity(properties.size());
        properties.forEach(prop -> {
            if (!prop.isOptional()) {
                mandatoryProperties.add(prop.getShortName());
            }
        });
        return mandatoryProperties;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static String checkMandatoryProperties(List<String> mandatoryProperties, Map<String, Object> properties) {
        StringBuilder error = new StringBuilder();
        if (!(mandatoryProperties.isEmpty() || properties.isEmpty())) {
            for (String propname : mandatoryProperties) {
                if (properties.containsKey(propname)) {
                    Object val = properties.get(propname);
                    if (val == null || val.toString().isEmpty()) {
                        error.append("Mandatory property '").append(propname).append("' value is empty. ");
                    }
                } else {
                    error.append("Mandatory property '").append(propname).append("' is missed. ");
                }
            }
        }
        return error.toString(); // "" in case of 'OK'. Otherwise contains cumulative error message
    }

    private static void appendWithParentFields(Set<Field> fieldSet, Class<?> superclass) {
        Class<?> parentClass = superclass;
        while (parentClass != null && PropertyProvider.class.isAssignableFrom(parentClass)) {
            Reflection.getFieldsAnnotatedBy(parentClass, Parameter.class).forEach(field -> {
                if (!fieldSet.contains(field)) {
                    fieldSet.add(field);
                }
            });
            parentClass = parentClass.getSuperclass();
        }
    }

    private static Set<Field> getFields(Class<?> clazz) {
        return Reflection.getFieldsAnnotatedBy(clazz, Parameter.class).parallelStream().collect(Collectors.toSet());
    }

}
