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

package org.qubership.automation.itf.core.model.extension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Fails if a class name read from serialized extension JSON goes back to being initialized before
 * {@code setExtensionsJson} checks whether it actually implements {@link Extension} (SEC-05).
 */
class ExtendableImplTest {

    /**
     * Records, in load order, the name of every class among {@link PoisonNotExtension} and
     * {@link ProbeExtension} whose static initializer has run. Recording the name here rather than
     * on a field of the class itself matters: reading a static field of {@link PoisonNotExtension}
     * or {@link ProbeExtension} directly would trigger its initialization as a side effect of the
     * check, defeating the point of the test.
     */
    static final class InitTracker {
        private static final List<String> INITIALIZED = new ArrayList<>();

        private InitTracker() {
        }

        static void record(String className) {
            INITIALIZED.add(className);
        }

        static boolean wasInitialized(String className) {
            return INITIALIZED.contains(className);
        }
    }

    static class PoisonNotExtension {
        static {
            InitTracker.record(PoisonNotExtension.class.getName());
        }
    }

    public static class ProbeExtension implements Extension {
        static {
            InitTracker.record(ProbeExtension.class.getName());
        }
    }

    @Test
    void doesNotInitializeAClassThatFailsTheExtensionCheck() {
        ExtendableImpl extendable = new ExtendableImpl();
        String json = "{\"" + PoisonNotExtension.class.getName() + "\": {}}";

        extendable.setExtensionsJson(json);

        assertFalse(InitTracker.wasInitialized(PoisonNotExtension.class.getName()),
                "a class rejected by asSubclass(Extension.class) must not have been initialized first");
    }

    @Test
    void stillLoadsAndInitializesAClassThatPassesTheExtensionCheck() {
        ExtendableImpl extendable = new ExtendableImpl();
        String json = "{\"" + ProbeExtension.class.getName() + "\": {}}";

        extendable.setExtensionsJson(json);

        assertTrue(InitTracker.wasInitialized(ProbeExtension.class.getName()),
                "a class that does implement Extension must still be loaded and used normally");
        assertNotNull(extendable.getExtension(ProbeExtension.class));
    }
}
