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

import org.qubership.automation.itf.core.model.communication.StubUser;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriggerStatusMessage {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger id;
    private ObjectType objectType;
    private String status;
    private boolean isSuccess;
    private String description;
    private StubUser user;
    private String sessionId;

    /**
     * Reports {@code status} for the object identified by {@code objectType} and {@code id}, on
     * behalf of {@code user}. {@code isSuccess} starts {@code false}.
     *
     * @param objectType the kind of object {@code id} identifies
     * @param id the object's id
     * @param status the status to report
     * @param description a human-readable description of the status
     * @param user the user the message is made on behalf of
     * @param sessionId the message's session id
     */
    public TriggerStatusMessage(ObjectType objectType, BigInteger id, String status, String description,
                                StubUser user, String sessionId) {
        this.id = id;
        this.objectType = objectType;
        this.status = status;
        this.description = description;
        this.user = user;
        this.isSuccess = false;
        this.sessionId = sessionId;
    }

    public enum ObjectType {
        TRIGGER, ENVIRONMENT
    }

}
