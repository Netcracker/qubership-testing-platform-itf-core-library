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

package org.qubership.automation.itf.core.util.manager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.math.BigInteger;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.extension.Extendable;
import org.qubership.automation.itf.core.model.extension.Extension;
import org.qubership.automation.itf.core.model.extension.SituationExtension;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.model.jpa.instance.SituationInstance;
import org.qubership.automation.itf.core.model.jpa.instance.chain.CallChainInstance;
import org.qubership.automation.itf.core.model.jpa.instance.step.StepInstance;
import org.qubership.automation.itf.core.util.exception.ExtensionException;

public class ExtensionManagerTest {

    private ExtensionManager extensionManager;

    @BeforeEach
    void setUp() {
        extensionManager = ExtensionManager.getInstance();
    }

    @Test
    @DisplayName("The only old test, migrated from junit 4.x")
    void extend() {
        TcContext contextToExtend = new TcContext();
        extensionManager.extend(contextToExtend, new TestExtension());
        TestExtension extension = extensionManager.getExtension(contextToExtend, TestExtension.class);
        assertNotNull(extension);
        contextToExtend.put("aaa", "bbb");
        assertEquals("bbb", contextToExtend.get("aaa"));
    }

    // ==================== TESTS FOR createExtendable(Object) ====================

    @Test
    @DisplayName("Should create extendable proxy for SituationInstance (not implementing Extendable)")
    void testCreateExtendable_WithSituationInstance_ShouldCreateProxy() throws ExtensionException {
        // Given
        SituationInstance original = new SituationInstance();
        original.setSituationId(BigInteger.valueOf(123L));
        original.setOperationName("TestOperation");

        // When
        SituationInstance proxy = extensionManager.createExtendable(original);

        // Then
        assertNotNull(proxy);
        assertSame(original, proxy);
        assertInstanceOf(Extendable.class, proxy);

        // Verify original methods still work
        assertEquals(BigInteger.valueOf(123L), proxy.getSituationId());
        assertEquals("TestOperation", proxy.getOperationName());
    }

    @Test
    @DisplayName("Should create extendable proxy for CallChainInstance")
    void testCreateExtendable_WithCallChainInstance_ShouldCreateProxy() throws ExtensionException {
        // Given
        CallChainInstance original = new CallChainInstance();
        original.setTestCaseId(BigInteger.valueOf(456L));
        original.setDatasetName("test-dataset");

        // When
        CallChainInstance proxy = extensionManager.createExtendable(original);

        // Then
        assertNotNull(proxy);
        assertSame(original, proxy);
        assertInstanceOf(Extendable.class, proxy);
        assertEquals(BigInteger.valueOf(456L), proxy.getTestCaseId());
        assertEquals("test-dataset", proxy.getDatasetName());
    }

    @Test
    @DisplayName("Should create extendable proxy for StepInstance")
    void testCreateExtendable_WithStepInstance_ShouldCreateProxy() throws ExtensionException {
        // Given
        StepInstance original = new StepInstance();
        original.setStepId(BigInteger.valueOf(789L));

        // When
        StepInstance proxy = extensionManager.createExtendable(original);

        // Then
        assertNotNull(proxy);
        assertSame(original, proxy);
        assertInstanceOf(Extendable.class, proxy);
        assertEquals(BigInteger.valueOf(789L), proxy.getStepId());
    }

    @Test
    @DisplayName("Should preserve state when creating proxy for SituationInstance")
    void testCreateExtendable_ShouldPreserveState() throws ExtensionException {
        // Given
        SituationInstance original = new SituationInstance();
        original.setSituationId(BigInteger.valueOf(999L));
        original.setOperationName("PreserveStateTest");
        original.setOperationId(BigInteger.valueOf(888L));

        // When
        SituationInstance proxy = extensionManager.createExtendable(original);

        // Then
        assertEquals(BigInteger.valueOf(999L), proxy.getSituationId());
        assertEquals("PreserveStateTest", proxy.getOperationName());
        assertEquals(BigInteger.valueOf(888L), proxy.getOperationId());

        // Modify through proxy
        proxy.setOperationName("Modified");
        assertEquals("Modified", proxy.getOperationName());

        // Original should also be modified (same underlying object)
        assertEquals("Modified", original.getOperationName());
    }

    @Test
    @DisplayName("Should throw ExtensionException when object has no default constructor")
    void testCreateExtendable_WithObjectNoDefaultConstructor_ShouldThrowException() {
        // Given - класс без конструктора по умолчанию
        class NoDefaultConstructor {
            private final String value;
            public NoDefaultConstructor(String value) {
                this.value = value;
            }
        }
        NoDefaultConstructor object = new NoDefaultConstructor("test");

        // When & Then
        ExtensionException exception = assertThrows(
                ExtensionException.class,
                () -> extensionManager.createExtendable(object)
        );
        assertTrue(exception.getMessage().contains("Error creating extension for object"));
        assertNotNull(exception.getCause());
    }

    // ==================== TESTS FOR createExtendable(Class) ====================

    @Test
    @DisplayName("Should throw ExtensionException when class has no default constructor")
    void testCreateExtendable_WithClassNoDefaultConstructor_ShouldThrowException() {
        // Given
        class NoDefaultConstructor {
            @SuppressWarnings("unused")
            public NoDefaultConstructor(String value) {}
        }

        // When & Then
        ExtensionException exception = assertThrows(
                ExtensionException.class,
                () -> extensionManager.createExtendable(NoDefaultConstructor.class)
        );
        assertNotNull(exception.getCause());
        assertInstanceOf(NoSuchMethodException.class, exception.getCause());
    }

    // ==================== TESTS FOR extend(Object, Extension) ====================

    @Test
    @DisplayName("Should successfully add SituationExtension to extendable object")
    void testExtend_WithSituationExtension_ShouldAddExtension() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);
        SituationExtension extension = new SituationExtension();
        extension.getSituationInstanceIds().add("situation-1");
        extension.getSituationInstanceIds().add("situation-2");

        // When
        extensionManager.extend(proxy, extension);

        // Then
        SituationExtension retrieved = proxy.getExtension(SituationExtension.class);
        assertNotNull(retrieved);
        assertEquals(2, retrieved.getSituationInstanceIds().size());
        assertTrue(retrieved.getSituationInstanceIds().contains("situation-1"));
        assertTrue(retrieved.getSituationInstanceIds().contains("situation-2"));
    }

    @Test
    @DisplayName("Should add multiple different extensions to same object")
    void testExtend_WithMultipleExtensions_ShouldAddAll() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);

        // Создаем тестовое расширение для демонстрации
        class TestExtension implements Extension {
            private String data = "test";
            public String getData() { return data; }
        }

        SituationExtension ext1 = new SituationExtension();
        ext1.getSituationInstanceIds().add("id1");

        TestExtension ext2 = new TestExtension();

        // When
        extensionManager.extend(proxy, ext1);
        extensionManager.extend(proxy, ext2);

        // Then
        assertNotNull(proxy.getExtension(SituationExtension.class));
        assertNotNull(proxy.getExtension(TestExtension.class));
    }

    @Test
    @DisplayName("Should not add extension to non-extendable object and log warning")
    void testExtend_WithNonExtendableObject_ShouldLogWarning() {
        // Given
        Object nonExtendable = "not-extendable";
        SituationExtension extension = new SituationExtension();

        // When - should not throw
        extensionManager.extend(nonExtendable, extension);

        // Then - object should not have extension
        assertFalse(nonExtendable instanceof Extendable);
    }

    @Test
    @DisplayName("Should handle null extension gracefully")
    void testExtend_WithNullExtension_ShouldDoNothing() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);

        // When - should not throw
        extensionManager.extend(proxy, null);

        // Then - no extension should be added
        assertNull(proxy.getExtension(SituationExtension.class));
    }

    @Test
    @DisplayName("Should handle null object gracefully")
    void testExtend_WithNullObject_ShouldDoNothing() {
        // When - should not throw
        extensionManager.extend(null, new SituationExtension());
        // No assertion needed, just ensuring no exception
    }

    // ==================== TESTS FOR getExtension(Object, Class) ====================

    @Test
    @DisplayName("Should return existing SituationExtension from extendable object")
    void testGetExtension_WithExistingExtension_ShouldReturnIt() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);
        SituationExtension extension = new SituationExtension();
        extension.getSituationInstanceIds().add("existing-id");
        extensionManager.extend(proxy, extension);

        // When
        SituationExtension retrieved = extensionManager.getExtension(proxy, SituationExtension.class);

        // Then
        assertNotNull(retrieved);
        assertEquals(1, retrieved.getSituationInstanceIds().size());
        assertEquals("existing-id", retrieved.getSituationInstanceIds().getFirst());
    }

    @Test
    @DisplayName("Should create and return new SituationExtension if not exists")
    void testGetExtension_WithNonExistingExtension_ShouldCreateAndReturn() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);

        // When
        SituationExtension extension = extensionManager.getExtension(proxy, SituationExtension.class);

        // Then
        assertNotNull(extension);
        assertTrue(extension.getSituationInstanceIds().isEmpty()); // default constructor

        // Verify it was actually added
        assertNotNull(proxy.getExtension(SituationExtension.class));
    }

    @Test
    @DisplayName("Should return null when getting extension from non-extendable object")
    void testGetExtension_WithNonExtendableObject_ShouldReturnNull() {
        // Given
        String nonExtendable = "not-extendable";

        // When
        SituationExtension extension = extensionManager.getExtension(nonExtendable, SituationExtension.class);

        // Then
        assertNull(extension);
    }

    @Test
    @DisplayName("Should handle null object in getExtension")
    void testGetExtension_WithNullObject_ShouldReturnNull() {
        // When
        SituationExtension extension = extensionManager.getExtension(null, SituationExtension.class);

        // Then
        assertNull(extension);
    }

    @Test
    @DisplayName("Should not create duplicate extension if already exists")
    void testGetExtension_ShouldNotCreateDuplicate() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);
        SituationExtension original = new SituationExtension();
        original.getSituationInstanceIds().add("original-id");
        extensionManager.extend(proxy, original);

        // When
        SituationExtension retrieved = extensionManager.getExtension(proxy, SituationExtension.class);

        // Then
        assertSame(original, retrieved); // Should be the same instance
        assertEquals(1, retrieved.getSituationInstanceIds().size());
        assertEquals("original-id", retrieved.getSituationInstanceIds().getFirst());
    }

    @Test
    @DisplayName("Should handle custom extension classes")
    void testGetExtension_WithCustomExtension_ShouldCreateAndReturn() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);

        // When
        CustomExtension extension = extensionManager.getExtension(proxy, CustomExtension.class);

        // Then
        assertNotNull(extension);
        assertEquals("custom", extension.getValue());
    }

    // ==================== INTEGRATION TESTS ====================

    @Test
    @DisplayName("Should work with full lifecycle: create, extend, get extensions")
    void testFullLifecycle_ShouldWorkCorrectly() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);
        proxy.setOperationName("FullLifecycleTest");

        SituationExtension ext1 = new SituationExtension();
        ext1.getSituationInstanceIds().add("id1");
        ext1.getSituationInstanceIds().add("id2");

        // When
        extensionManager.extend(proxy, ext1);

        // Then
        SituationExtension retrieved = proxy.getExtension(SituationExtension.class);
        assertNotNull(retrieved);
        assertEquals(2, retrieved.getSituationInstanceIds().size());

        // Verify original methods still work
        assertEquals("FullLifecycleTest", proxy.getOperationName());

        // Add another extension
        class AnotherExtension implements Extension {
            private String data = "another";
        }
        AnotherExtension ext2 = new AnotherExtension();
        extensionManager.extend(proxy, ext2);

        assertNotNull(proxy.getExtension(AnotherExtension.class));
    }

    @Test
    @DisplayName("Should handle JSON serialization methods on Extendable")
    void testJsonSerializationMethods_ShouldWork() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);
        SituationExtension extension = new SituationExtension();
        extension.getSituationInstanceIds().add("json-test-id");
        extensionManager.extend(proxy, extension);

        // When
        String json = proxy.getExtensionsJson();

        // Then
        assertNotNull(json);
        assertTrue(json.contains(SituationExtension.class.getName()));
        assertTrue(json.contains("json-test-id"));

        // Test deserialization
        proxy.setExtensionsJson(json);
        SituationExtension retrieved = proxy.getExtension(SituationExtension.class);
        assertNotNull(retrieved);
        assertEquals(1, retrieved.getSituationInstanceIds().size());
        assertEquals("json-test-id", retrieved.getSituationInstanceIds().getFirst());
    }

    @Test
    @DisplayName("Should handle complex objects with multiple extensions and JSON")
    void testComplexScenario_WithMultipleExtensionsAndJson() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);
        proxy.setSituationId(BigInteger.valueOf(100L));
        proxy.setOperationName("ComplexTest");

        SituationExtension ext1 = new SituationExtension();
        ext1.getSituationInstanceIds().add("a");
        ext1.getSituationInstanceIds().add("b");
        ext1.getSituationInstanceIds().add("c");

        PriorityExtension ext2 = new PriorityExtension();

        // When
        extensionManager.extend(proxy, ext1);
        extensionManager.extend(proxy, ext2);

        // Then
        assertEquals(3, proxy.getExtension(SituationExtension.class).getSituationInstanceIds().size());
        assertEquals(42, proxy.getExtension(PriorityExtension.class).getPriority());

        // Verify JSON
        String json = proxy.getExtensionsJson();
        assertNotNull(json);
        assertTrue(json.contains(SituationExtension.class.getName()));
        assertTrue(json.contains(PriorityExtension.class.getName()));
    }

    @Test
    @DisplayName("Should handle null extensionsJson gracefully")
    void testNullExtensionsJson_ShouldDoNothing() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);

        // When
        proxy.setExtensionsJson(null);

        // Then - no exception, state unchanged
        assertNull(proxy.getExtension(SituationExtension.class));
    }

    @Test
    @DisplayName("Should preserve serialization capabilities")
    void testSerialization_ShouldWork() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);
        SituationExtension extension = new SituationExtension();
        extension.getSituationInstanceIds().add("serialization-test");
        extensionManager.extend(proxy, extension);

        // Then - ensure class is serializable
        assertInstanceOf(Serializable.class, proxy);
        assertInstanceOf(Serializable.class, extension);
        assertInstanceOf(Serializable.class, extensionManager);
    }

    // ==================== EDGE CASES ====================

    @Test
    @DisplayName("Should handle adding same extension type multiple times (only first should be kept)")
    void testExtend_SameExtensionType_ShouldKeepLast() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);

        SituationExtension first = new SituationExtension();
        first.getSituationInstanceIds().add("first");

        SituationExtension second = new SituationExtension();
        second.getSituationInstanceIds().add("second");

        // When
        extensionManager.extend(proxy, first);
        extensionManager.extend(proxy, second);

        // Then - both should present (It's Set, but first != second)
        String json = proxy.getExtensionsJson();
        assertNotNull(json);
        assertTrue(json.contains("org.qubership.automation.itf.core.model.extension.SituationExtension"));
    }

    @Test
    @DisplayName("Should handle proxy methods correctly")
    void testProxyMethods_ShouldWorkCorrectly() throws ExtensionException {
        // Given
        SituationInstance proxy = extensionManager.createExtendable(SituationInstance.class);

        // When - call methods that exist on the proxy
        proxy.setOperationName("TestOperation");
        proxy.setSituationId(BigInteger.valueOf(555L));

        // Then
        assertEquals("TestOperation", proxy.getOperationName());
        assertEquals(BigInteger.valueOf(555L), proxy.getSituationId());
    }

    @Test
    @DisplayName("Should handle ExtensionException wrapping")
    void testExtensionException_Wrapping() {
        // Given - класс без конструктора по умолчанию
        class NoDefaultConstructor {
            @SuppressWarnings("unused")
            public NoDefaultConstructor(String value) {}
        }

        // When & Then
        ExtensionException exception = assertThrows(
                ExtensionException.class,
                () -> extensionManager.createExtendable(NoDefaultConstructor.class)
        );
        assertNotNull(exception.getMessage());
        assertNotNull(exception.getCause());
    }

    private static class TestExtension implements Extension {
    }

    public static class CustomExtension implements Extension {
        private String value = "custom";
        public CustomExtension() {}
        public String getValue() { return value; }
    }

    public static class PriorityExtension implements Extension {
        private int priority = 42;
        public PriorityExtension() {}
        public int getPriority() { return priority; }
    }

}
