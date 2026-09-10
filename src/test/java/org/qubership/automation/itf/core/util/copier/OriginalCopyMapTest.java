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

package org.qubership.automation.itf.core.util.copier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.common.Storable;

/**
 * Covers {@link OriginalCopyMap}.
 *
 * <p>Every test uses a fresh, random session key: {@link OriginalCopyMap} is a JVM-wide
 * singleton, so a fixed key would leak state between tests and between runs of this class.</p>
 */
class OriginalCopyMapTest {

    @Test
    void putThenGetReturnsTheStoredCopy() {
        String key = UUID.randomUUID().toString();
        Storable copy = mock(Storable.class);

        OriginalCopyMap.getInstance().put(key, "orig-1", copy);

        assertSame(copy, OriginalCopyMap.getInstance().get(key, "orig-1"));
        OriginalCopyMap.getInstance().clear(key);
    }

    @Test
    void getReturnsNullForAnUnknownSessionOrOriginalId() {
        String key = UUID.randomUUID().toString();

        assertNull(OriginalCopyMap.getInstance().get(key, "missing"));

        OriginalCopyMap.getInstance().put(key, "orig-1", mock(Storable.class));
        assertNull(OriginalCopyMap.getInstance().get(key, "other-id"));

        OriginalCopyMap.getInstance().clear(key);
    }

    @Test
    void clearRemovesOnlyTheGivenSession() {
        String keyA = UUID.randomUUID().toString();
        String keyB = UUID.randomUUID().toString();
        Storable copyB = mock(Storable.class);
        OriginalCopyMap.getInstance().put(keyA, "id", mock(Storable.class));
        OriginalCopyMap.getInstance().put(keyB, "id", copyB);

        OriginalCopyMap.getInstance().clear(keyA);

        assertNull(OriginalCopyMap.getInstance().get(keyA, "id"));
        assertSame(copyB, OriginalCopyMap.getInstance().get(keyB, "id"));
        OriginalCopyMap.getInstance().clear(keyB);
    }

    /**
     * Regression test for CONC-08: the outer cache used to be a plain {@code HashMap}, so
     * concurrent {@code put} calls for distinct session keys, coming from distinct copy/move
     * requests, raced on the same unsynchronized bucket structure and could silently drop a
     * session's entry.
     */
    @Test
    void concurrentPutsForDistinctSessionsNeverLoseASession() throws Exception {
        int threads = 8;
        int sessionsPerThread = 2000;
        List<String> keys = Collections.synchronizedList(new ArrayList<>(threads * sessionsPerThread));
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> futures = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            futures.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < sessionsPerThread; i++) {
                    String key = UUID.randomUUID().toString();
                    keys.add(key);
                    OriginalCopyMap.getInstance().put(key, "id", mock(Storable.class));
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> future : futures) {
            future.get(30, TimeUnit.SECONDS);
        }
        pool.shutdownNow();

        try {
            long missing = keys.stream().filter(key -> OriginalCopyMap.getInstance().get(key, "id") == null).count();
            assertEquals(0, missing, missing + " of " + keys.size() + " sessions were lost");
        } finally {
            keys.forEach(key -> OriginalCopyMap.getInstance().clear(key));
        }
    }
}
