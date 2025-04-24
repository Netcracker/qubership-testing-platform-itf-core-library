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

package org.qubership.automation.itf.core.util.loader.base;

import static java.util.regex.Pattern.MULTILINE;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.util.loader.ChildFirstURLClassLoader;
import org.qubership.automation.itf.core.util.loader.helper.LoaderHelper;
import org.reflections.Reflections;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractLoader<T> implements Loader<T> {

    protected static String LIB;
    protected static String PATH_PATTERN;
    @Getter
    private final Map<String, ClassLoader> classLoaderHolder = Maps.newHashMapWithExpectedSize(10);
    private ClassLoader libClassLoader;

    protected abstract Class<T> getGenericType();

    /**
     * Load project's transport\trigger libraries (and custom libs).
     */
    public void load(String path, String customLibPath) {
        File file = new File(path);
        if (!file.exists()) {
            throw new IllegalArgumentException("File or directory by path '" + path + "' doesn't exist");
        }
        try {
            boolean isCustomLibPathSet = !StringUtils.isBlank(customLibPath);
            List<URL[]> urlsList = parseURLArrayByTransportType(LoaderHelper.getUrls(file));
            for (URL[] urls : urlsList) {
                ChildFirstURLClassLoader libClassLoader;
                if (isCustomLibPathSet) {
                    libClassLoader = (ChildFirstURLClassLoader) loadLibClassLoader(
                            path + LIB + "/" + parseUrlName(urls[0]), path + LIB,
                            customLibPath + "/" + parseUrlName(urls[0]), customLibPath);
                } else {
                    libClassLoader = (ChildFirstURLClassLoader) loadLibClassLoader(
                            path + LIB + "/" + parseUrlName(urls[0]), path + LIB);
                }
                ChildFirstURLClassLoader classLoader = new ChildFirstURLClassLoader(urls, libClassLoader);
                addClassesIntoClassLoaderHolder(find(classLoader), classLoader);
            }
        } catch (MalformedURLException e) {
            throw new IllegalArgumentException("Unable to convert file path '" + path + "' to url.", e);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to load libs for '" + file.getName() + "' file.", e);
        }
    }

    public void cleanClassLoaders() {
        this.classLoaderHolder.clear();
    }

    public void setLibLoader(ClassLoader classLoader) {
        libClassLoader = classLoader;
    }

    private Set<Class<? extends T>> find(ClassLoader classLoader) {
        Reflections reflections = new Reflections("org.qubership", classLoader);
        Set<Class<? extends T>> classes = Sets.newHashSet();
        reflections.getSubTypesOf(getGenericType()).forEach(clazz -> {
            if (!Modifier.isAbstract(clazz.getModifiers())) {
                classes.add(clazz);
            }
        });
        validateClasses(classes);
        return classes;
    }

    protected abstract void validateClasses(Set<Class<? extends T>> classes);

    protected void addClassesIntoClassLoaderHolder(Set<Class<? extends T>> classes, ClassLoader classLoader) {
        for (Class<? extends T> clazz : classes) {
            classLoaderHolder.put(clazz.getName(), classLoader);
        }
    }

    private ClassLoader loadLibClassLoader(String... paths) throws IOException {
        if (Objects.isNull(libClassLoader)) {
            List<URL> urls = Lists.newArrayList();
            for (String path : paths) {
                if (StringUtils.isBlank(path)) {
                    continue;
                }
                File fileForLib = new File(path);
                if (!fileForLib.exists()) {
                    log.warn("File or directory by path '{}' doesn't exist", path);
                    continue;
                }
                urls.addAll(Arrays.asList(LoaderHelper.getUrls(fileForLib)));
            }
            return new ChildFirstURLClassLoader(LoaderHelper.toArray(urls), this.getClass().getClassLoader());
        }
        return libClassLoader;
    }

    private List<URL[]> parseURLArrayByTransportType(URL[] urls) {
        Map<String, List<URL>> cache = Maps.newHashMap();
        for (URL url : urls) {
            String urlName = parseUrlName(url);
            List<URL> urls1 = cache.get(urlName);
            if (Objects.isNull(urls1)) {
                cache.put(urlName, Lists.newArrayList());
            }
            cache.get(urlName).add(url);
        }
        List<URL[]> list = Lists.newArrayList();
        for (Map.Entry<String, List<URL>> entry : cache.entrySet()) {
            list.add(entry.getValue().toArray(new URL[0]));
        }
        return list;
    }

    private String parseUrlName(URL url) {
        String urlPath = url.getPath();
        Pattern compile = Pattern.compile(PATH_PATTERN, MULTILINE);
        Matcher matcher = compile.matcher(urlPath);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }
}
