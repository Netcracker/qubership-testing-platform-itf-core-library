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

package org.qubership.automation.itf.core.model.jpa.server;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import javax.annotation.Nonnull;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.eci.EciConfigurable;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.environment.OutboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.transport.Configuration;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.transport.ConnectionProperties;
import org.qubership.automation.itf.core.util.exception.TransportException;

public interface Server extends Storable, EciConfigurable {

    String getUrl();

    void setUrl(String url);

    ConnectionProperties calculate(System receiver, TransportConfiguration configuration,
                                   Message message, Template template)
            throws RemoteException, TransportException;

    ConnectionProperties calculate(System receiver, TransportConfiguration configuration,
                                   Message message, Template template, InstanceContext instanceContext)
            throws RemoteException, TransportException;

    @Nonnull
    OutboundTransportConfiguration getOutbound(System system, String typeName);

    Collection<OutboundTransportConfiguration> getOutbounds(System system);

    Collection<OutboundTransportConfiguration> getOutbounds();

    void fillOutbounds(Collection<OutboundTransportConfiguration> outbounds);

    InboundTransportConfiguration getInbound(System system, TransportConfiguration configuration);

    InboundTransportConfiguration getInboundTransportConfiguration(TransportConfiguration configuration);

    Collection<InboundTransportConfiguration> getInbounds(System system);

    Collection<InboundTransportConfiguration> getInbounds();

    void fillInbounds(Collection<InboundTransportConfiguration> inbounds);

    void setConfigurationProperties(Configuration configuration, Map<String, String> properties);

    String getClassName();

    BigInteger getProjectId();

    void setProjectId(BigInteger projectId);
}
