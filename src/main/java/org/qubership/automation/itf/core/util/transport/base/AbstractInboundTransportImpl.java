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

package org.qubership.automation.itf.core.util.transport.base;

import java.util.UUID;

import org.qubership.automation.itf.core.hibernate.spring.managers.executor.EnvironmentObjectManager;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.transport.service.SessionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractInboundTransportImpl extends AbstractTransportImpl implements InboundTransport {

    private static final Logger LOGGER = LoggerFactory.getLogger(EnvironmentObjectManager.class);

    @Override
    public String send(Message message, String sessionId, UUID projectUuid) throws Exception {
        LOGGER.debug("SessionId {}: add message", sessionId);
        SessionHandler.INSTANCE.addMessage(sessionId, message); //So, let's put the message to holder
        return null; //Inbound response should not return anything
    }

}
