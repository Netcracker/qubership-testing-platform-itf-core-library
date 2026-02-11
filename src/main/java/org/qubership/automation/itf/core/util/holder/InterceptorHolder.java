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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jakarta.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.interceptor.TransportInterceptor;
import org.qubership.automation.itf.core.util.annotation.ApplyToTransport;
import org.qubership.automation.itf.core.util.config.ApplicationConfig;
import org.qubership.automation.itf.core.util.constants.InstanceSettingsConstants;
import org.qubership.automation.itf.core.util.loader.InterceptorClassLoader;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterceptorHolder {

    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptorHolder.class);

    public static InterceptorHolder getInstance() {
        return InterceptorHolderInstance.INSTANCE;
    }

    public static class InterceptorHolderInstance {
        public static final InterceptorHolder INSTANCE = new InterceptorHolder();
    }

    private Map<String, Map<String, Class<?>>> interceptors = new HashMap<>();

    private InterceptorHolder() {
        fillInterceptorHolder();
    }

    /**
     * Fill Holder with interceptor modules registered.
     */
    public void fillInterceptorHolder() {
        String interceptorFolderPath = ApplicationConfig.env.getProperty(InstanceSettingsConstants.INTERCEPTORS_FOLDER);
        if (!StringUtils.isEmpty(interceptorFolderPath)) {
            File interceptorsFolder = new File(interceptorFolderPath);
            File[] interceptorFiles = interceptorsFolder.listFiles();
            if (interceptorFiles != null && interceptorFiles.length > 0) {
                List<URL> commonJars = getCommonJars(interceptorFiles);
                for (File interceptorFile : interceptorFiles) {
                    if (interceptorFile.isDirectory()) {
                        File[] interceptorsFiles = interceptorFile.listFiles();
                        if (interceptorsFiles != null) {
                            loadInterceptorWithDependencies(interceptorsFiles, interceptorFile.getName(), commonJars);
                        }
                    }
                }
            }
        }
    }

    public void clearInterceptorHolder() {
        interceptors.clear();
    }

    public Map<String, Map<String, Class<?>>> getInterceptors() {
        return interceptors;
    }

    private void loadInterceptorWithDependencies(File[] interceptorsFiles,
                                                 String interceptorDirectoryName,
                                                 List<URL> commonJars) {
        List<URL> urls = getUrls(interceptorsFiles, true);
        urls.addAll(commonJars);
        if (!urls.isEmpty()) {
            ClassLoader classLoader =
                    new URLClassLoader(urls.toArray(new URL[urls.size()]), this.getClass().getClassLoader());
            Set<Class<? extends TransportInterceptor>> interceptorClasses = getInterceptorClasses(classLoader);
            if (interceptorClasses.isEmpty()) {
                LOGGER.warn("There is no interceptor in '{}' directory", interceptorDirectoryName);
            } else {
                for (Class<? extends TransportInterceptor> interceptorClass : interceptorClasses) {
                    InterceptorClassLoader.getInstance().getClassLoaderHolder()
                            .put(interceptorClass.getName(), classLoader);
                    addInterceptorToInterceptorHolder(classLoader, interceptorClass.getName());
                }
            }
        }
    }

    /* Parameter withChildren 
            = true  - means 'with direct children, but no recursion'
                    - It stands on interceptors' directory structure, which has only one child level.
            = false - means 'without children'.
    */
    private List<URL> getUrls(File[] interceptorsFiles, boolean withChildren) {
        List<URL> urls = new ArrayList<>();
        if (interceptorsFiles != null) {
            for (File interceptorFile : interceptorsFiles) {
                if (interceptorFile.isDirectory()) {
                    if (withChildren) {
                        urls.addAll(getUrls(interceptorFile.listFiles(), false));
                    }
                } else {
                    try {
                        urls.add(interceptorFile.toURI().toURL());
                    } catch (MalformedURLException e) {
                        LOGGER.error("Can't get URL of '{}' file", interceptorFile.getName());
                    }
                }
            }
        }
        return urls;
    }

    private Set<Class<? extends TransportInterceptor>> getInterceptorClasses(ClassLoader classLoader) {
        Reflections reflections = new Reflections("org.qubership", classLoader);
        return reflections.getSubTypesOf(TransportInterceptor.class);
    }

    private void addInterceptorToInterceptorHolder(ClassLoader classLoader, String className) {
        try {
            Class<?> resolvedClass = classLoader.loadClass(className);
            ApplyToTransport applyToTransports = resolvedClass.getAnnotation(ApplyToTransport.class);
            Named name = resolvedClass.getAnnotation(Named.class);
            if (applyToTransports != null) {
                for (String transportName : applyToTransports.transports()) {
                    Map<String, Class<?>> interceptorInfo = interceptors.get(transportName);
                    if (interceptorInfo == null) {
                        interceptorInfo = new HashMap<>();
                    }
                    interceptorInfo.put(name.value(), resolvedClass);
                    interceptors.put(transportName, interceptorInfo);
                }
                LOGGER.info("'{}' interceptor is registered successfully", name.value());
            }
        } catch (ClassNotFoundException e) {
            LOGGER.error("Cannot load '{}' class. Interceptor isn't registered.", className, e);
        }
    }

    private List<URL> getCommonJars(File[] interceptorFiles) {
        List<URL> commonJarsUrls = new ArrayList<>();
        for (File file : interceptorFiles) {
            if (!file.isDirectory() && file.getName().endsWith(".jar")) {
                try {
                    commonJarsUrls.add(file.toURI().toURL());
                } catch (MalformedURLException e) {
                    LOGGER.error("Can't get URL of {} file", file.getName(), e);
                }
            }
        }
        return commonJarsUrls;
    }
}
