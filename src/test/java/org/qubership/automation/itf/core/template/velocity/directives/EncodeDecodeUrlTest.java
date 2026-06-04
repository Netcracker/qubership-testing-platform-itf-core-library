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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

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
class EncodeDecodeUrlTest {

    private static final String TEST_CONTENT = "Test content: Günter";
    private static final String ENCODED_UTF8 = "Test+content%3A+G%C3%BCnter";

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

    private DecodeUrl decoder;
    private EncodeUrl encoder;

    @BeforeEach
    void setUp() throws Exception {
        decoder = new DecodeUrl();
        encoder = new EncodeUrl();

        // Set rsvc via reflection for both Directives
        setRuntimeServices(decoder, runtimeServices);
        setRuntimeServices(encoder, runtimeServices);

        when(runtimeServices.getLog()).thenReturn(log);
    }

    private void setRuntimeServices(Directive directive,
                                    RuntimeServices rsvc) throws Exception {
        java.lang.reflect.Field field = Directive.class.getDeclaredField("rsvc");
        field.setAccessible(true);
        field.set(directive, rsvc);
    }

    // ==================== DecodeUrl.decodeUrl TESTS ====================

    @Test
    void decodeUrl_WithUtf8Encoding_ShouldDecodeCorrectly() {
        // when
        String result = decoder.decodeUrl(ENCODED_UTF8, "UTF-8");

        // then
        assertEquals(TEST_CONTENT, result);
    }

    @Test
    void decodeUrl_WithNullContent_ShouldReturnEmptyString() {
        // when
        String result = decoder.decodeUrl(null, "UTF-8");

        // then
        assertEquals("", result);
    }

    @Test
    void decodeUrl_WithEmptyContent_ShouldReturnEmptyString() {
        // when
        String result = decoder.decodeUrl("", "UTF-8");

        // then
        assertEquals("", result);
    }

    @Test
    void decodeUrl_WithBlankContent_ShouldReturnEmptyString() {
        // when
        String result = decoder.decodeUrl("   ", "UTF-8");

        // then
        assertEquals("", result);
    }

    @Test
    void decodeUrl_WithJisEncoding_ShouldUseSpecialHandling() throws UnsupportedEncodingException {
        String encodingName = "SHIFT-JIS";

        // given
        String jisEncoded = "Test+content%3A+G%3Fnter"; // Günter in jis style

        // when
        String actualResult = decoder.decodeUrl(jisEncoded, "SHIFT-JIS");

        // below decoding using ISO_8859_1 is needed when we're decoding to Japanese charset
        String decoded = URLDecoder.decode(jisEncoded, StandardCharsets.ISO_8859_1);
        byte[] decodedBytes = decoded.getBytes(StandardCharsets.ISO_8859_1);
        String expectedResult = new String(decodedBytes, encodingName);

        // then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void decodeUrl_WithInvalidEncoding_ShouldReturnErrorMessage() {
        // given
        String invalidEncoding = "invalid-encoding";

        // when
        String result = decoder.decodeUrl(TEST_CONTENT, invalidEncoding);

        // then
        assertEquals("Unsupported encoding [invalid-encoding] for decodeUrl directive", result);
    }

    // ==================== EncodeUrl.encodeUrl TESTS ====================

    @Test
    void encodeUrl_WithUtf8Encoding_ShouldEncodeCorrectly() {
        // when
        String result = encoder.encodeUrl(TEST_CONTENT, "UTF-8");

        // then
        assertEquals(ENCODED_UTF8, result);
    }

    @Test
    void encodeUrl_WithNullContent_ShouldReturnEmptyString() {
        // when
        String result = encoder.encodeUrl(null, "UTF-8");

        // then
        assertEquals("", result);
    }

    @Test
    void encodeUrl_WithEmptyContent_ShouldReturnEmptyString() {
        // when
        String result = encoder.encodeUrl("", "UTF-8");

        // then
        assertEquals("", result);
    }

    @Test
    void encodeUrl_WithBlankContent_ShouldReturnEmptyString() {
        // when
        String result = encoder.encodeUrl("   ", "UTF-8");

        // then
        assertEquals("", result);
    }

    @Test
    void encodeUrl_WithJisEncoding_ShouldUseSpecialHandling() throws UnsupportedEncodingException {
        String encodingName = "SHIFT-JIS";
        // when
        String actualResult = encoder.encodeUrl(TEST_CONTENT, encodingName);

        // below encoding using ISO_8859_1 is needed when we're encoding to Japanese charset
        byte[] bytes = TEST_CONTENT.getBytes(encodingName);
        String s = new String(bytes, StandardCharsets.ISO_8859_1);
        String expectedResult = URLEncoder.encode(s, StandardCharsets.ISO_8859_1);

        // then
        assertEquals(expectedResult, actualResult);
    }

    @Test
    void encodeUrl_WithInvalidEncoding_ShouldReturnErrorMessage() {
        // given
        String invalidEncoding = "invalid-encoding";

        // when
        String result = encoder.encodeUrl(TEST_CONTENT, invalidEncoding);

        // then
        assertEquals("Unsupported encoding [invalid-encoding] for encodeUrl directive", result);
    }

    // ==================== DecodeUrl.render TESTS (требуют мокирования Velocity) ====================

    @Test
    void render_WithTwoChildren_ShouldRenderDecodedContent() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(ENCODED_UTF8);
        when(childNode1.value(context)).thenReturn("UTF-8");

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer).append(TEST_CONTENT);
    }

    @Test
    void render_DecodeUrlWithWrongNumberOfChildren_ShouldWriteErrorMessage() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer).append(org.mockito.ArgumentMatchers.contains("Incorrect decodeUrl directive format"));
    }

    // ==================== EncodeUrl.render TESTS ====================

    @Test
    void render_WithTwoChildren_ShouldRenderEncodedContent() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(TEST_CONTENT);
        when(childNode1.value(context)).thenReturn("UTF-8");

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(writer).append(ENCODED_UTF8);
    }

    @Test
    void render_EncodeUrlWithWrongNumberOfChildren_ShouldWriteErrorMessage() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(3);

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(writer).append(org.mockito.ArgumentMatchers.contains("Incorrect encodeUrl directive format"));
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnDecodeUrl() {
        assertEquals("decodeUrl", decoder.getName());
    }

    @Test
    void getName_ShouldReturnEncodeUrl() {
        assertEquals("encodeUrl", encoder.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, decoder.getType()); // Directive.LINE = 2
        assertEquals(Directive.LINE, encoder.getType());
    }

    @Test
    void encodeUrl_WithShiftJisEncoding_ShouldHandleJapaneseCharacters() {
        // given
        String japaneseText = "こんにちは";

        // when
        String result = encoder.encodeUrl(japaneseText, "SHIFT-JIS");

        // then
        // SHIFT-JIS for "こんにちは" then expected result is
        assertEquals("%82%B1%82%F1%82%C9%82%BF%82%CD", result);
    }

    @Test
    void decodeUrl_WithShiftJisEncoding_ShouldHandleJapaneseCharacters() {
        // given
        String encoded = "%82%B1%82%F1%82%C9%82%BF%82%CD";

        // when
        String result = decoder.decodeUrl(encoded, "SHIFT-JIS");

        // then
        assertEquals("こんにちは", result);
    }

    @Test
    void encodeDecode_WithShiftJis_ShouldBeReversible() {
        // given
        String original = "日本語テスト123!@#";

        // when
        String encoded = encoder.encodeUrl(original, "SHIFT-JIS");
        String decoded = decoder.decodeUrl(encoded, "SHIFT-JIS");

        // then
        assertEquals(original, decoded);
    }
}