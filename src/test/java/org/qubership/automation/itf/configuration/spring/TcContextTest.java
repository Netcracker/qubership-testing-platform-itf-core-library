/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.automation.itf.configuration.spring;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.json.simple.JSONArray;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;

public class TcContextTest {

    @Test
    public void testContextSaveOrder() {
        TcContext context = new TcContext();
        context.put("portnumber[0]", 23);
        context.put("portnumber[1]", 24);
        context.put("portnumber[2]", 25);
        context.put("portnumber[3]", 26);
        Object portnumber = context.get("portnumber");
        assertTrue(portnumber instanceof JSONArray); //JSONArray is order safe
    }

    @Test
    public void testIsContextContainsKey() {
        TcContext context = new TcContext();
        context.put("group", new JsonContext());
        String value = "value";
        context.get("group", JsonContext.class).put("param", value);
        assertEquals(value, context.get("group.param"));
        assertTrue(context.containsKey("group.param"));
    }
}
