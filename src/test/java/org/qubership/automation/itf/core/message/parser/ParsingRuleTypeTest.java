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

package org.qubership.automation.itf.core.message.parser;

import static org.qubership.automation.itf.core.util.parser.ParsingRuleType.XPATH;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jdom2.Element;
import org.junit.Test;
import org.testng.reporters.Files;

import org.qubership.automation.itf.core.model.content.Content;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.parser.MessageParameter;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.util.exception.ContentException;
import org.qubership.automation.itf.core.util.helper.ContentHelper;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;
import org.qubership.automation.itf.core.util.provider.content.XmlContentProvider;

public class ParsingRuleTypeTest {

    final String message = "{\"header\":{\"siteId\":null,\"locale\":null,\"version\":\"1\","
            + "\"type\":\"ASAP\",\"timestamp\":null},\"body\":{\"rollBack\":false,\""
            + "serviceModelName\":\"Terminal\",\"serviceComponents\":[{\"serviceComponentName\":"
            + "\"Terminal\",\"serviceComponentInstances\":[{\"selected\":true,\"attributes\":[{\"name\":"
            + "\"customer_id\",\"xpath\":\"/Terminal/Terminal/Terminal/customer_id\",\"displayName\":\"customer_id\",\"value\":"
            + "\"5af369885aba4\"},{\"name\":\"terminal_type\",\"xpath\":\"/Terminal/Terminal/Terminal/terminal/terminal_type\",\"displayName\":"
            + "\"terminal_type\",\"value\":\"UniFi Voip Phone UVP01\"},{\"name\":\"mac_address\",\"xpath\":\"/Terminal/Terminal/Terminal/terminal/mac_address\",\"displayName\":"
            + "\"mac_address\",\"value\":\"4f:25:12:15:ab:f5\"}]}]}]}}";

    @Test
    public void testParsingJsonPath() throws InterruptedException {
        ParsingRule parsingRule = mock(ParsingRule.class);
        when(parsingRule.getMultiple()).thenReturn(true);
        when(parsingRule.getExpression()).thenReturn("body.serviceModelName");
        when(parsingRule.getParsingType()).thenReturn(ParsingRuleType.JSON_PATH);
        int count = 100;
        ExecutorService service = Executors.newCachedThreadPool();
        for (int i = 0; i < count; i++) {
            service.execute(() -> {
                Message msg = new Message(message);
                try {
                    ContentHelper.getInstance().trySetContent(msg, ParsingRuleType.JSON_PATH.toString());
                } catch (ContentException e) {
                    e.printStackTrace();
                }
                assertEquals("Terminal", ParsingRuleType.JSON_PATH.parse(msg, parsingRule).getSingleValue());
            });
        }
        service.shutdown();
    }

    @Test
    public void testParsingUriRegexp() {
        ParsingRule parsingRule = mock(SystemParsingRule.class);
        when(parsingRule.getMultiple()).thenReturn(false);
        when(parsingRule.getExpression()).thenReturn("(.*)");
        when(parsingRule.getParsingType()).thenReturn(ParsingRuleType.REGEX_URI);
        when(parsingRule.getParamName()).thenReturn("param");
        Message message = new Message("");
        String er = "http://itf-virginmedia.openshift.qubership.cloud/#/system/975";
        message.getConnectionProperties().put("uriParams", er);
        MessageParameter parse = ParsingRuleType.REGEX_URI.parse(message, parsingRule);
        String value = parse.getSingleValue();
        assertEquals(er, value);
    }

    @Test
    public void testParsingXpath() throws URISyntaxException, IOException, ContentException {
        File file = new File(getClass().getResource("/parsing_rule/xml_expression.xml").toURI());
        String xmlContent = Files.readFile(file);
        ParsingRule parsingRule = mock(SystemParsingRule.class);
        when(parsingRule.getExpression()).thenReturn("//UPRN/text()");
        when(parsingRule.getParsingType()).thenReturn(XPATH);
        when(parsingRule.getMultiple()).thenReturn(true);
        Message message = new Message(xmlContent);
        Content<Element> provide = new XmlContentProvider().provide(message);
        message.setContent(provide);
        MessageParameter parse = XPATH.parse(message, parsingRule);
        assertEquals("900000002319", parse.getSingleValue());
    }
}
