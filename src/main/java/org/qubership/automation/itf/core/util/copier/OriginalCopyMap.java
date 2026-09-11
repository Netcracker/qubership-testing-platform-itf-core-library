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

import java.util.Map;
import java.util.concurrent.ConcurrentMap;

import org.qubership.automation.itf.core.model.common.Storable;

import com.google.common.collect.Maps;

public class OriginalCopyMap {
    /*
        Keyed by session id, one entry per copy/move request in flight; different sessions are
        written from different request threads, so the outer map has to be concurrency-safe on its
        own (EventTriggerHolder.listenerMap uses the same computeIfAbsent shape for the same
        reason). The per-session inner map does not need it: only the single thread running that
        session's StorableCopier ever touches it.
     */
    private final ConcurrentMap<Object, Map<Object, Storable>> cache = Maps.newConcurrentMap();

    private static OriginalCopyMap instance = new OriginalCopyMap();

    public static OriginalCopyMap getInstance() {
        return instance;
    }

    private OriginalCopyMap() {
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void put(Object key, Object originalId, Storable copy) {
        cache.computeIfAbsent(key, k -> Maps.newHashMap()).put(originalId, copy);
    }

    public Storable get(Object key, Object originalId) {
        Map<Object, Storable> objectStorableMap = cache.get(key);
        return objectStorableMap == null ? null : objectStorableMap.get(originalId);
    }

    public void clear(Object key) {
        cache.remove(key);
    }
}
