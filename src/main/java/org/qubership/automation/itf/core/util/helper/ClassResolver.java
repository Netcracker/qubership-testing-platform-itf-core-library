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

package org.qubership.automation.itf.core.util.helper;

import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.itf.core.util.annotation.UserName;
import org.reflections.Reflections;

public class ClassResolver {

    private Map<String, Map<String, String>> classNameWithUserNameCache = new HashMap<>();
    private Reflections reflections = Reflection.getReflections();

    public static ClassResolver getInstance() {
        return ClassResolverCacheHolder.instance;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Map<String, String> resolveByInterface(Class clazz) {
        Map<String, String> classNameWithUserName = classNameWithUserNameCache.get(clazz.getName());
        if (classNameWithUserName != null) {
            return classNameWithUserName;
        }

        classNameWithUserName = new HashMap<>();

        Set<Class> subClasses = getSubtypesOf(clazz);
        for (Class subClass : subClasses) {
            if (!(Modifier.isAbstract(subClass.getModifiers()))) {
                if (subClass.isAnnotationPresent(UserName.class)) {
                    classNameWithUserName.put(subClass.getName(),
                            ((UserName) subClass.getAnnotation(UserName.class)).value());
                } else {
                    classNameWithUserName.put(subClass.getName(), subClass.getSimpleName());
                }
            }
        }

        if (!classNameWithUserName.isEmpty()) {
            classNameWithUserNameCache.put(clazz.getName(), classNameWithUserName);
        }

        return classNameWithUserName;
    }

    private ClassResolver() {
    }

    private static class ClassResolverCacheHolder {
        private static final ClassResolver instance = new ClassResolver();

    }

    public Set<Class> getSubtypesOf(Class clazz) {
        return this.reflections.getSubTypesOf(clazz);
    }

    public Set<Class> getSubtypesOf(Class clazz, Reflections reflections) {
        return reflections.getSubTypesOf(clazz);
    }
}
