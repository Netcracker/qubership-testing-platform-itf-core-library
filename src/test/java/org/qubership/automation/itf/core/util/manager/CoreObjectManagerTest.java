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

package org.qubership.automation.itf.core.util.manager;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.jpa.system.System;

/**
 * Fails if {@link CoreObjectManager} goes back to returning {@code null} instead of throwing when
 * the Spring bean that sets its static service reference was never constructed (UX-P1-03).
 *
 * <p>The static field is shared by every caller in the JVM, so each test saves it before running
 * and restores it afterward to avoid leaking state into other tests.</p>
 */
class CoreObjectManagerTest {

    private static Field serviceField;

    private CoreObjectManagerService originalService;

    @BeforeAll
    static void resolveField() throws Exception {
        serviceField = CoreObjectManager.class.getDeclaredField("staticCoreObjectManagerService");
        serviceField.setAccessible(true);
    }

    @BeforeEach
    void saveState() throws Exception {
        originalService = (CoreObjectManagerService) serviceField.get(null);
    }

    @AfterEach
    void restoreState() throws Exception {
        serviceField.set(null, originalService);
    }

    @Test
    void getInstanceThrowsNamingTheRequiredComponentScanWhenNotWired() throws Exception {
        serviceField.set(null, null);

        IllegalStateException ex = assertThrows(IllegalStateException.class, CoreObjectManager::getInstance);

        assertTrue(ex.getMessage().contains("org.qubership.automation.itf.core"));
        assertTrue(ex.getMessage().contains("ComponentScan"));
    }

    @Test
    void getInstanceReturnsTheWiredServiceOnceSet() throws Exception {
        CoreObjectManagerService service = mock(CoreObjectManagerService.class);
        serviceField.set(null, service);

        assertSame(service, CoreObjectManager.getInstance());
    }

    @Test
    void managerForThrowsWhenNotWired() throws Exception {
        serviceField.set(null, null);

        assertThrows(IllegalStateException.class, () -> CoreObjectManager.managerFor(System.class));
    }

    @Test
    void managerForDelegatesToTheWiredServiceOnceSet() throws Exception {
        CoreObjectManagerService service = mock(CoreObjectManagerService.class);
        @SuppressWarnings("unchecked")
        ObjectManager<System> manager = mock(ObjectManager.class);
        when(service.getManager(eq(System.class))).thenReturn(manager);
        serviceField.set(null, service);

        assertSame(manager, CoreObjectManager.managerFor(System.class));
    }

    @Test
    void getManagerFactoryThrowsWhenNotWired() throws Exception {
        serviceField.set(null, null);
        CoreObjectManager manager = uninitializedInstance();

        assertThrows(IllegalStateException.class, manager::getManagerFactory);
    }

    @Test
    void getManagerThrowsWhenNotWired() throws Exception {
        serviceField.set(null, null);
        CoreObjectManager manager = uninitializedInstance();

        assertThrows(IllegalStateException.class, () -> manager.getManager(System.class));
    }

    @Test
    void getSpecialManagerThrowsWhenNotWired() throws Exception {
        serviceField.set(null, null);
        CoreObjectManager manager = uninitializedInstance();

        assertThrows(IllegalStateException.class,
                () -> manager.getSpecialManager(System.class, ObjectManager.class));
    }

    @Test
    void getSpecialManagerThrowsWhenWiredButOfTheWrongType() throws Exception {
        CoreObjectManagerService service = mock(CoreObjectManagerService.class);
        @SuppressWarnings("unchecked")
        ObjectManager<System> manager = mock(ObjectManager.class);
        when(service.getManager(eq(System.class))).thenReturn(manager);
        serviceField.set(null, service);
        CoreObjectManager coreObjectManager = uninitializedInstance();

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> coreObjectManager.getSpecialManager(System.class, WrongManagerType.class));

        verify(service).getManager(System.class);
        assertTrue(ex.getMessage().contains("not of type"));
    }

    /**
     * {@link CoreObjectManager}'s only constructor is private and Spring-managed; a plain instance
     * to call the non-static methods through is obtained via reflection instead of a fake bean.
     */
    private static CoreObjectManager uninitializedInstance() throws Exception {
        java.lang.reflect.Constructor<CoreObjectManager> constructor =
                CoreObjectManager.class.getDeclaredConstructor(CoreObjectManagerService.class);
        constructor.setAccessible(true);
        CoreObjectManagerService serviceAtConstructionTime = (CoreObjectManagerService) serviceField.get(null);
        CoreObjectManager instance = constructor.newInstance(serviceAtConstructionTime);
        return instance;
    }

    private interface WrongManagerType extends ObjectManager<System> {
    }
}
