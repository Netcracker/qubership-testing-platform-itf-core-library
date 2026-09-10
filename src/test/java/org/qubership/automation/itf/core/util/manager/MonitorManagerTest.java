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

package org.qubership.automation.itf.core.util.manager;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.google.common.cache.LoadingCache;

/**
 * Covers {@link MonitorManager#get(String)}.
 *
 * <p>Its cache evicts an idle key's monitor through {@code expireAfterAccess}. The
 * {@code removalListener} must notify whoever is synchronized on the evicted monitor as part of
 * the eviction itself, so a caller blocked in {@code wait()} on it still wakes up after a later
 * {@code get()} for the same key hands out a different {@code Object} (CONC-07): without that
 * listener, a notify issued after expiry landed on the freshly loaded, unrelated instance, and the
 * original waiter's notification was lost for good.</p>
 *
 * <p>{@code monitors} is swapped, through reflection, for a cache from {@link
 * MonitorManager#buildCache(long)} — the exact wiring the singleton itself uses — built with a
 * short TTL in place of the real ~20.5 minutes, so expiry does not have to be waited out for real;
 * the original cache is restored afterward since {@link MonitorManager} is a process-wide
 * singleton.</p>
 */
class MonitorManagerTest {

    private static Field monitorsField;

    private LoadingCache<String, Object> originalMonitors;

    @BeforeAll
    static void resolveField() throws Exception {
        monitorsField = MonitorManager.class.getDeclaredField("monitors");
        monitorsField.setAccessible(true);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void saveOriginalCache() throws Exception {
        originalMonitors = (LoadingCache<String, Object>) monitorsField.get(MonitorManager.getInstance());
    }

    @AfterEach
    void restoreOriginalCache() throws Exception {
        monitorsField.set(MonitorManager.getInstance(), originalMonitors);
    }

    @Test
    void expiryReissuesADifferentMonitorForTheSameKey() throws Exception {
        monitorsField.set(MonitorManager.getInstance(), MonitorManager.buildCache(60));
        String key = "ctx-" + System.nanoTime();

        Object first = MonitorManager.getInstance().get(key);
        Thread.sleep(240);
        Object second = MonitorManager.getInstance().get(key);

        assertNotSame(first, second, "the cache must actually have evicted and reissued the monitor");
    }

    /**
     * Regression test for CONC-07: without the removalListener, the notify below lands on the
     * freshly loaded {@code notifierMonitor}, while {@code waiter} stays blocked on the
     * {@code Object} it originally received, so its notification is lost and it only returns once
     * its own {@code wait(2000)} call times out on its own.
     */
    @Test
    void notifyStillReachesAWaiterWhoseMonitorExpiredWhileItWasWaiting() throws Exception {
        long ttlMillis = 60;
        monitorsField.set(MonitorManager.getInstance(), MonitorManager.buildCache(ttlMillis));
        String key = "ctx-" + System.nanoTime();

        Object waiterMonitor = MonitorManager.getInstance().get(key);
        CountDownLatch woken = new CountDownLatch(1);
        AtomicBoolean timedOut = new AtomicBoolean(true);
        Thread waiter = new Thread(() -> {
            synchronized (waiterMonitor) {
                long start = System.nanoTime();
                try {
                    waiterMonitor.wait(2000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                timedOut.set(System.nanoTime() - start >= TimeUnit.MILLISECONDS.toNanos(1500));
            }
            woken.countDown();
        });
        waiter.setDaemon(true);
        waiter.start();

        Thread.sleep(ttlMillis * 4); // let the entry actually expire while the waiter still holds it

        // Same call pattern as TCContextService#notifyATP: fetch, then notify whatever came back.
        // The fetch itself is what makes Guava notice the expiry and run the removalListener.
        Object notifierMonitor = MonitorManager.getInstance().get(key);
        synchronized (notifierMonitor) {
            notifierMonitor.notify();
        }

        assertTrue(woken.await(1, TimeUnit.SECONDS), "waiter never woke up");
        assertNotSame(waiterMonitor, notifierMonitor, "the scenario requires the entry to have actually expired");
        assertFalse(timedOut.get(), "waiter only woke via its own wait() timeout, not a real notify");
    }
}
