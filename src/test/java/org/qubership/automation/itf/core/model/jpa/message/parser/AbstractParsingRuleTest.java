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

package org.qubership.automation.itf.core.model.jpa.message.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Fails if {@link AbstractParsingRule#equals(Object)} goes back to comparing the id by reference
 * instead of by value, or to treating a rule as unequal to itself when its id is still {@code null}
 * (CORR-09).
 */
class AbstractParsingRuleTest {

    @Test
    @DisplayName("Should equal itself even before an id has been assigned")
    void equalsItself_beforeIdIsAssigned() {
        OperationParsingRule rule = new OperationParsingRule();

        assertTrue(rule.equals(rule));
    }

    @Test
    @DisplayName("Should equal a distinct instance with the same id and paramName, for an id outside "
            + "BigInteger.valueOf's small-value cache")
    void equalsADistinctInstance_withTheSameUncachedId() {
        OperationParsingRule ruleA = ruleWithIdAndParamName(BigInteger.valueOf(42), "code");
        OperationParsingRule ruleB = ruleWithIdAndParamName(BigInteger.valueOf(42), "code");
        assertNotSame(ruleA.getID(), ruleB.getID(), "sanity check: BigInteger.valueOf(42) is not cached, so the "
                + "two ids must already be distinct objects for this test to exercise value comparison");

        assertTrue(ruleA.equals(ruleB));
        assertEquals(ruleA.hashCode(), ruleB.hashCode(), "equal rules must share a hash code");
    }

    @Test
    @DisplayName("Should equal a distinct instance with the same id and paramName, even when both ids happen "
            + "to be built from a small, normally-cached value")
    void equalsADistinctInstance_withTheSameSmallIdBuiltFromFreshInstances() {
        OperationParsingRule ruleA = ruleWithIdAndParamName(new BigInteger("5"), "code");
        OperationParsingRule ruleB = ruleWithIdAndParamName(new BigInteger("5"), "code");
        assertNotSame(ruleA.getID(), ruleB.getID(), "sanity check: new BigInteger(...) never returns a cached "
                + "instance, so the two ids must already be distinct objects for this test to exercise value "
                + "comparison");

        assertTrue(ruleA.equals(ruleB));
        assertEquals(ruleA.hashCode(), ruleB.hashCode(), "equal rules must share a hash code");
    }

    @Test
    @DisplayName("Should not equal a distinct instance with a different paramName, even with the same id")
    void doesNotEqual_aDistinctInstanceWithADifferentParamName() {
        OperationParsingRule ruleA = ruleWithIdAndParamName(BigInteger.valueOf(42), "code");
        OperationParsingRule ruleB = ruleWithIdAndParamName(BigInteger.valueOf(42), "other");

        assertFalse(ruleA.equals(ruleB));
    }

    @Test
    @DisplayName("Should not equal a distinct instance that also has no id yet")
    void doesNotEqual_aDistinctInstanceWithNoIdEither() {
        OperationParsingRule ruleA = new OperationParsingRule();
        ruleA.setParamName("code");
        OperationParsingRule ruleB = new OperationParsingRule();
        ruleB.setParamName("code");

        assertFalse(ruleA.equals(ruleB));
    }

    @Test
    @DisplayName("Should let a HashSet recognize a distinct instance with the same id as already present")
    void hashSetRecognizesADistinctInstance_withTheSameId() {
        OperationParsingRule ruleA = ruleWithIdAndParamName(BigInteger.valueOf(42), "code");
        OperationParsingRule ruleB = ruleWithIdAndParamName(BigInteger.valueOf(42), "code");
        Set<OperationParsingRule> rules = new HashSet<>();
        rules.add(ruleA);

        assertTrue(rules.contains(ruleB));
    }

    private OperationParsingRule ruleWithIdAndParamName(BigInteger id, String paramName) {
        OperationParsingRule rule = new OperationParsingRule();
        rule.setID(id);
        rule.setParamName(paramName);
        return rule;
    }
}
