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

package org.qubership.automation.itf.core.model.condition.parameter;

import static java.lang.Float.parseFloat;
import static org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants.CONDITIONS_STYLE_LEGACY;
import static org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants.CONDITIONS_STYLE_LEGACY_DEFAULT_VALUE;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.util.constants.Condition;
import org.qubership.automation.itf.core.util.constants.Etc;
import org.qubership.automation.itf.core.util.engine.TemplateEngineFactory;
import org.qubership.automation.itf.core.util.services.CoreServices;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ConditionParameter implements Serializable {
    private static final long serialVersionUID = 20250303L;

    /* NITP-5825, TASUP-9946:
        if variable is absent in the context, then:
            1. EXISTS returns false,
            2. NOTEXISTS returns true,
            3. All rest conditions:
                In case conditions.style.legacy = false (default):
                    return false for all rest conditions,
                In case conditions.style.legacy = true:
                    return true for {NOTEQUALS, NOTMATCHES},
                    otherwise - return false
     */

    private String name;
    private Condition condition;
    private String value;
    private Etc etc;
    private int orderId;

    /**
     * Evaluate condition against given context.
     */
    public boolean applicable(JsonContext context) {
        if (condition == null) {
            return false;
        } else if (Condition.EXISTS.equals(condition)) {
            return context.containsKey(getName());
        } else if (Condition.NOTEXISTS.equals(condition)) {
            return !context.containsKey(getName());
        } else {
            if (!context.containsKey(getName()) && context instanceof InstanceContext) {
                return Boolean.parseBoolean(CoreServices.getProjectSettingsService().get(
                        ((InstanceContext) context).tc().getProjectId(),
                        CONDITIONS_STYLE_LEGACY,
                        CONDITIONS_STYLE_LEGACY_DEFAULT_VALUE))
                        && (Condition.NOTEQUALS.equals(condition) || Condition.NOTMATCHES.equals(condition));
            }
            Object keyValue = context.get(getName());
            String stringValue = (keyValue == null) ? "" : keyValue.toString();
            String processedValue = TemplateEngineFactory.process(null, value, context);
            switch (condition) {
                case EQUALS:
                    return stringValue.equals(processedValue);
                case NOTEQUALS:
                    return !stringValue.equals(processedValue);
                case MATCHES:
                case NOTMATCHES:
                    Pattern pattern = Pattern.compile(processedValue);
                    Matcher matcher = pattern.matcher(stringValue);
                    return isKeyFound(matcher);
                case LESS:
                    return less(stringValue, processedValue);
                case GREATER:
                    return less(processedValue, stringValue);
                default:
                    return false;
            }
        }
    }

    private boolean isKeyFound(Matcher matcher) {
        return Condition.MATCHES.equals(this.condition) == matcher.matches();
    }

    private boolean less(String left, String right) {
        try {
            float num1 = parseFloat(left);
            float num2 = parseFloat(right);
            return num1 < num2;
        } catch (Exception e) {
            return false; // Maybe we must throw an exception here
        }
    }

    @Override
    public String toString() {
        return "ConditionParameter{"
                + "name='" + getName() + '\''
                + ", condition=" + condition
                + ", value='" + value + '\''
                + ", etc=" + etc
                + ", orderId=" + orderId
                + '}';
    }
}
