/*
 * Copyright 2024-2026 NetCracker Technology Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.qubership.automation.itf.core.template.velocity.directives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.RuntimeServices;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComputeHashTest {

    private static final String TEST_CONTENT = "Hello World";
    private static final String SHA1_HASH = DigestUtils.sha1Hex(TEST_CONTENT);
    private static final String SHA256_HASH = DigestUtils.sha256Hex(TEST_CONTENT);
    private static final String MD5_HASH = DigestUtils.md5Hex(TEST_CONTENT);
    private static final String TEST_KEY = "01234567890123456789012345678901"; // 32 bytes for AES-256
    private static final String TEST_HMAC_KEY = "secret-key-for-hmac";
    @Mock
    private InternalContextAdapter context;

    @Mock
    private Writer writer;

    @Mock
    private Node node;

    @Mock
    private Node childNode0;

    @Mock
    private Node childNode1;

    @Mock
    private Node childNode2;

    @Mock
    private Node childNode3;

    @Mock
    private RuntimeServices runtimeServices;

    @Mock
    private Logger log;

    private ComputeHash directive;

    @BeforeEach
    void setUp() throws Exception {
        directive = new ComputeHash();

        java.lang.reflect.Field field = Directive.class.getDeclaredField("rsvc");
        field.setAccessible(true);
        field.set(directive, runtimeServices);

        when(runtimeServices.getLog()).thenReturn(log);
    }

    // ==================== computeHash (private method) TESTS ====================

    @Test
    void computeHash_Sha1_ShouldReturnCorrectHash() throws Exception {
        String result = invokeComputeHash("SHA-1", TEST_CONTENT, "UTF-8", null, null);
        assertEquals(SHA1_HASH, result);
    }

    @Test
    void computeHash_Sha256_ShouldReturnCorrectHash() throws Exception {
        String result = invokeComputeHash("SHA-256", TEST_CONTENT, "UTF-8", null, null);
        assertEquals(SHA256_HASH, result);
    }

    @Test
    void computeHash_Md5_ShouldReturnCorrectHash() throws Exception {
        String result = invokeComputeHash("MD5", TEST_CONTENT, "UTF-8", null, null);
        assertEquals(MD5_HASH, result);
    }

    @Test
    void computeHash_WithBlankContent_ShouldReturnEmptyString() throws Exception {
        String result = invokeComputeHash("SHA-1", "", "UTF-8", null, null);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void computeHash_WithNullContent_ShouldReturnEmptyString() throws Exception {
        String result = invokeComputeHash("SHA-1", null, "UTF-8", null, null);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void computeHash_WithUnknownAlgorithm_ShouldThrowException() {
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                invokeComputeHash("UNKNOWN", TEST_CONTENT, "UTF-8", null, null));

        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Wrong algorithm 'UNKNOWN'"));
    }

    // Helper method to invoke private computeHash
    private String invokeComputeHash(String algorithm, String content, String encoding,
                                     String key, Boolean encodeToBase64) throws Exception {
        Method method = ComputeHash.class.getDeclaredMethod("computeHash",
                String.class, String.class, String.class, String.class, Boolean.class);
        method.setAccessible(true);
        return (String) method.invoke(directive, algorithm, content, encoding, key, encodeToBase64);
    }

    // ==================== render TESTS ====================

    @Test
    void render_WithOneParameter_ShouldUseDefaultSha1() throws Exception {
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(TEST_CONTENT);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer).append(SHA1_HASH);
    }

    @Test
    void render_WithTwoParameters_ShouldUseSpecifiedAlgorithm() throws Exception {
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn("SHA-256");
        when(childNode1.value(context)).thenReturn(TEST_CONTENT);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer).append(SHA256_HASH);
    }

    @Test
    void render_WithInvalidAlgorithm_ShouldThrowException() {
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn("INVALID");
        when(childNode1.value(context)).thenReturn(TEST_CONTENT);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                directive.render(context, writer, node));

        assertTrue(ex.getMessage().contains("Wrong algorithm 'INVALID'"));
    }

    @Test
    void render_WithInvalidHmacAlgorithm_ShouldThrowException() {
        // given
        when(node.jjtGetNumChildren()).thenReturn(4);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(node.jjtGetChild(2)).thenReturn(childNode2);
        when(node.jjtGetChild(3)).thenReturn(childNode3);
        when(childNode0.value(context)).thenReturn("HMAC-INVALID");
        when(childNode1.value(context)).thenReturn(TEST_CONTENT);
        when(childNode2.value(context)).thenReturn("UTF-8");
        when(childNode3.value(context)).thenReturn(TEST_HMAC_KEY);

        // when & then
        assertThrows(IllegalArgumentException.class, () ->
                directive.render(context, writer, node));
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnHashsum() {
        assertEquals("hashsum", directive.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, directive.getType());
    }
}