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

import java.math.BigInteger;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Identified;
import org.qubership.automation.itf.core.model.common.Named;
import org.qubership.automation.itf.core.model.common.OptimisticLockable;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;
import org.qubership.automation.itf.core.util.provider.ParsingRuleProvider;

@Entity
public interface ParsingRule<T extends ParsingRuleProvider>
        extends Storable, Named, Identified<Object>, OptimisticLockable<Object> {
    String getParamName();

    Boolean getMultiple();

    Boolean getAutosave();

    ParsingRuleType getParsingType();

    String getExpression();

    T getParent();

    BigInteger getProjectId();

    String getName();

    String getParsedExpression();

    MessageParameter apply(Message message, InstanceContext context, boolean projectExpressionVarValue);

    String getParsingRulePath();

    boolean applicable(Message message);

    void setParamName(String paramName);

    void setMultiple(Boolean isMultiple);

    void setAutosave(Boolean autosave);

    void setParsingType(ParsingRuleType parsingType);

    void setExpression(String expression);

    void setParent(T parent);

    void setProjectId(BigInteger projectId);

    void setName(String name);

    void fillProperties(String paramName, String type, String expression, String multiple, String autosave);
}
