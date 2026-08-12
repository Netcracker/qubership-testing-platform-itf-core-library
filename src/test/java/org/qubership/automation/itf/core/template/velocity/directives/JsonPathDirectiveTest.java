/*
 * Copyright 2024-2026 Netcracker Technology Corporation
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class JsonPathDirectiveTest {

    private static final String SIMPLE_JSON = "{\"name\":\"John\",\"age\":30,\"city\":\"New York\"}";
    private static final String NESTED_JSON = "{\"store\":{\"book\":[{\"title\":\"Book1\"},{\"title\":\"Book2\"}],\"bicycle\":{\"color\":\"red\"}}}";
    private static final String ARRAY_JSON = "[\"apple\",\"banana\",\"cherry\"]";
    private static final String ERROR_MESSAGE = "Directive '#json_path' arguments are missed. The 1st argument is $json, all others are JsonPath's.";

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

    private JsonPathDirective directive;

    @BeforeEach
    void setUp() {
        directive = new JsonPathDirective();
    }

    // ==================== render TESTS ====================

    @Test
    void render_WithOneJsonPath_ShouldExtractAndWriteValue() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(SIMPLE_JSON);
        when(childNode1.value(context)).thenReturn("$.name");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append("John");
    }

    @Test
    void render_WithMultipleJsonPaths_ShouldExtractAndConcatAllValues() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(3);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(node.jjtGetChild(2)).thenReturn(childNode2);
        when(childNode0.value(context)).thenReturn(SIMPLE_JSON);
        when(childNode1.value(context)).thenReturn("$.name");
        when(childNode2.value(context)).thenReturn("$.age");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append("John");
        verify(writer).append("30");
    }

    @Test
    void render_WithNestedJsonPath_ShouldExtractNestedValue() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(NESTED_JSON);
        when(childNode1.value(context)).thenReturn("$.store.bicycle.color");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append("red");
    }

    @Test
    void render_WithArrayJsonPath_ShouldExtractArrayValues() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(ARRAY_JSON);
        when(childNode1.value(context)).thenReturn("$[1]");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append("banana");
    }

    @Test
    void render_WithJsonPathToArray_ShouldReturnArrayString() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(NESTED_JSON);
        when(childNode1.value(context)).thenReturn("$.store.book[*].title");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append("[\"Book1\",\"Book2\"]");
    }

    @Test
    void render_WithLessThanTwoChildren_ShouldThrowIllegalArgumentException() {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> directive.render(context, writer, node));
        assertEquals(ERROR_MESSAGE, ex.getMessage());
    }

    @Test
    void render_WithZeroChildren_ShouldThrowIllegalArgumentException() {
        // given
        when(node.jjtGetNumChildren()).thenReturn(0);

        // when & then
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> directive.render(context, writer, node));
        assertEquals(ERROR_MESSAGE, ex.getMessage());
    }

    @Test
    void render_WithInvalidJson_ShouldThrowException() {
        // given
        String invalidJson = "not a valid json";

        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(invalidJson);
        when(childNode1.value(context)).thenReturn("$.name");

        // when & then
        assertThrows(Exception.class,
                () -> directive.render(context, writer, node));
    }

    @Test
    void render_WithNonExistentJsonPath_ShouldThrowException() {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(SIMPLE_JSON);
        when(childNode1.value(context)).thenReturn("$.nonexistent");

        // when & then
        assertThrows(Exception.class,
                () -> directive.render(context, writer, node));
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnJsonPath() {
        assertEquals("json_path", directive.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, directive.getType());
    }
}