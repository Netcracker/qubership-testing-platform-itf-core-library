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

public enum TransportState {

    NOT_READY("Not Ready"), REGISTERING("Registering"), REGISTERED("Registered"),
    PUBLISHING_PROPERTIES("Publishing properties"), PUBLISHED_PROPERTIES("Published properties"),
    APPLYING_CONFIGURATION("Applying configuration"), READY("Ready"), UNDEPLOYED("Undeployed");

    private final String state;

    TransportState(String s) {
        state = s;
    }

    @Override
    public String toString() {
        return state;
    }
}
