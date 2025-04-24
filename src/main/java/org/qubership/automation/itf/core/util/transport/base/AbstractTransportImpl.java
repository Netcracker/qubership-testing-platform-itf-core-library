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

import java.math.BigInteger;
import java.util.UUID;

import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;

import com.fasterxml.jackson.annotation.JsonBackReference;

public abstract class AbstractTransportImpl implements Transport {

    @JsonBackReference
    private AccessTransport remote;

    public AbstractTransportImpl() {
    }

    @Override
    public AccessTransport getRemote() {
        return remote;
    }

    @Override
    public void setRemote(AccessTransport remote) {
        this.remote = remote;
    }

    @Override
    public Message receive(String sessionId) throws Exception {
        throw new UnsupportedOperationException(
                String.format("Receiving is not supported in asynchronous %s transport", getClass().getSimpleName())
        );
    }

    @Override
    public Message sendReceiveSync(Message messageToSend, BigInteger projectId) throws Exception {
        throw new UnsupportedOperationException(
                String.format("This method is not supported in %s transport", getClass().getSimpleName())
        );
    }

    @Override
    public String send(Message message, String sessionId, UUID projectUuid) throws Exception {
        throw new UnsupportedOperationException(
                String.format("Send is not supported in synchronous %s transport", getClass().getSimpleName())
        );
    }

}
