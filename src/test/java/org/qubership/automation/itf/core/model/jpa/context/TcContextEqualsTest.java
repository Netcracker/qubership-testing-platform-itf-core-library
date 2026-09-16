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

package org.qubership.automation.itf.core.model.jpa.context;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Fails if {@link TcContext#equals(Object)} throws a {@link NullPointerException} for a context
 * that hasn't been saved yet ({@code getID() == null}), instead of honoring the
 * {@link Object#equals} contract, so two fresh contexts can't be placed in the same
 * {@link HashSet} (CORR-08).
 */
class TcContextEqualsTest {

    @Test
    void equalsIsReflexiveForContextWithoutId() {
        TcContext context = new TcContext();

        assertEquals(context, context);
    }

    @Test
    void equalsDoesNotThrowWhenBothContextsHaveNoId() {
        TcContext first = new TcContext();
        TcContext second = new TcContext();

        assertDoesNotThrow(() -> first.equals(second));
        assertNotEquals(first, second);
    }

    @Test
    void secondFreshContextCanBeAddedToHashSet() {
        Set<TcContext> contexts = new HashSet<>();
        contexts.add(new TcContext());

        assertDoesNotThrow(() -> contexts.add(new TcContext()));
        assertEquals(2, contexts.size());
    }

    @Test
    void contextsWithSameIdAreEqual() {
        TcContext first = new TcContext();
        first.setID(BigInteger.ONE);
        TcContext second = new TcContext();
        second.setID(BigInteger.ONE);

        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void contextsWithDifferentIdsAreNotEqual() {
        TcContext first = new TcContext();
        first.setID(BigInteger.ONE);
        TcContext second = new TcContext();
        second.setID(BigInteger.TWO);

        assertNotEquals(first, second);
    }
}
