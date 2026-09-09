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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class TriggerStateManagerTest  {

    private TriggerState triggerState;
    private TriggerStateManager triggerStateManager = TriggerStateManager.getInstance();
    private Set<TriggerState> triggerStates = Sets.newHashSetWithExpectedSize(4);
    private static Method stateCalculationForStub;



    @BeforeAll
    public static void prepare() throws NoSuchMethodException {
        stateCalculationForStub = TriggerStateManager.class.getDeclaredMethod("stateCalculationForStub", Set.class);
        stateCalculationForStub.setAccessible(true);
    }

    @BeforeEach
    public void prepareTest(){
        triggerStates.clear();
    }

    @Test
    public void stateCalculationForStabTestNull() throws InvocationTargetException, IllegalAccessException {
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertTrue(triggerState == null || triggerState == TriggerState.EMPTY,
                "Expected null or EMPTY for an empty trigger-state set, but was " + triggerState);
    }

    @Test
    public void stateCalculationForStabTestActivate() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.ACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ACTIVE, triggerState, "Need Active");
        triggerStates.add(TriggerState.ACTIVE);
        triggerStates.add(TriggerState.ACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ACTIVE, triggerState, "Need Active");
    }

    @Test
    public void stateCalculationForStabTestActivatePart() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.ACTIVE);
        triggerStates.add(TriggerState.INACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ACTIVE_PART, triggerState, "Need Active (Not all)");
        triggerStates.add(TriggerState.ACTIVE_PART);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ACTIVE_PART, triggerState, "Need Active (Not all)");

    }

    @Test
    public void stateCalculationForStabTestActivateButError() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.ACTIVE);
        triggerStates.add(TriggerState.ERROR);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ACTIVE_ERROR, triggerState, "Need Active (Errors)");
        triggerStates.add(TriggerState.INACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ACTIVE_ERROR, triggerState, "Need Active (Errors)");
        triggerStates.add(TriggerState.ACTIVE_ERROR);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ACTIVE_ERROR, triggerState, "Need Active (Errors)");
    }

    @Test
    public void stateCalculationForStabTestError() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.ERROR);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ERROR, triggerState, "Need Active");
        triggerStates.add(TriggerState.ERROR);
        triggerStates.add(TriggerState.ERROR);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.ERROR, triggerState, "Need Active");
    }

    @Test
    public void stateCalculationForStabTestInactive() throws InvocationTargetException, IllegalAccessException {
        triggerStates.add(TriggerState.INACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.INACTIVE, triggerState, "Need Active");
        triggerStates.add(TriggerState.INACTIVE);
        triggerStates.add(TriggerState.INACTIVE);
        triggerState = (TriggerState) stateCalculationForStub.invoke(triggerStateManager, triggerStates);
        Assertions.assertEquals(TriggerState.INACTIVE, triggerState, "Need Active");
    }
}
