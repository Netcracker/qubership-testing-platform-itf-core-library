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
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.javers.core.metamodel.annotation.Id;
import org.qubership.automation.itf.core.util.constants.SituationLevelValidation;

import com.google.common.collect.Sets;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class HistorySituation extends HistoryAbstractStorable implements HistoryIdentified<BigInteger> {
    @Id
    private BigInteger id;
    private List<String> labels;
    private Set<HistorySituationEventTrigger> situationEventTriggers;
    private Set<HistoryOperationEventTrigger> operationEventTriggers;
    private Set<BigInteger> parsingRulesIds = Sets.newHashSet();
    private Map<String, String> keysToRegenerate;
    private SituationLevelValidation validateIncoming;
    private HistoryIntegrationStep integrationStep;
    private String bvTestcase;
    private String preScript;
    private String postScript;
    private String preValidationScript;
    private boolean ignoreErrors;

}
