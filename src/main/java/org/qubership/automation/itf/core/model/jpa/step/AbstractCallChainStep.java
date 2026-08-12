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

package org.qubership.automation.itf.core.model.jpa.step;

import java.beans.Transient;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import jakarta.annotation.Nonnull;

import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.regenerator.KeysRegeneratable;
import org.qubership.automation.itf.core.util.helper.StorableUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

public abstract class AbstractCallChainStep extends AbstractStep implements Step, KeysRegeneratable {

    private Map<String, String> keysToRegenerate = Maps.newLinkedHashMap();
    private int conditionMaxAttempts;
    private long conditionMaxTime;
    private String conditionUnitMaxTime = TimeUnit.SECONDS.toString();
    private boolean conditionRetry = false;
    private List<ConditionParameter> conditionParameters = new ArrayList<>();
    private String preScript;

    @Override
    public Map<String, String> getKeysToRegenerate() {
        return keysToRegenerate;
    }

    protected void setKeysToRegenerate(Map<String, String> keysToRegenerate) {
        this.keysToRegenerate = keysToRegenerate;
    }

    @Override
    public void fillKeysToRegenerate(Map<String, String> keysToRegenerate) {
        StorableUtils.fillMap(getKeysToRegenerate(), keysToRegenerate);
    }

    @Override
    public void addKeyToRegenerate(@Nonnull String key, @Nonnull String script) {
        this.keysToRegenerate.put(key, script);
    }

    @Override
    public void getScript(@Nonnull String key) {
    }

    @Override
    public void removeKey(@Nonnull String key) {
        this.keysToRegenerate.remove(key);
    }

    void setLastOrderToImportedStep(CallChain callChain) {
        Step step = callChain.getSteps().stream().filter(Objects::nonNull).max(Comparator.comparing(Step::getOrder))
                .orElse(null);
        setOrder(step != null
                ? step.getOrder() + 1
                : 0);
    }

    public int getConditionMaxAttempts() {
        return conditionMaxAttempts;
    }

    public void setConditionMaxAttempts(int conditionMaxAttempts) {
        this.conditionMaxAttempts = conditionMaxAttempts;
    }

    public long getConditionMaxTime() {
        return conditionMaxTime;
    }

    public void setConditionMaxTime(long conditionMaxTime) {
        this.conditionMaxTime = conditionMaxTime;
    }

    public String getConditionUnitMaxTime() {
        return conditionUnitMaxTime;
    }

    public void setConditionUnitMaxTime(String conditionUnitMaxTime) {
        this.conditionUnitMaxTime = checkUnit(conditionUnitMaxTime);
    }

    @Transient
    @JsonIgnore
    public TimeUnit retrieveConditionUnitMaxTime() {
        return convertToTimeUnit(conditionUnitMaxTime);
    }

    public boolean isConditionRetry() {
        return conditionRetry;
    }

    public void setConditionRetry(boolean conditionRetry) {
        this.conditionRetry = conditionRetry;
    }

    public List<ConditionParameter> getConditionParameters() {
        return conditionParameters;
    }

    public void setConditionParameters(List<ConditionParameter> conditionParameters) {
        this.conditionParameters = conditionParameters;
    }

    public String getPreScript() {
        return preScript;
    }

    public void setPreScript(String preScript) {
        this.preScript = preScript;
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
