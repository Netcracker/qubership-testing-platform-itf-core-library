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

package org.qubership.automation.itf.core.environment;

import com.google.common.collect.Sets;
import org.qubership.automation.itf.core.util.constants.TriggerState;
import org.qubership.automation.itf.core.util.manager.TriggerStateManager;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class TriggerStateManagerTest  {

    private TriggerState triggerState;
    private TriggerStateManager triggerStateManager = TriggerStateManager.getInstance();
    private Set<TriggerState> triggerStates = Sets.newHashSetWithExpectedSize(4);
    private static Method stateCalculationForStub;



    @BeforeClass
    public static void prepare() throws NoSuchMethodException {
        stateCalculationForStub = TriggerStateManager.class.getDeclaredMethod("stateCalculationForStub", Set.class);
        stateCalculationForStub.setAccessible(true);
    }

    @Before
    public void prepareTest(){
        triggerStates.clear();
    }

    @Test
    public void stateCalculationForStabTestNull() throws InvocationTargetException, IllegalAccessException {
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertNull(triggerState);
    }

    @Test
    public void stateCalculationForStabTestActivate() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.ACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active", TriggerState.ACTIVE, triggerState);
        triggerStates.add(TriggerState.ACTIVE);
        triggerStates.add(TriggerState.ACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active", TriggerState.ACTIVE, triggerState);
    }

    @Test
    public void stateCalculationForStabTestActivatePart() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.ACTIVE);
        triggerStates.add(TriggerState.INACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active (Not all)", TriggerState.ACTIVE_PART, triggerState);
        triggerStates.add(TriggerState.ACTIVE_PART);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active (Not all)", TriggerState.ACTIVE_PART, triggerState);

    }

    @Test
    public void stateCalculationForStabTestActivateButError() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.ACTIVE);
        triggerStates.add(TriggerState.ERROR);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active (Errors)", TriggerState.ACTIVE_ERROR, triggerState);
        triggerStates.add(TriggerState.INACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active (Errors)", TriggerState.ACTIVE_ERROR, triggerState);
        triggerStates.add(TriggerState.ACTIVE_ERROR);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active (Errors)", TriggerState.ACTIVE_ERROR, triggerState);
    }

    @Test
    public void stateCalculationForStabTestError() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.ERROR);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active", TriggerState.ERROR, triggerState);
        triggerStates.add(TriggerState.ERROR);
        triggerStates.add(TriggerState.ERROR);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active", TriggerState.ERROR, triggerState);
    }

    @Test
    public void stateCalculationForStabTestInactive() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.INACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active", TriggerState.INACTIVE, triggerState);
        triggerStates.add(TriggerState.INACTIVE);
        triggerStates.add(TriggerState.INACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assert.assertEquals("Need Active", TriggerState.INACTIVE, triggerState);
    }
}
