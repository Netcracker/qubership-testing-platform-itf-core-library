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

package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.hibernate.Hibernate;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.BvCaseContainingObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.ByProject;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.LabeledObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SituationEventTriggerRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SituationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.model.FastStubsCandidate;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.message.parser.OperationParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.EventTrigger;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.copier.OriginalCopyMap;
import org.qubership.automation.itf.core.util.copier.StorableCopier;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.exception.CopyException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.provider.TemplateProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SituationObjectManager extends AbstractObjectManager<Situation, Situation>
        implements BvCaseContainingObjectManager<Situation>, LabeledObjectManager<Situation>, ByProject<Situation> {

    private final StepRepository stepRepository;
    private final SituationEventTriggerRepository situationEventTriggerRepository;

    /**
     * Constructor.
     */
    @Autowired
    public SituationObjectManager(SituationRepository repository, StepRepository stepRepository,
                                  SituationEventTriggerRepository situationEventTriggerRepository) {
        super(Situation.class, repository);
        this.stepRepository = stepRepository;
        this.situationEventTriggerRepository = situationEventTriggerRepository;
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Situation objects are here")
    @Override
    public Collection<UsageInfo> remove(Storable object, boolean force) {
        for (Step step : ((Situation) object).getSteps()) {
            step.remove();
        }
        ((Situation) object).getSteps().clear();
        removeTriggers(((Situation) object).getSituationEventTriggers());
        removeTriggers(((Situation) object).getOperationEventTriggers());
        return super.remove(object, force);
    }

    @Override
    public void protectedOnRemove(Situation object) {
        stepRepository.onDeleteSituation(object);
        situationEventTriggerRepository.onDeleteSituation(object);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Situation objects are here")
    @Override
    public Collection<UsageInfo> findUsages(Storable storable) {
        Collection<UsageInfo> result = Lists.newArrayListWithExpectedSize(50);
        addToUsages(result, "situationSteps", getSteps((BigInteger) storable.getID()));
        Iterable<SituationEventTrigger> triggers =
                situationEventTriggerRepository.getTriggersBySituation((Situation) storable);
        addToUsages(result, "triggers", triggers);
        return result;
    }

    private Iterable<Step> getSteps(BigInteger id) {
        List<BigInteger> stepsIds = stepRepository.getIdsSteps(id);
        List<Step> steps = Lists.newArrayListWithCapacity(stepsIds.size());
        if (!stepsIds.isEmpty()) {
            for (BigInteger stepId : stepsIds) {
                Step step = stepRepository.getOne(stepId);
                steps.add(step);
            }
        }
        return steps;
    }

    public Collection<IdNamePair> findAllByProjectIdOfNameAndId(Object projectId) {
        return ((SituationRepository) repository).findAllByProjectIdOfNameAndId(toBigInt(projectId));
    }

    @Override
    public Collection<Situation> getAllByProject(Object projectId) {
        return ((SituationRepository) repository).findAllByProjectId(toBigInt(projectId));
    }

    @Override
    public Collection<Situation> getByPieceOfNameAndProject(String name, Object projectId) {
        return ((SituationRepository) repository).findByPieceOfNameAndProject(name, toBigInt(projectId));
    }

    @Override
    public List<Situation> getByNameAndProjectId(String name, BigInteger projectId) {
        return ((SituationRepository) repository).findByNameAndProjectId(name, toBigInt(projectId));
    }

    @Override
    public Collection<Situation> getByParentNameAndProject(String name, Object projectId) {
        throw new NotImplementedException("Not implemented method");
    }

    public int countBvCaseUsages(String bvCaseId) {
        return TxExecutor.executeUnchecked(() -> ((SituationRepository) repository).countBvCaseUsages(bvCaseId),
                TxExecutor.readOnlyTransaction());
    }

    @Override
    public Set<String> getAllLabels(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> ((SituationRepository) repository).getAllLabels(projectId),
                TxExecutor.readOnlyTransaction());
    }

    @Override
    public Collection<Situation> getByLabel(String label) {
        throw new NotImplementedException("Method getByLabel is not implemented");
    }

    @Override
    public Collection<Situation> getByLabel(String label, BigInteger projectId) {
        throw new NotImplementedException("Method getByLabel is not implemented");
    }

    @Override
    public List<Object[]> getObjectsWithBvLinks(BigInteger projectId) {
        List<Object[]> objects = TxExecutor.executeUnchecked(() ->
                ((SituationRepository) repository).getSituationsWithBvLinks(projectId),
                TxExecutor.readOnlyTransaction());
        if (objects == null || objects.isEmpty()) {
            return new ArrayList<>();
        }
        for (Object[] object : objects) {
            object[0] = object[0].toString();
            object[1] = object[1].toString();
            object[6] = object[6].toString();
        }
        return objects;
    }

    private void removeTriggers(Set<? extends EventTrigger> triggers) {
        for (EventTrigger trigger : triggers) {
            trigger.remove();
        }
        triggers.clear();
    }

    @Override
    public List<?> getReceiverSystemsFromCallChainSteps(Object chainId) {
        throw new NotImplementedException("Method getReceiverSystemsFromCallChainSteps is not implemented");
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Situation objects are here")
    @Override
    public void additionalMoveActions(Storable situationObj, String sessionId) {
        Situation situation = (Situation) situationObj;
        Operation operation = situation.getParent();
        System system = operation.getParent();
        IntegrationStep integrationStep = situation.getIntegrationStep();
        if (integrationStep != null) {
            integrationStep.setSender(system);
            if (operation.getTransport() != null) {
                logicForStepOperationForMoveCase(system, operation, situation, integrationStep,
                        operation.getTransport(), sessionId);
            }
            logicForStepTemplateForMoveCase(situation, integrationStep, sessionId);
        }
        logicForParsingRulesForMoveCase(operation, situation);
    }

    private void logicForStepOperationForMoveCase(System system, Operation operation, Situation situation,
                                                  IntegrationStep integrationStep, TransportConfiguration transport,
                                                  String sessionId) {
        if (Mep.INBOUND_REQUEST_ASYNCHRONOUS.equals(transport.getMep())) {
            Operation copiedOperationFromStep = integrationStep.getOperation();
            if (copiedOperationFromStep != null) {
                Operation existingOperation = system.getOperations().stream()
                        .filter(oper -> copiedOperationFromStep.getID().equals(oper.getID())).findAny().orElse(null);
                if (existingOperation == null) {
                    Storable cachedOperationCopy = OriginalCopyMap.getInstance().get(sessionId,
                            copiedOperationFromStep.getID());
                    if (cachedOperationCopy == null) {
                        try {
                            cachedOperationCopy = new StorableCopier(sessionId).copy(copiedOperationFromStep, system,
                                    system.getProjectId().toString(),"copy");
                            OriginalCopyMap.getInstance().put(sessionId, copiedOperationFromStep.getID(),
                                    cachedOperationCopy);
                            CoreObjectManager.getInstance().getManager(Operation.class)
                                    .additionalMoveActions(cachedOperationCopy, sessionId);
                            cachedOperationCopy.store();
                            integrationStep.setOperation((Operation)cachedOperationCopy);
                        } catch (CopyException e) {
                            log.error("Can't create copy of {} operation for moved {} situation. Please fill operation "
                                            + "manually.",  copiedOperationFromStep.getName(), situation.getName());
                        }
                    }
                }
            }
        } else {
            integrationStep.setOperation(operation);
        }
    }

    private void logicForStepTemplateForMoveCase(Situation situation, IntegrationStep integrationStep,
                                                 String sessionId) {
        Template template = integrationStep.returnStepTemplate();
        if (Objects.isNull(template)) {
            return;
        }
        if (template.getParent() instanceof System) {
            Template existingSystemTemplate = situation.getParent().getParent().returnTemplates().stream()
                            .filter(systemTemplate -> systemTemplate.getID().equals(template.getID())).findAny()
                    .orElse(null);
            if (existingSystemTemplate == null) {
                createAndSetTemplateCopyForMoveCase(template, situation.getParent().getParent(), integrationStep,
                        situation, sessionId);
            }
        } else {
            createAndSetTemplateCopyForMoveCase(template, situation.getParent(), integrationStep, situation, sessionId);
        }
    }

    private void createAndSetTemplateCopyForMoveCase(Template template,
                                                     TemplateProvider templateProvider,
                                                     IntegrationStep integrationStep,
                                                     Storable situation,
                                                     String sessionId) {
        Template cachedTemplateCopy = (Template) OriginalCopyMap.getInstance().get(sessionId, template.getID());
        if (cachedTemplateCopy == null) {
            if (template.getNaturalId() != null) {
                cachedTemplateCopy = (Template) OriginalCopyMap.getInstance()
                                .get(sessionId, new BigInteger(template.getNaturalId().toString()));
            }
            if (cachedTemplateCopy == null) {
                try {
                    cachedTemplateCopy = (Template) new StorableCopier(sessionId)
                            .copy(template, templateProvider, ((Template) Hibernate.unproxy(template))
                                    .getProjectId().toString(),"copy");
                    OriginalCopyMap.getInstance().put(sessionId, template.getID(), cachedTemplateCopy);
                    cachedTemplateCopy.setParent(templateProvider);
                    cachedTemplateCopy.store();
                    integrationStep.setTemplate(cachedTemplateCopy);
                } catch (CopyException e) {
                    log.error("Can't create copy of {} template for moved {} situation. Please fill template "
                                    + "manually.", template.getName(), situation.getName());
                }
            } else {
                integrationStep.setTemplate(cachedTemplateCopy);
            }
        } else {
            integrationStep.setTemplate(cachedTemplateCopy);
        }
    }

    private void logicForParsingRulesForMoveCase(Operation operation, Situation situation) {
        Set<OperationParsingRule> toDelete = Sets.newHashSet();
        for (ParsingRule parsingRule : situation.getParsingRules()) {
            ParsingRule existingParsingRule = operation.returnParsingRules().stream()
                    .filter(operationParsingRule -> parsingRule.getID().equals(operationParsingRule.getID()))
                    .findAny().orElse(null);
            if (existingParsingRule == null) {
                toDelete.add((OperationParsingRule)parsingRule);
            }
        }
        if (!toDelete.isEmpty()) {
            Set<OperationParsingRule> parsingRules = situation.getParsingRules().stream()
                    .map(OperationParsingRule.class::cast).collect(Collectors.toSet());
            parsingRules.removeAll(toDelete);
            situation.setParsingRules(parsingRules);
        }
    }

    public Optional<List<FastStubsCandidate>> getFastStubsCandidates(UUID projectUuid, List<BigInteger> operationIds) {
        return ((SituationRepository) repository).getFastStubsCandidates(projectUuid, operationIds);
    }
}
