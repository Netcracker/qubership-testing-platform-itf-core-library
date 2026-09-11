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

package org.qubership.automation.itf.core.util.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers {@link Config#getConfig()}.
 *
 * <p>The singleton must be published only once fully built: a caller that observes a non-null
 * {@code config} must always find {@link Config#RUNNING_HOSTNAME}, {@link Config#RUNNING_PORT}
 * and {@link Config#RUNNING_URL} already set on it (CONC-09). Before this fix, {@code config} was
 * assigned before {@code addDefaultHostProperties}/{@code parseResponseCodes} ran, so a second
 * thread arriving during that window could see a non-null but not-yet-populated instance.</p>
 *
 * <p>{@code config} is a private static field shared by every caller in the JVM, so each test
 * saves it before running and restores it afterward to avoid leaking state into other tests.</p>
 */
class ConfigTest {

    private static Field configField;

    private Config originalConfig;

    @BeforeAll
    static void resolveField() throws Exception {
        configField = Config.class.getDeclaredField("config");
        configField.setAccessible(true);
    }

    @BeforeEach
    void saveOriginalConfig() throws Exception {
        originalConfig = (Config) configField.get(null);
        configField.set(null, null);
    }

    @AfterEach
    void restoreOriginalConfig() throws Exception {
        configField.set(null, originalConfig);
    }

    @AfterAll
    static void restoreRealConfig() {
        // Leave a real, fully initialized singleton behind for any test that runs after this
        // class and happens to call Config.getConfig() without expecting it to be null.
        Config.getConfig();
    }

    @Test
    void getConfigReturnsAFullyPopulatedInstance() {
        Config config = Config.getConfig();

        assertNotNull(config);
        assertTrue(StringUtils.isNotBlank(config.getString(Config.RUNNING_HOSTNAME)));
        assertTrue(StringUtils.isNotBlank(config.getString(Config.RUNNING_PORT)));
        assertTrue(StringUtils.isNotBlank(config.getString(Config.RUNNING_URL)));
    }

    @Test
    void getConfigReturnsTheSameInstanceOnRepeatedCalls() {
        Config first = Config.getConfig();
        Config second = Config.getConfig();

        assertSame(first, second);
    }

    /**
     * Best-effort regression test for CONC-09: races {@link Config#getConfig()}'s first call
     * against an observer that reads the raw {@code config} field the moment it turns non-null,
     * then checks the observed instance's own properties directly (not through
     * {@link Config#getRunningHostname()}, which reads the static field again rather than
     * {@code this}). The window between publishing the field and finishing initialization is
     * sub-millisecond even when it exists, so this does not reliably reproduce the defect — it
     * failed to reproduce it even in the original report's own dedicated stress test — but it
     * costs little to keep as a check against a coarser regression.
     */
    @Test
    void concurrentFirstCallNeverPublishesAPartiallyInitializedInstance() throws Exception {
        int rounds = 200;
        for (int round = 0; round < rounds; round++) {
            configField.set(null, null);
            AtomicReference<Config> observedAtPublish = new AtomicReference<>();
            AtomicReference<Throwable> failure = new AtomicReference<>();

            Thread observer = new Thread(() -> {
                try {
                    Config seen;
                    do {
                        seen = (Config) configField.get(null);
                    } while (seen == null);
                    observedAtPublish.set(seen);
                } catch (Throwable t) {
                    failure.compareAndSet(null, t);
                }
            });
            Thread initializer = new Thread(Config::getConfig);

            observer.start();
            initializer.start();
            initializer.join(5000);
            observer.join(5000);

            assertNull(failure.get(), "round " + round + ": observer failed unexpectedly: " + failure.get());
            Config observed = observedAtPublish.get();
            assertNotNull(observed, "round " + round + ": observer never saw a published Config");
            assertTrue(StringUtils.isNotBlank(observed.getString(Config.RUNNING_HOSTNAME)),
                    "round " + round + ": published Config had no running hostname yet");
            assertTrue(StringUtils.isNotBlank(observed.getString(Config.RUNNING_PORT)),
                    "round " + round + ": published Config had no running port yet");
            assertTrue(StringUtils.isNotBlank(observed.getString(Config.RUNNING_URL)),
                    "round " + round + ": published Config had no running URL yet");
        }
    }
}
