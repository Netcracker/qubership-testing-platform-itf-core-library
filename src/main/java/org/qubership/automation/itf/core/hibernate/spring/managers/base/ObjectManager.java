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

package org.qubership.automation.itf.core.hibernate.spring.managers.base;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Triple;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.constants.Match;
import org.qubership.automation.itf.core.util.exception.CopyException;

public interface ObjectManager<T extends Storable> {

    Collection<? extends T> getAll();

    T getById(@Nonnull Object id);

    Collection<? extends T> getByNatureId(@Nonnull Object id, Object projectId);

    Collection<? extends T> getAllByParentId(@Nonnull Object id);

    Collection<? extends T> getByName(String name);

    Collection<? extends T> getByPieceOfName(String pieceOfName);

    Collection<? extends T> getAllByParentName(String name);

    Collection<? extends T> getByProperties(BigInteger projectId, Triple<String, Match, ?>... properties);

    Collection<? extends T> getByParentAndName(Storable parent, String name);

    @Nullable
    Collection<UsageInfo> remove(Storable object, boolean force);

    void onCreate(T object);

    void onUpdate(T object);

    void onRemove(T object);

    void store(Storable object);

    void replicate(Storable object);

    boolean contains(Storable object);

    void update(Storable object);

    void flush();

    void evict(Storable object);

    T create();

    T create(Storable parent);

    T create(Storable parent, String type);

    T create(Storable parent, String type, Map parameters);

    T create(Storable parent, String name, String type);

    T create(Storable parent, String name, String type, String description);

    T create(Storable parent, String name, String type, String description, List<String> labels);

    Storable copy(Storable dst, Storable obj, String projectId, String sessionId) throws CopyException;

    void move(Storable dst, Storable obj, String sessionId);

    @Nullable
    Collection<UsageInfo> findUsages(Storable storable);

    Map<String, List<BigInteger>> findImportantChildren(Storable storable);

    String acceptsTo(Storable storable);

    void additionalMoveActions(Storable object, String sessionId);

    void setReplicationRole(String roleName);
}
