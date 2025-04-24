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

package org.qubership.automation.itf.core.model.jpa.transport;

import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Entity;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.eci.EciConfigurable;
import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.environment.TriggerConfiguration;
import org.qubership.automation.itf.core.model.jpa.interceptor.TransportConfigurationInterceptor;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.exception.TransportException;
import org.qubership.automation.itf.core.util.holder.ActiveInterceptorHolder;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.provider.InterceptorProvider;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;
import org.qubership.automation.itf.core.util.transport.manager.TransportRegistryManager;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = TransportConfiguration.class)
public class TransportConfiguration extends EciConfiguration implements InterceptorProvider, EciConfigurable {
    private static final long serialVersionUID = 20240812L;

    @JsonProperty(value = "mep")
    private volatile Mep mepCache;
    private List<Interceptor> interceptors = Lists.newLinkedList();

    private System parent;

    public TransportConfiguration() {
        super();
    }

    /**
     * Constructor from parent, name and type.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only System objects are here")
    public TransportConfiguration(Storable parent, String name, String type) {
        setParent((System) parent);
        ((System) parent).getTransports().add(this);
        setName(name);
        setTypeName(type);
    }

    /**
     * Constructor from parent and map of parameters.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only System objects are here")
    public TransportConfiguration(Storable parent, Map parameters) {
        setParent((System) parent);
        ((System) parent).getTransports().add(this);
        if (parameters != null) {
            putAll(parameters);
        }
    }

    @Override
    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public System getParent() {
        return parent;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = System.class)
    public void setParent(System parent) {
        this.parent = parent;
    }

    /**
     * Get MEP value.
     * TODO: getMep() and getEndpointPrefix() methods must be checked,
     *       because they find the transport by name each time for retrieve constant properties Mep and endpointPrefix.
     */
    public Mep getMep() {
        try {
            AccessTransport transport = TransportRegistryManager.getInstance().find(getTypeName());
            if (transport == null) {
                return mepCache;
            }
            mepCache = transport.getMep();
            return mepCache;
        } catch (TransportException | RemoteException | NullPointerException e) {
            return mepCache;
        }
    }

    /**
     * for hb.
     */
    protected Mep getMepIfNotDeployed() {
        if (this.mepCache == null) {
            return getMep();
        }
        return this.mepCache;
    }

    public void setMepIfNotDeployed(Mep mepIfNotDeployed) {
        this.mepCache = mepIfNotDeployed;
    }

    public String viewEndpoint(Environment environment) {
        return StringUtils.EMPTY;
    }

    public String viewEndpoint() {
        return StringUtils.EMPTY;
    }

    /**
     * TODO: Add JavaDoc.
     */
    @JsonIgnore
    public Collection<TriggerConfiguration> getTriggers(BigInteger projectId) {
        Collection<TriggerConfiguration> configurations = Sets.newHashSetWithExpectedSize(20);
        Collection<Server> servers = CoreObjectManager.getInstance()
                .getSpecialManager(Server.class, SearchByProjectIdManager.class).getByProjectId(projectId);
        for (Server server : servers) {
            InboundTransportConfiguration inbound = server.getInboundTransportConfiguration(this);
            if (inbound != null) {
                configurations.addAll(inbound.getTriggerConfigurations());
            }
        }
        return Collections.unmodifiableCollection(configurations);
    }

    @JsonSerialize(contentAs = TransportConfigurationInterceptor.class)
    @JsonManagedReference
    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    @JsonDeserialize(contentAs = TransportConfigurationInterceptor.class)
    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    @Override
    public void performPostCopyActions(boolean statusOff) {
        if (statusOff) {
            for (Interceptor interceptor : interceptors) {
                interceptor.setActive(false);
            }
        } else {
            ActiveInterceptorHolder.getInstance().updateActiveInterceptorHolder(getID(), interceptors);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public String getEndpointPrefix() {
        try {
            AccessTransport transport = TransportRegistryManager.getInstance().find(getTypeName());
            if (transport == null) {
                return StringUtils.EMPTY;
            }
            return transport.getEndpointPrefix();
        } catch (TransportException | NullPointerException e) {
            return StringUtils.EMPTY;
        }
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        getInterceptors().forEach(
                interceptor -> interceptor.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId, projectUuid, needToUpdateProjectId,
                        needToGenerateNewId)
        );
    }

    @Override
    public void unbindEntityWithHierarchy() {
        setEciParameters(null, null);
        remove("ITF_TRANSPORT_LABEL"); //TODO - need to fix it. Business logic in entity-objects - bad practice.
    }
}
