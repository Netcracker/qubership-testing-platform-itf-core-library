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
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.stub.parser.SimpleParsingRule;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class StubEndpointConfig implements Serializable {
    private static final long serialVersionUID = 20250303L;

    private String configuredEndpoint;
    private String operationDefinitionScript;
    @JsonIgnore
    private String operationDefinitionKey;
    private ResponseDescription defaultResponse;
    private List<ResponseDescription> conditionalResponses;
    private List<SimpleParsingRule> parsingRules;
    private Map<String,List<SimpleParsingRule>> operationParsingRules = new HashMap<>();
    private Boolean skipReporting;
    private Boolean disabled;

    @Getter
    public enum TransportTypes {
        REST("org.qubership.automation.itf.transport.rest.inbound.RESTInboundTransport"),
        SOAP("org.qubership.automation.itf.transport.soap.http.inbound.SOAPOverHTTPInboundTransport");

        private final String fullTransportType;

        TransportTypes(String fullTransportType) {
            this.fullTransportType = fullTransportType;
        }
    }

    /**
     * Constructor.
     * @param fastSystem - FastSystem object,
     * @param fastOperation - FastOperation object,
     * @param storableOperation - Operation object,
     * @param transportType - type of transport,
     * @param endPoint - endpoint property value.
     */
    public StubEndpointConfig(FastConfigurationRequest.FastSystem fastSystem,
                              FastConfigurationRequest.FastOperation fastOperation,
                              Operation storableOperation,
                              TransportTypes transportType,
                              String endPoint) {
        this.skipReporting = false;
        this.disabled = false;
        this.configuredEndpoint = endPoint;
        this.operationDefinitionScript = fastSystem.getStorableSystem().getOperationKeyDefinition();
        this.parsingRules = new LinkedList<>();
        this.conditionalResponses = new LinkedList<>();
        Instant start = Instant.now();
        fastSystem.getStorableSystem().getSystemParsingRules().forEach(systemParsingRule ->
                this.parsingRules.add(new SimpleParsingRule(systemParsingRule)));
        addResponses(fastOperation, storableOperation, transportType);
        log.debug("StubEndpointConfig processed in {} millis", ChronoUnit.MILLIS.between(start, Instant.now()));
    }

    protected void addResponses(FastConfigurationRequest.FastOperation fastOperation,
                                Operation storableOperation,
                                TransportTypes transportType) {
        List<SimpleParsingRule> parsingRulesFromOperation = new ArrayList<>();
        storableOperation.getOperationParsingRules().forEach(operationParsingRule ->
                parsingRulesFromOperation.add(new SimpleParsingRule(operationParsingRule)));
        if (!parsingRulesFromOperation.isEmpty()) {
            this.operationParsingRules.put(storableOperation.getOperationDefinitionKey(), parsingRulesFromOperation);
        }
        List<ResponseDescription> orderedConditionalResponses = new LinkedList<>();
        for (FastConfigurationRequest.FastSituation fastSituation : fastOperation.getSituations()) {
            Situation storableSituation = CoreObjectManager.getInstance().getManager(Situation.class)
                    .getById(fastSituation.getId());
            if (storableSituation != null) {
                if (storableSituation.getSteps().isEmpty()) {
                    log.warn("Situation id={} is skipped because doesn't have steps", storableSituation.getID());
                    continue;
                }
                ResponseDescription responseDescription = new ResponseDescription(storableSituation, transportType);
                orderedConditionalResponses.add(responseDescription);
            }
        }
        orderedConditionalResponses.sort(Comparator.comparing(ResponseDescription::getPriority));
        this.conditionalResponses.addAll(orderedConditionalResponses);
    }
}
