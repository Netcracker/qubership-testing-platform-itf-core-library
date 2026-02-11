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

package org.qubership.automation.itf.core.model.jpa.message.parser;

import static org.qubership.automation.itf.core.util.parser.ParsingRuleType.from;

import java.math.BigInteger;

import org.jdom2.Element;
import org.json.simple.JSONObject;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.util.annotation.ProduceNewObject;
import org.qubership.automation.itf.core.util.engine.TemplateEngineFactory;
import org.qubership.automation.itf.core.util.helper.ContentHelper;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;
import org.qubership.automation.itf.core.util.provider.ParsingRuleProvider;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class AbstractParsingRule<T extends ParsingRuleProvider>
        extends AbstractStorable implements ParsingRule<T> {

    private Boolean multiple = false;
    private String paramName;
    private String expression;
    private String parsedExpression = "";
    private Boolean autosave = false;
    private ParsingRuleType parsingType;
    private BigInteger projectId;
    private T parent;

    /**
     * Generic constructor for Parsing Rules.
     * @param parent - System or Operation
     */
    public AbstractParsingRule(T parent) {
        setParent(parent);
        setProjectId(parent.getProjectId());
        parent.returnParsingRules().add(this);
    }

    /**
     * Compute parsedExpression, then apply the parsing rule to the message.
     *
     * @return MessageParameter
     */
    public MessageParameter apply(Message message, InstanceContext context, boolean projectExpressionVarValue) {
        this.parsedExpression = computeParsedExpression(context, projectExpressionVarValue);
        return parsingType.parse(message, this);
    }

    /**
     * Check if the parsing rule is applicable to the message.
     *
     * @return true/false
     */
    public boolean applicable(Message message) {
        switch (parsingType) {
            case XPATH:
                return ContentHelper.getInstance().tryForContentType(Element.class, message);
            case REGEX:
            case REGEX_HEADER:
            case REGEX_URI:
                return true;
            case JSON_PATH:
                return ContentHelper.getInstance().tryForContentType(JSONObject.class, message);
            default:
                throw new IllegalArgumentException("Undefined type of parsing rule: " + parsingType.name());
        }
    }

    /**
     * Get naturalId property of the object.
     *
     * @return Object
     */
    @Override
    @ProduceNewObject
    public BigInteger getNaturalId() {
        return super.getNaturalId();
    }

    /**
     * Make String representation of path-to-the-parsingRule.
     *
     * @return String
     */
    public abstract String getParsingRulePath();

    /**
     * Fill properties.
     */
    public void fillProperties(String paramName, String type, String expression, String multiple, String autosave) {
        setParamName(paramName);
        setParsingType(from(type));
        setExpression(expression);
        setMultiple(Boolean.valueOf(multiple));
        setAutosave(Boolean.valueOf(autosave));
    }

    /**
     * Overridden equals method.
     */
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!this.getClass().equals(obj.getClass())) {
            return false;
        }
        ParsingRule parsingRule = (ParsingRule)obj;
        return (this.getID() != null || parsingRule.getID() != null) && this.getID() == parsingRule.getID()
                && this.paramName.equals(parsingRule.getParamName());
    }

    public int hashCode() {
        return (this.getID() + paramName).hashCode();
    }

    private String computeParsedExpression(InstanceContext context, boolean projectExpressionVarValue) {
        if (projectExpressionVarValue) {
            try {
                return TemplateEngineFactory.process(null, getExpression(), context);
            } catch (Exception ex) {
                LOGGER.warn("Parameter {}: Velocity exception while rule expression parsing - {}", this.paramName,
                        ex.getMessage());
                return this.expression;
            }
        } else {
            return this.expression;
        }
    }
}
