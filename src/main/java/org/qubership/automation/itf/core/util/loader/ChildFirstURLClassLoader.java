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

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.Collections;

/**
 * Class loader for transport and trigger libraries.
 *
 * <p>Delegation is parent-first, except for the classes whose names start with one of the child-first prefixes passed
 * to the constructor: those are loaded from this loader's own URLs when they are present there, and from the parent
 * otherwise. A loader created without prefixes is parent-first for every class.</p>
 */
public class ChildFirstURLClassLoader extends URLClassLoader {

    private final String[] childFirstPrefixes;
    private ClassLoader realParentLoader;

    public ChildFirstURLClassLoader(URL[] urls) {
        super(urls);
        this.childFirstPrefixes = new String[0];
    }

    public ChildFirstURLClassLoader(URL[] urls, ClassLoader realParentLoader) {
        this(urls, realParentLoader, Collections.emptyList());
    }

    /**
     * Creates a loader that loads the classes under the given prefixes from its own URLs first.
     *
     * @param urls                URLs of the libraries this loader owns
     * @param realParentLoader    parent loader, consulted first for every class not under a child-first prefix
     * @param childFirstPrefixes  class name prefixes such as {@code "org.glassfish.hk2."}; may be empty
     */
    public ChildFirstURLClassLoader(URL[] urls, ClassLoader realParentLoader, Collection<String> childFirstPrefixes) {
        super(urls, realParentLoader);//TODO: super(urls, null)
        this.realParentLoader = realParentLoader;
        this.childFirstPrefixes = childFirstPrefixes.toArray(new String[0]);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (!isChildFirst(name)) {
            return super.loadClass(name, resolve);
        }
        synchronized (getClassLoadingLock(name)) {
            Class<?> loaded = findLoadedClass(name);
            if (loaded == null) {
                try {
                    loaded = super.findClass(name);
                } catch (ClassNotFoundException e) {
                    return super.loadClass(name, resolve);
                }
            }
            if (resolve) {
                resolveClass(loaded);
            }
            return loaded;
        }
    }

    private boolean isChildFirst(String name) {
        for (String prefix : childFirstPrefixes) {
            if (name.startsWith(prefix)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Class<?> findClass(String name) throws ClassNotFoundException {
        try {
            return super.findClass(name);
        } catch (ClassNotFoundException e) {
            if (realParentLoader != null) {
                return realParentLoader.loadClass(name);
            }
            throw e;
        }
    }

    public void setRealParentLoader(ClassLoader realParentLoader) {
        this.realParentLoader = realParentLoader;
    }
}
