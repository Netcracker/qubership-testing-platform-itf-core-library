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

import org.apache.commons.lang3.exception.ExceptionUtils;
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
     * TODO: Add JavaDoc.
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
            throw new RemoteException("Error sending data. Error Message: " + e.toString(),
                    new Throwable(ExceptionUtils.getStackTrace(e)));
        }
    }

    @Override
    public Message receive(String sessionId) throws RemoteException {
        try {
            return transport.receive(sessionId);
        } catch (Exception e) {
            throw new RemoteException("Error receiving data. Error Message: " + e.toString(),
                    new Throwable(ExceptionUtils.getStackTrace(e)));
        }
    }

    @Override
    public Message sendReceiveSync(Message messageToSend, BigInteger projectId) throws RemoteException {
        try {
            return transport.sendReceiveSync(messageToSend, projectId);
        } catch (Exception e) {
            throw new RemoteException("Error sending/receiving data. Error Message: " + e.toString(),
                    new Throwable(ExceptionUtils.getStackTrace(e)));
        }
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
