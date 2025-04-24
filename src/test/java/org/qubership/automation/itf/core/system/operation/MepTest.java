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

import org.testng.Assert;
import org.testng.annotations.Test;

public class MepTest {
    @Test
    public void testIsAsync() {
        Assert.assertFalse(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isAsync());
        Assert.assertTrue(OUTBOUND_REQUEST_ASYNCHRONOUS.isAsync());
        Assert.assertTrue(OUTBOUND_RESPONSE_ASYNCHRONOUS.isAsync());
        Assert.assertFalse(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isAsync());
        Assert.assertFalse(INBOUND_REQUEST_SYNCHRONOUS.isAsync());
        Assert.assertFalse(INBOUND_RESPONSE_SYNCHRONOUS.isAsync());
        Assert.assertTrue(INBOUND_REQUEST_ASYNCHRONOUS.isAsync());
        Assert.assertTrue(INBOUND_RESPONSE_ASYNCHRONOUS.isAsync());
    }

    @Test
    public void testIsSync() {
        Assert.assertTrue(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isSync());
        Assert.assertFalse(OUTBOUND_REQUEST_ASYNCHRONOUS.isSync());
        Assert.assertFalse(OUTBOUND_RESPONSE_ASYNCHRONOUS.isSync());
        Assert.assertTrue(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isSync());
        Assert.assertTrue(INBOUND_REQUEST_SYNCHRONOUS.isSync());
        Assert.assertTrue(INBOUND_RESPONSE_SYNCHRONOUS.isSync());
        Assert.assertFalse(INBOUND_REQUEST_ASYNCHRONOUS.isSync());
        Assert.assertFalse(INBOUND_RESPONSE_ASYNCHRONOUS.isSync());
    }

    @Test
    public void testIsOutbound() {
        Assert.assertTrue(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isOutbound());
        Assert.assertTrue(OUTBOUND_REQUEST_ASYNCHRONOUS.isOutbound());
        Assert.assertTrue(OUTBOUND_RESPONSE_ASYNCHRONOUS.isOutbound());
        Assert.assertFalse(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isOutbound());
        Assert.assertFalse(INBOUND_REQUEST_SYNCHRONOUS.isOutbound());
        Assert.assertFalse(INBOUND_RESPONSE_SYNCHRONOUS.isOutbound());
        Assert.assertFalse(INBOUND_REQUEST_ASYNCHRONOUS.isOutbound());
        Assert.assertFalse(INBOUND_RESPONSE_ASYNCHRONOUS.isOutbound());
    }

    @Test
    public void testIsInbound() {
        Assert.assertFalse(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isInbound());
        Assert.assertFalse(OUTBOUND_REQUEST_ASYNCHRONOUS.isInbound());
        Assert.assertFalse(OUTBOUND_RESPONSE_ASYNCHRONOUS.isInbound());
        Assert.assertTrue(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isInbound());
        Assert.assertTrue(INBOUND_REQUEST_SYNCHRONOUS.isInbound());
        Assert.assertTrue(INBOUND_RESPONSE_SYNCHRONOUS.isInbound());
        Assert.assertTrue(INBOUND_REQUEST_ASYNCHRONOUS.isInbound());
        Assert.assertTrue(INBOUND_RESPONSE_ASYNCHRONOUS.isInbound());
    }

    @Test
    public void testIsRequest() {
        Assert.assertTrue(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isRequest());
        Assert.assertTrue(OUTBOUND_REQUEST_ASYNCHRONOUS.isRequest());
        Assert.assertFalse(OUTBOUND_RESPONSE_ASYNCHRONOUS.isRequest());
        Assert.assertTrue(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isRequest());
        Assert.assertTrue(INBOUND_REQUEST_SYNCHRONOUS.isRequest());
        Assert.assertFalse(INBOUND_RESPONSE_SYNCHRONOUS.isRequest());
        Assert.assertTrue(INBOUND_REQUEST_ASYNCHRONOUS.isRequest());
        Assert.assertFalse(INBOUND_RESPONSE_ASYNCHRONOUS.isRequest());
    }

    @Test
    public void testIsResponse() {
        Assert.assertTrue(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isResponse());
        Assert.assertFalse(OUTBOUND_REQUEST_ASYNCHRONOUS.isResponse());
        Assert.assertTrue(OUTBOUND_RESPONSE_ASYNCHRONOUS.isResponse());
        Assert.assertTrue(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isResponse());
        Assert.assertFalse(INBOUND_REQUEST_SYNCHRONOUS.isResponse());
        Assert.assertTrue(INBOUND_RESPONSE_SYNCHRONOUS.isResponse());
        Assert.assertFalse(INBOUND_REQUEST_ASYNCHRONOUS.isResponse());
        Assert.assertTrue(INBOUND_RESPONSE_ASYNCHRONOUS.isResponse());
    }

    @Test
    public void testIsOneDirection() {
        Assert.assertFalse(OUTBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isOneDirection());
        Assert.assertTrue(OUTBOUND_REQUEST_ASYNCHRONOUS.isOneDirection());
        Assert.assertTrue(OUTBOUND_RESPONSE_ASYNCHRONOUS.isOneDirection());
        Assert.assertFalse(INBOUND_REQUEST_RESPONSE_SYNCHRONOUS.isOneDirection());
        Assert.assertTrue(INBOUND_REQUEST_SYNCHRONOUS.isOneDirection());
        Assert.assertTrue(INBOUND_RESPONSE_SYNCHRONOUS.isOneDirection());
        Assert.assertTrue(INBOUND_REQUEST_ASYNCHRONOUS.isOneDirection());
        Assert.assertTrue(INBOUND_RESPONSE_ASYNCHRONOUS.isOneDirection());
    }

}
