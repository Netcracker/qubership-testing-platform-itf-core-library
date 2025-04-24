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
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.Callable;

import javax.annotation.Nonnull;
import javax.persistence.Entity;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.executor.ServerObjectManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.eci.AbstractEciConfigurable;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.environment.OutboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.transport.Configuration;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.transport.ConnectionProperties;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.exception.TransportException;
import org.qubership.automation.itf.core.util.helper.ServerUtils;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import lombok.Getter;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ServerHB.class)
public class ServerHB extends AbstractEciConfigurable implements Server {
    private static final long serialVersionUID = 20240812L;

    private String url;
    @JsonProperty(value = "type")
    private String className = this.getClass().getName();
    private Collection<OutboundTransportConfiguration> outbounds = Lists.newArrayList();
    private Collection<InboundTransportConfiguration> inbounds = Lists.newArrayList();
    @Getter
    private BigInteger projectId;

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public ConnectionProperties calculate(@Nonnull System receiver,
                                          @Nonnull TransportConfiguration configuration,
                                          @Nonnull Message message, Template template) throws TransportException {
        return ServerUtils.calculate(this, receiver, configuration, message, template);
    }

    @Override
    public ConnectionProperties calculate(@Nonnull System receiver,
                                          @Nonnull TransportConfiguration configuration,
                                          @Nonnull Message message,
                                          Template template,
                                          InstanceContext instanceContext) throws TransportException {
        return ServerUtils.calculate(this, receiver, configuration, message, template, instanceContext);
    }

    @Nonnull
    @Override
    public OutboundTransportConfiguration getOutbound(final System system, final String type) {
        return Objects.requireNonNull(TxExecutor.executeUnchecked(() -> {
                    Server server = this;
                    ServerObjectManager serverObjectManager = ServerObjectManager.INSTANCE;
                    OutboundTransportConfiguration result = serverObjectManager.getOutbound(server, system, type);
                    if (result == null) {
                        server = ServerUtils.syncOutbounds(server, system);
                        result = serverObjectManager.getOutbound(server, system, type);
                    }
                    return result;
                }, TxExecutor.readOnlyTransaction()),
                String.format("No OutboundTransportConfiguration for system [%s] and type [%s] found", system, type));
    }

    @Override
    public Collection<OutboundTransportConfiguration> getOutbounds(final System system) {
        return TxExecutor.executeUnchecked((Callable<Collection<OutboundTransportConfiguration>>) () -> {
            Server server = ServerUtils.syncOutbounds(this, system);
            return ImmutableList.copyOf(ServerObjectManager.INSTANCE.getOutbounds(server, system));
        }, TxExecutor.readOnlyTransaction());
    }

    @Override
    public Collection<OutboundTransportConfiguration> getOutbounds() {
        return outbounds;
    }

    /**
     * Hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setOutbounds(Collection<OutboundTransportConfiguration> outbounds) {
        this.outbounds = outbounds;
    }

    @Override
    public void fillOutbounds(Collection<OutboundTransportConfiguration> outbounds) {
        StorableUtils.fillCollection(this.outbounds, outbounds);
    }

    @JsonSerialize(using = IdSerializer.class)
    public Storable getParent() {
        return super.getParent();
    }

    @Override
    public InboundTransportConfiguration getInbound(final System system, final TransportConfiguration configuration) {
        return TxExecutor.executeUnchecked(() -> {
            Server server = this;
            ServerObjectManager serverObjectManager = ServerObjectManager.INSTANCE;
            InboundTransportConfiguration result = serverObjectManager.getInbound(server, configuration);
            if (result == null) {
                server = ServerUtils.syncInbounds(server, system);
                result = serverObjectManager.getInbound(server, configuration);
            }
            return result;
        }, TxExecutor.readOnlyTransaction());
    }

    @Override
    public InboundTransportConfiguration getInboundTransportConfiguration(final TransportConfiguration configuration) {
        return TxExecutor.executeUnchecked(() -> {
            Server server = this;
            return ServerObjectManager.INSTANCE.getInbound(server, configuration);
        }, TxExecutor.readOnlyTransaction());
    }

    @Override
    public Collection<InboundTransportConfiguration> getInbounds(final System system) {
        return TxExecutor.executeUnchecked((Callable<Collection<InboundTransportConfiguration>>) () -> {
            Server server = ServerUtils.syncInbounds(this, system);
            return ImmutableList.copyOf(ServerObjectManager.INSTANCE.getInbounds(server, system));
        }, TxExecutor.readOnlyTransaction());
    }

    @Override
    public Collection<InboundTransportConfiguration> getInbounds() {
        return inbounds;
    }

    /**
     * Hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setInbounds(Collection<InboundTransportConfiguration> inbounds) {
        this.inbounds = inbounds;
    }

    @Override
    public void fillInbounds(Collection<InboundTransportConfiguration> inbounds) {
        StorableUtils.fillCollection(this.inbounds, inbounds);
    }

    public void setProjectId(BigInteger projectId) {
        this.projectId = projectId;
    }

    @Override
    public void setConfigurationProperties(Configuration configuration, Map<String, String> properties) {
    }

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    protected ObjectManager getManager() {
        return CoreObjectManager.getInstance().getManager(Server.class);
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        setProjectId(projectId);
        for (OutboundTransportConfiguration outbound : getOutbounds()) {
            outbound.performPostImportActions(projectId, sessionId);
        }
        for (InboundTransportConfiguration inbound : getInbounds()) {
            inbound.performPostImportActions(projectId, sessionId);
        }
    }

    @Override
    public void unbindEntityWithHierarchy() {
        setEciParameters(null, null);
        for (InboundTransportConfiguration itc : getInbounds()) {
            itc.unbindEntityWithHierarchy();
        }
        for (OutboundTransportConfiguration otc : getOutbounds()) {
            otc.unbindEntityWithHierarchy();
        }
    }

    @Override
    public void upStorableVersion() {
        super.upStorableVersion();
        for (OutboundTransportConfiguration outbound : getOutbounds()) {
            outbound.upStorableVersion();
        }
        for (InboundTransportConfiguration inbound : getInbounds()) {
            inbound.upStorableVersion();
        }
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        outbounds.forEach(
                outbound -> outbound.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
        inbounds.forEach(
                inbound -> inbound.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
    }
}
