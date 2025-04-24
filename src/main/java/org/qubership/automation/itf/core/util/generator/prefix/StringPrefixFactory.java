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

package org.qubership.automation.itf.core.util.generator.prefix;

import java.util.Map;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.dataset.DataSetList;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.environment.TriggerConfiguration;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.message.parser.OperationParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.project.IntegrationConfig;
import org.qubership.automation.itf.core.model.jpa.report.LinkCollectorConfiguration;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.step.EmbeddedStep;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.step.SituationStep;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.EventTrigger;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;

import com.google.common.collect.Maps;

public class StringPrefixFactory implements IPrefixFactory {
    private final Map<Class<? extends Storable>, String> prefixStorage = Maps.newLinkedHashMap();

    /**
     * TODO: Add JavaDoc.
     */
    public StringPrefixFactory() {
        prefixStorage.put(System.class, "SY_");
        prefixStorage.put(Situation.class, "SI_");
        prefixStorage.put(Operation.class, "OP_");
        prefixStorage.put(EventTrigger.class, "EV_");
        prefixStorage.put(SituationEventTrigger.class, "SE_");
        prefixStorage.put(IntegrationStep.class, "IS_");
        prefixStorage.put(SituationStep.class, "SS_");
        prefixStorage.put(EmbeddedStep.class, "ES_");
        prefixStorage.put(Environment.class, "EN_");
        prefixStorage.put(Template.class, "TM_");
        prefixStorage.put(TriggerConfiguration.class, "TR_");
        prefixStorage.put(Server.class, "SV_");
        prefixStorage.put(SystemParsingRule.class, "SPR_");
        prefixStorage.put(OperationParsingRule.class, "OPR_");
        prefixStorage.put(TcContext.class, "CT_");
        prefixStorage.put(DataSetList.class, "DS_");
        prefixStorage.put(Folder.class, "FL_");
        prefixStorage.put(TransportConfiguration.class, "TT_");
        prefixStorage.put(LinkCollectorConfiguration.class, "LC_");
        prefixStorage.put(CallChain.class, "CH_");
        prefixStorage.put(IntegrationConfig.class, "IC_");
        prefixStorage.put(Storable.class, "UK_");
    }

    @Override
    public Object getPrefix(Class<? extends Storable> clazz) {
        String prefix = prefixStorage.get(clazz);
        if (prefix == null) {
            for (Map.Entry<Class<? extends Storable>, String> entry : prefixStorage.entrySet()) {
                if (clazz.isAssignableFrom(entry.getKey()) || entry.getKey().isAssignableFrom(clazz)) {
                    return entry.getValue();
                }
            }
        }
        return prefix;
    }

    @Override
    public Object getPrefix(Storable object) {
        return getPrefix(object.getClass());
    }

    @Override
    public Class<? extends Storable> getClassByPrefix(Object prefix) {
        for (Map.Entry<Class<? extends Storable>, String> entry : prefixStorage.entrySet()) {
            if (entry.getValue().equals(prefix)) {
                return entry.getKey();
            }
        }
        throw new IllegalArgumentException("Prefix " + prefix + " is not bound to any class.");
    }

    @Override
    public Object removePrefix(Object id) {
        return id.toString().substring(3); //remove 1st 'EN_' chars
    }

}
