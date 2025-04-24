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

package org.qubership.automation.itf.core.model.condition;

import java.util.ArrayDeque;
import java.util.List;
import java.util.Queue;

import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.util.constants.Etc;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConditionsHelper {
    public static final Logger LOGGER = LoggerFactory.getLogger(ConditionsHelper.class);

    public static void fillConditionParameters(List<ConditionParameter> toConditionParameters,
                                               List<ConditionParameter> fromConditionParameters) {
        StorableUtils.fillCollection(toConditionParameters, fromConditionParameters);
    }

    private static boolean checkCondition(JsonContext context, ConditionParameter conditionParameter) {
        return conditionParameter.applicable(context);
    }

    /**
     * Evaluate conditions against context parameter.
     *
     * @param context             - Context with variables,
     * @param conditionParameters - Conditions list to evaluate,
     * @return - true/false result of evaluation.
     */
    public static boolean isApplicable(JsonContext context, List<ConditionParameter> conditionParameters) {
        if (conditionParameters == null || conditionParameters.isEmpty()) {
            LOGGER.info("Conditions list is empty, applicable anyway");
            return true;
        }

        Queue<Etc> etcQueue = new ArrayDeque<>();
        for (ConditionParameter conditionParameter : conditionParameters) {
            if (conditionParameter.getEtc() != null) {
                etcQueue.add(conditionParameter.getEtc());
            }
        }
        boolean result = checkCondition(context, conditionParameters.get(0));
        for (int i = 1; i < conditionParameters.size(); i++) {
            if (!etcQueue.contains(Etc.OR) && !result) {
                break;
            }
            Etc etc = etcQueue.poll();
            if (etc == null) {
                throw new IllegalStateException(String.format("Etc attribute was not specified for condition "
                                + "property %s, don't know how to process...",
                        conditionParameters.get(i - 1).getName()));
            }
            switch (etc) {
                case AND: {
                    result = result && checkCondition(context, conditionParameters.get(i));
                    break;
                }
                case OR: {
                    result = result || checkCondition(context, conditionParameters.get(i));
                    break;
                }
                default: {
                    throw new IllegalArgumentException(String.format("Illegal boolean operation: %s", etc));
                }
            }
        }
        return result;
    }
}
