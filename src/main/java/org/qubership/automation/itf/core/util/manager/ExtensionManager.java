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

import java.io.Serializable;
import java.lang.reflect.Method;

import org.qubership.automation.itf.core.model.extension.Extendable;
import org.qubership.automation.itf.core.model.extension.ExtendableImpl;
import org.qubership.automation.itf.core.model.extension.Extension;
import org.qubership.automation.itf.core.util.exception.ExtensionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.InvocationHandler;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

@SuppressWarnings("unchecked")
public class ExtensionManager implements Serializable {
    private static final long serialVersionUID = 20240812L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private static ExtensionManager ourInstance = new ExtensionManager();

    private ExtensionManager() {
    }

    public static ExtensionManager getInstance() {
        return ourInstance;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public <T> T createExtendable(T object) throws ExtensionException {
        if (!(object instanceof Extendable)) {
            try {
                return (T) enhanceObject(object);
            } catch (Exception e) {
                throw new ExtensionException(String.format("Error creating extension for object %s", object), e);
            }
        } else {
            return object;
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public <T> T createExtendable(Class<T> clazz) throws ExtensionException {
        if (!Extendable.class.isAssignableFrom(clazz)) {
            try {
                return (T) enhanceClass(clazz);
            } catch (Throwable e) {
                throw new ExtensionException(String.format("Error creating extension for class %s",
                        clazz.getName()), e);
            }
        } else {
            try {
                return clazz.newInstance();
            } catch (Exception e) {
                throw new ExtensionException(String.format("Error creating instance of class %s", clazz.getName()), e);
            }
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    @SuppressWarnings("unchecked")
    public void extend(Object object, Extension extension) {
        if (extension != null && object != null) {
            Extendable extendable;
            if (object instanceof Extendable) {
                extendable = (Extendable) object;
                extendable.extend(extension);
            } else {
                LOGGER.warn("Cannot extend unextendable object {}", object);
            }
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public <T extends Extension> T getExtension(Object object, Class<T> extensionClass) {
        if (object instanceof Extendable) {
            T extension = ((Extendable) object).getExtension(extensionClass);
            if (extension == null) {
                try {
                    extension = extensionClass.newInstance();
                    ((Extendable) object).extend(extension);
                } catch (InstantiationException | IllegalAccessException e) {
                    LOGGER.warn("Error creating extension instance", e);
                    return null;
                }
            }
            return extension;
        } else {
            return null;
        }
    }

    private Extendable enhanceObject(Object object) throws NoSuchMethodException {
        Class<?> clazz = object.getClass();
        clazz.getDeclaredConstructor().setAccessible(true);
        Object enhanced = Enhancer.create(clazz, new Class[]{Extendable.class}, new ExtendedInvocationHandler(object));
        return (Extendable) enhanced;
    }

    private Extendable enhanceClass(Class clazz) throws NoSuchMethodException {
        clazz.getDeclaredConstructor().setAccessible(true);
        Object enhanced = Enhancer.create(clazz, new Class[]{Extendable.class}, new ExtendedSelfMethodInterceptor());
        return (Extendable) enhanced;
    }

    private static class ExtendedInvocationHandler implements InvocationHandler {

        private final Object object;
        private final ExtendableImpl extendable = new ExtendableImpl();

        public ExtendedInvocationHandler(Object object) {
            this.object = object;
        }

        @Override
        public Object invoke(Object o, Method method, Object[] objects) throws Throwable {
            if (Extendable.class.isAssignableFrom(method.getDeclaringClass())) {
                return method.invoke(extendable, objects);
            } else {
                return method.invoke(object, objects);
            }
        }
    }

    private static class ExtendedSelfMethodInterceptor implements MethodInterceptor, Serializable {
        private static final long serialVersionUID = 20240812L;

        private final ExtendableImpl extendable = new ExtendableImpl();

        public ExtendedSelfMethodInterceptor() {
        }

        @Override
        public Object intercept(Object o, Method method, Object[] objects, MethodProxy methodProxy) throws Throwable {
            if (Extendable.class.isAssignableFrom(method.getDeclaringClass())) {
                return method.invoke(extendable, objects);
            } else {
                return methodProxy.invokeSuper(o, objects);
            }
        }
    }

}
