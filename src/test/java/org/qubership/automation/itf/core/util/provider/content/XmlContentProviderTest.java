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

package org.qubership.automation.itf.core.util.provider.content;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jdom2.Element;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qubership.automation.itf.core.model.content.Content;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.exception.ContentException;

@ExtendWith(MockitoExtension.class)
class XmlContentProviderTest {

    private static final String SECRET = "XXE-SECRET-VALUE-12345";

    @Mock
    private Message message;

    @TempDir
    private Path tempDir;

    private Path secretFile;

    @BeforeEach
    void setUp() throws IOException {
        secretFile = tempDir.resolve("secret.txt");
        Files.writeString(secretFile, SECRET);
    }

    @AfterEach
    void tearDown() throws IOException {
        Files.deleteIfExists(secretFile);
    }

    @Test
    void hardModeRejectsDoctypeAndNeverReadsTheReferencedFile() {
        when(message.getText()).thenReturn(xxeMessage());
        XmlContentProvider provider = new XmlContentProvider(false);

        ContentException exception = assertThrows(ContentException.class, () -> provider.provide(message));

        assertFalse(exception.getMessage().contains(SECRET));
    }

    @Test
    void hardModeStillParsesAMessageWithNoDoctype() throws ContentException {
        when(message.getText()).thenReturn("<root><data>value</data></root>");
        XmlContentProvider provider = new XmlContentProvider(false);

        Content<Element> content = provider.provide(message);

        assertEquals("value", content.get().getChildText("data"));
    }

    @Test
    void softModeParsesTheDoctypeButNeverReadsTheReferencedFile() {
        when(message.getText()).thenReturn(xxeMessage());
        XmlContentProvider provider = new XmlContentProvider();

        Content<Element> content = assertDoesNotThrow(() -> provider.provide(message));

        assertFalse(content.get().getChildText("data").contains(SECRET));
    }

    @Test
    void softModeStillParsesAMessageWithNoDoctype() throws ContentException {
        when(message.getText()).thenReturn("<root><data>value</data></root>");
        XmlContentProvider provider = new XmlContentProvider();

        Content<Element> content = provider.provide(message);

        assertEquals("value", content.get().getChildText("data"));
    }

    private String xxeMessage() {
        String uri = secretFile.toUri().toString();
        return "<?xml version=\"1.0\"?>\n"
                + "<!DOCTYPE root [<!ENTITY xxe SYSTEM \"" + uri + "\">]>\n"
                + "<root><data>&xxe;</data></root>";
    }
}
