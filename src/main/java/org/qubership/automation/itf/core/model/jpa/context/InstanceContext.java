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

package org.qubership.automation.itf.core.model.jpa.context;

import java.math.BigInteger;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Entity;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractInstance;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.transport.ConnectionProperties;
import org.qubership.automation.itf.core.util.annotation.JsonRef;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.google.common.base.Strings;

@Entity
@JsonFilter("reportWorkerFilter_InstanceContext")
public class InstanceContext extends JsonStorable {
    private static final long serialVersionUID = 20240812L;

    private BigInteger projectId;
    private UUID projectUuid;
    public static final String SESSION_ID = "sessionId";
    public static final String TRANSPORT = "_transport";
    public static final String CONNECTION_PROPERTIES = "_connection_properties";
    public static final String BROKER_MESSAGE_SELECTOR_VALUE = "brokerMessageSelectorValue";

    private AbstractInstance instance;

    public InstanceContext() {
        setStartTime(new Date());
    }

    public InstanceContext(AbstractInstance instance) {
        this.instance = instance;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static InstanceContext from(TcContext tc, SpContext sp) {
        InstanceContext context = new InstanceContext();
        context.put(TcContext.TC, tc);
        context.put(SpContext.SP, sp);
        return context;
    }

    public TcContext tc() {
        return get(TcContext.TC, TcContext.class);
    }

    public SpContext sp() {
        return get(SpContext.SP, SpContext.class);
    }

    public BigInteger getProjectId() {
        return projectId;
    }

    public void setProjectId(BigInteger projectId) {
        this.projectId = projectId;
    }

    public UUID getProjectUuid() {
        return projectUuid;
    }

    public void setProjectUuid(UUID projectUuid) {
        this.projectUuid = projectUuid;
    }

    @JsonRef
    public TcContext getTC() {
        return get(TcContext.TC, TcContext.class);
    }

    public SpContext getSP() {
        return get(SpContext.SP, SpContext.class);
    }

    public void setTC(TcContext tc) {
        put(TcContext.TC, tc);
    }

    public void setSP(SpContext sp) {
        put(SpContext.SP, sp);
    }

    public void nilsp() {
        remove(SpContext.SP);
    }

    public void setSessionId(Object sessionId) {
        put(SESSION_ID, sessionId);
    }

    public Object getSessionId() {
        return get(SESSION_ID);
    }

    public void setMessageBrokerSelectorValue(Object brokerSelectorValue) {
        put(BROKER_MESSAGE_SELECTOR_VALUE, brokerSelectorValue);
    }

    public Object getMessageBrokerSelectorValue() {
        return get(BROKER_MESSAGE_SELECTOR_VALUE);
    }

    @JsonRef
    public AbstractInstance getInstance() {
        return instance;
    }

    public void setInstance(AbstractInstance instance) {
        this.instance = instance;
    }

    public TransportConfiguration getTransport() {
        return get(TRANSPORT, TransportConfiguration.class);
    }

    public void setTransport(TransportConfiguration transport) {
        put(TRANSPORT, transport);
    }

    public void setConnectionProperties(ConnectionProperties connectionProperties) {
        put(CONNECTION_PROPERTIES, connectionProperties);
    }

    public ConnectionProperties getConnectionProperties() {
        return get(CONNECTION_PROPERTIES, ConnectionProperties.class);
    }

    @Override
    public void setJsonString(String jsonString) throws ParseException {
        JSONParser parser = new JSONParser();
        if (Strings.isNullOrEmpty(jsonString)) {
            return;
        }
        Object parse = parser.parse(jsonString);
        if (parse instanceof Map) {
            ((Map) parse).remove(TcContext.TC);
            ((Map) parse).remove(SpContext.SP);
            putAll((Map) parse);
        } else {
            put("parsed", parse);
        }
    }

    @Override
    public String getJsonString() {
        JsonContext copy = new JsonContext();
        copy.putAll(this);
        copy.remove(TcContext.TC);
        copy.remove(SpContext.SP);
        return copy.getJsonString();
    }
}
