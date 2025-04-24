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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonValue;

public enum TriggerState {

    ACTIVE("Active", true), INACTIVE("Inactive", false),
    ERROR("Error", false), STARTING("Starting", true),
    SHUTTING_DOWN("Shutting down", false), ACTIVE_PART("Active (Not all)", true),
    ACTIVE_ERROR("Active (Errors)", true), EMPTY("Empty", false);

    private String state;
    private boolean isOn;

    TriggerState(String state, boolean isOn) {
        this.state = state;
        this.isOn = isOn;
    }

    @Override
    public String toString() {
        return state;
    }

    @JsonValue
    public String getState() {
        return state;
    }

    public boolean isOn() {
        return isOn;
    }

    /**
     * TODO: Add JavaDoc.
     */
    @JsonCreator(mode = JsonCreator.Mode.DELEGATING)
    public static TriggerState fromString(@JsonProperty("triggerState") String state) {
        for (TriggerState triggerState : values()) {
            if (triggerState.state.equalsIgnoreCase(state)) {
                return triggerState;
            }
            if (triggerState.name().equalsIgnoreCase(state)) {
                return triggerState;
            }
        }
        return null;
    }
}
