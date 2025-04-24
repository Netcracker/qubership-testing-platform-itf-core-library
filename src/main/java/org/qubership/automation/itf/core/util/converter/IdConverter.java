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

package org.qubership.automation.itf.core.util.converter;

import java.math.BigInteger;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.StringUtils;

public class IdConverter {

    private static final String IS_NOT_NUMERIC = "Object id should be numeric: '%s'";

    /**
     * Convert Object id parameter (very often String) to BigInteger.
     * If id isn't numeric, throw IllegalArgumentException.
     */
    @Nonnull
    public static BigInteger toBigInt(@Nonnull Object id) {
        if (id instanceof BigInteger) {
            return (BigInteger) id;
        } else {
            String stringId = id.toString();
            if (!StringUtils.isNumeric(stringId)) {
                throw new IllegalArgumentException(String.format(IS_NOT_NUMERIC, stringId));
            }
            return new BigInteger(stringId);
        }
    }
}
