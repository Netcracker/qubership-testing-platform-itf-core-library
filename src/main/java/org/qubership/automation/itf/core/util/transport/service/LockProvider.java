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

package org.qubership.automation.itf.core.util.transport.service;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.qubership.automation.itf.core.util.constants.InstanceSettingsConstants.LOCK_PROVIDER_PROCESS_TIMEOUT;

import jakarta.annotation.Nonnull;

import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.config.ApplicationConfig;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.cache.RemovalListener;

public enum LockProvider {
    INSTANCE;
    private final int timeout = setTimeout(Integer.parseInt(
            ApplicationConfig.env.getProperty(LOCK_PROVIDER_PROCESS_TIMEOUT, "25000")), 25000);
    private LoadingCache<String, WeakValue> locks = CacheBuilder.newBuilder()
            .expireAfterWrite(timeout, MILLISECONDS)
            .removalListener((RemovalListener<String, WeakValue>) notification -> {
                synchronized (notification.getValue()) {
                    notification.getValue().notify();
                }
            }).build(new CacheLoader<String, WeakValue>() {
                @Override
                public WeakValue load(@Nonnull String id) {
                    return new WeakValue();
                }
            });

    /**
     * Method will take the {@link WeakValue} from cache by the key
     * if value is present, method will call {@link Cache#invalidate(Object)}
     * in the cache I've added the remove listener, which call notify for {@link WeakValue} object on Remove event.
     *
     * @param key - session key.
     */
    public void notify(@Nonnull String key) {
        WeakValue value = locks.getIfPresent(key);
        if (value != null) {
            locks.invalidate(key);
        }
    }

    /**
     * Method will create an object in {@link Cache} and call {@link #wait()} method
     * for this object to stop the current thread.
     * Until some object call {@link #notify(String)}
     *
     * @param sessionId - transport session id
     * @throws InterruptedException - exception
     */
    public void wait(@Nonnull String sessionId) throws InterruptedException {
        WeakValue lock = locks.getUnchecked(sessionId);
        synchronized (lock) {
            lock.wait(timeout);
        }
    }

    /**
     * Wait for a response message.
     *
     * @param sessionId - Session Uuid to wait a response,
     * @param interval - interval to wait, in milliseconds,
     * @return Response Message.
     * @throws InterruptedException - in case wait is interrupted.
     */
    public Message waitResponse(@Nonnull String sessionId, int interval) throws InterruptedException {
        return waitResponse(sessionId, interval, interval, 1.0f);
    }

    /**
     * Wait for a response message, fixed or exponential waits in the loop.
     *
     * @param sessionId - Session Uuid to wait a response,
     * @param interval - interval to wait inside loop, in milliseconds,
     * @param maxInterval - max interval to wait, in milliseconds,
     * @param multiplier - multiplier for exponential waits,
     * @return Response Message.
     * @throws InterruptedException - in case wait is interrupted.
     */
    public Message waitResponse(@Nonnull String sessionId, int interval, int maxInterval, float multiplier)
            throws InterruptedException {
        int checkedInterval = interval < 50 || interval >= timeout ? 500 : interval;
        float checkedMultiplier = Math.max(multiplier, 1.0f);
        int checkedMaxInterval = (checkedMultiplier == 1.0f) ? checkedInterval : maxInterval;
        return waitAndGetResponse(sessionId, checkedInterval, checkedMaxInterval, checkedMultiplier);
    }

    private Message waitAndGetResponse(@Nonnull String sessionId, int interval, int maxInterval, float multiplier)
            throws InterruptedException {
        Message response = SessionHandler.INSTANCE.getMessage(sessionId);
        if (response == null) {
            int elapsed = 0;
            int newInterval = interval;
            WeakValue lock = locks.getUnchecked(sessionId);
            synchronized (lock) {
                while (response == null) {
                    lock.wait(newInterval);
                    response = SessionHandler.INSTANCE.getMessage(sessionId);
                    elapsed += newInterval;
                    if (elapsed >= timeout) {
                        break;
                    }
                    newInterval = computeInterval(newInterval, maxInterval, multiplier);
                }
            }
        }
        return response;
    }

    /**
     * Just to initialize instance not while the 1st inbound message processing,
     * but near the activation of the 1st trigger.
     *
     * @return true.
     */
    public boolean init() {
        return true;
    }

    public void cleanupCache() {
        locks.cleanUp();
    }

    /**
     * Get configured value of the setting (in milliseconds), check it against 0.
     *
     * @param timeout - configured and parsed value.
     * @param defaultValue - default timeout value.
     * @return - checked value.
     */
    private int setTimeout(int timeout, int defaultValue) {
        return timeout > 0 ? timeout : defaultValue;
    }

    private int computeInterval(int oldInterval, int maxInterval, float multiplier) {
        if (multiplier == 1.0f) {
            return oldInterval;
        }
        int newInterval = Math.round(oldInterval * multiplier);
        return Math.min(newInterval, maxInterval);
    }
}
