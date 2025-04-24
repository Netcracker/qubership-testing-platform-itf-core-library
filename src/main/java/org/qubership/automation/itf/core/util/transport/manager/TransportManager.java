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

package org.qubership.automation.itf.core.util.transport.manager;

import java.rmi.MarshalledObject;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.descriptor.StorableDescriptor;
import org.qubership.automation.itf.core.util.exception.TransportException;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;
import org.qubership.automation.itf.core.util.transport.base.Transport;

public interface TransportManager {

    void registerTransports(Set<Transport> transports);

    void unregister(String typeName) throws TransportException;

    void produceEvent(String typeName,
                      MarshalledObject<Message> message,
                      StorableDescriptor triggerConfigurationDescriptor,
                      String sessionId) throws TransportException;

    AccessTransport find(String typeName) throws TransportException;

    Map<String, PropertyDescriptor> getProperties(String typeName) throws TransportException;

    Map<String, AccessTransport> getTransports() throws TransportException;

    Map<String, String> getTransportTypes() throws TransportException;

    Map<String, Pair<String, String>> getViews() throws TransportException;
}
