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

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.qubership.automation.itf.core.model.common.Storable;

public class StorableUtils {

    /**
     * Checks whether {@code collection} holds a {@link Storable} whose {@link Storable#getID()}
     * equals {@code id}.
     *
     * @param collection the storables to search
     * @param id the id to look for
     * @return {@code false} when {@code id} is {@code null} or no element's id equals it
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
     * Replaces {@code to}'s entries with {@code from}'s, in place. Does nothing when {@code to} and
     * {@code from} are the same map instance; otherwise clears {@code to} and, when {@code from} is
     * not {@code null}, copies every entry of {@code from} into it.
     *
     * @param to the map to overwrite
     * @param from the entries to copy in, or {@code null} to leave {@code to} empty
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
     * Replaces {@code to}'s elements with {@code from}'s, in place. Does nothing when {@code to} and
     * {@code from} are the same collection instance; otherwise clears {@code to} and, when
     * {@code from} is not {@code null}, adds every element of {@code from} to it.
     *
     * @param to the collection to overwrite
     * @param from the elements to copy in, or {@code null} to leave {@code to} empty
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
