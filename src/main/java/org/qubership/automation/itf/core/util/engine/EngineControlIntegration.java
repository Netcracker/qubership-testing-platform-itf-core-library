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

package org.qubership.automation.itf.core.util.engine;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.project.IntegrationConfig;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.util.exception.EngineIntegrationException;

public interface EngineControlIntegration extends EngineIntegration {

    void create(CallChain callChain, IntegrationConfig integrationConf,
                Map<String, String> properties, BigInteger projectId) throws EngineIntegrationException;

    void create(Situation situation, IntegrationConfig integrationConf,
                BigInteger projectId) throws EngineIntegrationException;

    void delete(CallChain callChain, IntegrationConfig integrationConf,
                Map<String, String> properties, BigInteger projectId) throws EngineIntegrationException, IOException;

    void delete(Situation situation, IntegrationConfig integrationConf,
                BigInteger projectId) throws EngineIntegrationException, IOException;

    void configure(CallChain callChain, IntegrationConfig integrationConf,
                   Map<String, String> properties, BigInteger projectId) throws EngineIntegrationException;

    boolean isExist(Storable storable, IntegrationConfig integrationConf,
                    Map<String, String> properties, BigInteger projectId) throws IOException;

    String copyWithName(IntegrationConfig integrationConf, String newName,
                        String sourceTcId, BigInteger projectId) throws EngineIntegrationException;
}
