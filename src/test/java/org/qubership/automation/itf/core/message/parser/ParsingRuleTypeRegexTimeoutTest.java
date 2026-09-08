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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;
import org.qubership.atp.common.utils.regex.TimeoutRegexCharSequence;
import org.qubership.atp.common.utils.regex.TimeoutRegexException;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.parser.MessageParameter;
import org.qubership.automation.itf.core.stub.parser.SimpleParsingRule;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;

/**
 * Regression tests for SEC-02: REGEX, REGEX_URI and REGEX_HEADER must all guard their matcher
 * subject with {@link TimeoutRegexCharSequence}, using the same timeout budget, so that none of
 * them can be pinned by a catastrophically-backtracking pattern.
 */
class ParsingRuleTypeRegexTimeoutTest {

    private static final int EXPECTED_TIMEOUT_SECONDS = 60;

    @Test
    void regexWrapsMessageTextInTimeoutRegexCharSequence() {
        SimpleParsingRule rule = new SimpleParsingRule();
        rule.setParamName("p");
        rule.setParsedExpression("(.*)");
        rule.setMultiple(false);
        rule.setAutosave(false);

        Message message = new Message("payload");

        List<Object> constructorArgs = new ArrayList<>();
        try (MockedConstruction<TimeoutRegexCharSequence> construction = mockConstruction(
                TimeoutRegexCharSequence.class,
                (mock, context) -> {
                    constructorArgs.addAll(context.arguments());
                    String subject = (String) context.arguments().get(0);
                    when(mock.length()).thenReturn(subject.length());
                    when(mock.charAt(anyInt())).thenAnswer(invocation -> subject.charAt(invocation.getArgument(0)));
                    when(mock.subSequence(anyInt(), anyInt())).thenAnswer(invocation ->
                            subject.subSequence(invocation.getArgument(0), invocation.getArgument(1)));
                })) {

            MessageParameter result = ParsingRuleType.REGEX.parse(message, rule);

            assertEquals(1, construction.constructed().size(),
                    "REGEX must wrap the message text in TimeoutRegexCharSequence");
            assertEquals(List.of("payload", EXPECTED_TIMEOUT_SECONDS), constructorArgs);
            assertEquals("payload", result.getSingleValue());
        }
    }

    @Test
    void regexUriWrapsUriParamsInTimeoutRegexCharSequence() {
        SimpleParsingRule rule = new SimpleParsingRule();
        rule.setParamName("p");
        rule.setParsedExpression("(.*)");
        rule.setMultiple(false);
        rule.setAutosave(false);

        Message message = new Message("");
        String uriParams = "http://example.com/account/create";
        message.getConnectionProperties().put("uriParams", uriParams);

        List<Object> constructorArgs = new ArrayList<>();
        try (MockedConstruction<TimeoutRegexCharSequence> construction = mockConstruction(
                TimeoutRegexCharSequence.class,
                (mock, context) -> {
                    constructorArgs.addAll(context.arguments());
                    String subject = (String) context.arguments().get(0);
                    when(mock.length()).thenReturn(subject.length());
                    when(mock.charAt(anyInt())).thenAnswer(invocation -> subject.charAt(invocation.getArgument(0)));
                    when(mock.subSequence(anyInt(), anyInt())).thenAnswer(invocation ->
                            subject.subSequence(invocation.getArgument(0), invocation.getArgument(1)));
                })) {

            MessageParameter result = ParsingRuleType.REGEX_URI.parse(message, rule);

            assertEquals(1, construction.constructed().size(),
                    "REGEX_URI must wrap the uriParams subject in TimeoutRegexCharSequence");
            assertEquals(List.of(uriParams, EXPECTED_TIMEOUT_SECONDS), constructorArgs);
            assertEquals(uriParams, result.getSingleValue());
        }
    }

    @Test
    void regexHeaderWrapsHeaderValueInTimeoutRegexCharSequence() {
        SimpleParsingRule rule = new SimpleParsingRule();
        rule.setParamName("p");
        rule.setParsedExpression("X-Evil/(.*)");
        rule.setMultiple(false);
        rule.setAutosave(false);

        Message message = new Message("irrelevant");
        String headerValue = "a".repeat(30);
        message.getHeaders().put("X-Evil", headerValue);

        List<Object> constructorArgs = new ArrayList<>();
        try (MockedConstruction<TimeoutRegexCharSequence> construction = mockConstruction(
                TimeoutRegexCharSequence.class,
                (mock, context) -> {
                    constructorArgs.addAll(context.arguments());
                    String subject = (String) context.arguments().get(0);
                    when(mock.length()).thenReturn(subject.length());
                    when(mock.charAt(anyInt())).thenAnswer(invocation -> subject.charAt(invocation.getArgument(0)));
                    when(mock.subSequence(anyInt(), anyInt())).thenAnswer(invocation ->
                            subject.subSequence(invocation.getArgument(0), invocation.getArgument(1)));
                })) {

            MessageParameter result = ParsingRuleType.REGEX_HEADER.parse(message, rule);

            assertEquals(1, construction.constructed().size(),
                    "REGEX_HEADER must wrap the header-value subject in TimeoutRegexCharSequence");
            assertEquals(List.of(headerValue, EXPECTED_TIMEOUT_SECONDS), constructorArgs);
            assertEquals(headerValue, result.getSingleValue());
        }
    }

    /**
     * SEC-02's reported trigger: a "Regex Header" or "Regex URI" parsing rule whose expression
     * nests a quantifier over {@code .*}, matched against a same-length subject that does not
     * match. This pins {@link Matcher#find()} in catastrophic backtracking with no protection
     * unless the subject is wrapped in {@link TimeoutRegexCharSequence}, as REGEX_URI and
     * REGEX_HEADER now are. The 1-second budget here is only for test speed; production code
     * guards with {@code ParsingRuleType.MAX_REGEX_TIMEOUT_SECONDS} (60 seconds).
     *
     * <p>{@link Pattern#matcher(CharSequence)} only builds the {@link Matcher}; that call
     * returns instantly for any pattern. The backtracking, and with it this test's assertion,
     * only happens once {@link Matcher#find()} runs, and {@code find()} does not respond to
     * {@link Thread#interrupt()} either, which is why a deadline checked on every
     * {@link TimeoutRegexCharSequence#charAt(int)} call is the only guard that works here.</p>
     */
    @Test
    void timeoutRegexCharSequenceAbortsTheSec02CatastrophicBacktrackingTrigger() {
        String evilRegex = "(.*a){26}X";
        String subject = "a".repeat(30);
        Matcher matcher = Pattern.compile(evilRegex).matcher(new TimeoutRegexCharSequence(subject, 1));

        assertTimeoutPreemptively(Duration.ofSeconds(5),
                () -> assertThrows(TimeoutRegexException.class, matcher::find));
    }
}
