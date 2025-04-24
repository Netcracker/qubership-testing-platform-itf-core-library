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

package org.qubership.automation.itf.core.util.loader;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.interceptor.TransportInterceptor;
import org.qubership.automation.itf.core.util.loader.base.AbstractLoader;

import com.google.common.collect.Maps;
import lombok.Getter;

@Getter
public class InterceptorClassLoader extends AbstractLoader<TransportInterceptor> {

    private static final InterceptorClassLoader INSTANCE = new InterceptorClassLoader();
    private final Map<String, ClassLoader> classLoaderHolder = Maps.newHashMapWithExpectedSize(10);

    public static InterceptorClassLoader getInstance() {
        return INSTANCE;
    }

    @Override
    protected Class<TransportInterceptor> getGenericType() {
        return TransportInterceptor.class;
    }

    @Override
    public TransportInterceptor getInstanceClass(String className, Object... paramForConstructor)
            throws ClassNotFoundException {
        try {
            return getClass(className).getConstructor(Interceptor.class).newInstance(paramForConstructor);
        } catch (InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            throw new ClassNotFoundException("Classloader not found for class", e);
        }
    }

    @Override
    public Class<? extends TransportInterceptor> getClass(String typeName) throws ClassNotFoundException {
        try {
            if (getClassLoaderHolder().containsKey(typeName)) {
                return getClassLoaderHolder().get(typeName).loadClass(typeName).asSubclass(TransportInterceptor.class);
            } else {
                throw new ClassNotFoundException("Classloader not found for class " + typeName);
            }
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("CommonInterceptor with name " + typeName
                    + " is not found in classloader", e);
        }
    }

    /**
     * Do not use it in production, it's needed only for tests.
     */
    @Deprecated
    public void cleanClassLoaders() {
        this.classLoaderHolder.clear();
    }

    @Override
    protected void validateClasses(Set<Class<? extends TransportInterceptor>> classes) {
        if (classes.size() > 1) {
            throw new IllegalArgumentException("More then one class found: " + classes);
        }
        if (!classes.iterator().hasNext()) {
            throw new IllegalArgumentException("No one class found");
        }
    }
}
