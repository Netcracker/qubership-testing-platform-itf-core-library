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

import java.io.IOException;
import java.rmi.MarshalledObject;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nullable;

import org.apache.commons.lang3.tuple.Pair;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.constants.TransportState;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.descriptor.StorableDescriptor;
import org.qubership.automation.itf.core.util.exception.ExportException;
import org.qubership.automation.itf.core.util.exception.TransportException;
import org.qubership.automation.itf.core.util.transport.access.AccessInboundTransport;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;
import org.qubership.automation.itf.core.util.transport.service.CoreCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public abstract class AbstractTransportRegistry implements TransportRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractTransportRegistry.class);

    /*  Solution for NITP-4887
            - activate triggers not while deploying transports but later
     */
    private static final boolean deferredActivation = true;
    private static boolean triggersActivationCompleted;
    private Map<String, TransportState> states = Maps.newConcurrentMap();
    private Map<String, String> transportTypes = Maps.newHashMapWithExpectedSize(50);
    private Map<String, Boolean> availableServers = Maps.newConcurrentMap();
    private CoreCallback coreCallback;
    private boolean loaded;

    public static boolean isTriggersActivationCompleted() {
        return triggersActivationCompleted;
    }

    public abstract void init() throws ExportException;

    /**
     * TODO: Add JavaDoc.
     */
    public void register(AccessTransport accessTransport) {
        String transportName = "";
        try {
            transportName = accessTransport.getUserName();
            states.put(accessTransport.getTypeName(), TransportState.REGISTERING);
            protectedRegister(accessTransport);
            transportTypes.put(accessTransport.getTypeName(), transportName);
            states.put(accessTransport.getTypeName(), TransportState.REGISTERED);
            LOGGER.info("Transport {} registered", transportName);
        } catch (TransportException | RemoteException e) {
            LOGGER.error("Transport didn't registered " + transportName, e);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    protected abstract void protectedRegister(AccessTransport accessTransport) throws TransportException;

    /**
     * TODO: Add JavaDoc.
     */
    public void unregister(String typeName) throws RemoteException {
        protectedUnregister(typeName);
        String userName = transportTypes.remove(typeName);
        states.put(typeName, TransportState.UNDEPLOYED);
        LOGGER.info("Transport {} unregistered", userName);
    }

    protected abstract void protectedUnregister(String typeName) throws RemoteException;

    @Override
    public void produceEvent(String typeName, MarshalledObject<Message> message,
                             StorableDescriptor triggerConfigurationDescriptor,
                             String sessionId) throws RemoteException {
        LOGGER.info("SessionId {}: Transport '{}' / trigger '{}': produced inbound event", sessionId,
                transportTypes.get(typeName), triggerConfigurationDescriptor.getName());
        try {
            coreCallback.produceEvent(message.get(), triggerConfigurationDescriptor, sessionId);
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Error unmarshalling object", e);
        }
    }

    @Override
    public void produceEvent(MarshalledObject<Message> message, Object transportId, Object contextId,
                             String sessionId) throws RemoteException {
        try {
            coreCallback.produceEvent(message.get(), transportId, contextId, sessionId);
        } catch (IOException | ClassNotFoundException e) {
            LOGGER.error("Error unmarshalling object", e);
        }
    }

    @Override
    @Nullable
    public AccessTransport find(String typeName) throws RemoteException {
        return protectedFind(typeName);
    }

    protected abstract AccessTransport protectedFind(String typeName) throws RemoteException;

    @Override
    public Map<String, PropertyDescriptor> getProperties(String typeName) throws RemoteException {
        AccessTransport transport = protectedFind(typeName);
        if (transport == null) {
            return Collections.emptyMap();
        }
        return Maps.uniqueIndex(transport.getProperties(), PropertyDescriptor::getShortName);
    }

    @Override
    public Map<String, AccessTransport> getTransports() throws RemoteException {
        Map<String, AccessTransport> transports = Maps.newHashMapWithExpectedSize(transportTypes.size());
        transportTypes.keySet().forEach(s -> {
            try {
                transports.put(s, protectedFind(s));
            } catch (RemoteException e) {
                LOGGER.error("NoDeployedTransportException", e);
            }
        });
        return Collections.unmodifiableMap(transports);
    }

    @Override
    public Map<String, String> getTransportTypes() throws RemoteException {
        return Collections.unmodifiableMap(transportTypes);
    }

    @Override
    public Map<String, Pair<String, String>> getViews() throws RemoteException {
        Map<String, Pair<String, String>> views = Maps.newHashMapWithExpectedSize(transportTypes.size());
        transportTypes.keySet().forEach(typeName -> {
            try {
                AccessTransport accessTransport = protectedFind(typeName);
                String directive;
                String view;
                directive = accessTransport.getDirective();
                view = accessTransport.getView();
                if (directive == null) {
                    if (accessTransport instanceof AccessInboundTransport) {
                        directive = "__defaultInboundView__";
                    } else {
                        directive = "__defaultView__";
                    }
                }
                views.put(typeName, Pair.of(directive, view));
            } catch (RemoteException ignored) {
                LOGGER.warn("RemoteException:", ignored);
            }
        });
        return Collections.unmodifiableMap(views);
    }

    public CoreCallback getCoreCallback() {
        return coreCallback;
    }

    public void setCoreCallback(CoreCallback coreCallback) {
        this.coreCallback = coreCallback;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Map<String, TransportState> getStates() {
        return states;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public TransportState getState(String typeName) {
        TransportState state = states.get(typeName);
        if (state == null) {
            state = TransportState.NOT_READY;
        }
        return state;
    }

    public boolean isLoaded() {
        return loaded;
    }

    public void setLoaded(boolean loaded) {
        this.loaded = loaded;
    }

    public void setTriggersActivationCompleted(boolean triggersActivationCompleted) {
        this.triggersActivationCompleted = triggersActivationCompleted;
    }

    public abstract void destroy();
}
