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

package org.qubership.automation.itf.core.system.stub.conditions;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.automation.itf.core.model.condition.ConditionsHelper;
import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;
import org.qubership.automation.itf.core.model.jpa.context.SpContext;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.constants.Condition;
import org.qubership.automation.itf.core.util.constants.Etc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:*core-test-context.xml"})
public class DefaultConditionPropertyTest {

    @Test
    public void testMatches() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam(".*", Etc.OR));
        assertTrue(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testMatchesNegative() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam("^a", Etc.OR));
        assertFalse(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testOr() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam("^a", Etc.OR));
        conditionParameters.add(createConditionParam(".*", Etc.OR));
        assertTrue(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testOrNegative() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam("^a", Etc.OR));
        conditionParameters.add(createConditionParam("^b", Etc.OR));
        assertFalse(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }


    @Test
    public void testOrAndNegative() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam(".*", Etc.OR));
        conditionParameters.add(createConditionParam(".*", Etc.OR));
        conditionParameters.add(createConditionParam("^a", null));
        assertTrue(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testOrNegativeAnd() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam(".*", Etc.OR));
        conditionParameters.add(createConditionParam(".*", Etc.AND));
        conditionParameters.add(createConditionParam("^b", Etc.OR));
        assertFalse(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testOrAnd() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam(".*", Etc.OR));
        conditionParameters.add(createConditionParam(".*", Etc.OR));
        conditionParameters.add(createConditionParam(".*", null));
        assertTrue(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testAndNegative() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam(".*", Etc.AND));
        conditionParameters.add(createConditionParam("^b", Etc.AND));
        assertFalse(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testAnd() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam(".*", Etc.AND));
        conditionParameters.add(createConditionParam(".*", Etc.AND));
        assertTrue(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testOrOrEmpty() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam("^a", Etc.OR));
        conditionParameters.add(createConditionParam("^b", Etc.OR));
        conditionParameters.add(createConditionParam(".*", null));
        assertTrue(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testOrAndEmpty() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam("^a", Etc.AND));
        conditionParameters.add(createConditionParam(".*", null));
        assertFalse(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    @Test
    public void testOrAndEmptyPositive() {
        SpContext spContext = mockSP();
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam(".*", Etc.AND));
        conditionParameters.add(createConditionParam(".*", null));
        assertTrue(ConditionsHelper.isApplicable(spContext, conditionParameters));
    }

    private SpContext mockSP() {
        SpContext spContext = mock(SpContext.class);
        when(spContext.getIncomingMessage()).thenReturn(new Message("123"));
        when(spContext.getOutgoingMessage()).thenReturn(new Message("123"));
        return spContext;
    }

    private ConditionParameter createConditionParam(String value, Etc etc) {
        ConditionParameter conditionParameter = new ConditionParameter();
        conditionParameter.setCondition(Condition.MATCHES);
        conditionParameter.setEtc(etc);
        conditionParameter.setValue(value);
        return conditionParameter;
    }

}
