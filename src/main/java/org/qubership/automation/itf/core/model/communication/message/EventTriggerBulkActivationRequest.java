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

package org.qubership.automation.itf.core.model.communication.message;

import java.util.ArrayList;
import java.util.List;

import org.qubership.automation.itf.core.model.communication.EventTriggerBriefInfo;
import org.qubership.automation.itf.core.model.communication.StubUser;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventTriggerBulkActivationRequest implements EventTriggerActivationRequest {

    private String type = "bulk";
    private List<EventTriggerBriefInfo> eventTriggers;
    private boolean turnOn;
    private StubUser user;
    private String sessionId;

    /**
     * All args constructor.
     */
    public EventTriggerBulkActivationRequest(List<EventTriggerBriefInfo> eventTriggers, boolean turnOn,
                                             StubUser user, String sessionId) {
        this.eventTriggers = eventTriggers;
        this.turnOn = turnOn;
        this.user = user;
        this.sessionId = sessionId;
    }

    /**
     * No args constructor.
     */
    public EventTriggerBulkActivationRequest() {
        this.eventTriggers = new ArrayList<>();
        this.turnOn = false;
        this.user = null;
        this.sessionId = "";
    }
}
