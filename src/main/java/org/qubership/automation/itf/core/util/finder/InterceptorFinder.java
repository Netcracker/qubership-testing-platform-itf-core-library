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

package org.qubership.automation.itf.core.util.finder;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import jakarta.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.util.config.Config;
import org.reflections.Reflections;

public class InterceptorFinder {
    /**
     * Returns all of subclasses of {@link Interceptor} in specified package.
     *
     * @param path - package where interceptor is defined
     * @return Set of classes which extends of {@link Interceptor}
     */
    @Nonnull
    public static Set<Class<? extends Interceptor>> find(@Nonnull String path) {
        if (StringUtils.isBlank(path)) {
            throw new IllegalArgumentException("The package of interceptors is not specified");
        }
        Reflections reflections = new Reflections(path);
        return reflections.getSubTypesOf(Interceptor.class);
    }

    /**
     * Returns all of subclasses of {@link Interceptor} from package which specified in
     * config.properties by prefix "interceptor"
     * for example:
     * interceptor.encryption=org.qubership.automation.itf.encryption
     * interceptor.decryption=org.qubership.automation.itf.decryption
     * the method will take this paths and find all of interceptor in both packages
     *
     * @return Set of classes which extends of {@link Interceptor}
     */
    @Nonnull
    public static Set<Class<? extends Interceptor>> find() {
        Set<Class<? extends Interceptor>> interceptors = new HashSet<>(10);
        Collection<Object> interceptorPaths = Config.getConfig().getByPrefix("interceptor").getValues();
        if (interceptorPaths != null && !interceptorPaths.isEmpty()) {
            interceptorPaths.forEach(value -> interceptors.addAll(find((String) value)));
        }
        return interceptors;
    }

}
