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

import java.math.BigInteger;
import java.util.Map;

import org.qubership.automation.itf.core.model.communication.StubUser;
import org.qubership.automation.itf.core.util.constants.TriggerState;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class TriggerStateResponse {
    private Map<BigInteger, TriggerState> states;
    private String errorMessage;
    private StubUser user;
    private String sessionId;

    /**
     * TODO: Add JavaDoc.
     */
    public TriggerStateResponse(Map<BigInteger, TriggerState> states, String errorMessage,
                                StubUser user, String sessionId) {
        this.states = states;
        this.errorMessage = errorMessage;
        this.user = user;
        this.sessionId = sessionId;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void merge(TriggerStateResponse responseToMergeWith) {
        this.getStates().putAll(responseToMergeWith.getStates());
        this.setErrorMessage(this.getErrorMessage() + responseToMergeWith.getErrorMessage());
        if (this.getUser() == null) {
            setUser(responseToMergeWith.getUser());
        }
        if (this.getSessionId().isEmpty()) {
            setSessionId(responseToMergeWith.getSessionId());
        }
    }
}
