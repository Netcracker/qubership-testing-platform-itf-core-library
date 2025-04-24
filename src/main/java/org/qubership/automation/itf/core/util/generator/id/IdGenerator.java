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

import javax.inject.Inject;
import javax.inject.Named;

import org.qubership.automation.itf.core.model.common.Storable;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;

/**
 * Factory with an ability to pass an instance thru constructor using DI.
 * If instance is not set before 'get' method invoked, the DEFAULT strategy is used
 * You can {@link #init(Supplier)} singleton manually
 */
public final class IdGenerator {

    private static final Supplier<IdGeneratorInterface> DEFAULT = new Supplier<IdGeneratorInterface>() {
        @Override
        public IdGeneratorInterface get() {
            throw new RuntimeException("Please set IDGeneratorInterface as \"idGenerator\" for IDGenerator");
        }
    };

    private static volatile IdGeneratorInterface INSTANCE;

    @Inject
    protected IdGenerator(@Named("idGenerator") IdGeneratorInterface factory) {
        init(factory);
    }

    public static IdGeneratorInterface get() {
        init(DEFAULT);
        return INSTANCE;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static void init(Supplier<IdGeneratorInterface> instance) {
        if (INSTANCE == null) {
            synchronized (IdGenerator.class) {
                if (INSTANCE == null) {
                    INSTANCE = instance.get();
                }
            }
        }
    }

    public static void init(IdGeneratorInterface instance) {
        init(Suppliers.ofInstance(instance));
    }

    public static Object getId(Class<? extends Storable> clazz) {
        return get().getId(clazz);
    }

    public static void setStartFrom(Object id, Class<? extends Storable> clazz) {
        get().setStartFrom(id, clazz);
    }

}
