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

package org.qubership.automation.itf.core.message.parser;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hamcrest.core.StringStartsWith;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
import org.qubership.automation.itf.core.util.provider.content.PlainContentProvider;
import org.qubership.automation.itf.core.util.provider.content.XmlContentProvider;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(locations = {"classpath*:*core-test-context-no-broker-bean.xml"})
public class ParsingRuleTest {
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();


    private final String text = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            <starterchain>
                <name>Bereitstellung TriplePlay BNG Starter chain 17.2</name>
                <starters>
                    <starter>
                        <enabled>true</enabled>
                        <endSituation>RMK-Access receive reserveServiceCallback (PreOrder)- ServiceOrderResponse</endSituation>
                        <manualStart>false</manualStart>
                        <starter>PreOrder</starter>
                    </starter>
                    <starter>
                        <enabled>true</enabled>
                        <endSituation>VRE Recieve ServiceOrder Message (2)</endSituation>
                        <manualStart>false</manualStart>
                        <starter>VRE send activateService to SMF - new TriplePlay</starter>
                    </starter>
                    <starter>
                        <enabled>true</enabled>
                        <endSituation>OpDiNG send 2nd executeDiagnosticCallback</endSituation>
                        <manualStart>false</manualStart>
                        <starter>[SMF][AL-PS] Last Order Accepted</starter>
                    </starter>
                </starters>
                <datasetLists>
                    <dataset>sz_FTTH</dataset>
                </datasetLists>
            </starterchain>
            """;

    private final String textUmlaut = """
            <?xml version="1.0" encoding="UTF-8" standalone="yes"?>
            						<businessInteractionItem>
            							<entityKey>
            								<keyA>9147412680913944911</keyA>
            							</entityKey>
            							<state>
            								<value>incomplete</value>
            							</state>
            							<specification>
            								<specificationName>Konsistenzsicherung abschließen</specificationName>
            								<specificationID>Konsistenzsicherung abschließen</specificationID>
            								<characteristic>
            									<characteristicID>Access_Line</characteristicID>
            									<characteristic>
            										<characteristicID>EntityReference</characteristicID>
            										<characteristic>
            											<characteristicID>keyA</characteristicID>
            											<characteristicValue>16c7f314-eb5c-4989-a640-45d5d5ecaf45</characteristicValue>
            										</characteristic>
            										<characteristic>
            											<characteristicID>keyB</characteristicID>
            											<characteristicValue />
            										</characteristic>
            									</characteristic>
            								</characteristic>
            							</specification>
            						</businessInteractionItem>""";


    private MessageParameter testURIRegexpReturnsGroups(boolean asMultiple, ParsingRuleType type, String expression, String groups) throws ContentException {
        ParsingRule parsingRule = new SystemParsingRule();
        parsingRule.setParsingType(type);
        parsingRule.setMultiple(asMultiple);
        parsingRule.setExpression(expression);
        Message message = new Message();
        message.getConnectionProperties().put("uriParams", groups);
        message.setContent(new PlainContentProvider().provide(message));
        setParents(parsingRule);
        TcContext context = new TcContext();
        context.put("aaa", "bbb");
        return parsingRule.apply(message, InstanceContext.from(context, null), false);
    }

    @Test
    public void testURIRegexpReturnsGroupsWhenAsMultipleTrue() throws ContentException {
        MessageParameter messageParameter = testURIRegexpReturnsGroups(true, ParsingRuleType.REGEX_URI,
                ".*/api/space/managed-domain/managed-elements/(\\S*)/equipment-holders/([^\\s/]*)$",
                "http://mockingbird-tst:8080/mockingbird-transports-df/api/space/managed-domain/managed-elements/4F850DF7BFED2A03E0537402E80A8497/equipment-holders/4F850DF7BFED2A03E0537402E80A8497");
        assertArrayEquals(new String[]{"4F850DF7BFED2A03E0537402E80A8497", "4F850DF7BFED2A03E0537402E80A8497"}, messageParameter.getMultipleValue().toArray());
    }

    @Test
    public void testURIRegexpReturnsGroupsWhenAsMultipleFalse() throws ContentException {
        MessageParameter messageParameter = testURIRegexpReturnsGroups(false, ParsingRuleType.REGEX_URI,
                ".*/api/space/managed-domain/managed-elements/(\\S*)/equipment-holders/([^\\s/]*)$",
                "http://mockingbird-tst:8080/mockingbird-transports-df/api/space/managed-domain/managed-elements/4F850DF7BFED2A03E0537402E80A8497/equipment-holders/4F850DF7BFED2A03E0537402E80A8497");
        assertArrayEquals(new String[]{"4F850DF7BFED2A03E0537402E80A8497"}, messageParameter.getMultipleValue().toArray());
    }

    @Test
    public void testURIRegexpReturnsGroupsIsEmpty() throws Exception {
        MessageParameter messageParameter = testURIRegexpReturnsGroups(true, ParsingRuleType.REGEX_URI,
                ".*/api/space/managed-domain/managed-elements/(\\S*)/equipment-holders/([^\\s/]*)$",
                "");
        assertArrayEquals(new String[]{}, messageParameter.getMultipleValue().toArray());
    }

    @Test
    public void testURIRegexpReturnsGroupsButResultIsEmpty() throws Exception {
        MessageParameter messageParameter = testURIRegexpReturnsGroups(true, ParsingRuleType.REGEX_URI,
                ".*/api/space/managed-domain/managed-elements/(\\S*)/equipment-holders/([^\\s/]*)$",
                "http://mockingbird-tst:8080/mockingbird-transports-df/some/other/api/managed-domain/managed-elements/4F850DF7BFED2A03E0537402E80A8497/equipment-holders/4F850DF7BFED2A03E0537402E80A8497");
        assertArrayEquals(new String[]{}, messageParameter.getMultipleValue().toArray());
    }

    @Test
    public void testThrowingExceptionIfIllegalXpath() throws ContentException {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage(StringStartsWith.startsWith("Failed applying xpath. Probably xPaths is incorrect"));
        ParsingRule parsingRule = createXpathParsingRule(".//com:characteristic[com:characteristicID = \"Line_ID\"]//com:characteristicValue/text()");
        Message message = createMessage();
        setParents(parsingRule);
        TcContext context = new TcContext();
        context.put("aaa", "bbb");
        parsingRule.apply(message, InstanceContext.from(context, null), false);
    }

    @Test
    public void testValidXpath() throws ContentException {
        ParsingRule xpathParsingRule = createXpathParsingRule(".//starterchain");
        Message message = createMessage();
        setParents(xpathParsingRule);
        TcContext context = new TcContext();
        context.put("aaa", "bbb");
        MessageParameter apply = xpathParsingRule.apply(message, InstanceContext.from(context, null), false);
    }

    @Test
    public void testUmlaut() throws ContentException {
//        ParsingRule xpathParsingRule = createXpathParsingRule("//*[contains(text(),'Konsistenzsicherung')]/text()");
        ParsingRule xpathParsingRule = createXpathParsingRule("//*[local-name()='specificationID' and text()='Konsistenzsicherung abschließen']/../../*[local-name()='entityKey']/*[local-name()='keyA']/text()");
        Message message = createMessage(textUmlaut);
        setParents(xpathParsingRule);
        TcContext context = new TcContext();
        context.put("aaa", "bbb");
        MessageParameter messageParameter = xpathParsingRule.apply(message, InstanceContext.from(context, null), false);
        assertEquals("9147412680913944911", messageParameter.getSingleValue());
    }


    @Test
    public void testJSONParsedSingleValue() {
        ParsingRule jsonPathParsingRule = new SystemParsingRule();
        jsonPathParsingRule.setParsingType(ParsingRuleType.JSON_PATH);
        jsonPathParsingRule.setExpression("$.string");
        jsonPathParsingRule.setMultiple(false);
        Message message = createJSONMessage();
        TcContext context = new TcContext();
        context.put("aaa", "bbb");
        MessageParameter apply = jsonPathParsingRule.apply(message, InstanceContext.from(context, null), false);
        assertEquals("Hello World", apply.getSingleValue());
    }

    @Test
    public void testJSONParsedMultipleValue() {
        ParsingRule jsonPathParsingRule = new SystemParsingRule();
        jsonPathParsingRule.setParsingType(ParsingRuleType.JSON_PATH);
        jsonPathParsingRule.setExpression("$.array");
        jsonPathParsingRule.setMultiple(true);
        Message message = createJSONMessage();
        TcContext context = new TcContext();
        context.put("aaa", "bbb");
        MessageParameter apply = jsonPathParsingRule.apply(message, InstanceContext.from(context, null), false);
        assertArrayEquals(new String[]{"1", "2", "3"}, apply.getMultipleValue().toArray());
    }


    private Message createJSONMessage() {
        return new Message("""
                {
                  "array": [
                    1,
                    2,
                    3
                  ],
                  "boolean": true,
                  "null": null,
                  "number": 123,
                  "object": {
                    "a": "b",
                    "c": "d",
                    "e": "f"
                  },
                  "string": "Hello World"
                }\
                """);
    }

    private Message createMessage() throws ContentException {
        return createMessage(this.text);
    }

    private Message createMessage(String text) throws ContentException {
        Message message = new Message();
        message.setText(text);
        message.setContent(new XmlContentProvider().provide(message));
        return message;
    }

    private void setParents(ParsingRule parsingRule) {
        System system = new System();
        system.setName("SomeSystem");
        Operation mock = mock(Operation.class);
        when(mock.getName()).thenReturn("SomeOperation");
        system.getOperations().add(mock);
        when(mock.getParent()).thenReturn(system);
        parsingRule.setParent(mock);
    }

    private ParsingRule createXpathParsingRule(String expression) {
        ParsingRule parsingRule = new SystemParsingRule();
        parsingRule.setParsingType(ParsingRuleType.XPATH);
        parsingRule.setExpression(expression);
        parsingRule.setName("SomeParsingRule");
        return parsingRule;
    }

}
