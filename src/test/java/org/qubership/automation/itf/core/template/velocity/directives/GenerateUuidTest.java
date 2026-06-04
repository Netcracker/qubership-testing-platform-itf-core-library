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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;
import java.util.regex.Pattern;

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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerateUuidTest {

    private static final Pattern UUID_PATTERN =
            Pattern.compile("^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
                    Pattern.CASE_INSENSITIVE);

    @Mock
    private InternalContextAdapter context;

    @Mock
    private Writer writer;

    @Mock
    private Node node;

    private GenerateUuid directive;

    @BeforeEach
    void setUp() {
        directive = new GenerateUuid();
    }

    // ==================== render TESTS ====================

    @Test
    void render_ShouldGenerateValidUuid() throws Exception {
        // when
        boolean result = directive.render(context, writer, node);

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).append(captor.capture());

        String generatedUuid = captor.getValue();
        assertNotNull(generatedUuid);
        assertTrue(UUID_PATTERN.matcher(generatedUuid).matches());
    }

    @Test
    void render_ShouldGenerateDifferentUuidsOnEachCall() throws Exception {
        // when
        directive.render(context, writer, node);
        directive.render(context, writer, node);

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer, times(2)).append(captor.capture());

        String firstUuid = captor.getAllValues().get(0);
        String secondUuid = captor.getAllValues().get(1);

        assertNotNull(firstUuid);
        assertNotNull(secondUuid);
        assertNotEquals(firstUuid, secondUuid);
    }

    @Test
    void render_ShouldAlwaysReturnTrue() throws Exception {
        // when
        boolean result = directive.render(context, writer, node);

        // then
        assertTrue(result);
    }

    @Test
    void render_ShouldIgnoreNodeChildren() throws Exception {
        // given
        // Set that the node has children (they should be ignored)
        when(node.jjtGetNumChildren()).thenReturn(3);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(writer).append(captor.capture());

        // UUID should be generated in spite of children presence
        assertTrue(UUID_PATTERN.matcher(captor.getValue()).matches());
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnGenerateUUID() {
        assertEquals("generateUUID", directive.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, directive.getType());
    }
}