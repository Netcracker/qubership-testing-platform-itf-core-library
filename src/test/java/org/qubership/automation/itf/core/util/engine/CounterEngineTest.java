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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.DateUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.counter.Counter;
import org.qubership.automation.itf.core.model.counter.CounterImpl;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.manager.CoreObjectManagerService;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * Covers {@link CounterEngine#nextIndex(Set, String)}.
 *
 * <p>{@link CoreObjectManager} and {@link TxExecutor} are wired to real, plain Mockito mocks
 * through their actual static fields, via reflection, rather than through {@link
 * org.mockito.MockedStatic}: a {@code MockedStatic} only intercepts calls made from the thread
 * that created it, so a worker thread in the concurrency tests below would see the real,
 * unmocked classes and fail with a {@link NullPointerException} instead of exercising {@link
 * CounterEngine}.</p>
 */
@SuppressWarnings({"unchecked", "rawtypes"})
class CounterEngineTest {

    private static Field counterEngineInstanceField;
    private static Field coreObjectManagerServiceField;
    private static Field txExecutorInstanceField;

    private CounterEngine originalCounterEngineInstance;
    private CoreObjectManagerService originalCoreObjectManagerService;
    private PlatformTransactionManager originalTxExecutorInstance;

    private final List<Counter> createdCounters = Collections.synchronizedList(new ArrayList<>());

    private CounterEngine engine;

    @BeforeAll
    static void resolveFields() throws Exception {
        counterEngineInstanceField = CounterEngine.class.getDeclaredField("instance");
        counterEngineInstanceField.setAccessible(true);
        coreObjectManagerServiceField = CoreObjectManager.class.getDeclaredField("staticCoreObjectManagerService");
        coreObjectManagerServiceField.setAccessible(true);
        txExecutorInstanceField = TxExecutor.class.getDeclaredField("INSTANCE");
        txExecutorInstanceField.setAccessible(true);
    }

    @BeforeEach
    void setUp() throws Exception {
        originalCounterEngineInstance = (CounterEngine) counterEngineInstanceField.get(null);
        counterEngineInstanceField.set(null, null);
        originalCoreObjectManagerService = (CoreObjectManagerService) coreObjectManagerServiceField.get(null);
        originalTxExecutorInstance = (PlatformTransactionManager) txExecutorInstanceField.get(null);
        txExecutorInstanceField.set(null, null);

        CoreObjectManagerService coreObjectManagerService = mock(CoreObjectManagerService.class);
        ObjectManager<Counter> counterManager = mock(ObjectManager.class);
        when(coreObjectManagerService.getManager(any())).thenReturn((ObjectManager) counterManager);
        when(counterManager.getAll()).thenReturn(Collections.emptyList());
        when(counterManager.create()).thenAnswer(invocation -> {
            Counter counter = spy(new CounterImpl());
            doNothing().when(counter).store();
            doReturn(Collections.emptyList()).when(counter).remove();
            createdCounters.add(counter);
            return counter;
        });
        coreObjectManagerServiceField.set(null, coreObjectManagerService);

        PlatformTransactionManager transactionManager = mock(PlatformTransactionManager.class);
        TransactionStatus transactionStatus = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any())).thenReturn(transactionStatus);
        TxExecutor.init(transactionManager);

        engine = CounterEngine.getInstance();
    }

    @AfterEach
    void tearDown() throws Exception {
        counterEngineInstanceField.set(null, originalCounterEngineInstance);
        coreObjectManagerServiceField.set(null, originalCoreObjectManagerService);
        txExecutorInstanceField.set(null, originalTxExecutorInstance);
    }

    @Test
    void nextIndexReturnsNullAndSkipsAllStorageWhenFormatIsNull() throws Exception {
        String result = engine.nextIndex(Set.of("owner"), null);

        assertNull(result);
        assertTrue(createdCounters.isEmpty());
    }

    @Test
    void nextIndexCreatesACounterOnFirstCallForNewOwners() throws Exception {
        String result = engine.nextIndex(Set.of("owner"), "00");

        assertEquals("01", result);
        assertEquals(1, createdCounters.size());
    }

    @Test
    void nextIndexIncrementsTheSameCounterOnSubsequentCallsForTheSameOwners() throws Exception {
        Set<Object> owners = Set.of("owner");

        engine.nextIndex(owners, "00");
        String second = engine.nextIndex(owners, "00");
        String third = engine.nextIndex(owners, "00");

        assertEquals("02", second);
        assertEquals("03", third);
        assertEquals(1, createdCounters.size(), "a single counter must serve every call for the same owners");
    }

    /**
     * Regression test for the day-boundary rule the constructor states (isSameDay against
     * {@link Counter#getDate()}) but that {@link CounterEngine#nextIndex(Set, String)} did not
     * enforce: it kept incrementing a counter dated before today until the next JVM restart.
     */
    @Test
    void nextIndexReplacesAStaleCounterInsteadOfContinuingItsIndex() throws Exception {
        Set<Object> owners = Set.of("owner");
        engine.nextIndex(owners, "00");
        Counter yesterdayCounter = createdCounters.get(0);
        yesterdayCounter.setDate(DateUtils.addDays(Calendar.getInstance().getTime(), -1));

        String result = engine.nextIndex(owners, "00");

        assertEquals("01", result, "a counter from a previous day must restart at 1, not continue its old index");
        assertEquals(2, createdCounters.size(), "the stale counter must be replaced by a freshly created one");
        verify(yesterdayCounter).remove();
    }

    @Test
    void concurrentNextIndexCallsForTheSameOwnersNeverDuplicateOrLoseAnIndex() throws Exception {
        Set<Object> owners = Set.of("shared");
        int threads = 8;
        int callsPerThread = 50;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<String>> futures = new ArrayList<>();
        for (int i = 0; i < threads * callsPerThread; i++) {
            futures.add(pool.submit(() -> {
                start.await();
                return engine.nextIndex(owners, "0000");
            }));
        }
        start.countDown();

        Set<String> results = new HashSet<>();
        for (Future<String> future : futures) {
            results.add(future.get(10, TimeUnit.SECONDS));
        }
        pool.shutdownNow();

        assertEquals(threads * callsPerThread, results.size(), "a lost or duplicated index was returned");
    }

    /**
     * Regression test for {@code synchronized (counterMap)} wrapping the whole method, including
     * the {@link TxExecutor#execute} DB round trip: any two owners serialized on that one monitor,
     * so a slow call for one owner stalled every other owner's counter generation JVM-wide.
     */
    @Test
    void nextIndexForOneOwnerDoesNotWaitOnAnotherOwnersInFlightStore() throws Exception {
        Set<Object> ownersA = Set.of("A");
        Set<Object> ownersB = Set.of("B");
        engine.nextIndex(ownersA, "00");
        engine.nextIndex(ownersB, "00");
        Counter counterA = createdCounters.get(0);

        CountDownLatch aReachedStore = new CountDownLatch(1);
        CountDownLatch releaseA = new CountDownLatch(1);
        doAnswer(invocation -> {
            aReachedStore.countDown();
            releaseA.await(5, TimeUnit.SECONDS);
            return null;
        }).when(counterA).store();

        ExecutorService pool = Executors.newSingleThreadExecutor();
        try {
            Future<String> aResult = pool.submit(() -> engine.nextIndex(ownersA, "00"));
            assertTrue(aReachedStore.await(2, TimeUnit.SECONDS), "owner A never reached its DB round trip");

            String bResult = assertTimeoutPreemptively(Duration.ofSeconds(3),
                    () -> engine.nextIndex(ownersB, "00"),
                    "owner B's call waited on owner A's in-flight store(), a global lock is back");
            assertEquals("02", bResult);

            releaseA.countDown();
            assertEquals("02", aResult.get(5, TimeUnit.SECONDS));
        } finally {
            releaseA.countDown();
            pool.shutdownNow();
        }
    }
}
