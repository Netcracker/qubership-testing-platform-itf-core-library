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

package org.qubership.automation.itf.core.util.generator.id;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.hibernate.HibernateException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qubership.automation.itf.core.util.generator.tools.InternalDataBaseSqlExecutor;

/**
 * The business and reporting id spaces used to refill under one shared class monitor, so a slow
 * database round trip filling one id space blocked every caller of the other. Each id space must
 * now refill under a lock of its own, and the database round trip itself must not run under any
 * lock.
 */
@ExtendWith(MockitoExtension.class)
class UniqueIdGeneratorTest {

    @Mock
    private InternalDataBaseSqlExecutor internalDataBaseSqlExecutor;

    @BeforeEach
    void setUp() throws Exception {
        UniqueIdGenerator.INTERNAL_DATABASE_SQL_EXECUTOR = internalDataBaseSqlExecutor;
        clearPool("IDS");
        clearPool("IDS_REPORTING");
    }

    @AfterEach
    void tearDown() throws Exception {
        clearPool("IDS");
        clearPool("IDS_REPORTING");
        UniqueIdGenerator.INTERNAL_DATABASE_SQL_EXECUTOR = null;
    }

    /**
     * Empties the pre-fetched queue backing the named {@code IdPool} field, so ids left over from
     * one test cannot satisfy the next one without a database call.
     */
    private static void clearPool(String poolFieldName) throws Exception {
        Field poolField = UniqueIdGenerator.class.getDeclaredField(poolFieldName);
        poolField.setAccessible(true);
        Object pool = poolField.get(null);
        Field queueField = pool.getClass().getDeclaredField("queue");
        queueField.setAccessible(true);
        ((Queue<?>) queueField.get(pool)).clear();
    }

    @Test
    void generateRefillsTheBusinessPoolFromTheDatabaseWhenItIsEmpty() throws SQLException {
        when(internalDataBaseSqlExecutor.selectArrayViaNonParameterizedFunction(true))
                .thenReturn(List.of(BigInteger.ONE, BigInteger.valueOf(2)));

        assertEquals(BigInteger.ONE, UniqueIdGenerator.generate());
        assertEquals(BigInteger.valueOf(2), UniqueIdGenerator.generate());
        verify(internalDataBaseSqlExecutor, times(1)).selectArrayViaNonParameterizedFunction(true);
    }

    @Test
    void generateReportingIdQueriesTheReportingIdSpaceOnly() throws SQLException {
        when(internalDataBaseSqlExecutor.selectArrayViaNonParameterizedFunction(false))
                .thenReturn(List.of(BigInteger.TEN));

        assertEquals(BigInteger.TEN, UniqueIdGenerator.generateReportingId());
        verify(internalDataBaseSqlExecutor).selectArrayViaNonParameterizedFunction(false);
        verify(internalDataBaseSqlExecutor, never()).selectArrayViaNonParameterizedFunction(true);
    }

    @Test
    void generateWrapsAnSqlExceptionFromTheRefillAsAHibernateException() throws SQLException {
        when(internalDataBaseSqlExecutor.selectArrayViaNonParameterizedFunction(true))
                .thenThrow(new SQLException("connection refused"));

        assertThrows(HibernateException.class, UniqueIdGenerator::generate);
    }

    @Test
    void reportingIdGenerationIsNotBlockedByASlowBusinessRefill() throws Exception {
        CountDownLatch businessRefillStarted = new CountDownLatch(1);
        CountDownLatch releaseBusinessRefill = new CountDownLatch(1);
        when(internalDataBaseSqlExecutor.selectArrayViaNonParameterizedFunction(true)).thenAnswer(invocation -> {
            businessRefillStarted.countDown();
            releaseBusinessRefill.await(10, TimeUnit.SECONDS);
            return List.of(BigInteger.ONE);
        });
        when(internalDataBaseSqlExecutor.selectArrayViaNonParameterizedFunction(false))
                .thenReturn(List.of(BigInteger.TEN));

        Thread businessThread = new Thread(UniqueIdGenerator::generate, "business-ids");
        businessThread.start();
        assertTrue(businessRefillStarted.await(5, TimeUnit.SECONDS), "the business refill should have started");

        CountDownLatch reportingDone = new CountDownLatch(1);
        Thread reportingThread = new Thread(() -> {
            UniqueIdGenerator.generateReportingId();
            reportingDone.countDown();
        }, "reporting-ids");
        reportingThread.start();

        assertTrue(reportingDone.await(5, TimeUnit.SECONDS),
                "reporting id generation must not block behind an unrelated business-id refill");

        releaseBusinessRefill.countDown();
        businessThread.join(TimeUnit.SECONDS.toMillis(10));
    }

    @Test
    void concurrentCallersRefillingTheSameEmptyPoolEachGetADistinctId() throws Exception {
        int callerCount = 8;
        when(internalDataBaseSqlExecutor.selectArrayViaNonParameterizedFunction(true)).thenAnswer(invocation -> {
            TimeUnit.MILLISECONDS.sleep(20);
            return List.of(BigInteger.valueOf(System.nanoTime()));
        });

        CountDownLatch start = new CountDownLatch(1);
        Set<Serializable> ids = Collections.synchronizedSet(new HashSet<>());
        List<Thread> callers = new ArrayList<>();
        for (int i = 0; i < callerCount; i++) {
            Thread caller = new Thread(() -> {
                await(start);
                ids.add(UniqueIdGenerator.generate());
            });
            callers.add(caller);
            caller.start();
        }
        start.countDown();
        for (Thread caller : callers) {
            caller.join(TimeUnit.SECONDS.toMillis(10));
        }

        assertEquals(callerCount, ids.size(), "every caller must receive a distinct, non-null id");
    }

    private static void await(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
