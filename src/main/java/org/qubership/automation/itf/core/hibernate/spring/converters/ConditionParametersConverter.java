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

package org.qubership.automation.itf.core.hibernate.spring.converters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.AttributeConverter;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ConditionParametersConverter implements AttributeConverter<List<ConditionParameter>, String> {

    ObjectMapper objectMapper = new ObjectMapper()
            .configure(DeserializationFeature.READ_UNKNOWN_ENUM_VALUES_AS_NULL, true);

    @Override
    public String convertToDatabaseColumn(List<ConditionParameter> attribute) {
        String attributeJson = null;
        try {
            attributeJson = objectMapper.writeValueAsString(attribute);
        } catch (final JsonProcessingException e) {
            log.error("JSON writing error", e);
        }
        return attributeJson;
    }

    @Override
    public List<ConditionParameter> convertToEntityAttribute(String dbData) {
        if (StringUtils.isBlank(dbData)) {
            return new ArrayList<>();
        }
        try {
            return objectMapper.readValue(dbData, new TypeReference<List<ConditionParameter>>() {});
        } catch (final IOException e) {
            log.error("JSON reading error", e);
            // An empty list reads as "no conditions, applicable anyway"; a bare ConditionParameter never is.
            List<ConditionParameter> neverSatisfied = new ArrayList<>();
            neverSatisfied.add(new ConditionParameter());
            return neverSatisfied;
        }
    }
}
