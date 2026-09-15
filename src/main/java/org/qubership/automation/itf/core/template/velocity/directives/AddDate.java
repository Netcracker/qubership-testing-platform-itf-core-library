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

package org.qubership.automation.itf.core.template.velocity.directives;

import java.io.IOException;
import java.io.Writer;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

/**
 * Adds a day/hour/minute offset to the date rendered as its first argument, one offset per extra
 * argument: {@code #add_date($date, '5d')}, {@code #add_date($date, '-1 hour', '30 min')}.
 *
 * <p>Each offset argument is a signed integer followed by a unit, matched case-insensitively and
 * with or without a space between the two: {@code d}/{@code day}/{@code days} for
 * {@link Calendar#DATE}, {@code h}/{@code hour}/{@code hours} for {@link Calendar#HOUR}, and
 * {@code m}/{@code min}/{@code mins}/{@code minute}/{@code minutes} for {@link Calendar#MINUTE}.
 * A blank offset argument leaves the date unchanged; any other offset outside this shape throws
 * {@link IllegalArgumentException} naming the accepted units, rather than a
 * {@link NumberFormatException} that names neither the directive nor the accepted units, or a
 * silent no-op.</p>
 */
public class AddDate extends Directive {
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"; // 24-hour format
    private static final DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)
            .withZone(ZoneId.systemDefault());
    // DT Excel datasets contain timestamp fields like 2019-03-23T21:59:32.123Z
    private static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS'Z'";
    private static final DateTimeFormatter longDateTimeFormatter = DateTimeFormatter.ofPattern(LONG_DATE_FORMAT)
            .withZone(ZoneId.systemDefault());

    private static final Pattern ADDED_TIME_PATTERN = Pattern.compile("\\s*([+-]?\\d+)\\s*([a-zA-Z]+)\\s*");
    private static final Set<String> DAY_UNITS = Set.of("d", "day", "days");
    private static final Set<String> HOUR_UNITS = Set.of("h", "hour", "hours");
    private static final Set<String> MINUTE_UNITS = Set.of("m", "min", "mins", "minute", "minutes");

    @Override
    public String getName() {
        return "add_date";
    }

    @Override
    public int getType() {
        return LINE;
    }

    @Override
    public boolean render(InternalContextAdapter internalContextAdapter, Writer writer, Node node) throws IOException,
            ResourceNotFoundException, ParseErrorException, MethodInvocationException {
        DateTimeFormatter currentFormatter = defaultDateTimeFormatter;
        String currentDate = String.valueOf(node.jjtGetChild(0).value(internalContextAdapter));
        currentDate = currentDate.replace('T', ' ');
        if (currentDate.length() != 19) {
            currentFormatter = longDateTimeFormatter;
        }
        Date date = null;
        date = getDate(currentDate, date, currentFormatter);
        if (date != null) {
            String addedTime = StringUtils.EMPTY;
            String dataString = currentDate;
            for (int i = 1; i < node.jjtGetNumChildren(); i++) {
                if (node.jjtGetChild(i) != null) {
                    addedTime = String.valueOf(node.jjtGetChild(i).value(internalContextAdapter));
                    dataString = addTimeToData(date, addedTime, currentFormatter);
                    date = getDate(dataString, date, currentFormatter);
                } else {
                    rsvc.getLog().warn("Not added date");
                }
            }
            dataString = dataString.replace(' ', 'T');
            rsvc.evaluate(internalContextAdapter, writer, addedTime, dataString);

        }
        return true;
    }

    private Date getDate(String stringDate, Date date, DateTimeFormatter currentFormatter) {
        try {
            date = Date.from(Instant.from(currentFormatter.parse(stringDate)));
        } catch (Exception e) {
            rsvc.getLog().error("Error while parsing of " + stringDate + " to date (expected format is "
                    + currentFormatter.toString() + ")", e);
        }
        return date;
    }

    private String addTimeToData(Date date, String addedTime, DateTimeFormatter currentFormatter) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        if (StringUtils.isNotBlank(addedTime)) {
            Matcher matcher = ADDED_TIME_PATTERN.matcher(addedTime);
            if (!matcher.matches()) {
                throw new IllegalArgumentException(invalidOffsetMessage(addedTime));
            }
            int amount = Integer.parseInt(matcher.group(1));
            int calendarField = determineCalendarField(matcher.group(2), addedTime);
            calendar.add(calendarField, amount);
        }
        return currentFormatter.format(calendar.getTime().toInstant());
    }

    private int determineCalendarField(String unit, String originalOffset) {
        String normalizedUnit = unit.toLowerCase(Locale.ROOT);
        if (DAY_UNITS.contains(normalizedUnit)) {
            return Calendar.DATE;
        } else if (HOUR_UNITS.contains(normalizedUnit)) {
            return Calendar.HOUR;
        } else if (MINUTE_UNITS.contains(normalizedUnit)) {
            return Calendar.MINUTE;
        }
        throw new IllegalArgumentException(invalidOffsetMessage(originalOffset));
    }

    private static String invalidOffsetMessage(String offset) {
        return "Directive '#add_date': offset '" + offset + "' is not a number followed by a recognized unit "
                + "(d/day/days, h/hour/hours, m/min/mins/minute/minutes; case-insensitive; a space before "
                + "the unit is optional)";
    }
}
