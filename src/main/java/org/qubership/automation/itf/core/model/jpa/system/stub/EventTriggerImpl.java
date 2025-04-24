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

package org.qubership.automation.itf.core.model.jpa.system.stub;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.util.constants.TriggerState;
import org.qubership.automation.itf.core.util.helper.StorableUtils;

public abstract class EventTriggerImpl extends AbstractStorable implements EventTrigger {

    private TriggerState state = TriggerState.INACTIVE;
    private List<ConditionParameter> conditionParameters = new ArrayList<>();
    private Throwable exception;

    public EventTriggerImpl(String name) {
        setName(name);
    }

    public List<ConditionParameter> getConditionParameters() {
        return conditionParameters;
    }

    public void setConditionParameters(List<ConditionParameter> conditionParameters) {
        this.conditionParameters = conditionParameters;
    }

    @Override
    public void fillConditionParameters(List<ConditionParameter> conditionParameters) {
        StorableUtils.fillCollection(this.conditionParameters, conditionParameters);
    }

    public TriggerState getState() {
        return state;
    }

    public void setState(TriggerState state) {
        this.state = state;
    }

    public void setTriggerState(String state) {
        this.state = TriggerState.fromString(state);
    }

    public Throwable getException() {
        return exception;
    }

    public void setException(Throwable exception) {
        this.exception = exception;
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
    }
}
