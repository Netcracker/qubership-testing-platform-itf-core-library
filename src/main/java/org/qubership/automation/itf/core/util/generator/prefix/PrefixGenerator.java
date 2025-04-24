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

package org.qubership.automation.itf.core.util.generator.prefix;

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
public class PrefixGenerator {

    private static final Supplier<IPrefixFactory> DEFAULT = new Supplier<IPrefixFactory>() {
        @Override
        public IPrefixFactory get() {
            throw new RuntimeException("Please set IPrefixFactory as \"prefixFactory\" for PrefixGenerator");
        }
    };

    private static volatile IPrefixFactory INSTANCE;

    @Inject
    protected PrefixGenerator(@Named("prefixFactory") IPrefixFactory factory) {
        init(factory);
    }

    public static IPrefixFactory get() {
        init(DEFAULT);
        return INSTANCE;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static void init(Supplier<IPrefixFactory> instance) {
        if (INSTANCE == null) {
            synchronized (PrefixGenerator.class) {
                if (INSTANCE == null) {
                    INSTANCE = instance.get();
                }
            }
        }
    }

    public static void init(IPrefixFactory instance) {
        init(Suppliers.ofInstance(instance));
    }

    public static Object getPrefix(Storable object) {
        return get().getPrefix(object.getClass());
    }

    public static Object getPrefix(Class<? extends Storable> clazz) {
        return get().getPrefix(clazz);
    }

    public static Class<? extends Storable> getClassByPrefix(Object prefix) {
        return get().getClassByPrefix(prefix);
    }

    public static Object removePrefix(Object id) {
        return get().removePrefix(id);
    }

}
