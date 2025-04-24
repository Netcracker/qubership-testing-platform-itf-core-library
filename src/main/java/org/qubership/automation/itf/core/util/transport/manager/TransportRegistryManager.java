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
import java.rmi.RemoteException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.constants.TransportState;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.descriptor.StorableDescriptor;
import org.qubership.automation.itf.core.util.exception.ExportException;
import org.qubership.automation.itf.core.util.exception.NoDeployedTransportException;
import org.qubership.automation.itf.core.util.exception.TransportException;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;
import org.qubership.automation.itf.core.util.transport.base.Transport;
import org.qubership.automation.itf.core.util.transport.registry.base.AbstractTransportRegistry;
import org.qubership.automation.itf.core.util.transport.service.Wrapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransportRegistryManager implements TransportManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransportRegistryManager.class);

    private static TransportRegistryManager INSTANCE = new TransportRegistryManager();

    private AbstractTransportRegistry registry;

    private TransportRegistryManager() {
    }

    public static TransportRegistryManager getInstance() {
        return INSTANCE;
    }

    public void init(AbstractTransportRegistry registry) throws ExportException {
        this.registry = registry;
        this.registry.init();
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void registerTransports(Set<Transport> transports) {
        for (Transport transport : transports) {
            try {
                registerTransport(transport);
            } catch (ExportException e) {
                LOGGER.error("Error exporting transport {}", transport.getClass().getName(), e);
            }
        }
    }

    private void registerTransport(Transport transport) throws ExportException {
        try {
            AccessTransport accessTransport = Wrapper.wrap(transport, registry);
            registry.register(accessTransport);
        } catch (Exception e) {
            throw new ExportException("Error while performing transport export", e);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void unregister(String typeName) throws TransportException {
        try {
            registry.unregister(typeName);
        } catch (RemoteException e) {
            throw new TransportException(e);
        }
    }

    @Override
    public void produceEvent(String typeName, MarshalledObject<Message> message,
                             StorableDescriptor triggerConfigurationDescriptor, String sessionId)
            throws TransportException {
        try {
            registry.produceEvent(typeName, message, triggerConfigurationDescriptor, sessionId);
        } catch (RemoteException e) {
            throw new TransportException(e);
        }
    }

    @Override
    public AccessTransport find(String typeName) throws NoDeployedTransportException {
        try {
            return registry.find(typeName);
        } catch (RemoteException e) {
            LOGGER.error("find exception: ", e);
            throw new NoDeployedTransportException(e);
        }
    }

    @Override
    public Map<String, PropertyDescriptor> getProperties(String typeName) throws TransportException {
        try {
            return registry.getProperties(typeName);
        } catch (RemoteException e) {
            throw new TransportException(e);
        }
    }

    @Override
    public Map<String, AccessTransport> getTransports() throws TransportException {
        try {
            return registry.getTransports();
        } catch (RemoteException e) {
            throw new TransportException(e);
        }
    }

    @Override
    public Map<String, String> getTransportTypes() throws TransportException {
        try {
            return registry.getTransportTypes();
        } catch (RemoteException e) {
            throw new TransportException(e);
        }
    }

    @Override
    public Map<String, Pair<String, String>> getViews() throws TransportException {
        try {
            return registry.getViews();
        } catch (RemoteException e) {
            throw new TransportException(e);
        }
    }

    public void setRegistry(AbstractTransportRegistry registry) {
        this.registry = registry;
    }

    public void destroy() {
        registry.destroy();
    }

    public TransportState getState(String typeName) {
        return registry.getState(typeName);
    }

    public Map<String, TransportState> getStates() {
        return registry.getStates();
    }
}
