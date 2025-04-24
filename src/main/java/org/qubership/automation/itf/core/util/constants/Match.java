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

package org.qubership.automation.itf.core.util.constants;

public enum Match {

    EQUALS("equals"), NOT_EQUALS("not equals"), IN("in");

    private final String name;

    Match(String name) {
        this.name = name;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static Match fromString(String string) {
        for (Match m : values()) {
            if (m.toString().equalsIgnoreCase(string) || m.name().equalsIgnoreCase(string)) {
                return m;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return name;
    }
}
