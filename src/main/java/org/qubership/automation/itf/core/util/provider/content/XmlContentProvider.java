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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.qubership.automation.itf.core.model.content.Content;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.exception.ContentException;
import org.xml.sax.InputSource;

public class XmlContentProvider implements MessageContentProvider<Element> {

    private static final String FEATURE_DISALLOW_DOCTYPE = "http://apache.org/xml/features/disallow-doctype-decl";
    private static final String FEATURE_EXTERNAL_GENERAL_ENTITIES =
            "http://xml.org/sax/features/external-general-entities";
    private static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES =
            "http://xml.org/sax/features/external-parameter-entities";
    private static final String FEATURE_LOAD_EXTERNAL_DTD =
            "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    private final boolean allowDoctype;

    /**
     * Parses a DOCTYPE, but never resolves an external general or parameter
     * entity or an external DTD subset (the soft mitigation).
     */
    public XmlContentProvider() {
        this(true);
    }

    /**
     * @param allowDoctype {@code false} rejects a DOCTYPE declaration outright (the hard mitigation).
     *     {@code true} still parses a DOCTYPE, but never resolves an external general or parameter
     *     entity or an external DTD subset (the soft mitigation).
     */
    public XmlContentProvider(boolean allowDoctype) {
        this.allowDoctype = allowDoctype;
    }

    /**
     * Parses the message body as XML.
     *
     * <p>Either mitigation keeps an XML external entity reference in the message from reading a local
     * file or reaching a remote host through the parser.</p>
     *
     * @throws ContentException if the message text is not well-formed XML, or the hard mitigation
     *     rejects a DOCTYPE declaration
     */
    public Content<Element> provide(Message message) throws ContentException {
        SAXBuilder builder = new SAXBuilder();
        builder.setIgnoringBoundaryWhitespace(true);
        configureXxeProtection(builder);
        Reader reader = new StringReader(message.getText());
        try {
            return new XmlContent(builder.build(reader).getRootElement());
        } catch (JDOMException e) {
            throw new ContentException("Cannot parse XML in message", e);
        } catch (IOException e) {
            throw new ContentException("Cannot load content", e);
        }
    }

    private void configureXxeProtection(SAXBuilder builder) {
        if (allowDoctype) {
            builder.setFeature(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
            builder.setFeature(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
            builder.setFeature(FEATURE_LOAD_EXTERNAL_DTD, false);
            builder.setEntityResolver((publicId, systemId) -> new InputSource(new StringReader("")));
        } else {
            builder.setFeature(FEATURE_DISALLOW_DOCTYPE, true);
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
