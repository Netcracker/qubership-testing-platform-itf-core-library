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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.parser.MessageParameter;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.util.exception.ContentException;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;
import org.qubership.automation.itf.core.util.provider.content.JsonContentProvider;
import org.qubership.automation.itf.core.util.provider.content.PlainContentProvider;
import org.qubership.automation.itf.core.util.provider.content.XmlContentProvider;

@RunWith(MockitoJUnitRunner.class)
public class ParsingRuleTest {

    private static final String TEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<starterchain>\n" +
            "    <name>Bereitstellung TriplePlay BNG Starter chain 17.2</name>\n" +
            "    <starters>\n" +
            "        <starter>\n" +
            "            <enabled>true</enabled>\n" +
            "            <endSituation>RMK-Access receive reserveServiceCallback (PreOrder)- ServiceOrderResponse</endSituation>\n" +
            "            <manualStart>false</manualStart>\n" +
            "            <starter>PreOrder</starter>\n" +
            "        </starter>\n" +
            "    </starters>\n" +
            "    <datasetLists>\n" +
            "        <dataset>sz_FTTH</dataset>\n" +
            "    </datasetLists>\n" +
            "</starterchain>\n";

    private static final String XML_WITH_UMLAUT = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>\n" +
            "<businessInteractionItem>\n" +
            "    <entityKey>\n" +
            "        <keyA>9147412680913944911</keyA>\n" +
            "    </entityKey>\n" +
            "    <specification>\n" +
            "        <specificationID>Konsistenzsicherung abschließen</specificationID>\n" +
            "    </specification>\n" +
            "</businessInteractionItem>";

    private System system;
    private Operation mockOperation;
    private TcContext tcContext;

    @Before
    public void setUp() {
        system = new System();
        system.setName("SomeSystem");

        mockOperation = mock(Operation.class);
        when(mockOperation.getName()).thenReturn("SomeOperation");
        when(mockOperation.getParent()).thenReturn(system);

        tcContext = new TcContext();
        tcContext.put("aaa", "bbb");
    }

    @Test
    public void testURIRegexpReturnsGroupsWhenAsMultipleTrue() throws ContentException {
        ParsingRule parsingRule = createUriRegexParsingRule(true);
        Message message = createMessageWithUriParams(
                "http://mockingbird-tst:8080/mockingbird-transports-df/api/space/managed-domain/managed-elements/4F850DF7BFED2A03E0537402E80A8497/equipment-holders/4F850DF7BFED2A03E0537402E80A8497"
        );

        MessageParameter result = parsingRule.apply(message, InstanceContext.from(tcContext, null), false);

        assertArrayEquals(
                new String[]{"4F850DF7BFED2A03E0537402E80A8497", "4F850DF7BFED2A03E0537402E80A8497"},
                result.getMultipleValue().toArray()
        );
    }

    @Test
    public void testURIRegexpReturnsGroupsWhenAsMultipleFalse() throws ContentException {
        ParsingRule parsingRule = createUriRegexParsingRule(false);
        Message message = createMessageWithUriParams(
                "http://mockingbird-tst:8080/mockingbird-transports-df/api/space/managed-domain/managed-elements/4F850DF7BFED2A03E0537402E80A8497/equipment-holders/4F850DF7BFED2A03E0537402E80A8497"
        );

        MessageParameter result = parsingRule.apply(message, InstanceContext.from(tcContext, null), false);

        assertArrayEquals(
                new String[]{"4F850DF7BFED2A03E0537402E80A8497"},
                result.getMultipleValue().toArray()
        );
    }

    @Test
    public void testURIRegexpReturnsGroupsIsEmpty() throws ContentException {
        ParsingRule parsingRule = createUriRegexParsingRule(true);
        Message message = createMessageWithUriParams("");

        MessageParameter result = parsingRule.apply(message, InstanceContext.from(tcContext, null), false);

        assertArrayEquals(new String[]{}, result.getMultipleValue().toArray());
    }

    @Test
    public void testURIRegexpReturnsGroupsButResultIsEmpty() throws ContentException {
        ParsingRule parsingRule = createUriRegexParsingRule(true);
        Message message = createMessageWithUriParams(
                "http://mockingbird-tst:8080/some/other/api/managed-domain/managed-elements/4F850DF7BFED2A03E0537402E80A8497/equipment-holders/4F850DF7BFED2A03E0537402E80A8497"
        );

        MessageParameter result = parsingRule.apply(message, InstanceContext.from(tcContext, null), false);

        assertArrayEquals(new String[]{}, result.getMultipleValue().toArray());
    }

    @Test
    public void testThrowingExceptionIfIllegalXpath() {
        ParsingRule parsingRule = createXpathParsingRule(
                ".//com:characteristic[com:characteristicID = \"Line_ID\"]//com:characteristicValue/text()"
        );
        Message message = createXmlMessage(TEST_XML);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> parsingRule.apply(message, InstanceContext.from(tcContext, null), false)
        );

        assertTrue(exception.getMessage().startsWith("Failed applying xpath. Probably xPaths is incorrect"));
    }

    @Test
    public void testValidXpath() {
        ParsingRule parsingRule = createXpathParsingRule("//starterchain");
        Message message = createXmlMessage(TEST_XML);

        MessageParameter result = parsingRule.apply(message, InstanceContext.from(tcContext, null), false);

        assertNotNull(result);
        assertNotNull(result.getSingleValue());
        assertTrue(result.getSingleValue().contains("starterchain"));
    }

    @Test
    public void testUmlaut() {
        ParsingRule parsingRule = createXpathParsingRule(
                "//*[local-name()='specificationID' and text()='Konsistenzsicherung abschließen']/../../*[local-name()='entityKey']/*[local-name()='keyA']/text()"
        );
        Message message = createXmlMessage(XML_WITH_UMLAUT);

        MessageParameter result = parsingRule.apply(message, InstanceContext.from(tcContext, null), false);

        assertEquals("9147412680913944911", result.getSingleValue());
    }

    @Test
    public void testJSONParsedSingleValue() {
        SystemParsingRule parsingRule = new SystemParsingRule();
        parsingRule.setParsingType(ParsingRuleType.JSON_PATH);
        parsingRule.setExpression("$.string");
        parsingRule.setMultiple(false);
        parsingRule.setParamName("someJsonPathParam");
        parsingRule.setParent(system);

        Message message = createJsonMessage();

        MessageParameter result = parsingRule.apply(message, InstanceContext.from(tcContext, null), false);

        assertEquals("Hello World", result.getSingleValue());
    }

    @Test
    public void testJSONParsedMultipleValue() {
        SystemParsingRule parsingRule = new SystemParsingRule();
        parsingRule.setParsingType(ParsingRuleType.JSON_PATH);
        parsingRule.setExpression("$.array");
        parsingRule.setMultiple(true);
        parsingRule.setParamName("someJsonPathParam");
        parsingRule.setParent(system);
        Message message = createJsonMessage();

        MessageParameter result = parsingRule.apply(message, InstanceContext.from(tcContext, null), false);

        assertArrayEquals(new String[]{"1", "2", "3"}, result.getMultipleValue().toArray());
    }

    // ==================== HELPER METHODS ====================

    private SystemParsingRule createUriRegexParsingRule(boolean multiple) {
        SystemParsingRule parsingRule = new SystemParsingRule();
        parsingRule.setParsingType(ParsingRuleType.REGEX_URI);
        parsingRule.setMultiple(multiple);
        parsingRule.setParamName("someRegexParam");
        parsingRule.setExpression(".*/api/space/managed-domain/managed-elements/(\\S*)/equipment-holders/([^\\s/]*)$");
        parsingRule.setParent(system);
        return parsingRule;
    }

    private SystemParsingRule createXpathParsingRule(String expression) {
        SystemParsingRule parsingRule = new SystemParsingRule();
        parsingRule.setParsingType(ParsingRuleType.XPATH);
        parsingRule.setExpression(expression);
        parsingRule.setParamName("someXpathParam");
        parsingRule.setParent(system);
        return parsingRule;
    }

    private Message createMessageWithUriParams(String uriParams) throws ContentException {
        Message message = new Message();
        message.getConnectionProperties().put("uriParams", uriParams);
        message.setContent(new PlainContentProvider().provide(message));
        return message;
    }

    private Message createXmlMessage(String xmlContent) {
        Message message = new Message();
        message.setText(xmlContent);
        try {
            message.setContent(new XmlContentProvider().provide(message));
        } catch (ContentException e) {
            throw new RuntimeException("Failed to create XML message", e);
        }
        return message;
    }

    private Message createJsonMessage() {
        Message message = new Message("{\n" +
                "  \"array\": [1, 2, 3],\n" +
                "  \"boolean\": true,\n" +
                "  \"null\": null,\n" +
                "  \"number\": 123,\n" +
                "  \"object\": {\"a\": \"b\", \"c\": \"d\", \"e\": \"f\"},\n" +
                "  \"string\": \"Hello World\"\n" +
                "}");
        try {
            message.setContent(new JsonContentProvider().provide(message));
        } catch (ContentException e) {
            throw new RuntimeException("Failed to create JSON message", e);
        }
        return message;
    }
}