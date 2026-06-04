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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;
import java.util.List;
import java.util.Map;

import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.skyscreamer.jsonassert.JSONAssert;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ToJsonStringTest {

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
    private JsonContext jsonContext;

    private ToJsonString directive;

    @BeforeEach
    void setUp() {
        directive = new ToJsonString();
    }

    // ==================== render TESTS ====================

    @Test
    void render_WithOneParameterMap_ShouldConvertMapToJson() throws Exception {
        Map<String, String> map = Map.of("key1", "value1", "key2", "value2");

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(map);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);

        // Instead of String comparison, JSONAssert.assertEquals is used.
        // Because Map.of doesn't guarantee entries order.

        //verify(writer).append("{\"key1\":\"value1\",\"key2\":\"value2\"}");

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).append(captor.capture());

        String actualJson = captor.getValue();
        String expectedJson = "{\"key1\":\"value1\",\"key2\":\"value2\"}";

        JSONAssert.assertEquals(expectedJson, actualJson, false);
    }

    @Test
    void render_WithOneParameterList_ShouldConvertListToJson() throws Exception {
        List<String> list = List.of("item1", "item2", "item3");

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(list);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer).append("[\"item1\",\"item2\",\"item3\"]");
    }

    @Test
    void render_WithOneParameterString_ShouldWriteStringAsIs() throws Exception {
        String text = "plain text string";

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(text);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer).append(text);
    }

    @Test
    void render_WithOneParameterNumber_ShouldWriteNumberAsString() throws Exception {
        Integer number = 12345;

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(number);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer).append("12345");
    }

    @Test
    void render_WithOneParameterJsonContext_ShouldUseContextJsonString() throws Exception {
        String jsonString = "{\"context\":\"value\"}";
        when(jsonContext.getJsonString()).thenReturn(jsonString);

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(jsonContext);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer).append(jsonString);
    }

    @Test
    void render_WithTwoParametersAndPrettyPrintTrue_ShouldFormatJson() throws Exception {
        Map<String, String> map = Map.of("key", "value");

        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(map);
        when(childNode1.value(context)).thenReturn(true);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        // Pretty-printed JSON has newlines and spaces
        verify(writer).append(org.mockito.ArgumentMatchers.contains("\n"));
    }

    @Test
    void render_WithTwoParametersAndPrettyPrintFalse_ShouldNotFormatJson() throws Exception {
        Map<String, String> map = Map.of("key", "value");

        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(map);
        when(childNode1.value(context)).thenReturn(false);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer).append("{\"key\":\"value\"}");
    }

    @Test
    void render_WithTwoParametersAndPrettyPrintStringTrue_ShouldParseBoolean() throws Exception {
        Map<String, String> map = Map.of("key", "value");

        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(map);
        when(childNode1.value(context)).thenReturn("true");

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer).append(org.mockito.ArgumentMatchers.contains("\n"));
    }

    @Test
    void render_WithNullObject_ShouldDoNothing() throws Exception {
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(null);

        boolean result = directive.render(context, writer, node);

        assertTrue(result);
        verify(writer, never()).append(anyString());
    }

    @Test
    void render_WithNoParameters_ShouldThrowException() {
        when(node.jjtGetNumChildren()).thenReturn(0);

        assertThrows(IllegalArgumentException.class, () ->
                directive.render(context, writer, node));
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnToJson() {
        assertEquals("toJson", directive.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, directive.getType());
    }
}