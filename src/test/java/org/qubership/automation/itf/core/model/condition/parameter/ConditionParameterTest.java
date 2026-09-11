/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.automation.itf.core.model.condition.parameter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants.CONDITIONS_STYLE_LEGACY;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.model.jpa.context.SpContext;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.util.constants.Condition;
import org.qubership.automation.itf.core.util.services.CoreServices;
import org.qubership.automation.itf.core.util.services.CoreServicesNames;
import org.qubership.automation.itf.core.util.services.projectsettings.IProjectSettingsService;

/**
 * Covers {@link ConditionParameter#applicable(JsonContext)} for a condition variable absent from
 * the context (CORR-02).
 *
 * <p>The documented contract (the class comment above {@link ConditionParameter#applicable} and
 * the {@code conditions.style.legacy} setting) applies to any context, but the check used to be
 * gated on {@code context instanceof InstanceContext}: a {@link TcContext} or a plain {@link
 * JsonContext} fell through to the ordinary value comparison instead, which read a missing key as
 * an empty string and made {@code NOTEQUALS}/{@code NOTMATCHES} return {@code true} regardless of
 * the setting, and made a dotted name throw {@link IllegalStateException} when its parent segment
 * was also absent.</p>
 */
class ConditionParameterTest {

    private static Field coreServicesField;

    private Object originalService;
    private IProjectSettingsService settingsService;

    @BeforeAll
    static void resolveField() throws Exception {
        coreServicesField = CoreServices.class.getDeclaredField("coreServices");
        coreServicesField.setAccessible(true);
    }

    @BeforeEach
    @SuppressWarnings("unchecked")
    void installMockSettingsService() throws Exception {
        Map<String, Object> coreServices = (Map<String, Object>) coreServicesField.get(null);
        originalService = coreServices.get(CoreServicesNames.PROJECT_SETTINGS_SERVICE);
        settingsService = mock(IProjectSettingsService.class);
        coreServices.put(CoreServicesNames.PROJECT_SETTINGS_SERVICE, settingsService);
    }

    @AfterEach
    @SuppressWarnings("unchecked")
    void restoreOriginalSettingsService() throws Exception {
        Map<String, Object> coreServices = (Map<String, Object>) coreServicesField.get(null);
        if (originalService == null) {
            coreServices.remove(CoreServicesNames.PROJECT_SETTINGS_SERVICE);
        } else {
            coreServices.put(CoreServicesNames.PROJECT_SETTINGS_SERVICE, originalService);
        }
    }

    private static ConditionParameter conditionParameter(String name, Condition condition) {
        ConditionParameter parameter = new ConditionParameter();
        parameter.setName(name);
        parameter.setCondition(condition);
        parameter.setValue("whatever");
        return parameter;
    }

    @Test
    void absentVariableOnTcContextUsesItsOwnProjectIdAndHonorsLegacyTrue() {
        BigInteger projectId = BigInteger.valueOf(42);
        TcContext tcContext = new TcContext();
        tcContext.setProjectId(projectId);
        when(settingsService.get(eq(projectId), eq(CONDITIONS_STYLE_LEGACY), any())).thenReturn("true");

        assertTrue(conditionParameter("missing", Condition.NOTEQUALS).applicable(tcContext));
        assertTrue(conditionParameter("missing", Condition.NOTMATCHES).applicable(tcContext));
        assertFalse(conditionParameter("missing", Condition.EQUALS).applicable(tcContext));

        verify(settingsService, atLeastOnce()).get(eq(projectId), eq(CONDITIONS_STYLE_LEGACY), any());
    }

    @Test
    void absentVariableOnTcContextDefaultsToFalseWhenLegacyIsOff() {
        BigInteger projectId = BigInteger.valueOf(42);
        TcContext tcContext = new TcContext();
        tcContext.setProjectId(projectId);
        when(settingsService.get(eq(projectId), eq(CONDITIONS_STYLE_LEGACY), any())).thenReturn("false");

        assertFalse(conditionParameter("missing", Condition.NOTEQUALS).applicable(tcContext));
        assertFalse(conditionParameter("missing", Condition.NOTMATCHES).applicable(tcContext));
    }

    @Test
    void absentVariableSkipsTheProjectSettingsLookupWhenConditionCannotReturnTrue() {
        TcContext tcContext = new TcContext();
        tcContext.setProjectId(BigInteger.valueOf(42));

        assertFalse(conditionParameter("missing", Condition.EQUALS).applicable(tcContext));
        assertFalse(conditionParameter("missing", Condition.MATCHES).applicable(tcContext));
        assertFalse(conditionParameter("missing", Condition.LESS).applicable(tcContext));
        assertFalse(conditionParameter("missing", Condition.GREATER).applicable(tcContext));

        verifyNoInteractions(settingsService);
    }

    @Test
    void absentVariableOnInstanceContextStillUsesItsTcContextProjectId() {
        BigInteger projectId = BigInteger.valueOf(7);
        TcContext tc = new TcContext();
        tc.setProjectId(projectId);
        InstanceContext instanceContext = InstanceContext.from(tc, null);
        when(settingsService.get(eq(projectId), eq(CONDITIONS_STYLE_LEGACY), any())).thenReturn("true");

        assertTrue(conditionParameter("missing", Condition.NOTMATCHES).applicable(instanceContext));

        verify(settingsService).get(eq(projectId), eq(CONDITIONS_STYLE_LEGACY), any());
    }

    @Test
    void absentVariableOnSpContextUsesItsParentInstanceContextProjectId() {
        BigInteger projectId = BigInteger.valueOf(13);
        TcContext tc = new TcContext();
        tc.setProjectId(projectId);
        InstanceContext instanceContext = InstanceContext.from(tc, null);
        SpContext spContext = new SpContext();
        spContext.setParent(instanceContext); // mirrors SpContext(StepInstance): parent = step.getContext()
        when(settingsService.get(eq(projectId), eq(CONDITIONS_STYLE_LEGACY), any())).thenReturn("true");

        assertTrue(conditionParameter("missing", Condition.NOTEQUALS).applicable(spContext));

        verify(settingsService).get(eq(projectId), eq(CONDITIONS_STYLE_LEGACY), any());
    }

    @Test
    void absentVariableOnSpContextWithoutAnInstanceContextParentFallsBackToTheSettingsDefault() {
        SpContext spContext = new SpContext(); // no step, so no parent was ever set
        when(settingsService.get(isNull(), eq(CONDITIONS_STYLE_LEGACY), any())).thenReturn("false");

        assertFalse(conditionParameter("missing", Condition.NOTEQUALS).applicable(spContext));

        verify(settingsService).get(isNull(), eq(CONDITIONS_STYLE_LEGACY), any());
    }

    @Test
    void absentVariableOnPlainJsonContextFallsBackToTheSettingsDefault() {
        JsonContext context = new JsonContext();
        when(settingsService.get(isNull(), eq(CONDITIONS_STYLE_LEGACY), any())).thenReturn("false");

        assertFalse(conditionParameter("missing", Condition.NOTEQUALS).applicable(context));

        verify(settingsService).get(isNull(), eq(CONDITIONS_STYLE_LEGACY), any());
    }

    @Test
    void absentDottedKeyDoesNotThrowRegardlessOfContextType() {
        JsonContext context = new JsonContext(); // no "a" key at all, so "a.b" can't be resolved
        when(settingsService.get(isNull(), eq(CONDITIONS_STYLE_LEGACY), any())).thenReturn("false");

        boolean result = assertDoesNotThrow(
                () -> conditionParameter("a.b", Condition.EQUALS).applicable(context));

        assertFalse(result);
    }
}
