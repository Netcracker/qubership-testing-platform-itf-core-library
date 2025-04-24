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

package org.qubership.automation.itf.core.util.generator.id;

import java.util.Objects;

import org.qubership.automation.itf.core.model.common.Storable;

import com.fasterxml.jackson.annotation.ObjectIdGenerator;

public class JsonIdGen extends ObjectIdGenerator<String> {
    private static final long serialVersionUID = 20240812L;

    protected final Class<?> protectedScope;

    public JsonIdGen() {
        this(Storable.class);
    }

    protected JsonIdGen(Class<?> scope) {
        protectedScope = scope;
    }

    @Override
    public final Class<?> getScope() {
        return protectedScope;
    }


    // Can just return base instance since this is essentially scopeless
    @Override
    public ObjectIdGenerator<String> forScope(Class<?> scope) {
        return this;
    }

    // Can just return base instance since this is essentially scopeless
    @Override
    public ObjectIdGenerator<String> newForSerialization(Object context) {
        return this;
    }

    @Override
    public String generateId(Object forPojo) {
        return Objects.toString(((Storable) forPojo).getID(), null);
    }

    @Override
    public IdKey key(Object key) {
        return key == null ? null : new IdKey(getClass(), null, key);
    }

    // Should be usable for generic Opaque String ids?
    @Override
    public boolean canUseFor(ObjectIdGenerator<?> gen) {
        return gen instanceof JsonIdGen;
    }
}
