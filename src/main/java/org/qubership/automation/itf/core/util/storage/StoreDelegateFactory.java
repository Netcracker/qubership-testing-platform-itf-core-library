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

package org.qubership.automation.itf.core.util.storage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StoreDelegateFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(StoreDelegateFactory.class);

    private static StoreDelegateFactory ourInstance = new StoreDelegateFactory();

    public static StoreDelegateFactory getInstance() {
        return ourInstance;
    }

    private final Class<? extends StoreInformationDelegate> delegateClass;

    private StoreDelegateFactory() {
        String delegateClassName = DelegateImpl.class.getName();
        Class<? extends StoreInformationDelegate> tmp = null;
        try {
            tmp = Class.forName(delegateClassName).asSubclass(StoreInformationDelegate.class);
        } catch (ClassNotFoundException e) {
            LOGGER.error("Error initializing factory", e);
        }
        delegateClass = tmp;
    }

    /**
     * Creates a new {@link StoreInformationDelegate} instance of the configured delegate class.
     *
     * @return a new delegate instance, or {@code null} if the delegate class could not be resolved
     *     at construction, or could not be instantiated
     */
    public StoreInformationDelegate newDelegate() {
        try {
            return delegateClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Error instantiating delegate", e);
            return null;
        }
    }
}
