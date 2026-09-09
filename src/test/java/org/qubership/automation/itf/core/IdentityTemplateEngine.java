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

package org.qubership.automation.itf.core;

import java.util.Map;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.util.engine.TemplateEngine;

/**
 * Returns every input unchanged, for tests that need {@link TemplateEngine#process} wired but assert
 * on a string with no template placeholders in it (a regex, a plain value). {@link TestTemplateEngine}
 * is not a substitute here: it unconditionally appends a marker suffix, which corrupts such a string.
 */
public class IdentityTemplateEngine implements TemplateEngine {
    @Override
    public String process(Storable owner, String someString, JsonContext context) {
        return someString;
    }

    @Override
    public String process(Map<String, Storable> storables, String someString, JsonContext context) {
        return someString;
    }

    @Override
    public String process(Storable owner, String someString, JsonContext context, String coords) {
        return someString;
    }

    @Override
    public String process(Map<String, Storable> storables, String someString, JsonContext context, String coords) {
        return someString;
    }
}
