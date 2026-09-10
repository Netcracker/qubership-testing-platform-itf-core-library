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

package org.qubership.automation.itf.core.util.engine;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Collections;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.counter.Counter;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.manager.CoreObjectManagerService;

/**
 * Covers {@link CounterEngine#getInstance()}.
 *
 * <p>A call reaching it before Spring has wired {@link CoreObjectManager} must fail, and must
 * leave the singleton unset so a later call, made once wiring completes, still builds it. Before
 * this class held an eager {@code static final} field, so that first failing call wrapped the
 * {@link NullPointerException} in {@link ExceptionInInitializerError} and permanently marked the
 * class erroneous: every later reference threw {@link NoClassDefFoundError}, surviving even a
 * successful wiring, until the JVM restarted.</p>
 *
 * <p>{@code instance} and {@link CoreObjectManager}'s static service reference are both shared by
 * every caller in the JVM, so each test saves them before running and restores them afterward to
 * avoid leaking state into other tests.</p>
 */
class CounterEngineInitializationTest {

    private static Field instanceField;
    private static Field coreObjectManagerServiceField;

    private CounterEngine originalInstance;
    private CoreObjectManagerService originalService;

    @BeforeAll
    static void resolveFields() throws Exception {
        instanceField = CounterEngine.class.getDeclaredField("instance");
        instanceField.setAccessible(true);
        coreObjectManagerServiceField = CoreObjectManager.class.getDeclaredField("staticCoreObjectManagerService");
        coreObjectManagerServiceField.setAccessible(true);
    }

    @BeforeEach
    void clearState() throws Exception {
        originalInstance = (CounterEngine) instanceField.get(null);
        instanceField.set(null, null);
        originalService = (CoreObjectManagerService) coreObjectManagerServiceField.get(null);
        coreObjectManagerServiceField.set(null, null);
    }

    @AfterEach
    void restoreState() throws Exception {
        instanceField.set(null, originalInstance);
        coreObjectManagerServiceField.set(null, originalService);
    }

    @Test
    void getInstanceThrowsWhenCoreObjectManagerIsNotWiredYet() {
        assertThrows(NullPointerException.class, CounterEngine::getInstance);
    }

    @Test
    void aFailedGetInstanceLeavesTheSingletonUnset() throws Exception {
        assertThrows(NullPointerException.class, CounterEngine::getInstance);

        assertNull(instanceField.get(null));
    }

    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    void counterEngineCanStillInitializeAfterAFailedGetInstance() throws Exception {
        assertThrows(NullPointerException.class, CounterEngine::getInstance);

        CoreObjectManagerService service = mock(CoreObjectManagerService.class);
        ObjectManager<Counter> counterManager = mock(ObjectManager.class);
        when(service.getManager(any())).thenReturn((ObjectManager) counterManager);
        when(counterManager.getAll()).thenReturn(Collections.emptyList());
        coreObjectManagerServiceField.set(null, service);

        CounterEngine result = CounterEngine.getInstance();

        assertNotNull(result);
        assertSame(result, CounterEngine.getInstance());
    }
}
