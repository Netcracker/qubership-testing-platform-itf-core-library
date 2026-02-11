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

import java.nio.charset.StandardCharsets;

import jakarta.inject.Inject;

import org.qubership.automation.itf.core.model.common.Storable;

public class BinaryIdGenerator implements IdGeneratorInterface {

    private String data;

    public BinaryIdGenerator(String value) {
        this.data = value;
    }

    @Inject
    public BinaryIdGenerator() {
        data = "data" + (Math.random() * System.currentTimeMillis() / 31);
    }

    @Override
    public Object getId(Class<? extends Storable> clazz) {
        return getBinary();
    }

    @Override
    public void setStartFrom(Object id, Class<? extends Storable> clazz) {
    }

    private String getBinary() {
        byte[] bytes = this.data.getBytes(StandardCharsets.UTF_8);
        StringBuilder binary = new StringBuilder();
        for (byte b : bytes) {
            int val = b;
            for (int i = 0; i < 8; i++) {
                binary.append((val & 128) == 0 ? 0 : 1);
                val <<= 1;
            }
        }
        return binary.toString();
    }
}
