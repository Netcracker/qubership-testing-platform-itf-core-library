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

package org.qubership.automation.itf.core.util.generator.id;

import java.math.BigInteger;
import java.util.Map;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.util.generator.prefix.PrefixGenerator;

import com.google.common.collect.Maps;

public class CounterIdGenerator implements IdGeneratorInterface {
    private static volatile Map<Class<? extends Storable>, BigInteger> idMapStorage = Maps.newHashMap();

    /**
     * TODO: Add JavaDoc.
     */
    public void setStartFrom(Object id, Class<? extends Storable> clazz) {
        BigInteger bigInteger = new BigInteger(PrefixGenerator.removePrefix(id).toString());
        BigInteger counter = idMapStorage.get(clazz);
        if (counter == null) {
            idMapStorage.put(clazz, BigInteger.ZERO);
            return;
        }
        if (bigInteger.compareTo(counter) == 1) {
            idMapStorage.put(clazz, bigInteger);
        }
    }

    @Override
    public Object getId(Class<? extends Storable> clazz) {
        BigInteger bigInteger = idMapStorage.get(clazz);
        if (bigInteger == null) {
            bigInteger = BigInteger.ZERO;
        }
        BigInteger integer = bigInteger.add(BigInteger.ONE);
        idMapStorage.put(clazz, integer);
        return PrefixGenerator.getPrefix(clazz) + integer.toString();
    }
}
