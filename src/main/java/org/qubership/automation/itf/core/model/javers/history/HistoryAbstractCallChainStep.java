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

package org.qubership.automation.itf.core.model.javers.history;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.javers.core.metamodel.annotation.Id;
import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;

import com.google.common.collect.Maps;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class HistoryAbstractCallChainStep extends HistoryAbstractStep
        implements HistoryIdentified<BigInteger> {

    @Id
    private BigInteger id;
    private String TYPE;
    private Map<String, String> keysToRegenerate = Maps.newLinkedHashMap();
    private int conditionMaxAttempts;
    private long conditionMaxTime;
    private String conditionUnitMaxTime;
    private boolean conditionRetry;
    private List<ConditionParameter> conditionParameters = new ArrayList<>();
    private String preScript;
}
