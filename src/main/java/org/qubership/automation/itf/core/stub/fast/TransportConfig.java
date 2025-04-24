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

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TransportConfig implements Serializable {
    private static final long serialVersionUID = 20250303L;
    private static final Logger log = LoggerFactory.getLogger(TransportConfig.class);

    private StubEndpointConfig.TransportTypes transportType;
    private List<StubEndpointConfig> endpoints;

    /**
     * Constructor.
     * @param fastTransportConfig - FastTransportConfig object.
     */
    public TransportConfig(FastConfigurationRequest.FastTransportConfig fastTransportConfig) {
        this.transportType = fastTransportConfig.getTransportType();
        this.endpoints = new ArrayList<>();
        Set<String> processedEndpoints = new HashSet<>();
        Instant start = Instant.now();
        for (FastConfigurationRequest.FastSystem fastSystem : fastTransportConfig.getSystems()) {
            Map<String, StubEndpointConfig> operationsEndpointMap = new HashMap<>();
            fastSystem.setStorableSystem(CoreObjectManager.getInstance()
                    .getManager(System.class).getById(fastSystem.getId()));
            for (FastConfigurationRequest.FastOperation fastOperation : fastSystem.getOperations()) {
                Operation storableOperation = CoreObjectManager.getInstance()
                        .getManager(Operation.class).getById(fastOperation.getId());
                if (storableOperation != null) {
                    String endPoint = storableOperation.getTransport().getConfiguration().get("endpoint");
                    if (processedEndpoints.contains(endPoint)) {
                        endPoint = "[FAST_STUB_DUPLICATED][".concat(fastSystem.getId()).concat("]").concat(endPoint);
                    }
                    if (operationsEndpointMap.containsKey(endPoint)) {
                        operationsEndpointMap.get(endPoint).addResponses(fastOperation, storableOperation,
                                this.transportType);
                    } else {
                        operationsEndpointMap.put(endPoint, new StubEndpointConfig(
                                fastSystem, fastOperation, storableOperation, this.transportType, endPoint));
                    }
                } else {
                    log.warn("Storable Operation id={} is not found. "
                            + "Operation will not be added to FastStub configuration.", fastOperation.getId());
                }
            }
            processedEndpoints.addAll(operationsEndpointMap.keySet());
            endpoints.addAll(operationsEndpointMap.values());
            fastSystem.setStorableSystem(null);
        }
        log.debug("Configuration processed in {} millis", ChronoUnit.MILLIS.between(start, Instant.now()));
    }
}
