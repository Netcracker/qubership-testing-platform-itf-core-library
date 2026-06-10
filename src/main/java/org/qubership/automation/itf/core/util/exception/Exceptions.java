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

package org.qubership.automation.itf.core.util.exception;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.commons.lang3.exception.ExceptionUtils;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class Exceptions {

    private static final Function<Throwable, String> TO_MESSAGE = new Function<Throwable, String>() {
        @Nonnull
        @Override
        public String apply(@Nullable Throwable input) {
            return ExceptionUtils.getMessage(input);
        }
    };

    public static String getMessagesOnly(Throwable throwable) {
        return Joiner.on(":\n").join(Lists.transform(ExceptionUtils.getThrowableList(throwable), TO_MESSAGE));
    }

    /**
     * do the same as {@link #getMessagesOnly(Throwable)} but
     * will provide additional root cause info if throwable is not an instance of {@link CoreException}.
     */
    public static void fillWithBriefInfo(@Nonnull StringBuilder sb, @Nonnull Throwable throwable) {
        List<Throwable> throwableList = ExceptionUtils.getThrowableList(throwable);
        List<String> extractedRootCause = null;
        if (!(throwable instanceof CoreException)) {
            //take root cause from list, save its description into
            extractedRootCause = extractRootCause(throwableList);
        }
        Joiner.on(":\n").appendTo(sb, Lists.transform(throwableList, TO_MESSAGE));
        if (extractedRootCause != null) {
            boolean firstRow = true;
            for (String causeStr : extractedRootCause) {
                if (!firstRow || !throwableList.isEmpty()) {
                    sb.append("\n");
                }
                firstRow = false;
                sb.append(causeStr);
            }
        }
    }

    /**
     * Removes the last root cause from the list, returns root cause's description.
     *
     * @param list of throwable
     * @return root cause's list.
     */
    @Nullable
    private static List<String> extractRootCause(@Nonnull List<Throwable> list) {
        if (list.isEmpty()) {
            return null;
        }
        Throwable rootCause = list.get(list.size() - 1);
        String[] causeStrs = ExceptionUtils.getRootCauseStackTrace(rootCause);
        //the first str contains the message, so
        list.remove(rootCause);
        return Arrays.asList(causeStrs);
    }

    /**
     * Get 'Short' exception message (the 1st informative message from t and its cause).
     * In case t.getMessage() is null or empty, get message from cause().
     *
     * @param t Throwable to process
     * @return String message.
     */
    public static String getExceptionSummary(Throwable t) {
        if (t == null) return "null exception";

        // 1. Get exception message
        String message = t.getMessage();
        if (message != null && !message.trim().isEmpty()) {
            return message;
        }

        // 2. In case message is empty - check its cause
        String tClassName = t.getClass().getSimpleName();
        Throwable cause = t.getCause();
        if (cause != null && cause != t) { // defense against recursion
            String causeMessage = cause.getMessage();
            if (causeMessage != null && !causeMessage.trim().isEmpty()) {
                return tClassName + ", caused by: " + causeMessage;
            }
            // 3. If cause doesn't have message - get its class name
            return tClassName + ", caused by: " + cause.getClass().getSimpleName();
        }

        // 4. The last - get class name from the original throwable
        return tClassName;
    }
}
