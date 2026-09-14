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

import java.io.Serial;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.qubership.automation.itf.core.model.extension.Extendable;
import org.qubership.automation.itf.core.model.extension.ExtendableImpl;
import org.qubership.automation.itf.core.model.extension.Extension;
import org.qubership.automation.itf.core.util.exception.ExtensionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.InvocationHandler;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

@SuppressWarnings("unchecked")
public class ExtensionManager implements Serializable {
    @Serial
    private static final long serialVersionUID = 20240812L;

    private static final Logger LOGGER = LoggerFactory.getLogger(ExtensionManager.class);

    private static final ExtensionManager ourInstance = new ExtensionManager();

    private ExtensionManager() {
    }

    public static ExtensionManager getInstance() {
        return ourInstance;
    }

    /**
     * Makes {@code object} {@link Extendable}, wrapping it in a CGLIB proxy that adds the
     * {@link Extendable} methods and delegates everything else to {@code object}. Returns
     * {@code object} unchanged when it already is {@link Extendable}.
     *
     * @param object the object to make extendable
     * @return {@code object}, or a proxy around it that also implements {@link Extendable}
     * @throws ExtensionException if the proxy cannot be created
     */
    public <T> T createExtendable(T object) throws ExtensionException {
        if (!(object instanceof Extendable)) {
            try {
                return (T) enhanceObject(object);
            } catch (Exception e) {
                throw new ExtensionException("Error creating extension for object %s".formatted(object), e);
            }
        } else {
            return object;
        }
    }

    /**
     * Creates an instance of {@code clazz} that is {@link Extendable}: a CGLIB-enhanced instance
     * when {@code clazz} itself does not implement {@link Extendable}, or a plain instance built
     * from its no-arg constructor when it already does.
     *
     * @param clazz the class to instantiate
     * @return a new instance of {@code clazz} (or an {@link Extendable} enhancement of it)
     * @throws ExtensionException if the instance cannot be created
     */
    public <T> T createExtendable(Class<T> clazz) throws ExtensionException {
        if (!Extendable.class.isAssignableFrom(clazz)) {
            try {
                return (T) enhanceClass(clazz);
            } catch (Throwable e) {
                throw new ExtensionException("Error creating extension for class %s".formatted(
                        clazz.getName()), e);
            }
        } else {
            try {
                return clazz.getDeclaredConstructor().newInstance();
            } catch (Exception e) {
                throw new ExtensionException("Error creating instance of class %s".formatted(clazz.getName()), e);
            }
        }
    }

    /**
     * Attaches {@code extension} to {@code object}, when both are non-{@code null} and
     * {@code object} is {@link Extendable}. Logs a warning and does nothing otherwise.
     *
     * @param object the object to extend
     * @param extension the extension to attach
     */
    @SuppressWarnings("unchecked")
    public void extend(Object object, Extension extension) {
        if (extension != null && object != null) {
            Extendable extendable;
            if (object instanceof Extendable extendable1) {
                extendable = extendable1;
                extendable.extend(extension);
            } else {
                LOGGER.warn("Cannot extend unextendable object {}", object);
            }
        }
    }

    /**
     * Returns {@code object}'s extension of type {@code extensionClass}, creating and attaching one
     * from its no-arg constructor when {@code object} does not have it yet.
     *
     * @param object the object to get the extension from; must be {@link Extendable} to have one
     * @param extensionClass the extension type to get or create
     * @return the extension, or {@code null} when {@code object} is not {@link Extendable}, or the
     *     extension could not be created
     */
    public <T extends Extension> T getExtension(Object object, Class<T> extensionClass) {
        if (object instanceof Extendable extendable) {
            T extension = extendable.getExtension(extensionClass);
            if (extension == null) {
                try {
                    extension = extensionClass.getDeclaredConstructor().newInstance();
                    extendable.extend(extension);
                } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                         InvocationTargetException e) {
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
        @Serial
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
