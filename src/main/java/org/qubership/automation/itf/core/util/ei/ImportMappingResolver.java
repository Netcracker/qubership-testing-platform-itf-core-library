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

package org.qubership.automation.itf.core.util.ei;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.util.constants.EiConstants;

public class ImportMappingResolver {

    static final Map<String, Class<? extends Storable>> importMapping = new HashMap<>();

    static {
        importMapping.putAll(Collections.unmodifiableMap(EiConstants.importMapping));
    }

    public static Map<String, Class<? extends Storable>> getImportMapping() {
        return importMapping;
    }
}
