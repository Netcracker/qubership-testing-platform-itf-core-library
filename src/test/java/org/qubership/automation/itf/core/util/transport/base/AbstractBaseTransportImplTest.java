/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.automation.itf.core.util.transport.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.net.SocketTimeoutException;
import java.rmi.RemoteException;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;
import org.qubership.automation.itf.core.util.transport.loader.LoaderBaseTransportImpl;

/**
 * Regression test for ERR-06: a transport failure used to be wrapped as a {@link RemoteException}
 * whose cause was a fresh {@link Throwable} carrying the original exception's stack trace as its
 * message, rather than the original exception itself. A caller could not tell a
 * {@link SocketTimeoutException} (worth retrying) from any other failure except by parsing that
 * text, and the stack trace was lost as structured data.
 */
class AbstractBaseTransportImplTest {

    /** A {@link Transport} whose every operation fails with the exception it is built with. */
    private static final class FailingTransport implements Transport {

        private final Exception failure;

        private FailingTransport(Exception failure) {
            this.failure = failure;
        }

        @Override
        public String send(Message message, String sessionId, UUID projectUuid) throws Exception {
            throw failure;
        }

        @Override
        public Message receive(String sessionId) throws Exception {
            throw failure;
        }

        @Override
        public Message sendReceiveSync(Message messageToSend, BigInteger projectId) throws Exception {
            throw failure;
        }

        @Override
        public Mep getMep() {
            return null;
        }

        @Override
        public String getEndpointPrefix() {
            return "test";
        }

        @Override
        public String getShortName() {
            return "FailingTransport";
        }

        @Override
        public AccessTransport getRemote() {
            return null;
        }

        @Override
        public void setRemote(AccessTransport remote) {
            // not exercised by this test
        }
    }

    @Test
    void sendPreservesTheOriginalExceptionAsTheCause() {
        SocketTimeoutException original = new SocketTimeoutException("Read timed out");
        LoaderBaseTransportImpl<FailingTransport> transport =
                new LoaderBaseTransportImpl<>(new FailingTransport(original));

        RemoteException thrown = assertThrows(RemoteException.class,
                () -> transport.send(null, "session", null));

        assertSame(original, thrown.getCause());
    }

    @Test
    void receivePreservesTheOriginalExceptionAsTheCause() {
        SocketTimeoutException original = new SocketTimeoutException("Read timed out");
        LoaderBaseTransportImpl<FailingTransport> transport =
                new LoaderBaseTransportImpl<>(new FailingTransport(original));

        RemoteException thrown = assertThrows(RemoteException.class, () -> transport.receive("session"));

        assertSame(original, thrown.getCause());
        assertNull(thrown.getCause().getCause());
        assertTrue(thrown.getMessage().contains("Read timed out"), thrown.getMessage());
    }

    @Test
    void sendReceiveSyncPreservesTheOriginalExceptionAsTheCause() {
        IllegalStateException original = new IllegalStateException("endpoint not configured");
        LoaderBaseTransportImpl<FailingTransport> transport =
                new LoaderBaseTransportImpl<>(new FailingTransport(original));

        RemoteException thrown = assertThrows(RemoteException.class,
                () -> transport.sendReceiveSync(null, BigInteger.ONE));

        assertSame(original, thrown.getCause());
        assertEquals(IllegalStateException.class, thrown.getCause().getClass());
    }
}
