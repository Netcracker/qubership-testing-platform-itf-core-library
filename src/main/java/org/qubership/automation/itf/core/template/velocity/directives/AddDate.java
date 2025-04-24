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

import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.context.InternalContextAdapter;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;
import org.apache.velocity.runtime.directive.Directive;
import org.apache.velocity.runtime.parser.node.Node;

public class AddDate extends Directive {
    private static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss"; // 24-hour format
    private static final DateTimeFormatter defaultDateTimeFormatter = DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)
            .withZone(ZoneId.systemDefault());
    // DT Excel datasets contain timestamp fields like 2019-03-23T21:59:32.123Z
    private static final String LONG_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS'Z'";
    private static final DateTimeFormatter longDateTimeFormatter = DateTimeFormatter.ofPattern(LONG_DATE_FORMAT)
            .withZone(ZoneId.systemDefault());

    private static final String DAY = "d";
    private static final String HOUR = "h";
    private static final String MINUTE = "m";

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
            String dataString = StringUtils.EMPTY;
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
        int timeUnit = determineCalendarFieldByAddedTime(addedTime);
        if (timeUnit != 0) {
            String d = addedTime.replace(determineTimeUnitByCalendarField(timeUnit), StringUtils.EMPTY);
            calendar.add(timeUnit, Integer.parseInt(d));
        }
        return currentFormatter.format(calendar.getTime().toInstant());
    }

    private int determineCalendarFieldByAddedTime(String addedTime) {
        if (addedTime.toLowerCase().contains(DAY)) {
            return Calendar.DATE;
        } else if (addedTime.toLowerCase().contains(HOUR)) {
            return Calendar.HOUR;
        } else if (addedTime.toLowerCase().contains(MINUTE)) {
            return Calendar.MINUTE;
        }
        return 0;
    }

    private String determineTimeUnitByCalendarField(int timeUnit) {
        switch (timeUnit) {
            case Calendar.DATE :
                return DAY;
            case Calendar.HOUR :
                return HOUR;
            case Calendar.MINUTE :
                return MINUTE;
            default:
                return StringUtils.EMPTY;
        }
    }
}
