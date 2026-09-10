package org.qubership.automation.itf.core.util.ei.deserialize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.common.Storable;

/**
 * Covers {@link DeserializedEntitiesCache}: a session record must be retrievable under concurrent creation and
 * releasable through {@link DeserializedEntitiesCache#removeSessionRecord}.
 *
 * <p>{@link DeserializedEntitiesCache#getInstance()} returns one instance for the whole JVM, shared with every
 * other test, so each test uses session ids from its own {@link #nextSessionId()} range and removes what it
 * created in {@link #cleanUpCreatedSessions()}.</p>
 *
 * <p>{@code concurrentSessionCreationLosesNoRecord} is a regression test for a plain {@code HashMap} backing
 * {@code sessionToStorable}: under concurrent {@link DeserializedEntitiesCache#createSessionRecord}, a
 * non-thread-safe map silently drops entries, and {@link DeserializedEntitiesCache#getCacheBySessionId} then
 * returns {@code null} mid-import for a session that was, in fact, created.</p>
 */
class DeserializedEntitiesCacheTest {

    private static final AtomicLong SESSION_ID_SEQ = new AtomicLong(1);

    private final DeserializedEntitiesCache cache = DeserializedEntitiesCache.getInstance();
    private final List<BigInteger> createdSessionIds = new CopyOnWriteArrayList<>();

    private BigInteger nextSessionId() {
        BigInteger sessionId = BigInteger.valueOf(SESSION_ID_SEQ.getAndIncrement());
        createdSessionIds.add(sessionId);
        return sessionId;
    }

    @AfterEach
    void cleanUpCreatedSessions() {
        createdSessionIds.forEach(cache::removeSessionRecord);
        createdSessionIds.clear();
    }

    @Test
    void createSessionRecordIsRetrievableByItsSessionId() {
        BigInteger sessionId = nextSessionId();
        BigInteger projectId = BigInteger.valueOf(42);

        ImportedDataCache created = cache.createSessionRecord(sessionId, projectId);

        assertSame(created, cache.getCacheBySessionId(sessionId));
        assertEquals(projectId, cache.getCacheBySessionId(sessionId).getProjectId());
    }

    @Test
    void getCacheBySessionIdReturnsNullForAnUnknownSession() {
        assertNull(cache.getCacheBySessionId(nextSessionId()));
    }

    @Test
    void removeSessionRecordReleasesTheSession() {
        BigInteger sessionId = nextSessionId();
        cache.createSessionRecord(sessionId, BigInteger.ONE);
        assertNotNull(cache.getCacheBySessionId(sessionId));

        cache.removeSessionRecord(sessionId);

        assertNull(cache.getCacheBySessionId(sessionId));
    }

    @Test
    void removeSessionRecordIsANoOpForAnUnknownSession() {
        cache.removeSessionRecord(nextSessionId());
    }

    @Test
    void aStorableRegisteredDuringImportIsFoundByIdAfterRelease() {
        BigInteger sessionId = nextSessionId();
        BigInteger storableId = BigInteger.valueOf(7);
        Storable storable = mock(Storable.class);
        when(storable.getID()).thenReturn(storableId);
        cache.createSessionRecord(sessionId, BigInteger.ONE).put(storableId, storable);

        assertSame(storable, cache.getCacheBySessionId(sessionId).getById(storableId));

        cache.removeSessionRecord(sessionId);

        assertNull(cache.getCacheBySessionId(sessionId));
    }

    @Test
    void concurrentSessionCreationLosesNoRecord() throws InterruptedException {
        final int threads = 8;
        final int perThread = 500;
        List<BigInteger> sessionIds = IntStream.range(0, threads * perThread)
                .mapToObj(i -> nextSessionId())
                .collect(Collectors.toList());

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            final int base = t * perThread;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        BigInteger sessionId = sessionIds.get(base + i);
                        cache.createSessionRecord(sessionId, BigInteger.ONE);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        done.await(60, TimeUnit.SECONDS);
        pool.shutdownNow();

        long missing = sessionIds.stream().filter(id -> cache.getCacheBySessionId(id) == null).count();
        assertEquals(0, missing, "session records lost: " + missing + " of " + sessionIds.size());
    }
}
