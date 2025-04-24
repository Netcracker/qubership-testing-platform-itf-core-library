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

package org.qubership.automation.itf.core.triggers;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.qubership.automation.itf.core.model.condition.ConditionsHelper;
import org.qubership.automation.itf.core.model.condition.parameter.ConditionParameter;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.util.constants.Condition;
import org.qubership.automation.itf.core.util.constants.Etc;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.testng.Assert;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:*core-test-context.xml"})
public class ConditionPropertyTest {

    @Test
    public void testConditionsAnd() {
        List<ConditionParameter> conditionParameters = createConditionPropertyAndOnly();
        InstanceContext context = InstanceContext.from(new TcContext(), null);
        context.put("tc.aaa", "123");
        context.put("tc.ccc", "ddd");
        Assert.assertFalse(ConditionsHelper.isApplicable(context, conditionParameters));
    }

    @Test
    public void testConditionsOr() {
        List<ConditionParameter> conditionParameters = createConditionPropertyWithOr();
        InstanceContext context = InstanceContext.from(new TcContext(), null);
        context.put("tc.aaa", "123");
        context.put("tc.ccc", "ddd");
        context.put("tc.eee", "fff");
        Assert.assertTrue(ConditionsHelper.isApplicable(context, conditionParameters));
    }

    @Test
    public void testSingleCondition() {
        List<ConditionParameter> conditionParameters = createSingleConditionProperty();
        InstanceContext context = InstanceContext.from(new TcContext(), null);
        context.put("tc.aaa", "123");
        context.put("tc.ccc", "ddd");
        context.put("tc.eee", "fff");
        Assert.assertTrue(ConditionsHelper.isApplicable(context, conditionParameters));
    }

    private List<ConditionParameter> createSingleConditionProperty() {
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam("tc.aaa", Condition.EXISTS, null, null));
        return conditionParameters;
    }

    private List<ConditionParameter> createConditionPropertyAndOnly() {
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam("tc.aaa", Condition.EXISTS, null, Etc.AND));
        conditionParameters.add(createConditionParam("tc.bbb", Condition.EXISTS, null, Etc.AND));
        conditionParameters.add(createConditionParam("tc.bbb.abc", Condition.EXISTS, null, Etc.AND));
        conditionParameters.add(createConditionParam("tc.bbb.abc", Condition.MATCHES, ".*", Etc.AND));
        conditionParameters.add(createConditionParam("tc.ccc", Condition.MATCHES, "ddd", null));
        return conditionParameters;
    }

    private List<ConditionParameter> createConditionPropertyWithOr() {
        List<ConditionParameter> conditionParameters = new ArrayList<>();
        conditionParameters.add(createConditionParam("tc.aaa", Condition.EXISTS, null, Etc.AND));
        conditionParameters.add(createConditionParam("tc.bbb", Condition.EXISTS, null, Etc.AND));
        conditionParameters.add(createConditionParam("tc.ccc", Condition.MATCHES, "ggg", Etc.OR));
        conditionParameters.add(createConditionParam("tc.eee", Condition.MATCHES, "fff", null));
        return conditionParameters;
    }

    private ConditionParameter createConditionParam(String name, Condition condition, String value, Etc etc) {
        ConditionParameter conditionParameter = new ConditionParameter();
        conditionParameter.setName(name);
        conditionParameter.setCondition(condition);
        conditionParameter.setValue(value);
        conditionParameter.setEtc(etc);
        return conditionParameter;
    }

}
