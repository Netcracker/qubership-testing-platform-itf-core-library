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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;
import java.time.Duration;

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

/**
 * Fails when {@link DecodeSaml#decodeSaml(String, String)} inflates a deflate bomb without bound
 * (SEC-03). A stub template's {@code #decode_saml} argument comes from a counterparty message, so
 * an attacker who controls that message can submit a small, highly compressed payload and force
 * the directive to inflate it far past any legitimate SAML assertion size.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DecodeSamlSizeLimitTest {

    private static final int MAX_DECODED_SIZE_BYTES = 16 * 1024 * 1024;

    @Mock
    private InternalContextAdapter context;

    @Mock
    private Writer writer;

    @Mock
    private Node node;

    @Mock
    private Node childNode0;

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
        setRuntimeServices(decoder, runtimeServices);
        when(runtimeServices.getLog()).thenReturn(log);
    }

    private void setRuntimeServices(Directive directive, RuntimeServices rsvc) throws Exception {
        java.lang.reflect.Field field = Directive.class.getDeclaredField("rsvc");
        field.setAccessible(true);
        field.set(directive, rsvc);
    }

    private static String highlyCompressibleArgument(int decodedLength) {
        return new EncodeSaml().encodeSaml("A".repeat(decodedLength), "UTF-8");
    }

    @Test
    void decodeSaml_WithPayloadExceedingSizeLimit_ShouldThrowIllegalArgumentException() {
        // given: a small argument that deflates to well past the size limit.
        int decodedLength = MAX_DECODED_SIZE_BYTES + 1024 * 1024;
        String bomb = highlyCompressibleArgument(decodedLength);
        assertTrue(bomb.length() < decodedLength / 50,
                "The compressed argument should be a small fraction of the huge inflated payload");

        // when & then: the directive aborts instead of allocating the inflated content.
        IllegalArgumentException ex = assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThrows(IllegalArgumentException.class, () -> decoder.decodeSaml(bomb, "UTF-8")));
        assertTrue(ex.getMessage().contains(String.valueOf(MAX_DECODED_SIZE_BYTES)),
                "Exception message should name the configured limit: " + ex.getMessage());
    }

    @Test
    void decodeSaml_WithPayloadWellUnderSizeLimit_ShouldDecodeSuccessfully() {
        // given
        String original = "A".repeat(1024 * 1024);
        String encoded = encoder.encodeSaml(original, "UTF-8");

        // when
        String result = decoder.decodeSaml(encoded, "UTF-8");

        // then
        assertEquals(original, result);
    }

    @Test
    void render_DecodeSaml_WithPayloadExceedingSizeLimit_ShouldPropagateExceptionWithoutWriting() throws Exception {
        // given
        String bomb = highlyCompressibleArgument(MAX_DECODED_SIZE_BYTES + 1024 * 1024);
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(bomb);

        // when & then
        assertThrows(IllegalArgumentException.class, () -> decoder.render(context, writer, node));
        verify(writer, never()).append(anyString());
    }
}
