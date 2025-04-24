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

package org.qubership.automation.itf.core.message.parser;

import static org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants.EXPRESSION_VAR;
import static org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants.EXPRESSION_VAR_DEFAULT_VALUE;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.parser.MessageParameter;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.util.exception.ContentException;
import org.qubership.automation.itf.core.util.helper.ContentHelper;
import org.qubership.automation.itf.core.util.provider.ParsingRuleProvider;
import org.qubership.automation.itf.core.util.services.CoreServices;

import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Parser {

    /**
     * Parse message against parsingRules; add parsed parameters to a new JsonContext and return it.
     */
    public JsonContext parseToJsonContext(Message message, Collection<? extends ParsingRule> parsingRules,
                                          BigInteger projectId) {
        Map<String, MessageParameter> parsedParameters = parse(projectId, message, null, parsingRules);
        JsonContext context = new JsonContext();
        for (Map.Entry<String, MessageParameter> parameter : parsedParameters.entrySet()) {
            context.put(parameter.getKey(),
                    parameter.getValue().isMultiple()
                            ? parameter.getValue().getMultipleValue() : parameter.getValue().getSingleValue());
        }
        return context;
    }

    /**
     * Parse message against parsingRules provided by providers parameter.
     */
    public Map<String, MessageParameter> parse(BigInteger projectId, Message message, InstanceContext instanceContext,
                                               ParsingRuleProvider... providers) {
        log.debug("Parse start");
        if (providers.length > 0) {
            log.debug("ParsingRuleProviders.length = {}", providers.length);
            Map<String, MessageParameter> parameters = Maps.newHashMapWithExpectedSize(providers.length * 5);
            for (ParsingRuleProvider provider : providers) {
                if (provider == null) {
                    continue;
                }
                log.debug("ParsingRuleProvider is '{}', parsingRules count = {}", provider.getName(),
                        provider.returnParsingRules().size());
                applyRules(message, provider.returnParsingRules(), parameters, instanceContext, projectId);
            }
            log.debug("Parse finish");
            return parameters;
        } else {
            log.debug("ParsingRuleProviders.length = 0. Parse finish.");
            return Collections.emptyMap();
        }
    }

    /**
     * Parse message against parsingRules collection.
     */
    public Map<String, MessageParameter> parse(BigInteger projectId, Message message, InstanceContext instanceContext,
                                               Collection<? extends ParsingRule> parsingRules) {
        Map<String, MessageParameter> parameters = Maps.newHashMapWithExpectedSize(20);
        applyRules(message, parsingRules, parameters, instanceContext, projectId);
        return parameters;
    }

    private void applyRules(Message message, Collection<? extends ParsingRule> parsingRules,
                            Map<String, MessageParameter> parameters, InstanceContext instanceContext,
                            BigInteger projectId) {
        Map<String, Boolean> contentTypes = Maps.newHashMapWithExpectedSize(3);
        String parsingRuleType;
        long startTime;
        boolean expressionVarSetting = Boolean.parseBoolean(CoreServices.getProjectSettingsService().get(projectId,
                EXPRESSION_VAR, EXPRESSION_VAR_DEFAULT_VALUE));
        for (ParsingRule parsingRule : parsingRules) {
            /* Previous behaviour:
                 1. Apply rule - Assume that calculated parameter has not empty value
                 2. Check if parameters contain parameter with this name
                 3. If do NOT contain - add parameter

                 So, applying of the rule could be in vain.

                 Current behaviour:
                 1. Check if parameters contain parameter with this name
                 2. If do NOT contain - Apply rule
                 3. Add parameter if calculated parameter has not empty value
              */
            if (StringUtils.isBlank(parsingRule.getParamName())) {
                continue;
            }
            if (parameters.get(parsingRule.getParamName()) != null) {
                continue;
            }
            log.debug("prepared parsingRule is '{}'", parsingRule.getParamName());
            /* I think it's better to separate a rule parsing from other rules.
                Of course try-catch in the loop is overhead but...
                without try-catch one invalid rule breaks the whole parsing.
                I have discussed it with:
                 @saza (His point is: How in that case can we inform users about the rule parsing failure?),
                 @nidu (His point is: Let's try. It's better than all testcases fail due to the only invalid rule).
             */
            parsingRuleType = parsingRule.getParsingType().toString();
            // Do NOT again and again try to apply JsonPath rules for non-Json and/or Xpath rules for non-Xml
            if (contentTypes.containsKey(parsingRuleType) && !contentTypes.get(parsingRuleType)) {
                continue;
            }
            try {
                ContentHelper.getInstance().trySetContent(message, parsingRuleType);
                if (parsingRule.applicable(message)) {
                    contentTypes.put(parsingRuleType, true);
                    startTime = System.currentTimeMillis();
                    MessageParameter parameter = parsingRule.apply(message, instanceContext, expressionVarSetting);
                    String paramName = parameter.getParamName();
                    longDurationWarning(startTime, paramName, parsingRule.getParsedExpression());
                    if (!(parameter.getMultipleValue().isEmpty() || StringUtils.isBlank(parameter.getSingleValue()))) {
                        log.debug("Found message parameter: [{}] = '{}'", paramName, parameter.getSingleValue());
                        parameters.put(paramName, parameter);
                    }
                } else {
                    contentTypes.put(parsingRuleType, false);
                }
            } catch (ContentException ex) {
                contentTypes.put(parsingRuleType, false);
                log.error("Message parsing is failed (probably incorrect message format).  "
                                + "Rule '{}' (expression: '{}') at {}\nException: ", parsingRule.getParamName(),
                        parsingRule.getParsedExpression(), parsingRule.getParsingRulePath(), ex);
            } catch (Throwable ex) {
                log.error("Applying rule '{}' (expression: '{}') at {} failed with exception ",
                        parsingRule.getParamName(), parsingRule.getParsedExpression(), parsingRule.getParsingRulePath(),
                        ex);
            }
        }
    }

    private void longDurationWarning(long startTime, String paramName, String expression) {
        long duration = System.currentTimeMillis() - startTime;
        if (duration > 2000) {
            log.warn("Too slow parsing: {} ms (paramName: {}, expression: {})", duration, paramName, expression);
        }
    }
}
