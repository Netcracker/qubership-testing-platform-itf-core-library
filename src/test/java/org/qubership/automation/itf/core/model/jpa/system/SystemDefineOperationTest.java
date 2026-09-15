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

package org.qubership.automation.itf.core.model.jpa.system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.util.engine.TemplateEngine;
import org.qubership.automation.itf.core.util.engine.TemplateEngineFactory;
import org.qubership.automation.itf.core.util.exception.OperationDefinitionException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

/**
 * Regression test for ERR-08: {@code defineOperation} used to catch its own
 * {@link OperationDefinitionException}, which already names the system and states exactly what is
 * wrong, and replace it with a top-level "Cannot process operation definition" that names neither.
 * A caller reading only the top-level message, including through the library's own
 * {@code Exceptions.getExceptionSummary}, saw the uninformative text instead of the diagnosis.
 *
 * <p>{@link TemplateEngineFactory}'s and {@link CoreObjectManager}'s singleton fields are shared by
 * every caller in the JVM, so each test saves them before running and restores them afterward to
 * avoid leaking state into other tests.</p>
 */
class SystemDefineOperationTest {

    private static Field templateEngineInstanceField;
    private static Field coreObjectManagerServiceField;

    private Object originalTemplateEngineInstance;
    private Object originalCoreObjectManagerService;

    @BeforeAll
    static void resolveFields() throws Exception {
        templateEngineInstanceField = TemplateEngineFactory.class.getDeclaredField("INSTANCE");
        templateEngineInstanceField.setAccessible(true);
        coreObjectManagerServiceField = CoreObjectManager.class.getDeclaredField("staticCoreObjectManagerService");
        coreObjectManagerServiceField.setAccessible(true);
    }

    @BeforeEach
    void clearState() throws Exception {
        originalTemplateEngineInstance = templateEngineInstanceField.get(null);
        originalCoreObjectManagerService = coreObjectManagerServiceField.get(null);
        templateEngineInstanceField.set(null, null);
        coreObjectManagerServiceField.set(null, null);
    }

    @AfterEach
    void restoreState() throws Exception {
        templateEngineInstanceField.set(null, originalTemplateEngineInstance);
        coreObjectManagerServiceField.set(null, originalCoreObjectManagerService);
    }

    @Test
    void missingOperationKeyDefinitionSurfacesItsOwnMessageAtTheTopLevel() {
        System system = new System();
        system.setName("MySystem");

        OperationDefinitionException thrown = assertThrows(OperationDefinitionException.class,
                () -> system.defineOperation(null));

        assertEquals("Cannot define operation! Operation key definition is null! System: MySystem",
                thrown.getMessage());
        assertNull(thrown.getCause());
    }

    @Test
    void emptyOperationKeySurfacesItsOwnMessageAtTheTopLevel() throws Exception {
        templateEngineInstanceField.set(null, engineThatAlwaysReturns(""));
        System system = new System();
        system.setName("MySystem");
        system.setOperationKeyDefinition("${missingProperty}");

        OperationDefinitionException thrown = assertThrows(OperationDefinitionException.class,
                () -> system.defineOperation(null));

        assertEquals("Cannot define operation! Key defined by definition ${missingProperty} in empty! "
                + "System: MySystem", thrown.getMessage());
        assertNull(thrown.getCause());
    }

    @Test
    void unexpectedFailureIsStillWrappedWithTheSystemName() throws Exception {
        templateEngineInstanceField.set(null, engineThatAlwaysReturns("static-key"));
        System system = new System();
        system.setName("MySystem");
        system.setOperationKeyDefinition("static-key");

        OperationDefinitionException thrown = assertThrows(OperationDefinitionException.class,
                () -> system.defineOperation(null));

        assertEquals("Cannot process operation definition for system MySystem", thrown.getMessage());
        assertInstanceOf(IllegalStateException.class, thrown.getCause());
    }

    private static TemplateEngine engineThatAlwaysReturns(String result) {
        return new TemplateEngine() {
            @Override
            public String process(Storable owner, String someString, JsonContext context) {
                return result;
            }

            @Override
            public String process(Map<String, Storable> storables, String someString, JsonContext context) {
                return result;
            }

            @Override
            public String process(Storable owner, String someString, JsonContext context, String coords) {
                return result;
            }

            @Override
            public String process(Map<String, Storable> storables, String someString, JsonContext context,
                    String coords) {
                return result;
            }
        };
    }
}
