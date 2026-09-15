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

package org.qubership.automation.itf.core.hibernate.spring.converters;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.condition.ConditionsHelper;
import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.util.constants.Condition;

/**
 * Fails if {@code condition_parameters} JSON written by a newer version of {@link
 * ConditionParameter} - an added field, or a {@link Condition} constant this reader does not know -
 * stops parsing on an older reader, or if any parse failure makes {@link
 * ConditionsHelper#isApplicable} read the row as "no conditions, applicable anyway" (COMPAT-03).
 */
class ConditionParametersConverterTest {

    private final ConditionParametersConverter converter = new ConditionParametersConverter();

    @Test
    void parsesTheCurrentFormat() {
        List<ConditionParameter> parsed = converter.convertToEntityAttribute(
                "[{\"name\":\"a\",\"condition\":\"EXISTS\",\"value\":\"\",\"etc\":null,\"orderId\":0}]");

        assertEquals(1, parsed.size());
        assertEquals(Condition.EXISTS, parsed.get(0).getCondition());
        assertFalse(ConditionsHelper.isApplicable(new JsonContext(), parsed),
                "key `a` is absent from the context, so EXISTS should not be satisfied");
    }

    @Test
    void ignoresAFieldAddedByANewerWriter() {
        List<ConditionParameter> parsed = converter.convertToEntityAttribute(
                "[{\"name\":\"a\",\"condition\":\"EXISTS\",\"value\":\"\",\"etc\":null,\"orderId\":0,"
                        + "\"caseSensitive\":true}]");

        assertEquals(1, parsed.size());
        assertEquals("a", parsed.get(0).getName());
        assertEquals(Condition.EXISTS, parsed.get(0).getCondition());
    }

    @Test
    void unknownConditionConstantParsesAsNullAndNeverMatches() {
        List<ConditionParameter> parsed = converter.convertToEntityAttribute(
                "[{\"name\":\"a\",\"condition\":\"CONTAINS\",\"value\":\"\",\"etc\":null,\"orderId\":0}]");

        assertEquals(1, parsed.size());
        assertNull(parsed.get(0).getCondition());
        assertFalse(ConditionsHelper.isApplicable(new JsonContext(), parsed));
    }

    @Test
    void unparseableJsonNeverMatchesInsteadOfMeaningNoConditions() {
        List<ConditionParameter> parsed = converter.convertToEntityAttribute("not valid json");

        assertFalse(parsed.isEmpty(), "an empty list would read as \"no conditions, applicable anyway\"");
        assertFalse(ConditionsHelper.isApplicable(new JsonContext(), parsed));
    }

    @Test
    void blankColumnStillMeansNoConditions() {
        List<ConditionParameter> parsed = converter.convertToEntityAttribute("");

        assertTrue(parsed.isEmpty());
        assertTrue(ConditionsHelper.isApplicable(new JsonContext(), parsed));
    }
}
