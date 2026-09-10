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

package org.qubership.automation.itf.core.util.transport.registry.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.rmi.RemoteException;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.util.exception.ExportException;
import org.qubership.automation.itf.core.util.exception.TransportException;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;

/**
 * Covers {@link AbstractTransportRegistry}'s shared state under concurrent access.
 *
 * <p>{@code transportTypes} must survive a registration thread and a reading thread running at
 * the same time, the way its sibling fields {@code states} and {@code availableServers} already
 * do. {@code triggersActivationCompleted}, {@code loaded}, and {@code coreCallback} are each
 * written by one thread (activation, deployment, wiring) and read by another (inbound transport
 * threads) with no other synchronization between them, so each needs {@code volatile} for that
 * write to become visible at all.</p>
 */
class AbstractTransportRegistryTest {

    /** Minimal concrete registry: every hook is a no-op, since these tests exercise the base class's own state. */
    private static final class TestRegistry extends AbstractTransportRegistry {
        @Override
        public void init() throws ExportException {
            // not exercised by these tests
        }

        @Override
        protected void protectedRegister(AccessTransport accessTransport) throws TransportException {
            // not exercised by these tests
        }

        @Override
        protected void protectedUnregister(String typeName) throws RemoteException {
            // not exercised by these tests
        }

        @Override
        protected AccessTransport protectedFind(String typeName) throws RemoteException {
            return null;
        }

        @Override
        public void destroy() {
            // not exercised by these tests
        }
    }

    private static AccessTransport transportNamed(String typeName) throws RemoteException {
        AccessTransport transport = mock(AccessTransport.class);
        when(transport.getTypeName()).thenReturn(typeName);
        when(transport.getUserName()).thenReturn("user-" + typeName);
        return transport;
    }

    @Test
    void registerAddsAndUnregisterRemovesTheTransportType() throws Exception {
        TestRegistry registry = new TestRegistry();

        registry.register(transportNamed("http"));
        assertEquals("user-http", registry.getTransportTypes().get("http"));

        registry.unregister("http");
        assertNull(registry.getTransportTypes().get("http"));
    }

    /**
     * Regression test for {@code transportTypes} being a plain {@code HashMap}: a reader iterating
     * {@link AbstractTransportRegistry#getTransportTypes()} raced against {@code register}/
     * {@code unregister} on another thread threw {@link java.util.ConcurrentModificationException},
     * since {@code Collections.unmodifiableMap} delegates iteration to the same fail-fast backing
     * map. A {@link java.util.concurrent.ConcurrentHashMap} iterates without that check.
     */
    @Test
    void transportTypesSurvivesConcurrentRegisterAndIteration() throws Exception {
        TestRegistry registry = new TestRegistry();
        AtomicBoolean stop = new AtomicBoolean(false);
        AtomicReference<Throwable> failure = new AtomicReference<>();

        Thread writer = new Thread(() -> {
            try {
                int i = 0;
                while (!stop.get()) {
                    String typeName = "type-" + (i++ % 50);
                    registry.register(transportNamed(typeName));
                    registry.unregister(typeName);
                }
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        });
        Thread reader = new Thread(() -> {
            try {
                while (!stop.get()) {
                    for (Map.Entry<String, String> entry : registry.getTransportTypes().entrySet()) {
                        assertTrue(entry.getKey().startsWith("type-"));
                    }
                }
            } catch (Throwable t) {
                failure.compareAndSet(null, t);
            }
        });

        writer.start();
        reader.start();
        Thread.sleep(2000);
        stop.set(true);
        writer.join(5000);
        reader.join(5000);

        Throwable observed = failure.get();
        assertNull(observed, () -> "concurrent register/iterate failed: " + observed);
    }

    @Test
    void triggersActivationCompletedFieldIsVolatile() throws NoSuchFieldException {
        assertFieldIsVolatile("triggersActivationCompleted");
    }

    @Test
    void loadedFieldIsVolatile() throws NoSuchFieldException {
        assertFieldIsVolatile("loaded");
    }

    @Test
    void coreCallbackFieldIsVolatile() throws NoSuchFieldException {
        assertFieldIsVolatile("coreCallback");
    }

    private static void assertFieldIsVolatile(String fieldName) throws NoSuchFieldException {
        Field field = AbstractTransportRegistry.class.getDeclaredField(fieldName);
        assertTrue(Modifier.isVolatile(field.getModifiers()), fieldName + " must be volatile");
    }
}
