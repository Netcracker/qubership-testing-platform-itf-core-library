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

package org.qubership.automation.itf.core.model.testcase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.dataset.DataSetList;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.manager.CoreObjectManagerService;
import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

/**
 * Fails if a Dataset service failure on one {@code natureId} stops
 * {@link AbstractTestCase#getCompatibleDataSetLists(Object)} from resolving the rest, drops the
 * failure from the log, or breaks the exception chain when every {@code natureId} fails
 * (ERR-11).
 *
 * <p>The static field {@link CoreObjectManager} reads its service from is shared by every caller
 * in the JVM, so each test saves it before running and restores it afterward to avoid leaking
 * state into other tests.</p>
 */
class AbstractTestCaseTest {

    private static Field serviceField;

    private CoreObjectManagerService originalService;
    private ObjectManager<DataSetList> dsMan;
    private ListAppender<ILoggingEvent> logAppender;
    private ch.qos.logback.classic.Logger logger;

    @BeforeAll
    static void resolveField() throws Exception {
        serviceField = CoreObjectManager.class.getDeclaredField("staticCoreObjectManagerService");
        serviceField.setAccessible(true);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() throws Exception {
        originalService = (CoreObjectManagerService) serviceField.get(null);

        CoreObjectManagerService service = mock(CoreObjectManagerService.class);
        dsMan = mock(ObjectManager.class);
        when(service.getManager(DataSetList.class)).thenReturn(dsMan);
        serviceField.set(null, service);

        logAppender = new ListAppender<>();
        logAppender.start();
        logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(AbstractTestCase.class);
        logger.addAppender(logAppender);
    }

    @AfterEach
    void tearDown() throws Exception {
        serviceField.set(null, originalService);
        logger.detachAppender(logAppender);
    }

    @Test
    @DisplayName("Should resolve the remaining natureId after another one throws, instead of stopping the loop")
    void continuesToTheRemainingNatureId_whenOneThrows() {
        CallChain testCase = new CallChain();
        testCase.setCompatibleDataSetListIds(Set.of("broken", "ok"));
        DataSetList resolved = mock(DataSetList.class);
        when(dsMan.getByNatureId(eq("broken"), any())).thenThrow(new RuntimeException("dataset service down"));
        doReturn(List.of(resolved)).when(dsMan).getByNatureId(eq("ok"), any());

        Set<DataSetList> result = testCase.getCompatibleDataSetLists("project-1");

        assertEquals(Set.of(resolved), result);
        verify(dsMan).getByNatureId(eq("broken"), eq("project-1"));
        verify(dsMan).getByNatureId(eq("ok"), eq("project-1"));
    }

    @Test
    @DisplayName("Should log the failing natureId with its exception, instead of discarding it silently")
    void logsTheFailure_insteadOfDiscardingItSilently() {
        CallChain testCase = new CallChain();
        testCase.setCompatibleDataSetListIds(Set.of("broken", "ok"));
        DataSetList resolved = mock(DataSetList.class);
        when(dsMan.getByNatureId(eq("broken"), any())).thenThrow(new RuntimeException("dataset service down"));
        doReturn(List.of(resolved)).when(dsMan).getByNatureId(eq("ok"), any());

        testCase.getCompatibleDataSetLists("project-1");

        assertTrue(logAppender.list.stream().anyMatch(event -> event.getLevel() == Level.WARN
                        && event.getFormattedMessage().contains("broken")
                        && event.getThrowableProxy() != null),
                "the failing natureId must be logged, with its exception, not silently discarded");
    }

    @Test
    @DisplayName("Should throw with the caught exception itself as the cause, when every natureId fails")
    void preservesTheFullCauseChain_whenEveryNatureIdFails() {
        CallChain testCase = new CallChain();
        testCase.setCompatibleDataSetListIds(Set.of("broken"));
        RuntimeException failure = new RuntimeException("dataset service down");
        when(dsMan.getByNatureId(eq("broken"), any())).thenThrow(failure);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> testCase.getCompatibleDataSetLists("project-1"));

        assertEquals("dataset service down", thrown.getMessage());
        assertSame(failure, thrown.getCause(),
                "the caught exception itself must become the cause, not its own cause (usually null)");
    }

    @Test
    @DisplayName("Should log every failing natureId, not only the one that ends up as the cause")
    void logsEveryFailure_whenEveryNatureIdFails() {
        CallChain testCase = new CallChain();
        testCase.setCompatibleDataSetListIds(Set.of("broken-1", "broken-2"));
        RuntimeException sharedFailure = new RuntimeException("dataset service down");
        when(dsMan.getByNatureId(eq("broken-1"), any())).thenThrow(sharedFailure);
        when(dsMan.getByNatureId(eq("broken-2"), any())).thenThrow(sharedFailure);

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> testCase.getCompatibleDataSetLists("project-1"));

        assertSame(sharedFailure, thrown.getCause());
        long warnCount = logAppender.list.stream().filter(event -> event.getLevel() == Level.WARN).count();
        assertEquals(2, warnCount, "both natureIds must be attempted and logged, not just the first");
    }

    @Test
    @DisplayName("Should return the results that resolved, not throw, when at least one natureId succeeds")
    void returnsThePartialResult_whenAtLeastOneNatureIdSucceeds() {
        CallChain testCase = new CallChain();
        testCase.setCompatibleDataSetListIds(Set.of("broken", "ok"));
        DataSetList resolved = mock(DataSetList.class);
        when(dsMan.getByNatureId(eq("broken"), any())).thenThrow(new RuntimeException("dataset service down"));
        doReturn(List.of(resolved)).when(dsMan).getByNatureId(eq("ok"), any());

        Set<DataSetList> result = testCase.getCompatibleDataSetLists("project-1");

        assertEquals(Set.of(resolved), result);
    }
}
