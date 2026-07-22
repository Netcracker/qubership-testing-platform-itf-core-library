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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.qubership.automation.itf.core.util.parser.ParsingRuleType.XPATH;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jdom2.Element;
import org.junit.Test;
import org.qubership.automation.itf.core.model.content.Content;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.parser.MessageParameter;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.exception.ContentException;
import org.qubership.automation.itf.core.util.helper.ContentHelper;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;
import org.qubership.automation.itf.core.util.provider.content.XmlContentProvider;
import org.testng.reporters.Files;

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
    public void testParsingJsonPath() {
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
        when(parsingRule.getParsedExpression()).thenReturn("(.*)");
        when(parsingRule.getParsingType()).thenReturn(ParsingRuleType.REGEX_URI);
        when(parsingRule.getParamName()).thenReturn("param");
        Message message = new Message("");
        String er = "http://example.com/account/create";
        message.getConnectionProperties().put("uriParams", er);
        MessageParameter parse = ParsingRuleType.REGEX_URI.parse(message, parsingRule);
        String value = parse.getSingleValue();
        assertEquals(er, value);
    }

    @Test
    public void testParsingXpath() throws URISyntaxException, IOException, ContentException {
        File file = new File(getClass().getResource("/parsing_rule/xml_expression.xml").toURI());
        String xmlContent = Files.readFile(file);
        String xpath = "//*[local-name() = 'UPRN']/text()"; // "//UPRN/text()" - it works only w/o namespace
        ParsingRule parsingRule = mock(SystemParsingRule.class);
        when(parsingRule.getExpression()).thenReturn(xpath);
        when(parsingRule.getParsedExpression()).thenReturn(xpath);
        when(parsingRule.getParsingType()).thenReturn(XPATH);
        when(parsingRule.getMultiple()).thenReturn(true);
        Message message = new Message(xmlContent);
        Content<Element> provide = new XmlContentProvider().provide(message);
        message.setContent(provide);
        MessageParameter parse = XPATH.parse(message, parsingRule);
        assertEquals("900000002319", parse.getSingleValue());
        assertEquals(2, parse.getMultipleValue().size());
    }

    @Test
    public void testParsingXpath_getOperation() throws ContentException {
        String xml = """
                <soapenv:Envelope
                    xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/"
                    xmlns:env="http://schemas.xmlsoap.org/soap/envelope/">
                    <soapenv:Header/>
                    <soapenv:Body>
                        <CheckEligibilityServiceRequestMessage
                            xmlns="http://zain.com/telecom/esb/xsd/checkeligibilityservice/verifyusereligibility/request/v1_0_0_0"
                            xmlns:ns0="http://zain.com/telecom/esb/xsd/SecurityManagement/common/headers/v1_0_0_0">
                            <RequestHeader>
                                <ns0:Timestamp>2023-01-12T10:05:09.169+03:00</ns0:Timestamp>
                                <ns0:ChannelId>NCCSRD</ns0:ChannelId>
                                <ns0:ChannelTransctionId>571a7b94-f745-447a-b1fb-0971ec9a097d</ns0:ChannelTransctionId>
                                <ns0:Auth_key>YWRtaW4=</ns0:Auth_key>
                            </RequestHeader>
                            <CheckEligibilityServiceRequestPayload>
                                <requestType>1</requestType>
                                <Operator>
                                    <employeeIdType>1</employeeIdType>
                                    <operatorTCN>fabd81c5-bb81-46a9-9bcc-de402ba5837f</operatorTCN>
                                    <EmployeeId>1233329999</EmployeeId>
                                </Operator>
                                <Person>
                                    <personId>{v2c}{AES/ECB/PKCS5Padding}{default-key-alias_1646810792369}{e3uYmrz/CLAH+uQu4Ybi/g==}</personId>
                                    <nationality>{v2c}{AES/ECB/PKCS5Padding}{default-key-alias_1646810792369}{fFcvMytR+/w1uwpFvGyCqQ==}</nationality>
                                    <IdType>1</IdType>
                                    <exceptionflag>0</exceptionflag>
                                </Person>
                                <Mobile>
                                    <subscriptionType>0</subscriptionType>
                                    <msisdnType>V</msisdnType>
                                </Mobile>
                            </CheckEligibilityServiceRequestPayload>
                        </CheckEligibilityServiceRequestMessage>
                    </soapenv:Body>
                </soapenv:Envelope>""";
        Message message = new Message(xml);
        Content<Element> provide = new XmlContentProvider().provide(message);
        message.setContent(provide);

        String xpath = "local-name(/*[local-name() = 'Envelope']/*[local-name() = 'Body']/*[1])";

        ParsingRule<System> parsingRule = mock(SystemParsingRule.class);
        when(parsingRule.getExpression()).thenReturn(xpath);
        when(parsingRule.getParsedExpression()).thenReturn(xpath);
        when(parsingRule.getParsingType()).thenReturn(XPATH);
        when(parsingRule.getMultiple()).thenReturn(false);
        when(parsingRule.getAutosave()).thenReturn(true);

        MessageParameter parameter = XPATH.parse(message, parsingRule);
        assertEquals("CheckEligibilityServiceRequestMessage", parameter.getSingleValue());

        // Briefly, Xpath parsing is the following sequence of steps:
        /*
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(new StringReader(xml));
        XPathFactory xpf = XPathFactory.instance();
        XPathExpression<Object> expr = xpf.compile(xpath);
        Object result = expr.evaluateFirst(doc);
        java.lang.System.out.println("Result: " + result);
         */
    }
}
