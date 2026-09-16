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

package org.qubership.automation.itf.core.util.services.projectsettings;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import com.hazelcast.map.IMap;

/**
 * Fails if a failure while reading one project setting out of an already-fetched Hazelcast map
 * goes back to being logged without its exception (ERR-07).
 */
class AbstractProjectSettingsServiceTest {

    private static final BigInteger PROJECT_ID = BigInteger.valueOf(100L);
    private static final String SHORT_NAME = "timeout";

    private ListAppender<ILoggingEvent> logAppender;
    private ch.qos.logback.classic.Logger logger;

    @SuppressWarnings("unchecked")
    private final IMap<String, Map<String, String>> cache = mock(IMap.class);
    private final AbstractProjectSettingsService service = new AbstractProjectSettingsService() {
        @Override
        protected IMap<String, Map<String, String>> getProjectSettingsCache() {
            return cache;
        }
    };

    @BeforeEach
    void setUp() {
        logAppender = new ListAppender<>();
        logAppender.start();
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AbstractProjectSettingsService.class);
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() {
        logger.detachAppender(logAppender);
    }

    @Test
    @DisplayName("Should return the default value and log the exception with its stack trace when reading "
            + "one setting out of an already-fetched map fails")
    void returnsDefault_andLogsTheExceptionWithItsStackTrace_whenReadingOneSettingFails() {
        @SuppressWarnings("unchecked")
        Map<String, String> projectSettings = mock(Map.class);
        RuntimeException cacheError = new RuntimeException("boom");
        when(cache.get(PROJECT_ID.toString())).thenReturn(projectSettings);
        when(projectSettings.get(eq(SHORT_NAME))).thenThrow(cacheError);

        String result = service.get(PROJECT_ID, SHORT_NAME, "default-value");

        assertEquals("default-value", result);
        assertTrue(logAppender.list.stream().anyMatch(event -> event.getLevel() == Level.ERROR
                        && event.getThrowableProxy() != null
                        && cacheError.getMessage().equals(event.getThrowableProxy().getMessage())),
                "the caught exception must be logged with its stack trace, not discarded");
    }
}
