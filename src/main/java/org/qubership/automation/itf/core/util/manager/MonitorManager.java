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

public class MonitorManager {

    private static final MonitorManager INSTANCE = new MonitorManager();
    private static final Logger LOGGER = LoggerFactory.getLogger(MonitorManager.class);
    /*
        expireAfterWrite(1, TimeUnit.HOURS) setting is potentially erroneous:
            - Execution of durability test cases can be longer,
            so entry will be invalidated earlier than execution is finished,
            so AtpCallchainExecutor#execute thread, waiting for notification, never receive it.
     */
    // TODO: based on above analysis, remove .expireAfterWrite or increase duration or do smth. else
    private final LoadingCache<String, Object> monitors = CacheBuilder.newBuilder()
            .expireAfterAccess(20 * 60 * 1000 + 30000, TimeUnit.MILLISECONDS)
            .build(new CacheLoader<String, Object>() {
                @Override
                public Object load(@Nonnull String id) {
                    return new Object();
                }
            });

    public static MonitorManager getInstance() {
        return INSTANCE;
    }

    private MonitorManager() {
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
