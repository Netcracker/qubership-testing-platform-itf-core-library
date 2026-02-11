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

import static org.qubership.automation.itf.core.util.helper.Reflection.toStringMap;

import java.io.Serializable;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.system.stub.OperationEventTrigger;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.template.OutboundTemplateTransportConfiguration;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Getter
@Setter
@NoArgsConstructor
public class ResponseDescription implements Serializable {
    private static final long serialVersionUID = 20250303L;

    private String name;
    private String id;
    private String body;
    private String responseCode;
    private Map<String, Object> headers = new HashMap<>();
    private FastResponseCondition responseCondition;
    private Boolean skipReporting;
    private Boolean disabled = false;
    private Integer priority = 0;

    /**
     * Constructor.
     * @param storableSituation - situation object,
     * @param transportType - type of transport.
     */
    public ResponseDescription(Situation storableSituation, StubEndpointConfig.TransportTypes transportType) {
        this.name = String.format("%s__%s", storableSituation.getID(), storableSituation.getName());
        this.id = storableSituation.getID().toString();
        Instant start = Instant.now();
        fillBodyAndHeaders(storableSituation, transportType);
        log.debug("ResponseDescription.fillBodyAndHeaders processed in {} millis",
                ChronoUnit.MILLIS.between(start, Instant.now()));
        start = Instant.now();
        this.responseCondition = new FastResponseCondition(storableSituation);
        fillPriorityAndDisabled(storableSituation);
        this.skipReporting = false;
        log.debug("ResponseDescription.responseCondition processed in {} millis",
                ChronoUnit.MILLIS.between(start, Instant.now()));
    }

    private void fillBodyAndHeaders(Situation storableSituation, StubEndpointConfig.TransportTypes transportType) {
        IntegrationStep step = (IntegrationStep) storableSituation.getSteps().get(0);
        if (step != null) {
            Template<?> template = step.getOperationTemplate();
            if (template == null) {
                template = step.getSystemTemplate();
            }
            if (template != null) {
                this.body = template.getText();
                if (!StringUtils.isEmpty(storableSituation.getPreScript())) {
                    this.body = storableSituation.getPreScript().concat("\n").concat(this.body);
                }
                for (OutboundTemplateTransportConfiguration transportConfig : template.getTransportProperties()) {
                    if (transportType.getFullTransportType().equals(transportConfig.getTypeName())) {
                        this.responseCode = transportConfig.get("responseCode");
                        fillHeaders(transportConfig.get("headers"));
                    }
                }
            }
        } else {
            log.warn("Step is not found for situation id={}", storableSituation.getID());
        }
    }

    private void fillHeaders(String headersFromConfiguration) {
        Map<String, Object> map = toStringMap(headersFromConfiguration);
        if (map != null) {
            this.headers.putAll(map);
        }
    }

    private void fillPriorityAndDisabled(Situation storableSituation) {
        Optional<OperationEventTrigger> trigger =  storableSituation.getOperationEventTriggers().stream().findFirst();
        if (trigger.isPresent()) {
            this.priority = trigger.get().getPriority();
            this.disabled = !trigger.get().getState().isOn();
        }
    }
}
