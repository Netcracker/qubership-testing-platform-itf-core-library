package org.qubership.automation.itf.core.util.holder;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.jpa.system.stub.Listener;

/**
 * Probe: EventTriggerHolder.add(listener, true) synchronizes on listener.getSituationId().
 * In production each SituationEventTrigger is loaded in its own Hibernate session, so two
 * triggers of the same situation carry equal-but-distinct BigInteger situation ids.
 * synchronized(id) then locks two different monitors, and the getOrDefault/add/put sequence
 * on situationOnStartListenersMap loses registrations.
 */
public class EventTriggerHolderRaceTest {
    private record StubListener(Object id, BigInteger situationId) implements Listener {
        @Override
        public Object getId() {
            return id;
        }

        @Override
        public Object getSituationId() {
            return situationId;
        }
    }

    @Test
    public void concurrentAddsForSameSituationLoseListeners() throws Exception {
        final int threads = 8;
        final int perThread = 500;
        EventTriggerHolder holder = EventTriggerHolder.getInstance();
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        for (int t = 0; t < threads; t++) {
            final int base = t * perThread;
            pool.submit(() -> {
                try {
                    start.await();
                    for (int i = 0; i < perThread; i++) {
                        // equal-but-distinct BigInteger per call: same value, different identity,
                        // exactly what two Hibernate sessions produce for one situation id
                        BigInteger situationId = new BigInteger("424242424242424242424242");
                        holder.add(new StubListener("L" + (base + i), situationId), true);
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

        List<Listener> registered =
                holder.getOnStartSituationListeners(new BigInteger("424242424242424242424242"));
        int got = registered == null ? 0 : registered.size();
        assertEquals(threads * perThread, got,
                "listeners lost: expected " + (threads * perThread) + ", got " + got);
    }
}
