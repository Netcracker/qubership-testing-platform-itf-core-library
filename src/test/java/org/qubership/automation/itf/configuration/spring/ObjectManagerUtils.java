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

package org.qubership.automation.itf.configuration.spring;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.google.common.collect.Lists;
import org.qubership.automation.itf.core.hibernate.ObjectManagerFactoryHB;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.common.LabeledStorable;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import javax.annotation.Nonnull;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectManagerUtils extends ObjectManagerFactoryHB {

    private static final DateTimeFormatter dateTimeFormatter =
            DateTimeFormatter.ofPattern("dd/MM/YY 'in' HH:mm:ss").withZone(ZoneId.systemDefault());
    private static final List<String> LABELS = Lists.newArrayList("TEST_PURPOSES", "TOTALLY_BROKEN", "NICE_TO_HAVE");
    private static Supplier<ObjectManagerUtils> INSTANCE = new Supplier<ObjectManagerUtils>() {
        @Override
        public ObjectManagerUtils get() {
            throw new IllegalStateException("This class should be initialized by spring" + " thru package scanning, "
                    + "check your context configuration");
        }
    };
    private final Map<Class<? extends Storable>, Integer> counters;
    @Autowired
    protected ApplicationContext myContext;

    protected ObjectManagerUtils() {
        counters = new HashMap<>();
        INSTANCE = Suppliers.ofInstance(this);
    }

    public static <T extends Storable> ObjectManager<T> managerFor(Class<T> type) {
        return INSTANCE.get().getManager(type);
    }

    @SuppressWarnings("unchecked")
    public static <T extends Storable> ObjectManager<T> managerFor(T storable) {
        Class<T> type = (Class<T>) storable.getClass();
        return managerFor(type);
    }

    @Nonnull
    public static StubProject project() {
        return getFirst(StubProject.class);
    }

    @Nonnull
    public static <T extends Storable> T create(Class<T> type) {
        return managerFor(type).create();
    }

    @Nonnull
    public static <T extends Storable> T create(Storable parent, Class<T> type, Map props) {
        return managerFor(type).create(parent, type.getSimpleName(), props);
    }

    @Nonnull
    public static <T extends Storable> T getFirst(Class<T> type) {
        T result = managerFor(type).getAll().stream().findFirst().get();
        Assert.assertNotNull(result);
        return result;
    }

    @Nonnull
    public static <T extends Storable> T getById(Class<T> type, Object id) {
        T result = managerFor(type).getById(id);
        Assert.assertNotNull(result);
        return result;
    }

    @Nonnull
    public static <T extends Storable> Collection<? extends T> getAll(Class<T> type) {
        Collection<? extends T> result = managerFor(type).getAll();
        Assert.assertNotNull(result);
        return result;
    }

    public static void store(Storable storable) {
        ObjectManager<? extends Storable> om = managerFor(storable.getClass());
        om.store(storable);
    }

    /**
     * fills info with {@link #fillNameDescrLabels(Storable)}
     * doing validation also
     * see {@link #validateStorableSearch(Storable)}
     */
    public static void renameStoreValidate(Storable storable) {
        renameStoreValidate(managerFor(storable.getClass()), storable);
    }

    /**
     * see {@link #renameStoreValidate(Storable)}
     */
    public static void renameStoreValidate(ObjectManager om, Storable storable) {
        fillNameDescrLabels(storable);
        om.store(storable);
        for (int i = 0; i < 2; i++) {
            setName(storable);
            om.store(storable);
        }
        validateStorableSearch(om, storable);
    }

    /**
     * Validates that {@link ObjectManager#getAll()}
     * {@link ObjectManager#getById(Object)},
     * {@link ObjectManager#getByName(String)},
     * {@link ObjectManager#getAllByParentId(Object)},
     * {@link ObjectManager#getByParentAndName(Storable, String)}
     * works properly
     */
    protected static <T extends Storable> void validateStorableSearch(@Nonnull T stored) {
        validateStorableSearch(managerFor(stored), stored);
    }

    /**
     * see {@link #validateStorableSearch(Storable)}
     */
    protected static <T extends Storable> void validateStorableSearch(@Nonnull ObjectManager<T> om, @Nonnull T stored) {
        T byId = om.getById(stored.getID());
        Assert.assertEquals(stored, byId);
        validateStorableSearchInternal(om, byId);
    }

    /**
     * see {@link #validateStorableSearch(Storable)}
     */
    protected static <T extends Storable> void validateStorableSearch(@Nonnull BigInteger id,
                                                                      @Nonnull Class<T> itsClass) {
        //ObjectManager<T> om = CoreObjectManager.getInstance().getManager(itsClass);
//        T byId = om.getById(id);
//        Assert.assertNotNull(byId);
//        validateStorableSearchInternal(om, byId);
    }

    private static <T extends Storable> void validateStorableSearchInternal(@Nonnull ObjectManager<T> om,
                                                                            @Nonnull T stored) {
        String storedName = stored.getName();
        if (storedName != null) {
            Assert.assertTrue(String.format("No [%s] with name [%s] found by name", stored, storedName),
                    om.getByName(storedName).contains(stored));
        }
        Storable parent = stored.getParent();
        if (parent != null) {
            BigInteger parentId = (BigInteger) parent.getID();
            Assert.assertTrue(String.format("No [%s] with parent [%s] found by parentId", stored, parent),
                    om.getAllByParentId(parentId).contains(stored));
            if (storedName != null) {
                Assert.assertTrue(String.format("No [%s] with parent [%s] and name [%s] found by parent and name",
                        stored, parent, storedName), om.getByParentAndName(parent, storedName).contains(stored));
            }
            String parentName = parent.getName();
            if (parentName != null) {
                Assert.assertTrue(String.format("No [%s] with parent [%s] found by parent name", stored, parent),
                        om.getAllByParentName(parentName).contains(stored));
            }
        }
        Assert.assertTrue(String.format("No [%s] found by [getAll] call", stored), om.getAll().contains(stored));
    }

    private static void fillNameDescrLabels(Storable storable) {
        setName(storable);
        storable.setDescription("Created " + LocalDateTime.now().format(dateTimeFormatter));
        if (storable instanceof LabeledStorable) {
            LabeledStorable labeledStorable = (LabeledStorable) storable;
            labeledStorable.fillLabels(LABELS);
        }
    }

    private static void setName(Storable storable) {
        Class<? extends Storable> clazz = storable.getClass();
        Map<Class<? extends Storable>, Integer> counters = INSTANCE.get().counters;
        Integer count = counters.get(clazz);
        if (count == null) {
            count = 1;
        } else {
            ++count;
        }
        counters.put(clazz, count);
        storable.setName(clazz.getSimpleName() + count);
    }

    public void init() {
    }

    @EventListener
    public void init(ContextRefreshedEvent event) {
        if (!event.getSource().equals(myContext)) return;
        init();
    }
}
