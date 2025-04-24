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

package org.qubership.automation.itf.core.stub.fast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public enum FastResponseConfigsHolder {
    INSTANCE;
    private static final ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private final ConcurrentHashMap<String, StubEndpointConfig> cfg = new ConcurrentHashMap<>();

    public StubEndpointConfig getConfig(String projectUuid, String transportType, String configuredEndpoint) {
        return cfg.get(projectUuid + "/" + transportType + "/" + configuredEndpoint);
    }

    public void putConfig(String projectUuid, String transportType, StubEndpointConfig config) {
        cfg.put(projectUuid + "/" + transportType + "/" + config.getConfiguredEndpoint(), config);
    }

    public void resetConfigByKey(String key) {
        cfg.entrySet().removeIf(entry -> entry.getKey().equals(key));
    }

    public void resetConfigForProject(String projectUuid) {
        cfg.entrySet().removeIf(entry -> entry.getKey().startsWith(projectUuid));
    }

    /**
     * Load project configuration for fast-stubs, for project Uuid, from savedFile.
     *
     * @param uuidFromInfo - project Uuid,
     * @param savedFile - File information.
     */
    public void loadFromFile(String uuidFromInfo, File savedFile) {
        try {
            FastResponseConfig fastResponseConfig = objectMapper.readValue(savedFile, FastResponseConfig.class);
            if (uuidFromInfo.equals(fastResponseConfig.getProjectUuid())) {
                // resetConfigForProject(fastResponseConfig.getProjectUuid());
                for (TransportConfig transportConfig: fastResponseConfig.getTransportConfigs()) {
                    for (StubEndpointConfig config: transportConfig.getEndpoints()) {
                        putConfig(fastResponseConfig.getProjectUuid(),
                                transportConfig.getTransportType().name().toUpperCase(), config);
                    }
                }
                log.info("Config is loaded for {} project from file {}", fastResponseConfig.getProjectUuid(),
                        savedFile.getName());
            } else {
                log.warn("Project UUID mismatch: {} vs {} (in file); config is not loaded!",
                        uuidFromInfo, fastResponseConfig.getProjectUuid());
            }
        } catch (IOException ex) {
            log.error("Error while post-processing of the file {} [path: {}]", savedFile.getName(),
                    savedFile.getPath(), ex);
        }
    }

    /**
     * Get project endpoints list.
     *
     * @param projectUuid Project UUID parameter.
     * @return List of endpoints.
     */
    public List<FastStubsTreeView> getEndpoints(UUID projectUuid) {
        List<FastStubsTreeView> endpoints = new ArrayList<>();

        Pattern restPattern = Pattern.compile(
                String.format("%s\\/%s\\/.*", projectUuid, StubEndpointConfig.TransportTypes.REST));
        Pattern soapPattern = Pattern.compile(
                String.format("%s\\/%s\\/.*", projectUuid, StubEndpointConfig.TransportTypes.SOAP));
        Matcher restMatcher;
        Matcher soapMatcher;
        for (String key : cfg.keySet()) {
            restMatcher = restPattern.matcher(key);
            if (restMatcher.find()) {
                endpoints.add(createFastStubsTreeView(cfg.get(key), StubEndpointConfig.TransportTypes.REST));
                continue;
            }
            soapMatcher = soapPattern.matcher(key);
            if (soapMatcher.find()) {
                endpoints.add(createFastStubsTreeView(cfg.get(key), StubEndpointConfig.TransportTypes.SOAP));
            }
        }
        return endpoints;
    }

    private FastStubsTreeView createFastStubsTreeView(StubEndpointConfig stubEndpointConfig,
                                                      StubEndpointConfig.TransportTypes transportType) {
        return new FastStubsTreeView(stubEndpointConfig.getConfiguredEndpoint(), transportType);
    }
}
