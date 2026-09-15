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
     * Requests that a server deactivate and reactivate the given triggers, on behalf of {@code user}.
     *
     * @param triggerIdToDeactivate triggers to deactivate
     * @param triggerIdToReactivate triggers to reactivate
     * @param user the user the request is made on behalf of
     * @param sessionId the request's session id
     */
    public ServerTriggerSyncRequest(List<TriggerSample> triggerIdToDeactivate,
                                    List<TriggerSample> triggerIdToReactivate, StubUser user, String sessionId) {
        this.triggerIdToDeactivate = triggerIdToDeactivate;
        this.triggerIdToReactivate = triggerIdToReactivate;
        this.user = user;
        this.sessionId = sessionId;
    }

    /**
     * Creates an empty request: no triggers to deactivate or reactivate, no user, and an empty
     * session id.
     */
    public ServerTriggerSyncRequest() {
        this.triggerIdToDeactivate = new ArrayList<>();
        this.triggerIdToReactivate = new ArrayList<>();
        this.user = null;
        this.sessionId = "";
    }

    /**
     * Folds {@code requestToMergeWith}'s triggers into this request's lists, and fills this
     * request's {@code user} and {@code sessionId} from it when this request does not have one of
     * its own yet.
     *
     * @param requestToMergeWith the request to merge into this one
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
