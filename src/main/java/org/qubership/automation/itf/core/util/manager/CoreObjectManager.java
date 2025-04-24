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

package org.qubership.automation.itf.core.util.manager;

import org.qubership.automation.itf.core.hibernate.ManagerFactory;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CoreObjectManager {
    private static CoreObjectManagerService staticCoreObjectManagerService;

    @Autowired
    private CoreObjectManager(CoreObjectManagerService coreObjectManagerService) {
        staticCoreObjectManagerService = coreObjectManagerService;
    }

    public static CoreObjectManagerService getInstance() {
        return staticCoreObjectManagerService;
    }

    public ManagerFactory getManagerFactory() {
        return staticCoreObjectManagerService.getManagerFactory();
    }

    public static <U extends Storable> ObjectManager<U> managerFor(Class<U> clazz) {
        return getInstance().getManager(clazz);
    }

    public <U extends Storable> ObjectManager<U> getManager(Class<U> clazz) {
        return staticCoreObjectManagerService.getManager(clazz);
    }

    /**
     * TODO: Add JavaDoc.
     */
    @SuppressWarnings("unchecked")//this is really typesafe, I perform check isAssignableFrom
    public <U extends Storable, T extends ObjectManager<U>> T getSpecialManager(Class<U> clazz, Class<T> toCast) {
        ObjectManager<U> manager = staticCoreObjectManagerService.getManager(clazz);
        if (toCast.isAssignableFrom(manager.getClass())) {
            return (T) manager;
        } else {
            throw new IllegalArgumentException(String.format("Object manager %s is not of type %s",
                    manager, toCast.getName()));
        }
    }
}
