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

package org.qubership.automation.itf.core.system.operation;

import static org.qubership.automation.itf.core.util.constants.Mep.INBOUND_REQUEST_ASYNCHRONOUS;
import static org.qubership.automation.itf.core.util.constants.Mep.INBOUND_REQUEST_RESPONSE_SYNCHRONOUS;
import static org.qubership.automation.itf.core.util.constants.Mep.INBOUND_REQUEST_SYNCHRONOUS;
import static org.qubership.automation.itf.core.util.constants.Mep.INBOUND_RESPONSE_ASYNCHRONOUS;
import static org.qubership.automation.itf.core.util.constants.Mep.INBOUND_RESPONSE_SYNCHRONOUS;
import static org.qubership.automation.itf.core.util.constants.Mep.OUTBOUND_REQUEST_ASYNCHRONOUS;
import static org.qubership.automation.itf.core.util.constants.Mep.OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS;
import static org.qubership.automation.itf.core.util.constants.Mep.OUTBOUND_RESPONSE_ASYNCHRONOUS;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class MepTest {
    @Test
    public void testIsAsync() {
        Assertions.assertFalse(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isAsync());
        Assertions.assertTrue(OUTBOUND_REQUEST_ASYNCHRONOUS.isAsync());
        Assertions.assertTrue(OUTBOUND_RESPONSE_ASYNCHRONOUS.isAsync());
        Assertions.assertFalse(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isAsync());
        Assertions.assertFalse(INBOUND_REQUEST_SYNCHRONOUS.isAsync());
        Assertions.assertFalse(INBOUND_RESPONSE_SYNCHRONOUS.isAsync());
        Assertions.assertTrue(INBOUND_REQUEST_ASYNCHRONOUS.isAsync());
        Assertions.assertTrue(INBOUND_RESPONSE_ASYNCHRONOUS.isAsync());
    }

    @Test
    public void testIsSync() {
        Assertions.assertTrue(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isSync());
        Assertions.assertFalse(OUTBOUND_REQUEST_ASYNCHRONOUS.isSync());
        Assertions.assertFalse(OUTBOUND_RESPONSE_ASYNCHRONOUS.isSync());
        Assertions.assertTrue(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isSync());
        Assertions.assertTrue(INBOUND_REQUEST_SYNCHRONOUS.isSync());
        Assertions.assertTrue(INBOUND_RESPONSE_SYNCHRONOUS.isSync());
        Assertions.assertFalse(INBOUND_REQUEST_ASYNCHRONOUS.isSync());
        Assertions.assertFalse(INBOUND_RESPONSE_ASYNCHRONOUS.isSync());
    }

    @Test
    public void testIsOutbound() {
        Assertions.assertTrue(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isOutbound());
        Assertions.assertTrue(OUTBOUND_REQUEST_ASYNCHRONOUS.isOutbound());
        Assertions.assertTrue(OUTBOUND_RESPONSE_ASYNCHRONOUS.isOutbound());
        Assertions.assertFalse(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isOutbound());
        Assertions.assertFalse(INBOUND_REQUEST_SYNCHRONOUS.isOutbound());
        Assertions.assertFalse(INBOUND_RESPONSE_SYNCHRONOUS.isOutbound());
        Assertions.assertFalse(INBOUND_REQUEST_ASYNCHRONOUS.isOutbound());
        Assertions.assertFalse(INBOUND_RESPONSE_ASYNCHRONOUS.isOutbound());
    }

    @Test
    public void testIsInbound() {
        Assertions.assertFalse(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isInbound());
        Assertions.assertFalse(OUTBOUND_REQUEST_ASYNCHRONOUS.isInbound());
        Assertions.assertFalse(OUTBOUND_RESPONSE_ASYNCHRONOUS.isInbound());
        Assertions.assertTrue(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isInbound());
        Assertions.assertTrue(INBOUND_REQUEST_SYNCHRONOUS.isInbound());
        Assertions.assertTrue(INBOUND_RESPONSE_SYNCHRONOUS.isInbound());
        Assertions.assertTrue(INBOUND_REQUEST_ASYNCHRONOUS.isInbound());
        Assertions.assertTrue(INBOUND_RESPONSE_ASYNCHRONOUS.isInbound());
    }

    @Test
    public void testIsRequest() {
        Assertions.assertTrue(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isRequest());
        Assertions.assertTrue(OUTBOUND_REQUEST_ASYNCHRONOUS.isRequest());
        Assertions.assertFalse(OUTBOUND_RESPONSE_ASYNCHRONOUS.isRequest());
        Assertions.assertTrue(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isRequest());
        Assertions.assertTrue(INBOUND_REQUEST_SYNCHRONOUS.isRequest());
        Assertions.assertFalse(INBOUND_RESPONSE_SYNCHRONOUS.isRequest());
        Assertions.assertTrue(INBOUND_REQUEST_ASYNCHRONOUS.isRequest());
        Assertions.assertFalse(INBOUND_RESPONSE_ASYNCHRONOUS.isRequest());
    }

    @Test
    public void testIsResponse() {
        Assertions.assertTrue(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isResponse());
        Assertions.assertFalse(OUTBOUND_REQUEST_ASYNCHRONOUS.isResponse());
        Assertions.assertTrue(OUTBOUND_RESPONSE_ASYNCHRONOUS.isResponse());
        Assertions.assertTrue(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isResponse());
        Assertions.assertFalse(INBOUND_REQUEST_SYNCHRONOUS.isResponse());
        Assertions.assertTrue(INBOUND_RESPONSE_SYNCHRONOUS.isResponse());
        Assertions.assertFalse(INBOUND_REQUEST_ASYNCHRONOUS.isResponse());
        Assertions.assertTrue(INBOUND_RESPONSE_ASYNCHRONOUS.isResponse());
    }

    @Test
    public void testIsOneDirection() {
        Assertions.assertFalse(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isOneDirection());
        Assertions.assertTrue(OUTBOUND_REQUEST_ASYNCHRONOUS.isOneDirection());
        Assertions.assertTrue(OUTBOUND_RESPONSE_ASYNCHRONOUS.isOneDirection());
        Assertions.assertFalse(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isOneDirection());
        Assertions.assertTrue(INBOUND_REQUEST_SYNCHRONOUS.isOneDirection());
        Assertions.assertTrue(INBOUND_RESPONSE_SYNCHRONOUS.isOneDirection());
        Assertions.assertTrue(INBOUND_REQUEST_ASYNCHRONOUS.isOneDirection());
        Assertions.assertTrue(INBOUND_RESPONSE_ASYNCHRONOUS.isOneDirection());
    }

}
