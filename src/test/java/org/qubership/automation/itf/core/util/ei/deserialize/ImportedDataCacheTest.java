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

package org.qubership.automation.itf.core.util.ei.deserialize;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Fails if a class name read from an imported project archive goes back to being initialized
 * before {@code getDeserializedEntity} checks whether it actually implements {@code Storable}
 * (SEC-05).
 */
class ImportedDataCacheTest {

    /**
     * Records, in load order, the name of every class among {@link PoisonNotStorable} whose static
     * initializer has run. Recording the name here rather than on a field of the class itself
     * matters: reading a static field of {@link PoisonNotStorable} directly would trigger its
     * initialization as a side effect of the check, defeating the point of the test.
     */
    static final class InitTracker {
        private static final List<String> INITIALIZED = new ArrayList<>();

        private InitTracker() {
        }

        static void record(String className) {
            INITIALIZED.add(className);
        }

        static boolean wasInitialized(String className) {
            return INITIALIZED.contains(className);
        }
    }

    static class PoisonNotStorable {
        static {
            InitTracker.record(PoisonNotStorable.class.getName());
        }
    }

    @Test
    void doesNotInitializeAClassThatFailsTheStorableCheck() throws Exception {
        InvocationTargetException wrapped = assertThrows(InvocationTargetException.class,
                () -> invokeGetDeserializedEntity(PoisonNotStorable.class.getName(), "not-root"));

        assertInstanceOf(ClassCastException.class, wrapped.getCause(),
                "a class that does not implement Storable must still be rejected");
        assertFalse(InitTracker.wasInitialized(PoisonNotStorable.class.getName()),
                "a class rejected by asSubclass(Storable.class) must not have been initialized first");
    }

    @Test
    void stillDeserializesAClassThatPassesTheStorableCheck() throws Exception {
        Object result = invokeGetDeserializedEntity(CallChain.class.getName(), "probe-call-chain");

        CallChain callChain = assertInstanceOf(CallChain.class, result);
        assertEquals("probe-call-chain", callChain.getName());
    }

    private Object invokeGetDeserializedEntity(String type, String name) throws Exception {
        ObjectMapper objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        String json = "{\"type\":\"" + type + "\",\"name\":\"" + name + "\"}";
        JsonNode treeNode = objectMapper.readTree(json);
        JsonParser parser = objectMapper.getFactory().createParser(json);

        Method method = ImportedDataCache.class.getDeclaredMethod("getDeserializedEntity", JsonParser.class,
                JsonNode.class, BigInteger.class);
        method.setAccessible(true);
        try {
            return method.invoke(new ImportedDataCache(), parser, treeNode, BigInteger.ONE);
        } finally {
            parser.close();
        }
    }
}
