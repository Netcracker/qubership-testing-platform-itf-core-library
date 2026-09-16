/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.automation.itf.core.model.jpa.storage;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.util.helper.Reflection;

/**
 * Fails when a {@link Storable} implementation, or any other {@link Serializable} class in the main
 * sources, has no explicit {@code serialVersionUID} of its own, or shares its value with another class.
 *
 * <p>A class serialized across process boundaries must pin {@code serialVersionUID} itself; left unset,
 * the JVM computes it from the class signature, and the value changes on the next field or method
 * change, breaking deserialization between two versions of the class. Two classes sharing a value is
 * not itself a wire-format defect, since the class name is still part of what identifies a stream
 * object, but it shows the value was copied from another class rather than chosen for this one.</p>
 *
 * <p>Add {@code @Serial private static final long serialVersionUID = ...;} to the class the failure
 * message names, using a value nothing else in the sources already uses.</p>
 */
class SerialVersionUidTest {

    @Test
    void everyStorableImplementationDeclaresItsOwnSerialVersionUid() {
        Set<Class<? extends Storable>> types = Reflection.getReflections().getSubTypesOf(Storable.class);
        List<String> withoutOwnUid = types.stream()
                .filter(type -> !type.isInterface() && !type.isSynthetic())
                .filter(type -> ownSerialVersionUidField(type) == null)
                .map(Class::getName)
                .sorted()
                .collect(Collectors.toList());

        assertTrue(withoutOwnUid.isEmpty(),
                "Storable implementations with no declared serialVersionUID: " + withoutOwnUid);
    }

    @Test
    void declaredSerialVersionUidValuesAreUnique() throws IllegalAccessException {
        Set<Class<? extends Serializable>> types = Reflection.getReflections().getSubTypesOf(Serializable.class);
        Map<Long, List<String>> classesByUidValue = new HashMap<>();

        for (Class<?> type : types) {
            Field field = ownSerialVersionUidField(type);
            if (field == null) {
                continue;
            }
            long value = field.getLong(null);
            classesByUidValue.computeIfAbsent(value, v -> new ArrayList<>()).add(type.getName());
        }

        List<String> duplicates = classesByUidValue.values().stream()
                .filter(classNames -> classNames.size() > 1)
                .map(classNames -> String.join(", ", classNames))
                .collect(Collectors.toList());

        assertTrue(duplicates.isEmpty(), "Classes sharing the same serialVersionUID: " + duplicates);
    }

    /**
     * Returns the class's own {@code static final long serialVersionUID} field, or {@code null} if the
     * class declares none or inherits one instead.
     */
    private static Field ownSerialVersionUidField(Class<?> type) {
        try {
            Field field = type.getDeclaredField("serialVersionUID");
            if (field.getType() != long.class || !Modifier.isStatic(field.getModifiers())) {
                return null;
            }
            field.setAccessible(true);
            return field;
        } catch (NoSuchFieldException e) {
            return null;
        }
    }
}
