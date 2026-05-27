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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;

import org.apache.commons.lang3.StringEscapeUtils;
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
class EscapeXmlTest {

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

    private EscapeXml directive;

    @BeforeEach
    void setUp() throws Exception {
        directive = new EscapeXml();

        // Set rsvc via reflection
        java.lang.reflect.Field field = Directive.class.getDeclaredField("rsvc");
        field.setAccessible(true);
        field.set(directive, runtimeServices);

        when(runtimeServices.getLog()).thenReturn(log);
    }

    // ==================== render TESTS ====================

    @Test
    void render_WithOneChild_ShouldEscapeXmlContent() throws Exception {
        // given
        String rawContent = "<test>&\"'</test>";
        String expectedEscaped = StringEscapeUtils.escapeXml10(rawContent);

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(rawContent);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append(expectedEscaped);
    }

    @Test
    void render_WithMultipleChildren_ShouldEscapeAndConcatAll() throws Exception {
        // given
        String content1 = "<tag1>";
        String content2 = "&content2";
        String content3 = "'quoted'";

        String expectedEscaped1 = StringEscapeUtils.escapeXml10(content1); // = "&lt;tag1&gt;";
        String expectedEscaped2 = StringEscapeUtils.escapeXml10(content2); // = "&amp;content2";
        String expectedEscaped3 = StringEscapeUtils.escapeXml10(content3); // = "&apos;quoted&apos;";

        when(node.jjtGetNumChildren()).thenReturn(3);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(node.jjtGetChild(2)).thenReturn(childNode0); // reuse for simplicity
        when(childNode0.value(context)).thenReturn(content1, content3);
        when(childNode1.value(context)).thenReturn(content2);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append(expectedEscaped1);
        verify(writer).append(expectedEscaped2);
        verify(writer).append(expectedEscaped3);
    }

    @Test
    void render_WithNullChild_ShouldLogWarningAndSkip() throws Exception {
        String normalText = "normal text";

        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(null);
        when(childNode0.value(context)).thenReturn(normalText);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(log).warn("escape_xml: child node is null");
        verify(writer).append(normalText);
    }

    @Test
    void render_WithNullValue_ShouldTreatAsEmptyString() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(null);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append(StringUtils.EMPTY);
    }

    @Test
    void render_WithEmptyChildren_ShouldRenderNothing() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(0);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer, never()).append(anyString());
    }

    @Test
    void render_WithSpecialXmlCharacters_ShouldEscapeCorrectly() throws Exception {
        // given
        String rawContent = "a & b < c > d \" e ' f";
        String expectedEscaped = StringEscapeUtils.escapeXml10(rawContent);

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(rawContent);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append(expectedEscaped);
    }

    @Test
    void render_WithNoSpecialCharacters_ShouldNotChangeContent() throws Exception {
        // given
        String rawContent = "Hello World 123";

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(rawContent);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append(rawContent);
    }

    @Test
    void render_WithNumericValue_ShouldConvertToString() throws Exception {
        // given
        Integer numericValue = 12345;

        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(numericValue);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append(numericValue.toString());
    }

    @Test
    void render_WithEmptyString_ShouldAppendEmpty() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn("");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(writer).append("");
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnEscapeXml() {
        assertEquals("escape_xml", directive.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, directive.getType());
    }
}