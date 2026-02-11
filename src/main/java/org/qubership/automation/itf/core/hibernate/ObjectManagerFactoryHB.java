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

package org.qubership.automation.itf.core.hibernate;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.util.exception.NoSuchManagerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;

@Component
public class ObjectManagerFactoryHB implements ManagerFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectManagerFactoryHB.class);
    private static final String ROOT = "ROOT";
    protected List<ObjectManager<? extends Storable>> objectManagersBeans;
    protected HashMap<Type, ObjectManager> objectManagers = Maps.newHashMapWithExpectedSize(9);

    public List<ObjectManager<? extends Storable>> getObjectManagersBeans() {
        return objectManagersBeans;
    }

    @Autowired
    public void setObjectManagersBeans(List<ObjectManager<? extends Storable>> objectManagersBeans) {
        this.objectManagersBeans = objectManagersBeans;
    }

    @Override
    public <U extends Storable> ObjectManager<U> getManager(Class<U> clazz) {
        return getManInner(clazz);
    }

    public <U extends Storable> ObjectManager<U> getManager(TypeToken<U> storable) {
        return getManInner(storable.getType());
    }

    @Override
    public void register(Class<? extends Storable> clazz, ObjectManager manager) {
        objectManagers.put(clazz, manager);
    }

    private <T extends Storable> ObjectManager<T> getManInner(Type storable) {
        ObjectManager<T> result = objectManagers.get(storable);
        if (result == null) {
            result = findManager(Lists.newArrayList(new ObjManTypesafeGarant(storable),
                    new ObjManTypesafeGarant2(storable)).iterator());
            objectManagers.put(storable, result);
        }
        return result;
    }

    /**
     * finds object manager by predicate.
     *
     * @param typesafeGarants should check if manager can be casted to {@link ObjectManager} of {@link T}
     */
    @Nonnull
    @SuppressWarnings("unchecked")
    private <T extends Storable> ObjectManager<T> findManager(
            @Nonnull Iterator<Predicate<ObjectManager>> typesafeGarants) {
        Set<ObjectManager<? extends Storable>> candidates = null;
        Predicate<ObjectManager> typesafeGarant = null;
        while (CollectionUtils.isEmpty(candidates) && Objects.requireNonNull(typesafeGarants).hasNext()) {
            typesafeGarant = typesafeGarants.next();
            candidates = objectManagersBeans.stream().filter(typesafeGarant).collect(Collectors.toSet());
        }
        if (CollectionUtils.isEmpty(candidates)) {
            throw new NoSuchManagerException(String.format("Object manager '%s' not found. Available managers: %s",
                    typesafeGarant, objectManagersBeans));
        }
        if (candidates.size() > 1) {
            throw new NoSuchManagerException(String.format("Got multiple candidates for '%s'. Candidates are: %s",
                    typesafeGarant, candidates));
        }
        ObjectManager<? extends Storable> found = candidates.iterator().next();
        try {
            return (ObjectManager<T>) found;
        } catch (Exception e) {
            throw new RuntimeException(String.format("Typesafe garant '%s' assumes that '%s' has proper type. But it "
                    + "has not", typesafeGarant, found), e);
        }
    }

    private static class ObjManTypesafeGarant implements Predicate<ObjectManager> {

        private final Type storableType;

        private ObjManTypesafeGarant(Type type) {
            this.storableType = type;
        }

        @Override
        public boolean test(ObjectManager input) {
            Type managersGenericType =
                    TypeToken.of(input.getClass()).resolveType(ObjectManager.class.getTypeParameters()[0]).getType();
            return storableType.equals(managersGenericType);
        }

        @Override
        public String toString() {
            return String.format("%s<%s>", ObjectManager.class.getSimpleName(), storableType);
        }
    }

    private static class ObjManTypesafeGarant2 implements Predicate<ObjectManager> {

        private final Type storableType;

        private ObjManTypesafeGarant2(Type type) {
            this.storableType = type;
        }

        @Override
        public boolean test(ObjectManager input) {
            /*return TypeToken.of(input.getClass())
                    .resolveType(ObjectManager.class.getTypeParameters()[0]).isSubtypeOf(storableType);*/
            //TODO is not typesafe
            return TypeToken.of(input.getClass()).resolveType(ObjectManager.class.getTypeParameters()[0])
                    .isSupertypeOf(storableType);
        }

        @Override
        public String toString() {
            return String.format("%s<? extends %s>", ObjectManager.class.getSimpleName(), storableType);
        }
    }
}
