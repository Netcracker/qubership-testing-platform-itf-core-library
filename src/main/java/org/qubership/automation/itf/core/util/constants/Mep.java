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

import com.google.common.base.Strings;

public enum Mep {

    OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS("outbound-request/response-synchronous", true, true, false, false),
    OUTBOUND_REQUEST_ASYNCHRONOUS("outbound-request-asynchronous", true, true, true, true),
    OUTBOUND_RESPONSE_ASYNCHRONOUS("outbound-response-asynchronous", true, false, true, true),

    INBOUND_REQUEST_RESPONSE_SYNCHRONOUS("inbound-request/response-synchronous", false, true, false, false),
    INBOUND_REQUEST_SYNCHRONOUS("inbound-request-synchronous", false, true, false, true),
    INBOUND_RESPONSE_SYNCHRONOUS("inbound-response-synchronous", false, false, false, true),
    INBOUND_REQUEST_ASYNCHRONOUS("inbound-request-asynchronous", false, true, true, true),
    INBOUND_RESPONSE_ASYNCHRONOUS("inbound-response-asynchronous", false, false, true, true);

    private final String string;
    private final boolean async;
    private final boolean outbound;
    private final boolean request;
    private final boolean oneDirection;

    Mep(String string, boolean outbound, boolean request, boolean async, boolean oneDirection) {
        this.string = string;
        this.async = async;
        this.outbound = outbound;
        this.request = request;
        this.oneDirection = oneDirection;
    }

    public boolean isAsync() {
        return async;
    }

    public boolean isSync() {
        return !async;
    }

    public boolean isOutbound() {
        return outbound;
    }

    public boolean isInbound() {
        return !outbound;
    }

    public boolean isRequest() {
        return request;
    }

    public boolean isResponse() {
        return !oneDirection || !request;
    }

    public boolean isOneDirection() {
        return oneDirection;
    }

    public boolean isBothDirection() {
        return !oneDirection;
    }

    public boolean isInboundRequest() {
        return !outbound && request;
    }

    public boolean isOutboundRequest() {
        return outbound && request;
    }

    @Override
    public String toString() {
        return string;
    }

    /**
     * Identify Mep by String ignore case.
     */
    public static Mep fromString(String string) {
        for (Mep mep : Mep.values()) {
            if (mep.name().equals(Strings.nullToEmpty(string).toUpperCase()) || mep.string.equalsIgnoreCase(string)) {
                return mep;
            }
        }
        return null;
    }
}
