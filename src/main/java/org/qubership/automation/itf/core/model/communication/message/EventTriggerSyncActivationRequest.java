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
public class EventTriggerSyncActivationRequest implements EventTriggerActivationRequest {

    private String type = "sync";
    private List<EventTriggerBriefInfo> triggersToDeactivate;
    private List<EventTriggerBriefInfo> triggersToReactivate;
    private StubUser user;
    private String sessionId;

    /**
     * All args constructor.
     */
    public EventTriggerSyncActivationRequest(List<EventTriggerBriefInfo> triggersToDeactivate,
                                             List<EventTriggerBriefInfo> triggersToReactivate,
                                             StubUser user, String sessionId) {
        this.triggersToDeactivate = triggersToDeactivate;
        this.triggersToReactivate = triggersToReactivate;
        this.user = user;
        this.sessionId = sessionId;
    }

    /**
     * No args constructor.
     */
    public EventTriggerSyncActivationRequest() {
        this.triggersToDeactivate = new ArrayList<>();
        this.triggersToReactivate = new ArrayList<>();
        this.user = null;
        this.sessionId = "";
    }

    /**
     * Merge another request to this one.
     */
    public void merge(EventTriggerSyncActivationRequest requestToMergeWith) {
        this.triggersToDeactivate.addAll(requestToMergeWith.getTriggersToDeactivate());
        this.triggersToReactivate.addAll(requestToMergeWith.getTriggersToReactivate());
        if (this.getUser() == null) {
            setUser(requestToMergeWith.getUser());
        }
        if (this.getSessionId().isEmpty()) {
            setSessionId(requestToMergeWith.getSessionId());
        }
    }
}
