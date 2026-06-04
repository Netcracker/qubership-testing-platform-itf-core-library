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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Writer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
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

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AddDateTest {

    private static final String DEFAULT_DATE = "2024-05-25 12:00:00";
    private static final String LONG_DATE = "2024-05-25 12:00:00.123Z";
    private static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault());
    private static final DateTimeFormatter LONG_FORMATTER = DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss.SSS'Z'")
            .withZone(ZoneId.systemDefault());

    @Mock
    private InternalContextAdapter context;

    @Mock
    private Writer writer;

    @Mock
    private Node node;

    @Mock
    private Node childNode0;

    @Mock
    private Node childNode1;

    @Mock
    private Node childNode2;

    @Mock
    private RuntimeServices runtimeServices;

    @Mock
    private Logger log;

    private AddDate directive;

    @BeforeEach
    void setUp() throws Exception {
        directive = new AddDate();

        // Set rsvc via reflection
        java.lang.reflect.Field field = Directive.class.getDeclaredField("rsvc");
        field.setAccessible(true);
        field.set(directive, runtimeServices);

        when(runtimeServices.getLog()).thenReturn(log);
    }

    // ==================== Helper Methods for Testing ====================

    private String addDays(String date, int days) {
        LocalDateTime dt = LocalDateTime.parse(date, DEFAULT_FORMATTER);
        return DEFAULT_FORMATTER.format(dt.plusDays(days));
    }

    private String addHours(String date, int hours) {
        LocalDateTime dt = LocalDateTime.parse(date, DEFAULT_FORMATTER);
        return DEFAULT_FORMATTER.format(dt.plusHours(hours));
    }

    private String addMinutes(String date, int minutes) {
        LocalDateTime dt = LocalDateTime.parse(date, DEFAULT_FORMATTER);
        return DEFAULT_FORMATTER.format(dt.plusMinutes(minutes));
    }

    // ==================== determineCalendarFieldByAddedTime TESTS ====================

    @Test
    void determineCalendarFieldByAddedTime_WithDayUnit_ShouldReturnCalendarDate() throws Exception {
        // when
        int result = invokeDetermineCalendarFieldByAddedTime("5d");
        // then
        assertEquals(java.util.Calendar.DATE, result);
    }

    @Test
    void determineCalendarFieldByAddedTime_WithDayUnitUppercase_ShouldReturnCalendarDate() throws Exception {
        // when
        int result = invokeDetermineCalendarFieldByAddedTime("5D");
        // then
        assertEquals(java.util.Calendar.DATE, result);
    }

    @Test
    void determineCalendarFieldByAddedTime_WithHourUnit_ShouldReturnCalendarHour() throws Exception {
        // when
        int result = invokeDetermineCalendarFieldByAddedTime("3h");
        // then
        assertEquals(java.util.Calendar.HOUR, result);
    }

    @Test
    void determineCalendarFieldByAddedTime_WithHourUnitUppercase_ShouldReturnCalendarHour() throws Exception {
        // when
        int result = invokeDetermineCalendarFieldByAddedTime("3H");
        // then
        assertEquals(java.util.Calendar.HOUR, result);
    }

    @Test
    void determineCalendarFieldByAddedTime_WithMinuteUnit_ShouldReturnCalendarMinute() throws Exception {
        // when
        int result = invokeDetermineCalendarFieldByAddedTime("15m");
        // then
        assertEquals(java.util.Calendar.MINUTE, result);
    }

    @Test
    void determineCalendarFieldByAddedTime_WithMinuteUnitUppercase_ShouldReturnCalendarMinute() throws Exception {
        // when
        int result = invokeDetermineCalendarFieldByAddedTime("15M");
        // then
        assertEquals(java.util.Calendar.MINUTE, result);
    }

    @Test
    void determineCalendarFieldByAddedTime_WithUnknownUnit_ShouldReturnZero() throws Exception {
        // when
        int result = invokeDetermineCalendarFieldByAddedTime("5x");
        // then
        assertEquals(0, result);
    }

    @Test
    void determineCalendarFieldByAddedTime_WithNegativeValue_ShouldStillDetectUnit() throws Exception {
        // when
        int result = invokeDetermineCalendarFieldByAddedTime("-2d");
        // then
        assertEquals(java.util.Calendar.DATE, result);
    }

    // ==================== determineTimeUnitByCalendarField TESTS ====================

    @Test
    void determineTimeUnitByCalendarField_WithCalendarDate_ShouldReturnDay() throws Exception {
        // when
        String result = invokeDetermineTimeUnitByCalendarField(java.util.Calendar.DATE);
        // then
        assertEquals("d", result);
    }

    @Test
    void determineTimeUnitByCalendarField_WithCalendarHour_ShouldReturnHour() throws Exception {
        // when
        String result = invokeDetermineTimeUnitByCalendarField(java.util.Calendar.HOUR);
        // then
        assertEquals("h", result);
    }

    @Test
    void determineTimeUnitByCalendarField_WithCalendarMinute_ShouldReturnMinute() throws Exception {
        // when
        String result = invokeDetermineTimeUnitByCalendarField(java.util.Calendar.MINUTE);
        // then
        assertEquals("m", result);
    }

    @Test
    void determineTimeUnitByCalendarField_WithUnknownField_ShouldReturnEmpty() throws Exception {
        // when
        String result = invokeDetermineTimeUnitByCalendarField(999);
        // then
        assertEquals(StringUtils.EMPTY, result);
    }

    // ==================== addTimeToData TESTS (via reflection) ====================

    @Test
    void addTimeToData_WithDaysAdded_ShouldIncreaseDateByDays() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(DEFAULT_FORMATTER.parse(DEFAULT_DATE)));
        String addedTime = "5d";

        // when
        String result = invokeAddTimeToData(date, addedTime, DEFAULT_FORMATTER);

        // then
        assertEquals(addDays(DEFAULT_DATE, 5), result);
    }

    @Test
    void addTimeToData_WithDaysSubtracted_ShouldDecreaseDateByDays() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(DEFAULT_FORMATTER.parse(DEFAULT_DATE)));
        String addedTime = "-3d";

        // when
        String result = invokeAddTimeToData(date, addedTime, DEFAULT_FORMATTER);

        // then
        assertEquals(addDays(DEFAULT_DATE, -3), result);
    }

    @Test
    void addTimeToData_WithHoursAdded_ShouldIncreaseDateByHours() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(DEFAULT_FORMATTER.parse(DEFAULT_DATE)));
        String addedTime = "7h";

        // when
        String result = invokeAddTimeToData(date, addedTime, DEFAULT_FORMATTER);

        // then
        assertEquals(addHours(DEFAULT_DATE, 7), result);
    }

    @Test
    void addTimeToData_WithHoursSubtracted_ShouldDecreaseDateByHours() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(DEFAULT_FORMATTER.parse(DEFAULT_DATE)));
        String addedTime = "-2h";

        // when
        String result = invokeAddTimeToData(date, addedTime, DEFAULT_FORMATTER);

        // then
        assertEquals(addHours(DEFAULT_DATE, -2), result);
    }

    @Test
    void addTimeToData_WithMinutesAdded_ShouldIncreaseDateByMinutes() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(DEFAULT_FORMATTER.parse(DEFAULT_DATE)));
        String addedTime = "30m";

        // when
        String result = invokeAddTimeToData(date, addedTime, DEFAULT_FORMATTER);

        // then
        assertEquals(addMinutes(DEFAULT_DATE, 30), result);
    }

    @Test
    void addTimeToData_WithMinutesSubtracted_ShouldDecreaseDateByMinutes() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(DEFAULT_FORMATTER.parse(DEFAULT_DATE)));
        String addedTime = "-15m";

        // when
        String result = invokeAddTimeToData(date, addedTime, DEFAULT_FORMATTER);

        // then
        assertEquals(addMinutes(DEFAULT_DATE, -15), result);
    }

    @Test
    void addTimeToData_WithInvalidUnit_ShouldNotChangeDate() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(DEFAULT_FORMATTER.parse(DEFAULT_DATE)));
        String addedTime = "5x";

        // when
        String result = invokeAddTimeToData(date, addedTime, DEFAULT_FORMATTER);

        // then
        assertEquals(DEFAULT_DATE, result);
    }

    @Test
    void addTimeToData_WithEmptyAddedTime_ShouldNotChangeDate() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(DEFAULT_FORMATTER.parse(DEFAULT_DATE)));
        String addedTime = "";

        // when
        String result = invokeAddTimeToData(date, addedTime, DEFAULT_FORMATTER);

        // then
        assertEquals(DEFAULT_DATE, result);
    }

    @Test
    void addTimeToData_WithLongDateFormat_ShouldPreserveLongFormat() throws Exception {
        // given
        java.util.Date date = java.util.Date.from(Instant.from(LONG_FORMATTER.parse(LONG_DATE)));
        String addedTime = "1d";

        // when
        String result = invokeAddTimeToData(date, addedTime, LONG_FORMATTER);

        // then
        // It's assumed that format is the same (with millis and Z)
        assertEquals("2024-05-26 12:00:00.123Z", result);
    }

    // Helper method to invoke private addTimeToData
    private String invokeAddTimeToData(java.util.Date date, String addedTime, DateTimeFormatter formatter)
            throws Exception {
        java.lang.reflect.Method method = AddDate.class.getDeclaredMethod(
                "addTimeToData", java.util.Date.class, String.class, DateTimeFormatter.class);
        method.setAccessible(true);
        return (String) method.invoke(directive, date, addedTime, formatter);
    }

    // Helper method to invoke private determineTimeUnitByCalendarField
    private String invokeDetermineTimeUnitByCalendarField(int timeUnit) throws Exception {
        java.lang.reflect.Method method = AddDate.class.getDeclaredMethod(
                "determineTimeUnitByCalendarField", int.class);
        method.setAccessible(true);
        return (String) method.invoke(directive, timeUnit);
    }

    // Helper method to invoke private determineCalendarFieldByAddedTime
    private int invokeDetermineCalendarFieldByAddedTime(String addedTime) throws Exception {
        java.lang.reflect.Method method = AddDate.class.getDeclaredMethod(
                "determineCalendarFieldByAddedTime", String.class);
        method.setAccessible(true);
        return (int) method.invoke(directive, addedTime);
    }

    // ==================== getDate TESTS (via reflection) ====================

    @Test
    void getDate_WithValidDefaultFormat_ShouldParseCorrectly() throws Exception {
        // given & when
        java.util.Date result = invokeGetDate(DEFAULT_DATE, DEFAULT_FORMATTER);

        // then
        assertNotNull(result);
        assertEquals(DEFAULT_DATE, DEFAULT_FORMATTER.format(result.toInstant()));
    }

    @Test
    void getDate_WithValidLongFormat_ShouldParseCorrectly() throws Exception {
        // given & when
        java.util.Date result = invokeGetDate(LONG_DATE, LONG_FORMATTER);

        // then
        assertNotNull(result);
        assertEquals(LONG_DATE, LONG_FORMATTER.format(result.toInstant()));
    }

    @Test
    void getDate_WithInvalidFormat_ShouldLogErrorAndReturnNull() throws Exception {
        // given
        String invalidDate = "invalid-date";

        // when
        java.util.Date result = invokeGetDate(invalidDate, DEFAULT_FORMATTER);

        // then
        verify(log).error(anyString(), any(Exception.class));
    }

    private java.util.Date invokeGetDate(String stringDate, DateTimeFormatter formatter) throws Exception {
        java.lang.reflect.Method method = AddDate.class.getDeclaredMethod(
                "getDate", String.class, java.util.Date.class, DateTimeFormatter.class);
        method.setAccessible(true);
        return (java.util.Date) method.invoke(directive, stringDate, null, formatter);
    }

    // ==================== render TESTS ====================

    @Test
    void render_WithOneChild_ShouldAddNoTimeAndRenderOriginalDate() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn(DEFAULT_DATE);

        doReturn(true).
                when(runtimeServices).evaluate(context, writer, "", DEFAULT_DATE.replace(' ', 'T'));

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(runtimeServices).evaluate(context, writer, "", DEFAULT_DATE.replace(' ', 'T'));
    }

    @Test
    void render_WithTwoChildren_ShouldAddTimeFromSecondChild() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(DEFAULT_DATE);
        when(childNode1.value(context)).thenReturn("5d");

        String expectedDate = addDays(DEFAULT_DATE, 5).replace(' ', 'T');
        when(runtimeServices.evaluate(context, writer, "5d", expectedDate)).thenReturn(true);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(runtimeServices).evaluate(context, writer, "5d", expectedDate);
    }

    @Test
    void render_WithThreeChildren_ShouldAddMultipleTimeUnits() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(3);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(node.jjtGetChild(2)).thenReturn(childNode2);
        when(childNode0.value(context)).thenReturn(DEFAULT_DATE);
        when(childNode1.value(context)).thenReturn("1d");
        when(childNode2.value(context)).thenReturn("2h");

        // Add 1 day, then 2 hours
        String afterFirst = addDays(DEFAULT_DATE, 1);
        String afterSecond = addHours(afterFirst, 2);
        String expectedDate = afterSecond.replace(' ', 'T');

        when(runtimeServices.evaluate(context, writer, "2h", expectedDate)).thenReturn(true);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(runtimeServices).evaluate(context, writer, "2h", expectedDate);
    }

    @Test
    void render_WithNullChild_ShouldLogWarningAndSkip() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(null);
        when(childNode0.value(context)).thenReturn(DEFAULT_DATE);

        doReturn(true)
                .when(runtimeServices).evaluate(context, writer, "", DEFAULT_DATE.replace(' ', 'T'));

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(log).warn("Not added date");
        verify(runtimeServices).evaluate(context, writer, "", DEFAULT_DATE.replace(' ', 'T'));
    }

    @Test
    void render_WithEmptyChild_ShouldAddNoTimeAndRenderOriginalDate() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(DEFAULT_DATE);
        when(childNode1.value(context)).thenReturn("");

        doReturn(true)
                .when(runtimeServices).evaluate(context, writer, "", DEFAULT_DATE.replace(' ', 'T'));

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(runtimeServices).evaluate(context, writer, "", DEFAULT_DATE.replace(' ', 'T'));
    }

    @Test
    void render_WithLongDateFormat_ShouldParseAndFormatCorrectly() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(2);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(node.jjtGetChild(1)).thenReturn(childNode1);
        when(childNode0.value(context)).thenReturn(LONG_DATE);
        when(childNode1.value(context)).thenReturn("1d");

        String expectedDate = "2024-05-26 12:00:00.123Z".replace(' ', 'T');
        when(runtimeServices.evaluate(context, writer, "1d", expectedDate)).thenReturn(true);

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(runtimeServices).evaluate(context, writer, "1d", expectedDate);
    }

    @Test
    void render_WithInvalidDate_ShouldLogErrorAndSkip() throws Exception {
        // given
        when(node.jjtGetNumChildren()).thenReturn(1);
        when(node.jjtGetChild(0)).thenReturn(childNode0);
        when(childNode0.value(context)).thenReturn("invalid-date");

        // when
        boolean result = directive.render(context, writer, node);

        // then
        verify(log).error(anyString(), any(Exception.class));
        verify(runtimeServices, never()).evaluate(any(), any(), anyString(), anyString());
    }

    // ==================== getName and getType TESTS ====================

    @Test
    void getName_ShouldReturnAddDate() {
        assertEquals("add_date", directive.getName());
    }

    @Test
    void getType_ShouldReturnLine() {
        assertEquals(Directive.LINE, directive.getType());
    }
}