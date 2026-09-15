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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

/**
 * Fails if {@link ApplicationConfig#getEnv()} goes back to reading a {@code null} {@link
 * ApplicationConfig#env} silently instead of throwing when the Spring bean that sets it was never
 * constructed (UX-P1-03).
 *
 * <p>{@link ApplicationConfig#env} is a static field shared by every caller in the JVM, so each
 * test saves it before running and restores it afterward to avoid leaking state into other
 * tests.</p>
 */
class ApplicationConfigTest {

    private Environment originalEnv;

    @BeforeEach
    void saveState() {
        originalEnv = ApplicationConfig.env;
    }

    @AfterEach
    void restoreState() {
        ApplicationConfig.env = originalEnv;
    }

    @Test
    void getEnvThrowsNamingTheRequiredComponentScanWhenNotWired() {
        ApplicationConfig.env = null;

        IllegalStateException ex = assertThrows(IllegalStateException.class, ApplicationConfig::getEnv);

        assertTrue(ex.getMessage().contains("org.qubership.automation.itf.core"));
        assertTrue(ex.getMessage().contains("ComponentScan"));
    }

    @Test
    void getEnvReturnsTheWiredEnvironmentOnceSet() {
        Environment environment = mock(Environment.class);
        ApplicationConfig.env = environment;

        assertSame(environment, ApplicationConfig.getEnv());
    }

    @Test
    void setEnvironmentWiresTheStaticField() {
        Environment environment = mock(Environment.class);

        new ApplicationConfig().setEnvironment(environment);

        assertSame(environment, ApplicationConfig.env);
    }
}
