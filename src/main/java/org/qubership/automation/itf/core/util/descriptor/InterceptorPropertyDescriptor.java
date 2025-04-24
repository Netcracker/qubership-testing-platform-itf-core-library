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

package org.qubership.automation.itf.core.util.descriptor;

import java.io.Serializable;

import org.qubership.automation.itf.core.util.constants.InterceptorConstants;

public class InterceptorPropertyDescriptor implements Serializable {
    private static final long serialVersionUID = 20240812L;

    private final String name;
    private final String longname;
    private final String description;
    private final String inputType;
    private final String[] options;
    private final String value;
    private final boolean optional;

    /**
     * TODO: Add JavaDoc.
     */
    public InterceptorPropertyDescriptor(String name, String longname, String description,
                                         InterceptorConstants inputType, String value, boolean optional) {
        this.name = name;
        this.longname = longname;
        this.description = description;
        this.inputType = inputType.getValue();
        this.options = new String[1];
        this.value = value;
        this.optional = optional;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public InterceptorPropertyDescriptor(String name, String longname, String description,
                                         InterceptorConstants inputType, String[] options, String value,
                                         boolean optional) {
        this.name = name;
        this.longname = longname;
        this.description = description;
        this.inputType = inputType.getValue();
        this.options = options;
        this.value = value;
        this.optional = optional;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getInputType() {
        return inputType;
    }

    public String[] getOptions() {
        return options;
    }

    public String getValue() {
        return value;
    }

    public String getLongname() {
        return longname;
    }

    public boolean isOptional() {
        return optional;
    }

}
