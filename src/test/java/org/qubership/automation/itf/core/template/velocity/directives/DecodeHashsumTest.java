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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecodeHashsumTest {

    private static final String TEST_CONTENT = "Hello World";
    private static final String RS256_TOKEN;
    private static final String RS512_TOKEN;

    // This is a mock JWS token - actual token would be much longer
    private static final String MOCK_JWS_TOKEN = "eyJhbGciOiJSUzI1NiJ9.SGVsbG8gV29ybGQ.signature";

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    static {
        try {
            RS256_TOKEN = generateSignedJwt("RS256");
            RS512_TOKEN = generateSignedJwt("RS512");
        } catch (JOSEException e) {
            throw new RuntimeException("Failed to generate test JWT tokens", e);
        }
    }

    // Generation of signed JWT token for tests
    private static String generateSignedJwt(String algorithm) throws JOSEException {
        // Generate RSA key pair of 2048 bits
        RSAKey rsaJWK = new RSAKeyGenerator(2048)
                .keyID("test-key-id")
                .generate();

        // Determine algorithm
        JWSAlgorithm jwsAlg;
        switch (algorithm) {
            case "RS256":
                jwsAlg = JWSAlgorithm.RS256;
                break;
            case "RS512":
                jwsAlg = JWSAlgorithm.RS512;
                break;
            default:
                throw new IllegalArgumentException("Unsupported algorithm: " + algorithm);
        }

        // Create signer object
        JWSSigner signer = new RSASSASigner(rsaJWK);

        // Create claims (payload)
        JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                .subject("test-subject")
                .issuer("test-issuer")
                .issueTime(new Date())
                .claim("content", TEST_CONTENT)
                .build();

        // Create and sign JWT
        SignedJWT signedJWT = new SignedJWT(
                new JWSHeader.Builder(jwsAlg).keyID(rsaJWK.getKeyID()).build(),
                claimsSet
        );
        signedJWT.sign(signer);

        // Serialize it
        return signedJWT.serialize();
    }

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

    private DecodeHashsum directive;

    @BeforeEach
    void setUp() {
        directive = new DecodeHashsum();
    }

    // ==================== decode_computeHash (private method) TESTS ====================

    @Test
    void decodeComputeHash_WithEmptyContent_ShouldReturnEmptyString() throws Exception {
        String result = invokeDecodeComputeHash("RS256", "");
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void decodeComputeHash_WithNullContent_ShouldReturnEmptyString() throws Exception {
        String result = invokeDecodeComputeHash("RS256", null);
        assertEquals(StringUtils.EMPTY, result);
    }

    @Test
    void decodeComputeHash_WithUnknownAlgorithm_ShouldThrowException() {
        // when & then
        InvocationTargetException ex = assertThrows(InvocationTargetException.class, () ->
                invokeDecodeComputeHash("UNKNOWN", MOCK_JWS_TOKEN));

        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Unknown algorithm 'UNKNOWN'"));

        // when & then
        ex = assertThrows(InvocationTargetException.class, () ->
                invokeDecodeComputeHash("", MOCK_JWS_TOKEN));

        assertInstanceOf(IllegalArgumentException.class, ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Unknown algorithm ''"));
    }

    // Real tokens decoding check

    @Test
    void decodeComputeHash_WithValidRs256Token_ShouldDecodeCorrectly() throws Exception {
        // when
        String result = invokeDecodeComputeHash("RS256", RS256_TOKEN);

        // then
        assertNotNull(result);
        checkDecodedMap(result);
    }

    @Test
    void decodeComputeHash_WithValidRs512Token_ShouldDecodeCorrectly() throws Exception {
        // when
        String result = invokeDecodeComputeHash("RS512", RS512_TOKEN);

        // then
        assertNotNull(result);
        checkDecodedMap(result);
    }

    // Helper method to assert decoded Map
    private void checkDecodedMap(String result) throws JsonProcessingException {
        Map<String, Object> resultMap = OBJECT_MAPPER.readValue(result, new TypeReference<>() {
        });

        assertEquals("test-issuer", resultMap.get("iss"));
        assertEquals("test-subject", resultMap.get("sub"));
        assertEquals(TEST_CONTENT, resultMap.get("content"));
        assertNotNull(resultMap.get("iat"));
    }

    // Helper method to invoke private decode_computeHash
    private String invokeDecodeComputeHash(String algorithm, String content) throws Exception {
        Method method = DecodeHashsum.class.getDeclaredMethod("decode_computeHash", String.class, String.class);
        method.setAccessible(true);
        return (String) method.invoke(directive, algorithm, content);
    }

    // ==================== render TESTS ====================

    @Test
    void render_WithTwoParametersRS256_ShouldProcess() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn("RS256");
        when(childNode1.value(context)).thenReturn(RS256_TOKEN);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).append(captor.capture());

        String capturedJson = captor.getValue();
        assertNotNull(capturedJson);
        checkDecodedMap(capturedJson);
    }

    @Test
    void render_WithTwoParametersRS512_ShouldProcess() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn("RS512");
        when(childNode1.value(context)).thenReturn(RS512_TOKEN);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertTrue(result);

        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).append(captor.capture());

        String capturedJson = captor.getValue();
        assertNotNull(capturedJson);
        checkDecodedMap(capturedJson);
    }

    @Test
    void render_WithOneParameter_ShouldThrowException() throws IOException {
        when(node.jjtGetNumChildren()).thenReturn(1);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                directive.render(context, writer, node));
        assertTrue(ex.getMessage().contains("Incorrect number of parameters"));
        verify(writer, never()).append(anyString());
    }

    @Test
    void render_WithThreeParameters_ShouldThrowException() throws IOException {
        when(node.jjtGetNumChildren()).thenReturn(3);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
                directive.render(context, writer, node));
        assertTrue(ex.getMessage().contains("Incorrect number of parameters"));
        verify(writer, never()).append(anyString());
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnDecodeHashsum() {
        assertEquals("decode_hashsum", directive.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, directive.getType());
    }
}