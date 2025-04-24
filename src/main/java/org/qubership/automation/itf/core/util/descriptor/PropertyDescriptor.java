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

import org.qubership.automation.itf.core.util.helper.Reflection;

public class PropertyDescriptor implements Serializable {

    private static final long serialVersionUID = -1115137482811099873L;
    private final boolean loadTemplate;
    private boolean userSettings;
    private final String shortName;
    private final String longName;
    private final String typeName;
    private final boolean optional;
    private final boolean fromServer;
    private final boolean forServer;
    private final boolean forTemplate;
    private final boolean forTrigger;
    private final boolean select;
    private final String[] options;
    private final String description;
    private final boolean dynamic;
    private final boolean redefined;
    private final int order;
    private final boolean map;
    private Object defaultValue;
    private final String fileDirectoryType;
    private final String uiCategory;
    private final String validatePattern;

    /**
     * TODO: Add JavaDoc.
     */
    public PropertyDescriptor(String shortName, String longName, String typeName, String description, boolean optional,
                              boolean forTemplate, boolean fromServer, boolean forServer, boolean forTrigger,
                              boolean select, String[] options, boolean dynamic, boolean redefined,
                              boolean loadTemplate, boolean userSettings, int order, String fileDirectoryType,
                              String uiCategory, String validatePattern) {
        this.shortName = shortName;
        this.longName = longName;
        this.typeName = typeName;
        this.optional = optional;
        this.forTemplate = forTemplate;
        this.description = description;
        this.fromServer = fromServer;
        this.forServer = forServer;
        this.forTrigger = forTrigger;
        this.select = select;
        this.options = options;
        this.dynamic = dynamic;
        this.redefined = redefined;
        this.loadTemplate = loadTemplate;
        this.userSettings = userSettings;
        this.order = order;
        this.map = typeName.equals("java.util.Map");
        this.fileDirectoryType = fileDirectoryType;
        this.uiCategory = uiCategory;
        this.validatePattern = validatePattern;
    }

    public String getDescription() {
        return description;
    }

    public String getShortName() {
        return shortName;
    }

    public String getLongName() {
        return longName;
    }

    public String getTypeName() {
        return typeName;
    }

    public boolean isOptional() {
        return optional;
    }

    public boolean isForTemplate() {
        return forTemplate;
    }

    public boolean isFromServer() {
        return fromServer;
    }

    public boolean isForServer() {
        return forServer;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public boolean isForTrigger() {
        return forTrigger;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Object convert(String value) {
        try {
            return Reflection.detectAndConvertFromString(Class.forName("java.io.File".equals(typeName)
                    ? "java.lang.String" : typeName), value);
        } catch (ClassNotFoundException e) {
            throw new IllegalStateException("Cannot convert string value", e);
        }
    }

    public boolean isSelect() {
        return select;
    }

    public boolean isDynamic() {
        return dynamic;
    }

    public String[] getOptions() {
        return options;
    }

    public boolean isRedefined() {
        return redefined;
    }

    public boolean loadTemplate() {
        return loadTemplate;
    }

    public boolean userSettings() {
        return this.userSettings;
    }

    public int getOrder() {
        return order;
    }

    public boolean isMap() {
        return map;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(Object defaultValue) {
        this.defaultValue = defaultValue;
    }

    public String getFileDirectoryType() {
        return fileDirectoryType;
    }

    public String getUiCategory() {
        return uiCategory;
    }

    public String getValidatePattern() {
        return validatePattern;
    }
}
