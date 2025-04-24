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

package org.qubership.automation.itf.core.model.transport;

import java.util.HashMap;
import java.util.Map;

public class ConnectionProperties extends HashMap<String, Object> {

    private static final long serialVersionUID = 8895341504195631045L;

    public ConnectionProperties() {
        super(20);
    }

    public ConnectionProperties(Map<? extends String, ?> m) {
        super(m);
    }

    /**
     * TODO: Add Javadoc.
     *
     * @param key map key
     * @param <T> generic type
     * @return value which would be taken by key, and cast to generic T
     */
    public <T> T obtain(String key) {
        return (T) get(key);
    }

}
