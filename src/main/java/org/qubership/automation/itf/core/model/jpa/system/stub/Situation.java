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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.common.LabeledStorable;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.jpa.message.parser.OperationParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.regenerator.KeysRegeneratable;
import org.qubership.automation.itf.core.util.annotation.ProduceNewObject;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.constants.SituationLevelValidation;
import org.qubership.automation.itf.core.util.constants.TriggerState;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdsListSerializer;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.provider.TriggerProvider;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Situation.class)
public class Situation extends LabeledStorable implements StepContainer, TriggerProvider, KeysRegeneratable {
    private static final long serialVersionUID = 20240812L;

    @JsonSerialize(contentAs = IntegrationStep.class)
    @JsonDeserialize(contentAs = IntegrationStep.class)
    private List<Step> steps = new ArrayList<>(2);

    private List<String> labels = new ArrayList<>(1);

    private Set<SituationEventTrigger> situationEventTriggers = Sets.newHashSetWithExpectedSize(3);
    private Set<OperationEventTrigger> operationEventTriggers = Sets.newHashSetWithExpectedSize(3);
    private Set<OperationParsingRule> parsingRules = Sets.newHashSetWithExpectedSize(3);
    private Map<String, String> keysToRegenerate = Maps.newHashMap();
    private SituationLevelValidation validateIncoming;
    private String bvTestcase;
    private String preScript;
    private String postScript;
    private String preValidationScript;
    private boolean ignoreErrors;

    public Situation() {
    }

    /**
     * Constructor from parent.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Operation objects are here")
    public Situation(Storable parent) {
        if (parent != null) {
            ((Operation) parent).getSituations().add(this);
            setParent(parent);
        }
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Operation getParent() {
        return (Operation) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Operation.class)
    public void setParent(Operation parent) {
        super.setParent(parent);
    }

    @RefCopy
    @JsonSerialize(using = IdsListSerializer.class)
    public Set<ParsingRule> getParsingRules() {
        return parsingRules.stream().map(ParsingRule.class::cast).collect(Collectors.toSet());
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id", scope = OperationParsingRule.class)
    public void setParsingRules(Set<OperationParsingRule> parsingRules) {
        this.parsingRules = parsingRules;
    }

    public void fillParsingRules(Set<OperationParsingRule> rules) {
        StorableUtils.fillCollection(getParsingRules(), rules);
    }

    @Override
    public void fillSituationEventTriggers(Set<SituationEventTrigger> triggers) {
        StorableUtils.fillCollection(getSituationEventTriggers(), triggers);
    }

    @Override
    public void fillOperationEventTriggers(Set<OperationEventTrigger> triggers) {
        StorableUtils.fillCollection(getOperationEventTriggers(), triggers);
    }

    /**
     * Return all Event Triggers in one collection.
     */
    @JsonIgnore
    public Set<EventTrigger> getAllEventTriggers() {
        Set<EventTrigger> triggers = getOperationEventTriggers().stream()
                .map(EventTrigger.class::cast).collect(Collectors.toSet());
        triggers.addAll(
                getSituationEventTriggers().stream().map(EventTrigger.class::cast).collect(Collectors.toSet())
        );
        return triggers;
    }

    public void fillSteps(List<Step> steps) {
        StorableUtils.fillCollection(getSteps(), steps);
    }

    public void addStep(Step step) {
        this.steps.add(step);
    }

    @JsonIgnore
    public Mep getMep() {
        Operation operation = getParent();
        return operation != null ? operation.getMep() : null;
    }

    /**
     * Get Integration Step, checking against null/empty array element is performed.
     * (Sometimes arrays with null/empty element were faced, producing sudden NPEs)
     */
    @JsonIgnore
    public IntegrationStep getIntegrationStep() {
        for (Step step : steps) {
            if (step instanceof IntegrationStep) {
                return (IntegrationStep) step;
            }
        }
        return null;
    }

    @Override
    public void fillKeysToRegenerate(@Nonnull Map<String, String> keysToRegenerate) {
        StorableUtils.fillMap(getKeysToRegenerate(), keysToRegenerate);
    }

    @Override
    public void addKeyToRegenerate(@Nonnull String key, @Nonnull String script) {
        this.keysToRegenerate.put(key, script);
    }

    @Override
    public void getScript(@Nonnull String key) {
        this.keysToRegenerate.get(key);
    }

    @Override
    public void removeKey(@Nonnull String key) {
        this.keysToRegenerate.remove(key);
    }

    @Override
    // NITP-4139 Initial value of Trigger state for copied situation should be set to InActive.
    //     A user should properly configure new situation and then activate it manually.
    public void performPostCopyActions(boolean statusOff) {
        if (statusOff) {
            setStateForTriggers(getSituationEventTriggers());
            setStateForTriggers(getOperationEventTriggers());
        }
    }

    private void setStateForTriggers(Set<? extends EventTrigger> triggers) {
        if (triggers == null || triggers.isEmpty()) {
            return;
        }
        for (EventTrigger trg : triggers) {
            if (trg.getState() != TriggerState.INACTIVE) {
                trg.setState(TriggerState.INACTIVE);
            }
        }
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        if (getIntegrationStep() != null) {
            getIntegrationStep().performPostImportActions(projectId, sessionId);
        }
    }

    public void setValidateIncoming(SituationLevelValidation validateIncoming) {
        this.validateIncoming = (validateIncoming == null) ? SituationLevelValidation.NO : validateIncoming;
    }

    @JsonIgnore
    public boolean getBooleanValidateIncoming() {
        return validateIncoming != null && validateIncoming != SituationLevelValidation.NO;
    }

    @Override
    public Storable returnSimpleParent() {
        Storable operation = super.returnSimpleParent();
        ((Operation) operation).setTransport(getParent().getTransport());
        ((System) operation.getParent()).getTransports().add(getParent().getTransport());
        return operation;
    }

    @ProduceNewObject
    @Override
    public Object getNaturalId() {
        return super.getNaturalId();
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        if (getIntegrationStep() != null) {
            getIntegrationStep().performActionsForImportIntoAnotherProject(
                    replacementMap, projectId, projectUuid, needToUpdateProjectId,
                    needToGenerateNewId);
        }
        if (getOperationEventTriggers() != null) {
            getOperationEventTriggers().forEach(
                    trigger -> trigger.performActionsForImportIntoAnotherProject(
                            replacementMap, projectId, projectUuid, needToUpdateProjectId,
                            needToGenerateNewId)
            );
        }
        if (getSituationEventTriggers() != null) {
            getSituationEventTriggers().forEach(
                    trigger -> trigger.performActionsForImportIntoAnotherProject(
                            replacementMap, projectId, projectUuid, needToUpdateProjectId,
                            needToGenerateNewId)
            );
        }
    }
}
