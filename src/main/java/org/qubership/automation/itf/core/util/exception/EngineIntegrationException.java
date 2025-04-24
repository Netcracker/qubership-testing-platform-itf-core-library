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

package org.qubership.automation.itf.core.util.exception;

public class EngineIntegrationException extends RuntimeException {
    private String shortMessage;

    public EngineIntegrationException(String message) {
        super(message);
        this.shortMessage = message;
    }

    public EngineIntegrationException(String shortMessage, String message) {
        super(shortMessage + "\n Details: " + message);
        this.shortMessage = shortMessage;
    }

    public EngineIntegrationException(String message, Throwable cause) {
        super(message, cause);
        this.shortMessage = message;
    }

    public EngineIntegrationException(Throwable cause) {
        super(cause);
        this.shortMessage = cause.getMessage();
    }

    public String getShortMessage() {
        return shortMessage;
    }
}
