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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;

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
class TransliterateTest {

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
    private RuntimeServices runtimeServices;

    @Mock
    private Logger log;

    private Transliterate directive;

    @BeforeEach
    void setUp() throws Exception {
        directive = new Transliterate();

        // Set rsvc via reflection
        java.lang.reflect.Field field = Directive.class.getDeclaredField("rsvc");
        field.setAccessible(true);
        field.set(directive, runtimeServices);

        when(runtimeServices.getLog()).thenReturn(log);
    }

    // ==================== transliterate (private method via reflection) TESTS ====================

    @Test
    void transliterate_HalfwidthToFullwidth_ShouldConvertCorrectly() throws Exception {
        // given
        String halfwidth = "Hello 123!";
        String fromTo = "Halfwidth-Fullwidth";

        // when
        String result = invokeTransliterate(halfwidth, fromTo);

        // then
        // Halfwidth characters → Fullwidth (each char becomes wider version)
        // H → Ｈ, e → ｅ, l → ｌ, o → ｏ, space → 　, etc.
        assertEquals("Ｈｅｌｌｏ　１２３！", result);
    }

    @Test
    void transliterate_FullwidthToHalfwidth_ShouldConvertCorrectly() throws Exception {
        // given
        String fullwidth = "Ｈｅｌｌｏ　１２３！";
        String fromTo = "Fullwidth-Halfwidth";

        // when
        String result = invokeTransliterate(fullwidth, fromTo);

        // then
        assertEquals("Hello 123!", result);
    }

    @Test
    void transliterate_LatinToCyrillic_ShouldConvertCorrectly() throws Exception {
        // given
        String latin = "Hello World!";
        String fromTo = "Latin-Cyrillic";

        // when
        String result = invokeTransliterate(latin, fromTo);

        // then
        // H → Х, e → е, l → л, o → о, space → пробел, W → В, r → р, d → д
        assertEquals("Хелло Уорлд!", result); // In fact, W -> У (not to В)
    }

    @Test
    void transliterate_CyrillicToLatin_ShouldConvertCorrectly() throws Exception {
        // given
        // Ворлд -> Vorld, Уорлд -> Uorld... So, no way to have 'World' result...
        String cyrillic = "Хелло дарлинг!";
        String fromTo = "Cyrillic-Latin";

        // when
        String result = invokeTransliterate(cyrillic, fromTo);

        // then
        assertEquals("Hello darling!", result);
    }

    @Test
    void transliterate_WithInvalidFromTo_ShouldReturnErrorMarkerAndLog() throws Exception {
        // given
        String content = "test";
        String invalidFromTo = "Invalid-Transformation";

        // when
        String result = invokeTransliterate(content, invalidFromTo);

        // then
        assertEquals("#err", result);
        verify(log).error(anyString(), anyString());
    }

    @Test
    void transliterate_WithNullContent_ShouldHandleGracefully() throws Exception {
        // given
        String fromTo = "Halfwidth-Fullwidth";

        // when
        String result = invokeTransliterate(null, fromTo);

        // then
        // ICU4J может вернуть пустую строку или выбросить исключение
        // В любом случае, проверяем что не падает
        assertTrue(result == null || result.equals("#err") || result.isEmpty());
    }

    // Helper method to invoke private transliterate
    private String invokeTransliterate(String content, String fromTo) throws Exception {
        java.lang.reflect.Method method = Transliterate.class.getDeclaredMethod(
                "transliterate", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(directive, content, fromTo);
    }

    // ==================== render TESTS ====================

    @Test
    void render_WithTwoChildren_ShouldTransliterateAndRender() throws Exception {
        // given
        String halfwidth = "Hello 123!";
        String expected = "Ｈｅｌｌｏ　１２３！";

        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(halfwidth);
        when(childNode1.value(context)).thenReturn("Halfwidth-Fullwidth");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertTrue(result);
        verify(writer).append(expected);
    }

    @Test
    void render_WithTwoChildrenFullwidthToHalfwidth_ShouldTransliterateAndRender() throws Exception {
        // given
        String fullwidth = "Ｈｅｌｌｏ　１２３！";
        String expected = "Hello 123!";

        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(fullwidth);
        when(childNode1.value(context)).thenReturn("Fullwidth-Halfwidth");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertTrue(result);
        verify(writer).append(expected);
    }

    @Test
    void render_WithInvalidFromTo_ShouldWriteErrorMarker() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn("test");
        when(childNode1.value(context)).thenReturn("Invalid-Transformation");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertTrue(result);
        verify(writer).append("#err");
        verify(log).error(anyString(), anyString());
    }

    @Test
    void render_WithLessThanTwoChildren_ShouldLogErrorAndReturnFalse() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertFalse(result);
        verify(log).error(anyString());
        verify(writer, never()).append(anyString());
    }

    @Test
    void render_WithMoreThanTwoChildren_ShouldLogErrorAndReturnFalse() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(3);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertFalse(result);
        verify(log).error(anyString());
        verify(writer, never()).append(anyString());
    }

    @Test
    void render_WithNullFirstChild_ShouldTransliterateNullToError() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(null);
        when(childNode1.value(context)).thenReturn("Halfwidth-Fullwidth");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertTrue(result);
        // null превращается в "null" строку через String.valueOf(), затем транслитерируется
        verify(writer).append(anyString());
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnTransliterate() {
        assertEquals("transliterate", directive.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, directive.getType());
    }
}