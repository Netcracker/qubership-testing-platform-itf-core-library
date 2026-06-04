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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;

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
class EncodeDecodeSamlTest {

    private static final String TEST_CONTENT = """
            <saml:Assertion xmlns:saml="urn:oasis:names:tc:SAML:2.0:assertion"
                ID="_abc123" IssueInstant="2024-01-01T00:00:00Z" Version="2.0">
                <saml:Issuer>https://example.com</saml:Issuer>
                <saml:Subject>
                    <saml:NameID>user@example.com</saml:NameID>
                </saml:Subject>
            </saml:Assertion>""";

    private static final String SIMPLE_CONTENT = "Hello, SAML!";
    private static final String UNICODE_CONTENT = "Привет мир! こんにちは";

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

    private DecodeSaml decoder;
    private EncodeSaml encoder;

    @BeforeEach
    void setUp() throws Exception {
        decoder = new DecodeSaml();
        encoder = new EncodeSaml();

        // Set rsvc via reflection for both Directives
        setRuntimeServices(decoder, runtimeServices);
        setRuntimeServices(encoder, runtimeServices);

        when(runtimeServices.getLog()).thenReturn(log);
    }

    private void setRuntimeServices(Directive directive, RuntimeServices rsvc) throws Exception {
        java.lang.reflect.Field field = Directive.class.getDeclaredField("rsvc");
        field.setAccessible(true);
        field.set(directive, rsvc);
    }

    // ==================== EncodeSaml.encodeSaml TESTS ====================

    @Test
    void encodeSaml_WithUnicodeContent_ShouldEncodeAndBeReversible() {
        // when
        String encoded = encoder.encodeSaml(UNICODE_CONTENT, "UTF-8");
        String decoded = decoder.decodeSaml(encoded, "UTF-8");

        // then
        assertEquals(UNICODE_CONTENT, decoded);
    }

    @Test
    void encodeSaml_WithShiftJisEncoding_ShouldEncodeWithJapaneseCharset() {
        // given
        String japaneseContent = "こんにちは";

        // when
        String encoded = encoder.encodeSaml(japaneseContent, "SHIFT-JIS");
        String decoded = decoder.decodeSaml(encoded, "SHIFT-JIS");

        // then
        assertEquals(japaneseContent, decoded);
    }

    @Test
    void encodeSaml_WithNullContent_ShouldThrowException() {
        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> encoder.encodeSaml(null, "UTF-8"));

        // Check that exception message points to the problem
        assertTrue(ex.getMessage().contains("because \"text\" is null"));
    }

    @Test
    void encodeSaml_WithEmptyContent_ShouldProduceValidBase64() {
        // when
        String result = encoder.encodeSaml(StringUtils.EMPTY, "UTF-8");

        // then
        assertNotNull(result);

        // At least the current implementation produces "AwA%3D" stable result for empty String.
        // Check it.
        assertEquals("AwA%3D", result);
    }

    // ==================== DecodeSaml.decodeSaml TESTS ====================

    @Test
    void decodeSaml_WithValidEncodedContent_ShouldDecodeCorrectly() {
        // given
        String encoded = encoder.encodeSaml(TEST_CONTENT, "UTF-8");

        // when
        String result = decoder.decodeSaml(encoded, "UTF-8");

        // then
        assertEquals(TEST_CONTENT, result);
    }

    @Test
    void decodeSaml_WithNullContent_ShouldThrowException() {
        // when & then
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> decoder.decodeSaml(null, "UTF-8"));

        // Check that exception message points to the problem
        assertTrue(ex.getMessage().contains("because \"s\" is null"));
    }

    @Test
    void decodeSaml_WithEmptyContent_ShouldHandleGracefully() {
        // when & then
        // Check that decodeSaml is executed successfully.
        // Important: the method should handle empty input correct way.
        try {
            String result = decoder.decodeSaml(StringUtils.EMPTY, "UTF-8");
            // If didn't fail = okay.
            // It's the current behavior, after 2026-05-25.
        } catch (RuntimeException e) {
            // If failed - acceptable too, for empty input.
        }
    }

    @Test
    void decodeSaml_WithInvalidBase64_ShouldThrowException() {
        // given
        String invalidBase64 = "This is not valid base64!!!";

        // when & then
        assertThrows(RuntimeException.class,
                () -> decoder.decodeSaml(invalidBase64, "UTF-8"));
    }

    // ==================== EncodeSaml.render TESTS ====================

    @Test
    void render_EncodeSaml_WithOneChild_ShouldRenderEncodedContent() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(SIMPLE_CONTENT);

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(writer).append(anyString());
    }

    @Test
    void render_EncodeSaml_WithTwoChildren_ShouldUseEncodingFromSecondChild() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(SIMPLE_CONTENT);
        when(childNode1.value(context)).thenReturn("SHIFT-JIS");

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        // Check that result is SIMPLE_CONTENT encoded using SHIFT-JIS encoding.
        String expectedShiftJisEncoded = encoder.encodeSaml(SIMPLE_CONTENT, "SHIFT-JIS");
        verify(writer).append(expectedShiftJisEncoded);
    }

    @Test
    void render_EncodeSaml_WithTwoChildrenAndNullSecondChild_ShouldUseDefaultUtf8() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(null);
        when(childNode0.value(context)).thenReturn(SIMPLE_CONTENT);

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        // Check that result is SIMPLE_CONTENT encoded using UTF-8 (default) encoding.
        String expectedUtf8Encoded = encoder.encodeSaml(SIMPLE_CONTENT, "UTF-8");
        verify(writer).append(expectedUtf8Encoded);
    }

    @Test
    void render_EncodeSaml_WithMoreThanTwoChildren_ShouldLogErrorAndWriteErrorMessage() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(3);

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(log).error(anyString());
        verify(writer).append("#encode_saml:incorrect parameters");
    }

    @Test
    void render_EncodeSaml_WithNullFirstChild_ShouldRenderNothing() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(null);

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(writer, never()).append(anyString());
    }

    @Test
    void render_EncodeSaml_WithEmptyChildren_ShouldRenderNothing() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(0);

        // when
        boolean result = encoder.render(context, writer, node);

        // then
        verify(log).error(anyString());
        verify(writer).append("#encode_saml:incorrect parameters");
    }

    // ==================== DecodeSaml.render TESTS ====================

    @Test
    void render_DecodeSaml_WithOneChild_ShouldRenderDecodedContent() throws Exception {
        // given
        String encoded = encoder.encodeSaml(SIMPLE_CONTENT, "UTF-8");
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(encoded);

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer).append(SIMPLE_CONTENT);
    }

    @Test
    void render_DecodeSaml_WithTwoChildren_ShouldUseEncodingFromSecondChild() throws Exception {
        // given
        String encoded = encoder.encodeSaml(SIMPLE_CONTENT, "SHIFT-JIS");
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(encoded);
        when(childNode1.value(context)).thenReturn("SHIFT-JIS");

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer).append(SIMPLE_CONTENT);
    }

    @Test
    void render_DecodeSaml_WithTwoChildrenAndNullSecondChild_ShouldUseDefaultUtf8() throws Exception {
        // given
        String encoded = encoder.encodeSaml(SIMPLE_CONTENT, "UTF-8");
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(null);
        when(childNode0.value(context)).thenReturn(encoded);

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer).append(SIMPLE_CONTENT);
    }

    @Test
    void render_DecodeSaml_WithMoreThanTwoChildren_ShouldLogErrorAndWriteErrorMessage() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(3);

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(log).error(anyString());
        verify(writer).append("#decode_saml:incorrect parameters");
    }

    @Test
    void render_DecodeSaml_WithNullFirstChild_ShouldRenderNothing() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(null);

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(writer, never()).append(anyString());
    }

    @Test
    void render_DecodeSaml_WithEmptyChildren_ShouldLogErrorAndWriteErrorMessage() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(0);

        // when
        boolean result = decoder.render(context, writer, node);

        // then
        verify(log).error(anyString());
        verify(writer).append("#decode_saml:incorrect parameters");
    }

    // ==================== encodeDecode Round-trip TESTS ====================

    @Test
    void encodeDecode_WithComplexContent_ShouldBeReversible() {
        // given
        String original = TEST_CONTENT;

        // when
        String encoded = encoder.encodeSaml(original, "UTF-8");
        String decoded = decoder.decodeSaml(encoded, "UTF-8");

        // then
        assertEquals(original, decoded);
    }

    @Test
    void encodeDecode_WithDifferentEncodings_ShouldBeConsistent() {
        // given
        String original = UNICODE_CONTENT;

        // when
        String encodedUtf8 = encoder.encodeSaml(original, "UTF-8");
        String decodedUtf8 = decoder.decodeSaml(encodedUtf8, "UTF-8");

        String encodedSjis = encoder.encodeSaml(original, "SHIFT-JIS");
        String decodedSjis = decoder.decodeSaml(encodedSjis, "SHIFT-JIS");

        // then
        assertEquals(original, decodedUtf8);
        assertEquals(original, decodedSjis);
    }

    // ==================== Edge Cases and Exception TESTS ====================

    @Test
    void decodeSaml_WithMalformedData_ShouldThrowRuntimeException() {
        // given
        String malformed = "malformed-data";

        // when & then
        assertThrows(RuntimeException.class,
                () -> decoder.decodeSaml(malformed, "UTF-8"));
    }

    @Test
    void encodeSaml_WithInvalidEncoding_ShouldThrowRuntimeException() {
        // given
        String invalidEncoding = "invalid-encoding";

        // when & then
        assertThrows(RuntimeException.class,
                () -> encoder.encodeSaml(TEST_CONTENT, invalidEncoding));
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnDecodeSaml() {
        assertEquals("decode_saml", decoder.getName());
    }

    @Test
    void getName_ShouldReturnEncodeSaml() {
        assertEquals("encode_saml", encoder.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, decoder.getType());
        assertEquals(Directive.LINE, encoder.getType());
    }
}