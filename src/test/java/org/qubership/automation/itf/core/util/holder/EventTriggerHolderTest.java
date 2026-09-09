package org.qubership.automation.itf.core.util.holder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.jpa.system.stub.Listener;

/**
 * Covers {@link EventTriggerHolder#add(Listener, boolean)} and {@link EventTriggerHolder#remove(Listener, boolean)}.
 *
 * <p>{@link EventTriggerHolderRaceTest} carries the concurrency regression this class does not repeat: the
 * {@code concurrentAddsForEqualButDistinctSituationIdsRegisterEveryListener} test below re-runs that scenario
 * through Mockito-built listeners as a second, independent check that {@code situationOnStartListenersMap} is
 * keyed by value rather than by the identity of {@link Listener#getSituationId()}.</p>
 */
class EventTriggerHolderTest {

    private static final AtomicLong SITUATION_ID_SEQ = new AtomicLong(1);

    private final EventTriggerHolder holder = EventTriggerHolder.getInstance();

    private static BigInteger nextSituationId() {
        return BigInteger.valueOf(SITUATION_ID_SEQ.getAndIncrement());
    }

    private static Listener mockListener(Object id, Object situationId) {
        Listener listener = mock(Listener.class);
        when(listener.getId()).thenReturn(id);
        when(listener.getSituationId()).thenReturn(situationId);
        return listener;
    }

    @Test
    void addStartListenerRegistersInBothMaps() {
        BigInteger situationId = nextSituationId();
        Listener listener = mockListener("listener-1", situationId);

        holder.add(listener, true);

        assertEquals(listener, holder.get("listener-1"));
        assertEquals(List.of(listener), holder.getOnStartSituationListeners(situationId));
    }

    @Test
    void addNonStartListenerSkipsSituationMap() {
        BigInteger situationId = nextSituationId();
        Listener listener = mockListener("listener-2", situationId);

        holder.add(listener, false);

        assertEquals(listener, holder.get("listener-2"));
        assertNull(holder.getOnStartSituationListeners(situationId));
    }

    @Test
    void removeStartListenerClearsBothMaps() {
        BigInteger situationId = nextSituationId();
        Listener listener = mockListener("listener-3", situationId);
        holder.add(listener, true);

        holder.remove(listener, true);

        assertNull(holder.get("listener-3"));
        assertTrue(holder.getOnStartSituationListeners(situationId).isEmpty());
    }

    @Test
    void removeNullListenerIsNoOp() {
        holder.remove(null, true);
    }

    @Test
    void removeUnregisteredSituationIsNoOp() {
        BigInteger situationId = nextSituationId();
        Listener listener = mockListener("listener-4", situationId);

        holder.remove(listener, true);

        assertNull(holder.getOnStartSituationListeners(situationId));
    }

    @Test
    void concurrentAddsForEqualButDistinctSituationIdsRegisterEveryListener() throws Exception {
        final int threads = 8;
        final int perThread = 500;
        final String situationValue = nextSituationId().toString();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            final int base = t * perThread;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        BigInteger situationId = new BigInteger(situationValue);
                        holder.add(mockListener("listener-" + (base + i), situationId), true);
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown();
        assertTrue(done.await(60, TimeUnit.SECONDS));
        pool.shutdownNow();

        List<Listener> registered = holder.getOnStartSituationListeners(new BigInteger(situationValue));
        assertEquals(threads * perThread, registered.size());
    }
}
