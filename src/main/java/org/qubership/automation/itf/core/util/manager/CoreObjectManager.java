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

import org.qubership.automation.itf.core.hibernate.ManagerFactory;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.springframework.stereotype.Component;

@Component
public class CoreObjectManager {
    private static CoreObjectManagerService staticCoreObjectManagerService;

    private CoreObjectManager(CoreObjectManagerService coreObjectManagerService) {
        staticCoreObjectManagerService = coreObjectManagerService;
    }

    public static CoreObjectManagerService getInstance() {
        return requireInitialized();
    }

    public ManagerFactory getManagerFactory() {
        return requireInitialized().getManagerFactory();
    }

    public static <U extends Storable> ObjectManager<U> managerFor(Class<U> clazz) {
        return getInstance().getManager(clazz);
    }

    public <U extends Storable> ObjectManager<U> getManager(Class<U> clazz) {
        return requireInitialized().getManager(clazz);
    }

    /**
     * Returns {@code clazz}'s {@link ObjectManager}, cast to its more specific type {@code toCast}.
     *
     * @param clazz the managed class to look up the manager for
     * @param toCast the manager's expected, more specific type
     * @return {@code clazz}'s manager, as a {@code T}
     * @throws IllegalArgumentException if {@code clazz}'s manager is not an instance of {@code toCast}
     */
    @SuppressWarnings("unchecked")//this is really typesafe, I perform check isAssignableFrom
    public <U extends Storable, T extends ObjectManager<U>> T getSpecialManager(Class<U> clazz, Class<T> toCast) {
        ObjectManager<U> manager = requireInitialized().getManager(clazz);
        if (toCast.isAssignableFrom(manager.getClass())) {
            return (T) manager;
        } else {
            throw new IllegalArgumentException("Object manager %s is not of type %s".formatted(
                    manager, toCast.getName()));
        }
    }

    private static CoreObjectManagerService requireInitialized() {
        if (staticCoreObjectManagerService == null) {
            throw new IllegalStateException("CoreObjectManager is not initialized: the Spring bean "
                    + CoreObjectManager.class.getName() + " was never constructed. Add "
                    + "\"org.qubership.automation.itf.core\" to your application's @ComponentScan (or "
                    + "@SpringBootApplication scanBasePackages) so its @Component/@Service beans, "
                    + "including this one, enter your application context.");
        }
        return staticCoreObjectManagerService;
    }
}
