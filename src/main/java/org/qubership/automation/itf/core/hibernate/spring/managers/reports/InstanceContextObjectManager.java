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

package org.qubership.automation.itf.core.hibernate.spring.managers.reports;

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.MonitoringManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.reports.InstanceContextRepository;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.TcContextBriefInfo;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceContextObjectManager extends AbstractObjectManager<InstanceContext, InstanceContext>
        implements MonitoringManager {

    private final InstanceContextRepository instanceContextRepository;

    @Autowired
    public InstanceContextObjectManager(InstanceContextRepository repository) {
        super(InstanceContext.class, repository);
        instanceContextRepository = repository;
    }

    @Override
    public void protectedOnRemove(InstanceContext object) {
    }

    @Override
    public HashMap<String, Object> getStepInstanceError(String stepInstanceId, Integer partNum) {
        List<Object[]> errors = instanceContextRepository.stepInstanceError(toBigInt(stepInstanceId), partNum);
        HashMap<String, Object> errorInfo = new HashMap<>();
        if (errors != null && !errors.isEmpty() && errors.get(0).length == 2) {
            errorInfo.put("errorName", errors.get(0)[0]);
            errorInfo.put("errorMessage", errors.get(0)[1]);
        }
        return errorInfo;
    }

    @Override
    public HashMap<String, Object> getSpMessageParameters(Object spcontextId, Integer partNum) {
        List<Object[]> queryResults = instanceContextRepository.getSpMessageParameters(toBigInt(spcontextId), partNum);
        HashMap<String, Object> messageParameters = new HashMap<>();
        for (Object[] elem : queryResults) {
            if (elem != null && elem.length == 2) {
                messageParameters.put(elem[0].toString(), elem[1]);
            }
        }
        return messageParameters;
    }

    @Override
    public List<Object[]> allTcContextInstancesErrors(String tcId, Integer partNum) {
        return instanceContextRepository.allTcContextInstancesErrors(toBigInt(tcId), partNum);
    }

    @Override
    public HashMap<String, Object> getStepInstanceMessageIds(String stepInstanceId, Integer partNum) {
        List<Object[]> ids = instanceContextRepository.getMessageIds(toBigInt(stepInstanceId), partNum);
        HashMap<String, Object> idsMap = new HashMap<>();
        if (ids != null && !ids.isEmpty() && ids.get(0).length == 4) {
            idsMap.put("spContextId", ids.get(0)[0]);
            idsMap.put("incomingMessageId", ids.get(0)[1]);
            idsMap.put("outgoingMessageId", ids.get(0)[2]);
            idsMap.put("stepContext", ids.get(0)[3]);
        }
        return idsMap;
    }

    public String getMessageText(Object messageId, Integer partNum) {
        return instanceContextRepository.getMessageText(toBigInt(messageId), partNum);
    }

    public HashMap<String, Object> getMessageHeaders(Object messageId, Integer partNum) {
        return fillMap(instanceContextRepository.getMessageHeaders(toBigInt(messageId), partNum));
    }

    public HashMap<String, Object> getMessageConnectionProperties(Object messageId, Integer partNum) {
        return fillMap(instanceContextRepository.getMessageConnectionProperties(toBigInt(messageId), partNum));
    }

    private HashMap<String, Object> fillMap(List<Object[]> list) {
        if (list == null || list.isEmpty()) {
            return new HashMap<>();
        }
        HashMap<String, Object> map = new HashMap<>();
        for (Object[] obj : list) {
            map.put((String) obj[0], obj[1]);
        }
        return map;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public List<Object[]> getTreeMessagesFromTcContext(String id, Integer partNum) {
        if (!StringUtils.isBlank(id)) {
            return instanceContextRepository.getTcContextTree(toBigInt(id), partNum);
        }
        return Collections.emptyList();
    }

    @Override
    public TcContextBriefInfo getTcContextInformation(String tcContextId, Integer partNum) {
        TcContextBriefInfoObjectManager objectManager = CoreObjectManager.getInstance()
                .getSpecialManager(TcContextBriefInfo.class, TcContextBriefInfoObjectManager.class);
        TcContextBriefInfo tcContextBriefInfo = partNum == null
                        ? objectManager.getById(toBigInt(tcContextId))
                        : objectManager.getByIDAndPartNum(toBigInt(tcContextId), partNum);
        if (tcContextBriefInfo == null) {
            return null;
        }
        if (Objects.isNull(tcContextBriefInfo.getInitiator())) {
            return tcContextBriefInfo;
        }
        List<Object[]> initiatorInfo = instanceContextRepository
              .getTcContextInitiatorInfo(toBigInt(tcContextBriefInfo.getInitiator()), tcContextBriefInfo.getPartNum());
        if (!(Objects.isNull(initiatorInfo) || initiatorInfo.isEmpty())) {
            tcContextBriefInfo.setIniname((String) initiatorInfo.get(0)[0]);
            tcContextBriefInfo.setInitiatortype((String) initiatorInfo.get(0)[1]);
            tcContextBriefInfo.setSituationId((BigInteger) initiatorInfo.get(0)[2]);
            tcContextBriefInfo.setChainId((BigInteger) initiatorInfo.get(0)[3]);
            tcContextBriefInfo.setExecutiondata((String) initiatorInfo.get(0)[4]);
            tcContextBriefInfo.setOperationName((String) initiatorInfo.get(0)[5]);
            tcContextBriefInfo.setSystemName((String) initiatorInfo.get(0)[6]);
            tcContextBriefInfo.setSystemId((BigInteger) initiatorInfo.get(0)[7]);
        }
        return tcContextBriefInfo;
    }

    @Override
    public List<Object[]> getTcContextInfo(String tcContextId) {
        return instanceContextRepository.getTcContextInfo(toBigInt(tcContextId));
    }

    @Override
    public String getContextVariables(String tcContextId, Integer partNum) {
        return instanceContextRepository.getContextVariables(toBigInt(tcContextId), partNum);
    }

    @Override
    public String getContextVariables(String tcContextId) {
        return instanceContextRepository.getContextVariables(toBigInt(tcContextId));
    }

    @Override
    public HashMap<String, String> getTcContextReportLinks(String tcContextId, Integer partNum) {
        List<Object[]> links = instanceContextRepository.getTcContextReportLinks(toBigInt(tcContextId), partNum);
        HashMap<String, String> linksMap = new HashMap<>();
        for (Object[] link : links) {
            if (link != null && link.length == 2) {
                linksMap.put(link[0].toString(), link[1].toString());
            }
        }
        return linksMap;
    }

    @Override
    public Set<String> getTcContextBindingKeys(String tcContextId, Integer partNum) {
        return instanceContextRepository.getTcContextBindingKeys(toBigInt(tcContextId), partNum);
    }

    @Override
    public Set<String> getTcContextBindingKeys(String tcContextId) {
        return instanceContextRepository.getTcContextBindingKeys(toBigInt(tcContextId));
    }

    /**
     * TODO: Add JavaDoc.
     */
    public String getValidationResults(Object spContextId, Integer partNum) {
        return instanceContextRepository.getValidationResults(toBigInt(spContextId), partNum);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public HashMap<String, String> getTcContextStepsSituations(String tcContextId, Integer partNum) {
        List<Object[]> situations = instanceContextRepository.getTcContextStepsSituations(toBigInt(tcContextId),
                partNum);
        HashMap<String, String> situationsMap = new HashMap<>();
        for (Object[] situation : situations) {
            if (situation != null && situation.length == 2) {
                situationsMap.put(situation[0].toString(), situation[1].toString());
            }
        }
        return situationsMap;
    }
}
