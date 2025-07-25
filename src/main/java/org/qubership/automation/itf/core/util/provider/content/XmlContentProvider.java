/*
 *  Copyright 2024-2025 NetCracker Technology Corporation
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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.qubership.automation.itf.core.model.content.Content;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.exception.ContentException;

public class XmlContentProvider implements MessageContentProvider<Element> {

    public XmlContentProvider() {
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Content<Element> provide(Message message) throws ContentException {
        SAXBuilder builder;
        builder = new SAXBuilder();
        builder.setIgnoringBoundaryWhitespace(true);
        InputStream stream = new ByteArrayInputStream(message.getText().getBytes(StandardCharsets.UTF_8));
        try {
            return new XmlContent(builder.build(stream).getRootElement());
        } catch (JDOMException e) {
            throw new ContentException("Cannot parse XML in message", e);
        } catch (IOException e) {
            throw new ContentException("Cannot load content", e);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public boolean supports(Message message) {
        //really, Google says it is the fastest way to check XML or not
        if (message == null || message.getText() == null) {
            return false;
        }
        return message.getText().trim().startsWith("<");
    }

    private static class XmlContent implements Content<Element> {

        private Element element;

        XmlContent(Element element) {
            this.element = element;
        }

        public Element get() {
            return element;
        }
    }
}
