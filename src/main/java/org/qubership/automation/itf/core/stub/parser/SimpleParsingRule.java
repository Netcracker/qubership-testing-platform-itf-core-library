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

package org.qubership.automation.itf.core.stub.parser;

import java.math.BigInteger;
import java.util.Map;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.message.parser.AbstractParsingRule;
import org.qubership.automation.itf.core.util.provider.ParsingRuleProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class SimpleParsingRule extends AbstractParsingRule {
    private static final long serialVersionUID = 20240812L;

    public SimpleParsingRule() {
    }

    @JsonIgnore
    @Override
    public String getParsedExpression() {
        return super.getParsedExpression();
    }

    @JsonIgnore
    @Override
    public Boolean getAutosave() {
        return super.getAutosave();
    }

    @JsonIgnore
    @Override
    public BigInteger getProjectId() {
        return super.getProjectId();
    }

    @JsonIgnore
    @Override
    public ParsingRuleProvider getParent() {
        return super.getParent();
    }

    @JsonIgnore
    @Override
    public String getDescription() {
        return super.getDescription();
    }

    @JsonIgnore
    @Override
    public Object getVersion() {
        return super.getVersion();
    }

    @JsonIgnore
    @Override
    public String getName() {
        return super.getName();
    }

    @JsonIgnore
    @Override
    public Storable getExtendsParameters() {
        return super.getExtendsParameters();
    }

    @JsonIgnore
    @Override
    public Object getID() {
        return super.getNaturalId();
    }

    @JsonIgnore
    @Override
    public String getParsingRulePath() {
        return null;
    }

    @JsonIgnore
    @Override
    public String getPrefix() {
        return super.getPrefix();
    }

    @JsonIgnore
    @Override
    public Map<String, String> getStorableProp() {
        return super.getStorableProp();
    }

    /**
     * Constructor.
     * @param storableParsingRule - parsing rule object.
     */
    public SimpleParsingRule(AbstractParsingRule<?> storableParsingRule) {
        super();
        this.setParamName(storableParsingRule.getParamName());
        this.setExpression(storableParsingRule.getExpression());
        this.setParsingType(storableParsingRule.getParsingType());
        this.setMultiple(storableParsingRule.getMultiple());
    }
}
