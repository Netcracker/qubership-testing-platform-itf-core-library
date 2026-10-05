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

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.qubership.automation.itf.core.util.loader.fixture.BundledService;

class ChildFirstURLClassLoaderTest {

    private static final String BUNDLED_NAME = BundledService.class.getName();
    private static final String FIXTURE_PREFIX = "org.qubership.automation.itf.core.util.loader.fixture.";

    @TempDir
    Path tempDir;

    @Test
    void classUnderChildFirstPrefixIsLoadedFromOwnJarInsteadOfParent() throws Exception {
        try (ChildFirstURLClassLoader loader = loaderWith(List.of(FIXTURE_PREFIX))) {
            Class<?> loaded = loader.loadClass(BUNDLED_NAME);

            assertSame(loader, loaded.getClassLoader());
            assertNotSame(BundledService.class, loaded);
        }
    }

    @Test
    void classWithoutChildFirstPrefixIsLoadedFromParent() throws Exception {
        try (ChildFirstURLClassLoader loader = loaderWith(List.of())) {
            assertSame(BundledService.class, loader.loadClass(BUNDLED_NAME));
        }
    }

    @Test
    void classUnderChildFirstPrefixThatOwnJarLacksIsLoadedFromParent() throws Exception {
        try (ChildFirstURLClassLoader loader = loaderWith(List.of("java.util."))) {
            assertSame(java.util.ArrayList.class, loader.loadClass("java.util.ArrayList"));
        }
    }

    private ChildFirstURLClassLoader loaderWith(List<String> prefixes) throws IOException {
        URL jar = bundledServiceJar();
        return new ChildFirstURLClassLoader(new URL[] {jar}, getClass().getClassLoader(), prefixes);
    }

    private URL bundledServiceJar() throws IOException {
        String entryName = BUNDLED_NAME.replace('.', '/') + ".class";
        Path jar = tempDir.resolve("bundled.jar");
        try (InputStream classBytes = getClass().getClassLoader().getResourceAsStream(entryName);
             JarOutputStream out = new JarOutputStream(Files.newOutputStream(jar))) {
            out.putNextEntry(new JarEntry(entryName));
            out.write(classBytes.readAllBytes());
            out.closeEntry();
        }
        return jar.toUri().toURL();
    }
}
