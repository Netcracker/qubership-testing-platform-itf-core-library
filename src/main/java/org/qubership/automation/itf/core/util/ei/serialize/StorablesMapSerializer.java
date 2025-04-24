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

package org.qubership.automation.itf.core.util.ei.serialize;

import java.io.IOException;
import java.util.Map;

import org.qubership.automation.itf.core.model.common.Storable;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.google.common.collect.Maps;

public class StorablesMapSerializer extends JsonSerializer<Map<Storable, Storable>> {

    @Override
    public void serialize(Map<Storable, Storable> storablesMap, JsonGenerator jsonGenerator,
                          SerializerProvider serializerProvider) throws IOException {
        Map<Object, Object> result = Maps.newHashMapWithExpectedSize(storablesMap.size());
        for (Map.Entry<Storable, Storable> entry : storablesMap.entrySet()) {
            result.put(entry.getKey().getID(), entry.getValue().getID());
        }
        jsonGenerator.writeObject(result);
    }

}
