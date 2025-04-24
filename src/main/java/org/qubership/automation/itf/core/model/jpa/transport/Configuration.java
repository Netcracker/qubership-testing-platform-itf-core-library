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

package org.qubership.automation.itf.core.model.jpa.transport;

import static org.qubership.automation.itf.core.util.constants.PropertyConstants.FILE_DIRECTORY_PROJECT_UUID_GROUP_NUMBER;

import java.beans.Transient;
import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.util.constants.PropertyConstants;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public abstract class Configuration extends AbstractStorable implements Map<String, String> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Configuration.class);

    private Map<String, String> configuration = new HashMap<>();
    private String typeName;

    public Configuration() {
        super();
    }

    public Configuration(String typeName) {
        super();
        this.typeName = typeName;
    }

    @Transient
    @Override
    public int size() {
        return configuration.size();
    }

    @Transient
    @Override
    public boolean isEmpty() {
        return configuration.isEmpty();
    }

    @Transient
    @Override
    public boolean containsKey(Object key) {
        return configuration.containsKey(key);
    }

    @Transient
    @Override
    public boolean containsValue(Object value) {
        return configuration.containsValue(value);
    }

    @Transient
    @Override
    public String get(Object key) {
        return configuration.get(key);
    }

    @Transient
    @Override
    public String put(String key, String value) {
        return configuration.put(key, value);
    }

    @Transient
    @Override
    public String remove(Object key) {
        return configuration.remove(key);
    }

    @Transient
    @Override
    public void putAll(Map<? extends String, ? extends String> m) {
        configuration.putAll(m);
    }

    @Transient
    @Override
    public void clear() {
        configuration.clear();
    }

    @Transient
    @Override
    public Set<String> keySet() {
        return configuration.keySet();
    }

    @Transient
    @Override
    public Collection<String> values() {
        return configuration.values();
    }

    @Transient
    @Override
    public Set<Entry<String, String>> entrySet() {
        return configuration.entrySet();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Map<String, String> getConfiguration() {
        return this.configuration;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setConfiguration(Map<String, String> configuration) {
        this.configuration = configuration;
    }

    @Transient
    public void fillConfiguration(Map<String, String> configuration) {
        StorableUtils.fillMap(getConfiguration(), configuration);
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        replaceProjectUuidInFilePathProperties(projectUuid);
    }

    private void replaceProjectUuidInFilePathProperties(UUID projectUuid) {
        replaceProjectUuidInFilePathPropertyByKey(projectUuid, PropertyConstants.Soap.WSDL_PATH);
        replaceProjectUuidInFilePathPropertyByKey(projectUuid, PropertyConstants.Soap.REQUEST_XSD_PATH);
        replaceProjectUuidInFilePathPropertyByKey(projectUuid, PropertyConstants.Soap.RESPONSE_XSD_PATH);
        replaceProjectUuidInFilePathPropertyByKey(
                projectUuid, PropertyConstants.DiameterTransportConstants.CONFIG_PATH
        );
    }

    private void replaceProjectUuidInFilePathPropertyByKey(UUID projectUuid, String key) {
        String value = get(key);
        if (StringUtils.isNotEmpty(value)) {
            Matcher matcher = PropertyConstants.FILE_DIRECTORY_PATTERN.matcher(value);
            if (matcher.find()) {
                put(key, new StringBuilder(value).replace(
                        matcher.start(FILE_DIRECTORY_PROJECT_UUID_GROUP_NUMBER),
                        matcher.end(FILE_DIRECTORY_PROJECT_UUID_GROUP_NUMBER),
                        projectUuid.toString()).toString());
            }
        }
    }
 }
