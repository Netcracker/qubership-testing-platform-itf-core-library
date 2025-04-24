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

package org.qubership.automation.itf.core.util.loader.helper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class LoaderHelper {

    private static final String jar = ".jar";
    private static final String zip = ".zip";
    private static final String clazz = ".class";

    /**
     * Get Urls array for class loader.
     */
    public static URL[] getUrls(File file) throws IOException {
        if (file.isDirectory()) {
            File[] files = file.listFiles(pathname -> {
                String name = pathname.getName();
                return name.endsWith(jar) || name.endsWith(zip) || name.endsWith(clazz);
            });
            if (files != null) {
                List<URL> urls = new ArrayList<>();
                for (File file1 : files) {
                    if (!file1.isDirectory()) {
                        if (file1.getName().endsWith(jar)) {
                            urls.addAll(getJarLibs(file1));
                        }
                        urls.add(file1.toURI().toURL());
                    }
                }
                return toArray(urls);
            }
        }
        return new URL[]{file.toURI().toURL()};
    }

    public static URL[] toArray(List<URL> urls) {
        return urls.toArray(new URL[urls.size()]);
    }

    private static Collection<URL> getJarLibs(File jarFile) throws IOException {
        Collection<URL> result = new ArrayList<>();
        URL url = new URL("jar:file:" + jarFile.getPath() + "!/");
        result.add(url);
        return result;
    }
}
