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

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

public class MonitorManager {

    private static final MonitorManager INSTANCE = new MonitorManager();
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManager.class);
    private static final long MONITOR_TTL_MILLIS = 20 * 60 * 1000 + 30000;

    private final LoadingCache<String, Object> monitors = buildCache(MONITOR_TTL_MILLIS);

    public static MonitorManager getInstance() {
        return INSTANCE;
    }

    private MonitorManager() {
    }

    /*
        expireAfterAccess releases a context's monitor once it has gone idle long enough;
        TCContextService#updateLastAccess re-fetches it on every update, so an active run keeps
        resetting the clock and only a truly stalled context ever expires. The removalListener
        notifies whoever is synchronized on the monitor as part of the eviction itself, the same
        way LockProvider does, so a waiter is never left blocked on an Object that nothing will
        notify again once its entry is gone from the cache.

        Package-visible, and parameterized on the TTL, so a test can exercise this exact wiring
        with a short duration instead of waiting out the real ~20.5 minutes.
     */
    static LoadingCache<String, Object> buildCache(long ttlMillis) {
        return CacheBuilder.newBuilder()
                .expireAfterAccess(ttlMillis, TimeUnit.MILLISECONDS)
                .removalListener((RemovalListener<String, Object>) notification -> {
                    synchronized (notification.getValue()) {
                        notification.getValue().notify();
                    }
                })
                .build(new CacheLoader<String, Object>() {
                    @Override
                    public Object load(@Nonnull String id) {
                        return new Object();
                    }
                });
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Object get(String key) {
        try {
            return monitors.get(key);
        } catch (ExecutionException e) {
            LOGGER.error("Monitor exception", e);
            throw new RuntimeException(e);
        }
    }
}
