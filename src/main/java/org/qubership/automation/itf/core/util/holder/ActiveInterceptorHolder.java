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

package org.qubership.automation.itf.core.util.holder;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.jpa.interceptor.TemplateInterceptor;
import org.qubership.automation.itf.core.model.jpa.interceptor.TransportConfigurationInterceptor;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.manager.CoreObjectManagerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ActiveInterceptorHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActiveInterceptorHolder.class);
    private Map<String, Map<String, Interceptor>> activeInterceptors = new HashMap<>();
    private static final ActiveInterceptorHolder INSTANCE = new ActiveInterceptorHolder();

    private ActiveInterceptorHolder() {
    }

    public static ActiveInterceptorHolder getInstance() {
        return INSTANCE;
    }

    /**
     * Fill Holder with active interceptors registered.
     */
    public void fillActiveInterceptorHolder() {
        try {
            TxExecutor.executeUnchecked((Callable<Void>) () -> {
                Map<String, Map<String, Class<?>>> registeredInterceptors =
                        InterceptorHolder.getInstance().getInterceptors();
                if (!registeredInterceptors.isEmpty()) {
                    getActiveInterceptorsFromProvider(TemplateInterceptor.class, registeredInterceptors);
                    getActiveInterceptorsFromProvider(TransportConfigurationInterceptor.class, registeredInterceptors);
                }
                return null;
            }, TxExecutor.nestedReadOnlyTransaction());
        } catch (Exception e) {
            LOGGER.error("Can't register active interceptors", e);
        }
    }

    /**
     * Clear active interceptor holder.
     */
    public void clearActiveInterceptorHolder() {
        activeInterceptors.clear();
    }

    /**
     * Get active interceptors.
     */
    public Map<String, Map<String, Interceptor>> getActiveInterceptors() {
        return activeInterceptors;
    }

    /**
     * Update active interceptor data in holder.
     */
    public void updateActiveInterceptorHolder(Object interceptorProvideId, Collection<Interceptor> interceptors) {
        for (Interceptor interceptor : interceptors) {
            if (interceptor.isActive()) {
                addInterceptorData(interceptorProvideId, interceptor);
            }
        }
    }

    private void getActiveInterceptorsFromProvider(Class interceptorClass,
                                                          Map<String, Map<String, Class<?>>> registeredInterceptors) {
        CoreObjectManagerService coreObjectManager = CoreObjectManager.getInstance();
        if (coreObjectManager != null && coreObjectManager.getManagerFactory() != null) {
            ObjectManager manager = coreObjectManager.getManager(interceptorClass);
            if (manager != null) {
                Collection<Interceptor> thisClassInterceptors = manager.getAll();
                if (thisClassInterceptors != null && !thisClassInterceptors.isEmpty()) {
                    for (Interceptor interceptor : thisClassInterceptors) {
                        if (interceptor.isActive() && interceptorIsRegistered(registeredInterceptors, interceptor)) {
                            addInterceptorData(interceptor.getParent().getID(), interceptor);
                        }
                    }
                }
            }
        }
    }

    private void addInterceptorData(Object interceptorProviderId, Interceptor interceptor) {
        Map<String, Interceptor> interceptors = activeInterceptors.get(interceptorProviderId.toString());
        if (interceptors == null) {
            interceptors = new LinkedHashMap<>();
            interceptors.put(interceptor.getID().toString(), interceptor);
            activeInterceptors.put(interceptorProviderId.toString(), interceptors);
        } else {
            interceptors.put(interceptor.getID().toString(), interceptor);
        }
    }

    private static boolean interceptorIsRegistered(Map<String, Map<String, Class<?>>> registeredInterceptors,
                                                   Interceptor interceptor) {
        Map<String, Class<?>> interceptorsInfo = registeredInterceptors.get(interceptor.getTransportName());
        if (interceptorsInfo == null || interceptorsInfo.isEmpty()) {
            return false;
        }
        for (Map.Entry<String, Class<?>> interceptorInfo : interceptorsInfo.entrySet()) {
            if (interceptorInfo.getValue().getName().equals(interceptor.getTypeName())) {
                return true;
            }
        }
        return false;
    }
}
