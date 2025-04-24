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

package org.qubership.automation.itf.core.util.transport.registry.base;

import java.rmi.MarshalledObject;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Map;

import org.apache.commons.lang3.tuple.Pair;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.descriptor.StorableDescriptor;
import org.qubership.automation.itf.core.util.exception.NoDeployedTransportException;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;

public interface TransportRegistry extends Remote {

    String MB_RMI_TRANSPORT_REGISTRY_KEY = "mb/rmi/transport/registry";
    int RMI_PORT = 1098;

    void register(AccessTransport accessTransport) throws RemoteException;

    void unregister(String typeName) throws RemoteException;

    void produceEvent(MarshalledObject<Message> message, Object transportId, Object contextId, String sessionId)
            throws RemoteException;

    void produceEvent(String typeName, MarshalledObject<Message> message,
                      StorableDescriptor triggerConfigurationDescriptor, String sessionId) throws RemoteException;

    AccessTransport find(String typeName) throws RemoteException, NoDeployedTransportException;

    Map<String, PropertyDescriptor> getProperties(String typeName) throws RemoteException;

    Map<String, AccessTransport> getTransports() throws RemoteException;

    Map<String, String> getTransportTypes() throws RemoteException;

    Map<String, Pair<String, String>> getViews() throws RemoteException;
}
