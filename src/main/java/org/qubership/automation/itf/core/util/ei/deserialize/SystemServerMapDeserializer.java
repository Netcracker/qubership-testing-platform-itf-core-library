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

package org.qubership.automation.itf.core.util.ei.deserialize;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.qubership.automation.itf.core.model.jpa.server.ServerHB;
import org.qubership.automation.itf.core.model.jpa.system.System;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SystemServerMapDeserializer extends JsonDeserializer<Map<System, ServerHB>> {

    @Override
    public Map<System, ServerHB> deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        return this.deserialize(p, ctxt, new HashMap<>());
    }

    @Override
    public Map<System, ServerHB> deserialize(JsonParser p,
                                           DeserializationContext ctxt,
                                           Map<System, ServerHB> intoValue) throws IOException {
        JsonNode node = p.readValueAsTree();
        ObjectMapper mapper = new ObjectMapper();
        Iterator<Map.Entry<String, JsonNode>> nodeFields = node.fields();
        while (nodeFields.hasNext()) {
            Map.Entry<String, JsonNode> next = nodeFields.next();
            JsonNode keyNode = mapper.readTree(next.getKey());
            JsonNode valueNode = next.getValue();
            intoValue.put(createSimpleSystem(new BigInteger(keyNode.toString())),
                    createSimpleServer(new BigInteger(valueNode.toString())));
        }
        return intoValue;
    }

    private System createSimpleSystem(BigInteger id) {
        System system = new System();
        system.setID(id);
        return system;
    }

    private ServerHB createSimpleServer(BigInteger id) {
        ServerHB server = new ServerHB();
        server.setID(id);
        return server;
    }
}
