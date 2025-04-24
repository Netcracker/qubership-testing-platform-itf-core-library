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

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.step.EmbeddedStep;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.step.SituationStep;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
public class StepObjectManager extends AbstractObjectManager<Step, Step> {

    private Map<String, Class<? extends Step>> subclasses;

    @Autowired
    public StepObjectManager(StepRepository repository) {
        super(Step.class, repository);
    }

    @Override
    public void protectedOnRemove(Step object) {

    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only StepContainer objects are here")
    @Override
    public Step create(Storable parent, String type) {
        Class<? extends Step> stepClass = subclasses.get(type);
        if (stepClass == null) {
            throw new IllegalArgumentException("Cannot create step of type " + type);
        }
        Step result;
        try {
            result = stepClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot create step of type " + type + " with class "
                    + stepClass.getCanonicalName(), e);
        }

        if (parent != null) {
            result.setParent(parent);
            result.setOrder(((StepContainer) parent).getSteps().size());
            ((StepContainer) parent).getSteps().add(result);
        }
        result = repository.save(result);

        return result;
    }

    @Override
    public Step create() {
        throw new IllegalArgumentException("Cannot create step of unknown type!");
    }

    @PostConstruct
    protected void init() {
        subclasses = new HashMap<String, Class<? extends Step>>() {
            {
                put(EmbeddedStep.TYPE, EmbeddedStep.class);
                put(IntegrationStep.TYPE, IntegrationStep.class);
                put(SituationStep.TYPE, SituationStep.class);
            }
        };
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Step objects are here")
    @Override
    public void afterDelete(Storable object) {
        if (object.getParent() instanceof CallChain) {
            synchronized (object.getParent()) {
                ((CallChain) object.getParent()).getSteps().remove((Step) object);
            }
        }
    }
}
