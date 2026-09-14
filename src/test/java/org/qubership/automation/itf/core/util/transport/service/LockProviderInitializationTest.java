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

package org.qubership.automation.itf.core.util.transport.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.qubership.automation.itf.core.util.constants.InstanceSettingsConstants.LOCK_PROVIDER_PROCESS_TIMEOUT;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.util.config.ApplicationConfig;
import org.springframework.core.env.Environment;

/**
 * Covers {@link LockProvider#init()}.
 *
 * <p>A call reaching it before {@link ApplicationConfig#getEnv()} is wired must fail, and must
 * leave {@code timeout} and {@code locks} unset so a later call, made once wiring completes, still
 * builds them. Before this class computed {@code timeout} and built {@code locks} in the enum
 * constant's field initializers, so the first failing reference wrapped the {@link
 * IllegalStateException} in {@link ExceptionInInitializerError} and permanently marked the class
 * erroneous: every later reference threw {@link NoClassDefFoundError}, surviving even a successful
 * wiring, until the JVM restarted.</p>
 *
 * <p>{@code timeout}, {@code locks}, and {@link ApplicationConfig#env} are all shared by every
 * caller in the JVM, so each test saves them before running and restores them afterward to avoid
 * leaking state into other tests.</p>
 */
class LockProviderInitializationTest {

    private static Field timeoutField;
    private static Field locksField;

    private int originalTimeout;
    private Object originalLocks;
    private Environment originalEnv;

    @BeforeAll
    static void resolveFields() throws Exception {
        timeoutField = LockProvider.class.getDeclaredField("timeout");
        timeoutField.setAccessible(true);
        locksField = LockProvider.class.getDeclaredField("locks");
        locksField.setAccessible(true);
    }

    @BeforeEach
    void clearState() throws Exception {
        originalTimeout = (int) timeoutField.get(LockProvider.INSTANCE);
        originalLocks = locksField.get(LockProvider.INSTANCE);
        timeoutField.set(LockProvider.INSTANCE, 0);
        locksField.set(LockProvider.INSTANCE, null);
        originalEnv = ApplicationConfig.env;
        ApplicationConfig.env = null;
    }

    @AfterEach
    void restoreState() throws Exception {
        timeoutField.set(LockProvider.INSTANCE, originalTimeout);
        locksField.set(LockProvider.INSTANCE, originalLocks);
        ApplicationConfig.env = originalEnv;
    }

    @Test
    void initThrowsWhenApplicationConfigEnvIsNotWiredYet() {
        assertThrows(IllegalStateException.class, LockProvider.INSTANCE::init);
    }

    @Test
    void aFailedInitLeavesTimeoutAndLocksUnset() throws Exception {
        assertThrows(IllegalStateException.class, LockProvider.INSTANCE::init);

        assertEquals(0, (int) timeoutField.get(LockProvider.INSTANCE));
        assertNull(locksField.get(LockProvider.INSTANCE));
    }

    @Test
    void lockProviderCanStillInitializeAfterAFailedInit() throws Exception {
        assertThrows(IllegalStateException.class, LockProvider.INSTANCE::init);

        Environment environment = mock(Environment.class);
        when(environment.getProperty(LOCK_PROVIDER_PROCESS_TIMEOUT, "25000")).thenReturn("5000");
        ApplicationConfig.env = environment;

        assertTrue(LockProvider.INSTANCE.init());

        assertEquals(5000, (int) timeoutField.get(LockProvider.INSTANCE));
        assertNotNull(locksField.get(LockProvider.INSTANCE));
    }
}
