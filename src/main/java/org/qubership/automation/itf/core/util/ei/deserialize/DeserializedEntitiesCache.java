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

package org.qubership.automation.itf.core.util.ei.deserialize;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeserializedEntitiesCache {

    private static volatile DeserializedEntitiesCache deserializedEntitiesCache;
    private final Map<BigInteger, ImportedDataCache> sessionToStorable = new ConcurrentHashMap<>();

    private DeserializedEntitiesCache() {
        if (deserializedEntitiesCache != null) {
            throw new RuntimeException("Use getInstance() method to get the single instance of this class.");
        }
    }

    /**
     * Get Instance of DeserializedEntitiesCache; initialize it if needed.
     *
     * @return DeserializedEntitiesCache instance
     */
    public static DeserializedEntitiesCache getInstance() {
        if (deserializedEntitiesCache == null) {
            synchronized (DeserializedEntitiesCache.class) {
                if (deserializedEntitiesCache == null) {
                    deserializedEntitiesCache = new DeserializedEntitiesCache();
                }
            }
        }
        return deserializedEntitiesCache;
    }

    public ImportedDataCache getCacheBySessionId(BigInteger sessionId) {
        return sessionToStorable.get(sessionId);
    }

    /**
     * Create a new ImportedDataCache object and link it with sessionId.
     *
     * @return ImportedDataCache object created
     */
    public ImportedDataCache createSessionRecord(BigInteger sessionId, BigInteger projectId) {
        ImportedDataCache importedDataCache = new ImportedDataCache();
        importedDataCache.setProjectId(projectId);
        sessionToStorable.put(sessionId, importedDataCache);
        return importedDataCache;
    }

    /**
     * Removes the session record {@code sessionId} created by {@link #createSessionRecord}, releasing the
     * imported entity graph it held.
     *
     * <p>Call this once an import session finishes, successfully or not; without it, the record and every
     * {@link org.qubership.automation.itf.core.model.common.Storable} it accumulated stay in this cache for
     * the life of the JVM.</p>
     *
     * @param sessionId the session to release
     */
    public void removeSessionRecord(BigInteger sessionId) {
        sessionToStorable.remove(sessionId);
    }
}
