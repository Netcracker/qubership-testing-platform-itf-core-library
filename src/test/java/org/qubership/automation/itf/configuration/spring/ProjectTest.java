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

package org.qubership.automation.itf.configuration.spring;

import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.create;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.getById;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.getFirst;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.managerFor;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.project;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.renameStoreValidate;

import java.math.BigInteger;
import java.util.Collection;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.annotation.Commit;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.NativeManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.executor.SituationObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.executor.TemplateObjectManager;
import org.qubership.automation.itf.core.model.condition.ConditionsHelper;
import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;
import org.qubership.automation.itf.core.model.counter.Counter;
import org.qubership.automation.itf.core.model.counter.CounterImpl;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.template.OperationTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.SystemTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.step.EmbeddedStep;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.step.SituationStep;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.EventTrigger;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;

@Transactional
@Commit
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:hibernate-configuration-test-context.xml"})
public class ProjectTest {

    @PersistenceContext
    protected EntityManager entityManager;

    private static Object systemId = "";
    private static Object callChainId = "";
    private static Object serverId = "";
    private static Object operationId = "";

    @Test
    public void aCreateSystem() {
        System sys = create(project().getSystems(), System.class, null);
        systemId = sys.getID();
        renameStoreValidate(sys);
    }

    @Test
    public void bCreateTransport() {
        System sys = getById(System.class, systemId);
//        TransportConfiguration transport = create(sys, TransportConfiguration.class, null);
//        renameStoreValidate(transport);
    }

    @Test
    public void bCreateParsingRule() {
        System sys = getById(System.class, systemId);
        ParsingRule rule = create(sys, SystemParsingRule.class, null);
        rule.setParsingType(ParsingRuleType.REGEX);
        rule.setParamName("test");
        rule.setMultiple(true);
        rule.setExpression("t");
        renameStoreValidate(rule);
    }

    @Test
    public void cCreateOperation() {
        System sys = getById(System.class, systemId);
//        TransportConfiguration transport = getFirst(TransportConfiguration.class);
        Operation operation = create(sys, Operation.class, null);
        operationId = operation.getID();
//        operation.setTransport(transport);
//        operation.fillParsingRules(Sets.newHashSet(getFirst(ParsingRule.class)));
        renameStoreValidate(operation);
    }

    @Test
    public void bCreateCallChain() {
        CallChain chain = create(project().getCallchains(), CallChain.class, null);
        callChainId = chain.getID();
        renameStoreValidate(chain);
    }

    @Test
    public void bCreateServer() {
        Server server = create(project().getServers(), Server.class, null);
        serverId = server.getID();
        renameStoreValidate(managerFor(Server.class), server);
    }

    @Test
    public void eCreateSituationStep() {
        SituationStep step = (SituationStep)  managerFor(Step.class)
                .create(getById(CallChain.class, callChainId), SituationStep.TYPE);
        step.getExceptionalSituations().add(managerFor(Situation.class).create(getById(Operation.class, operationId)));
        renameStoreValidate(managerFor(Step.class), step);
    }

    @Test
    public void cCreateEmbeddedStep() {
        Step step = managerFor(Step.class).create(getById(CallChain.class, callChainId), EmbeddedStep.TYPE);
        renameStoreValidate(managerFor(Step.class), step);
    }

    @Test
    public void fCreateTemplate() {
        Template template = create(getById(Operation.class, operationId), OperationTemplate.class, null);
        Assert.assertTrue(managerFor(Template.class) instanceof NativeManager);
        //((TemplateObjectManager)managerFor(Template.class)).getChildrenByClass(template, Configuration.class);
        renameStoreValidate(template);
        Template template2 = create(getById(System.class, systemId), SystemTemplate.class, null);
        renameStoreValidate(template2);
        Collection<Template> templates = ((TemplateObjectManager) managerFor(Template.class))
                .getByProjectId((BigInteger) getFirst(StubProject.class).getID());
        Assert.assertEquals(2, templates.size());
    }

    @Test
    public void fCreateSituation() {
        Situation situation = create(getFirst(Operation.class), Situation.class, null);
        renameStoreValidate(situation);
        Collection<Situation> situations = ((SituationObjectManager) managerFor(Situation.class))
                .getAllByProject(getFirst(StubProject.class).getID());
        Assert.assertFalse(situations.isEmpty());
    }

    @Test
    public void gCreateIntegrationStep() {
        Step step = managerFor(Step.class).create(getFirst(Situation.class), IntegrationStep.TYPE);
        renameStoreValidate(managerFor(Step.class), step);
    }

    @Test
    public void hCreateSituationEventTrigger() {
        EventTrigger trigger = managerFor(EventTrigger.class)
                .create(getFirst(Situation.class), SituationEventTrigger.TYPE);
        renameStoreValidate(managerFor(EventTrigger.class), trigger);
    }

    @Test
    @Transactional
    public void iCreateProperty() {
        EventTrigger trigger = managerFor(EventTrigger.class).create(null, SituationEventTrigger.TYPE);
        ConditionParameter conditionParameter = new ConditionParameter();
        ConditionsHelper.fillConditionParameters(trigger.getConditionParameters(),
                Lists.newArrayList(conditionParameter));
        renameStoreValidate(managerFor(EventTrigger.class), trigger);

        Assert.assertNotNull(managerFor(EventTrigger.class).getById(trigger.getID()).getConditionParameters());
    }

    @Test
    public void kCreateCounter() {
        Counter counter = managerFor(Counter.class).create(null, CounterImpl.class.getName());
        counter.getOwners().add(new BigInteger(String.valueOf(1)));
        counter.getOwners().add(new BigInteger(String.valueOf(2)));
        counter.store();
        Assert.assertEquals(0, (long) counter.getIndex());
    }
}
