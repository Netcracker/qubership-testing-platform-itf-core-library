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

package org.qubership.automation.itf.core.template.velocity.directives;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;

import org.apache.commons.lang3.StringUtils;
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
class EncodeDecodeBase64Test {

    private static final String TEST_CONTENT = """
            Test content to encode base64.
            ABCDEF GHIJKL MNOPQR STUVWX YZ
            abcdef ghijkl mnopqr stuvwx yz
            АБВГДЕ ЁЖЗИЙК ЛМНОПР СТУФХЦ ЧШЩЪЫЬ ЭЮЯ
            абвгде ёжзийк лмнопр стуфхц чшщъыь эюя
            `1234567890-=[];'\\,./
            ~!@#$%^&*()_+{}:"|<>?""";

    private static final String ENCODED_CONTENT = "VGVzdCBjb250ZW50IHRvIGVuY29kZSBiYXNlNjQuCkFCQ0RFRiBHSElKS0wg"
            + "TU5PUFFSIFNUVVZXWCBZWgphYmNkZWYgZ2hpamtsIG1ub3BxciBzdHV2d3gg"
            + "eXoK0JDQkdCS0JPQlNCVINCB0JbQl9CY0JnQmiDQm9Cc0J3QntCf0KAg0KHQ"
            + "otCj0KTQpdCmINCn0KjQqdCq0KvQrCDQrdCu0K8K0LDQsdCy0LPQtNC1INGR"
            + "0LbQt9C40LnQuiDQu9C80L3QvtC/0YAg0YHRgtGD0YTRhdGGINGH0YjRidGK"
            + "0YvRjCDRjdGO0Y8KYDEyMzQ1Njc4OTAtPVtdOydcLC4vCn4hQCMkJV4mKigp"
            + "Xyt7fToifDw+Pw==";

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

    private DecodeBase64 decoder;
    private EncodeBase64 encoder;

    @BeforeEach
    void setUp() {
        decoder = new DecodeBase64();
        encoder = new EncodeBase64();
    }

    // ==================== EncodeBase64.encodeContent TESTS ====================

    @Test
    void encodeContent_ShouldEncodeCorrectly() {
        // when
        String result = encoder.encodeContent(TEST_CONTENT);

        // then
        assertEquals(ENCODED_CONTENT, result);
    }

    @Test
    void encodeContent_WithNullContent_ShouldReturnEmptyString() {
        // when
        String result = encoder.encodeContent(null);

        // then
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void encodeContent_WithEmptyContent_ShouldReturnEmptyString() {
        // when
        String result = encoder.encodeContent(StringUtils.EMPTY);

        // then
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void encodeContent_WithBlankContent_ShouldReturnEmptyString() {
        // when
        String result = encoder.encodeContent("   ");

        // then
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void encodeContent_ShouldUseUtf8Charset() {
        // given
        String withUnicode = "こんにちは";

        // when
        String result = encoder.encodeContent(withUnicode);

        // then
        // Base64 of "こんにちは" in UTF-8
        assertEquals("44GT44KT44Gr44Gh44Gv", result);
    }

    // ==================== DecodeBase64.decodeContent TESTS ====================

    @Test
    void decodeContent_ShouldDecodeCorrectly() {
        // when
        String result = decoder.decodeContent(ENCODED_CONTENT);

        // then
        assertEquals(TEST_CONTENT, result);
    }

    @Test
    void decodeContent_WithNullContent_ShouldReturnEmptyString() {
        // when
        String result = decoder.decodeContent(null);

        // then
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void decodeContent_WithEmptyContent_ShouldReturnEmptyString() {
        // when
        String result = decoder.decodeContent(StringUtils.EMPTY);

        // then
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void decodeContent_WithBlankContent_ShouldReturnEmptyString() {
        // when
        String result = decoder.decodeContent("   ");

        // then
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void decodeContent_ShouldUseUtf8Charset() {
        // given
        String encoded = "44GT44KT44Gr44Gh44Gv";

        // when
        String result = decoder.decodeContent(encoded);

        // then
        assertEquals("こんにちは", result);
    }

    @Test
    void encodeDecode_ShouldBeReversible() {
        // given
        String original = "Hello World! Привет мир! こんにちは";

        // when
        String encoded = encoder.encodeContent(original);
        String decoded = decoder.decodeContent(encoded);

        // then
        assertEquals(original, decoded);
    }

    @Test
    void decodeContent_WithInvalidBase64_ShouldProduceGarbage() {
        // given
        String invalidBase64 = "This is not valid base64!!!";

        // when
        String result = decoder.decodeContent(invalidBase64);

        // then
        // Apache Commons Base64.decodeBase64 simply returns something or empty String (it's implementation-specific).
        // So, we check that decodeContent doesn't fail (no exception is thrown).
    }

    // ==================== EncodeBase64.render TESTS ====================

    @Test
    void render_EncodeBase64_WithOneChild_ShouldRenderEncodedContent() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(TEST_CONTENT);

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(writer).append(ENCODED_CONTENT);
    }

    @Test
    void render_EncodeBase64_WithMultipleChildren_ShouldRenderAllEncoded() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn("Hello");
        when(childNode1.value(context)).thenReturn("World");

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(writer).append("SGVsbG8=");
        verify(writer).append("V29ybGQ=");
    }

    @Test
    void render_EncodeBase64_WithNullChild_ShouldSkipNullChild() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(null);
        when(childNode0.value(context)).thenReturn("Hello");

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(writer).append("SGVsbG8=");
        verify(writer, times(1)).append(anyString()); // No invocation for null child
    }

    @Test
    void render_EncodeBase64_WithEmptyChildren_ShouldRenderNothing() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(0);

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(writer, never()).append(anyString());
    }

    // ==================== DecodeBase64.render TESTS ====================

    @Test
    void render_DecodeBase64_WithOneChild_ShouldRenderDecodedContent() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(ENCODED_CONTENT);

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer).append(TEST_CONTENT);
    }

    @Test
    void render_DecodeBase64_WithMultipleChildren_ShouldRenderAllDecoded() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn("SGVsbG8=");
        when(childNode1.value(context)).thenReturn("V29ybGQ=");

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer).append("Hello");
        verify(writer).append("World");
    }

    @Test
    void render_DecodeBase64_WithNullChild_ShouldSkipNullChild() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(null);
        when(childNode0.value(context)).thenReturn("SGVsbG8=");

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer).append("Hello");
        verify(writer, times(1)).append(anyString()); // No invocation for null child
    }

    @Test
    void render_DecodeBase64_WithEmptyChildren_ShouldRenderNothing() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(0);

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer, never()).append(anyString());
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnDecodeBase64() {
        assertEquals("decode_base64", decoder.getName());
    }

    @Test
    void getName_ShouldReturnEncodeBase64() {
        assertEquals("encode_base64", encoder.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, decoder.getType());
        assertEquals(Directive.LINE, encoder.getType());
    }
}