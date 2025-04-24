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

package org.qubership.automation.itf.core.hibernate.spring.managers.custom;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.TcContextBriefInfo;

public interface MonitoringManager extends ObjectManager<InstanceContext> {

    HashMap<String, Object> getStepInstanceError(String stepInstanceId, Integer partNum);

    HashMap<String, Object> getSpMessageParameters(Object spcontextId, Integer partNum);

    List<Object[]> allTcContextInstancesErrors(String tcId, Integer partNum);

    HashMap<String, Object> getStepInstanceMessageIds(String stepInstanceId, Integer partNum);

    String getMessageText(Object messageId, Integer partNum);

    HashMap<String, Object> getMessageHeaders(Object messageId, Integer partNum);

    HashMap<String, Object> getMessageConnectionProperties(Object messageId, Integer partNum);

    List<Object[]> getTreeMessagesFromTcContext(String id, Integer partNum);

    TcContextBriefInfo getTcContextInformation(String tcContextId, Integer partNum);

    List<Object[]> getTcContextInfo(String tcContextId);

    String getContextVariables(String tcContextId, Integer partNum);

    String getContextVariables(String tcContextId);

    HashMap<String, String> getTcContextReportLinks(String tcContextId, Integer partNum);

    Set<String> getTcContextBindingKeys(String tcContextId, Integer partNum);

    Set<String> getTcContextBindingKeys(String tcContextId);

    String getValidationResults(Object spContextId, Integer partNum);

    HashMap<String, String> getTcContextStepsSituations(String tcContextId, Integer partNum);
}
