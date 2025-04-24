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

import org.qubership.automation.itf.core.model.communication.StubUser;
import org.qubership.automation.itf.core.model.communication.TriggerSample;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class ServerTriggerSyncRequest {
    private List<TriggerSample> triggerIdToDeactivate;
    private List<TriggerSample> triggerIdToReactivate;
    private StubUser user;
    private String sessionId;

    /**
     * TODO: Add JavaDoc.
     */
    public ServerTriggerSyncRequest(List<TriggerSample> triggerIdToDeactivate,
                                    List<TriggerSample> triggerIdToReactivate, StubUser user, String sessionId) {
        this.triggerIdToDeactivate = triggerIdToDeactivate;
        this.triggerIdToReactivate = triggerIdToReactivate;
        this.user = user;
        this.sessionId = sessionId;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public ServerTriggerSyncRequest() {
        this.triggerIdToDeactivate = new ArrayList<>();
        this.triggerIdToReactivate = new ArrayList<>();
        this.user = null;
        this.sessionId = "";
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void merge(ServerTriggerSyncRequest requestToMergeWith) {
        this.triggerIdToDeactivate.addAll(requestToMergeWith.getTriggerIdToDeactivate());
        this.triggerIdToReactivate.addAll(requestToMergeWith.getTriggerIdToReactivate());
        if (this.getUser() == null) {
            setUser(requestToMergeWith.getUser());
        }
        if (this.getSessionId().isEmpty()) {
            setSessionId(requestToMergeWith.getSessionId());
        }
    }
}
