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

package org.qubership.automation.itf.core.util.eds.service;

import java.util.HashMap;

public enum EdsContentType {

    WSDL_XSD("wsdl-xsd"),
    DIAMETER_DICTIONARY("diameter-dictionary"),
    DATASET("dataset"),
    KEYSTORE("keystore"),
    FAST_STUB("fast-stub");

    private final String value;

    EdsContentType(String value) {
        this.value = value;
    }

    public String getStringValue() {
        return value;
    }

    /**
     * Construct enum value from String.
     *
     * @param value - String representation.
     * @return - The corresponding enum value.
     */
    public static EdsContentType fromValue(String value) {
        for (EdsContentType contentType : EdsContentType.values()) {
            if (contentType.getStringValue().equals(value)) {
                return contentType;
            }
        }
        throw new IllegalArgumentException("Unexpected eds contentType value '" + value + "'.");
    }

    private static final HashMap<String, String> predefinedPathMap = new HashMap<>();
    private static final HashMap<String, String> predefinedFileNameMap = new HashMap<>();

    static {
        predefinedPathMap.put(KEYSTORE.getStringValue(), "");
        predefinedPathMap.put(FAST_STUB.getStringValue(), "");
    }

    static {
        predefinedFileNameMap.put(FAST_STUB.getStringValue(), "fast_stub_config.json");
    }

    public static String getPredefinedPathByType(String type) {
        return predefinedPathMap.get(type);
    }

    public static String getPredefinedFileNameByType(String type) {
        return predefinedFileNameMap.get(type);
    }
}
