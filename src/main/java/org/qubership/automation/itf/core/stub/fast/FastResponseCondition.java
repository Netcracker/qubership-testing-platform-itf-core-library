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

package org.qubership.automation.itf.core.stub.fast;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;
import org.qubership.automation.itf.core.model.jpa.system.stub.OperationEventTrigger;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FastResponseCondition implements Serializable {
    private static final long serialVersionUID = 20250303L;

    String operationDefinitionKey;
    List<ConditionParameter> conditionParameters;

    /**
     * Constructor.
     * @param storableSituation - Situation object.
     */
    public FastResponseCondition(Situation storableSituation) {
        this.operationDefinitionKey = storableSituation.getParent().getOperationDefinitionKey();
        this.conditionParameters = new ArrayList<>();
        Optional<OperationEventTrigger> trigger =  storableSituation.getOperationEventTriggers().stream().findFirst();
        trigger.ifPresent(operationEventTrigger ->
                this.conditionParameters.addAll(operationEventTrigger.getConditionParameters()));
    }
}
