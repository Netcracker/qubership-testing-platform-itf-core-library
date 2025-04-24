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

package org.qubership.automation.itf.core.util.helper;

import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.qubership.automation.itf.core.model.common.Storable;

public class StorableUtils {

    /**
     * TODO: Add JavaDoc.
     */
    public static boolean containsId(Collection<Storable> collection, Object id) {
        if (id == null) {
            return false;
        }
        for (Storable storable : collection) {
            if (storable.getID().equals(id)) {
                return true;
            }
        }
        return false;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static <T, U> void fillMap(@Nonnull Map<T, U> to, @Nullable Map<? extends T, ? extends U> from) {
        if (to == from) {
            return;
        }
        to.clear();
        if (from != null) {
            to.putAll(from);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static <T> void fillCollection(@Nonnull Collection<T> to, @Nullable Collection<? extends T> from) {
        if (to == from) {
            return;
        }
        to.clear();
        if (from != null) {
            to.addAll(from);
        }
    }

}
