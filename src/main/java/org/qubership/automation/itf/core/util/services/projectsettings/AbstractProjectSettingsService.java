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

package org.qubership.automation.itf.core.util.services.projectsettings;

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;

import com.hazelcast.map.IMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractProjectSettingsService implements IProjectSettingsService {

    /**
     * Get project settings property value or default value from provided parameter.
     * Method gets value from Hazelcast NearCache "ATP_ITF_PROJECT_SETTINGS" if remote Hazelcast
     * service\NearCache is not available it will return default value from provided parameter.
     *
     * @param projectId    projectId
     * @param shortName    property short name
     * @param defaultValue default value
     * @return property value or provided default value.
     */
    public String get(Object projectId, String shortName, String defaultValue) {
        String value = get(toBigInt(projectId), shortName, getProjectSettingsCache());
        return StringUtils.isNotEmpty(value) ? value.trim() : defaultValue;
    }

    protected String get(BigInteger projectId, String shortName,
                         IMap<String, Map<String, String>> projectSettingsCache) {
        try {
            Map<String, String> projectSettings = getProjectSettings(projectSettingsCache, projectId.toString());
            if (Objects.nonNull(projectSettings)) {
                return projectSettings.get(shortName);
            }
            log.warn("Something went wrong with Hazelcast project settings cache - there is no project "
                    + "settings in cache for project {}", projectId);
            return null;
        } catch (Exception e) {
            log.warn("Error while getting project setting '{}' for project: {}", shortName, projectId);
            return null;
        }
    }

    /**
     * Get project settings property as INTEGER value or default value from provided parameter.
     * Method get value from Hazelcast NearCache "ATP_ITF_PROJECT_SETTINGS" if remote Hazelcast
     * service\NearCache is not available or key\map is not found it will return provided default value.
     *
     * @param projectId    - projectId
     * @param shortName    - property short name
     * @param defaultValue - default value
     * @return project setting value as INTEGER or null if short name is not found in cache\db or got some exception.
     */
    public Integer getInt(Object projectId, String shortName, int defaultValue) {
        IMap<String, Map<String, String>> projectSettingsCache = getProjectSettingsCache();
        String value = get(toBigInt(projectId), shortName, projectSettingsCache);
        return StringUtils.isNotEmpty(value) ? Integer.parseInt(value) : defaultValue;
    }

    /**
     * Get project settings with given name prefix. It will return empty Map if not found anything by prefix or some
     * exception is happened (Hazelcast is not available for example).
     *
     * @param projectId   - project id
     * @param prefix      - prefix for search.
     * @param truncPrefix - truncPrefix.
     * @return project settings Map found by prefix or empty map.
     */
    public Map<String, String> getByPrefix(Object projectId, String prefix, boolean truncPrefix) {
        Map<String, String> projectSettings = getAll(toBigInt(projectId));
        if (projectSettings.isEmpty()) {
            return projectSettings;
        }
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, String> property : projectSettings.entrySet()) {
            String key = property.getKey();
            if (StringUtils.startsWith(key, prefix)) {
                result.put(truncPrefix ? key.substring(prefix.length() + 1) : key, property.getValue());
            }
        }
        return result;
    }

    /**
     * Get all project settings by projectId.
     * This method will try to get data from Hazelcast NearCache. If remote Hazelcast
     * service\NearCache is not available or project settings is not found or some exception is happened - it will
     * return empty map.
     *
     * @param projectId internal ITF project id.
     * @return all project settings for project or empty map.
     */
    public Map<String, String> getAll(Object projectId) {
        IMap<String, Map<String, String>> projectsSettingsCache = getProjectSettingsCache();
        Map<String, String> projectSettings = getProjectSettings(projectsSettingsCache, projectId.toString());
        if (Objects.nonNull(projectSettings)) {
            return projectSettings;
        }
        log.warn("Can't get all project settings from cache for project '{}' ", projectId);
        return Collections.emptyMap();
    }

    protected abstract IMap<String, Map<String, String>> getProjectSettingsCache();

    protected Map<String, String> getProjectSettings(IMap<String, Map<String, String>> projectSettingsCache,
                                                     String projectId) {
        try {
            return projectSettingsCache.get(projectId);
        } catch (Exception e) {
            log.error("Can't get ProjectSettingsMap from Hazelcast for project {}", projectId, e);
            return null;
        }
    }
}

