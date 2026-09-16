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
import java.rmi.RemoteException;
import java.util.List;
import java.util.UUID;

import jakarta.annotation.Nullable;

import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.annotation.UserName;
import org.qubership.automation.itf.core.util.annotation.View;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.descriptor.Extractor;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;

import com.fasterxml.jackson.annotation.JsonManagedReference;

public abstract class AbstractBaseTransportImpl<T extends Transport> implements AccessTransport {

    @JsonManagedReference
    protected final T transport;
    private final String typeName;
    private final String userName;
    private final String directive;
    private final String view;
    private final List<PropertyDescriptor> properties;
    private final List<String> mandatoryProperties;

    /**
     * Wraps {@code transport}, reading its type name, {@link View} and {@link UserName} annotations,
     * and {@link org.qubership.automation.itf.core.util.annotation.Parameter}-annotated properties.
     *
     * @param transport the transport implementation to wrap
     */
    public AbstractBaseTransportImpl(T transport) {
        this.transport = transport;
        typeName = transport.getClass().getName();
        View viewAnn = transport.getClass().getAnnotation(View.class);
        directive = viewAnn == null ? null : viewAnn.directive();
        view = viewAnn == null ? null : viewAnn.view();
        if (transport.getClass().isAnnotationPresent(UserName.class)) {
            userName = transport.getClass().getAnnotation(UserName.class).value();
        } else {
            userName = transport.getClass().getSimpleName();
        }
        properties = Extractor.extractProperties(transport);
        mandatoryProperties = Extractor.extractMandatory(properties);
    }

    @Override
    public String getTypeName() throws RemoteException {
        return this.typeName;
    }

    @Override
    public String getUserName() throws RemoteException {
        return this.userName;
    }

    @Override
    public String send(Message message, String sessionId, UUID projectUuid) throws RemoteException {
        try {
            return transport.send(message, sessionId, projectUuid);
        } catch (Exception e) {
            throw wrapAsRemoteException("sending", e);
        }
    }

    @Override
    public Message receive(String sessionId) throws RemoteException {
        try {
            return transport.receive(sessionId);
        } catch (Exception e) {
            throw wrapAsRemoteException("receiving", e);
        }
    }

    @Override
    public Message sendReceiveSync(Message messageToSend, BigInteger projectId) throws RemoteException {
        try {
            return transport.sendReceiveSync(messageToSend, projectId);
        } catch (Exception e) {
            throw wrapAsRemoteException("sending/receiving", e);
        }
    }

    /**
     * Wraps a transport failure as a {@link RemoteException} carrying {@code cause} as its actual
     * cause, so a caller can still classify the failure with {@code instanceof} or
     * {@link Throwable#getCause()} rather than parsing the message.
     *
     * @param action what the transport was doing, for the message ("sending", "receiving", ...)
     * @param cause the transport's original failure
     * @return a {@link RemoteException} ready to throw
     */
    private RemoteException wrapAsRemoteException(String action, Exception cause) {
        return new RemoteException("Error " + action + " data via " + typeName + ": " + cause.getMessage(), cause);
    }

    @Nullable
    @Override
    public String getDirective() throws RemoteException {
        return directive;
    }

    @Nullable
    @Override
    public String getView() {
        return view;
    }

    @Override
    public List<PropertyDescriptor> getProperties() throws RemoteException {
        return properties;
    }

    @Override
    public Mep getMep() throws RemoteException {
        return transport.getMep();
    }

    @Override
    public String getEndpointPrefix() {
        return transport.getEndpointPrefix();
    }

    public T unwrap() {
        return transport;
    }

    public List<String> getMandatoryProperties() throws RemoteException {
        return mandatoryProperties;
    }
}
