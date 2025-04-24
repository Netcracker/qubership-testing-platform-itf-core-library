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

package org.qubership.automation.itf.core.model.jpa.environment;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.Entity;

import org.hibernate.proxy.HibernateProxy;
import org.qubership.automation.itf.core.model.eci.EciConfigurable;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.server.ServerHB;
import org.qubership.automation.itf.core.model.jpa.transport.EciConfiguration;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.ei.deserialize.TransportConfigurationDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.helper.StorableUtils;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id",
        scope = InboundTransportConfiguration.class)
public class InboundTransportConfiguration extends EciConfiguration implements EciConfigurable {
    private static final long serialVersionUID = 20240812L;

    private TransportConfiguration referencedConfiguration;

    private Set<TriggerConfiguration> triggerConfigurations = Sets.newHashSetWithExpectedSize(5);

    public InboundTransportConfiguration() {
    }

    public InboundTransportConfiguration(TransportConfiguration referencedConfiguration, Server parent) {
        setParent((ServerHB) parent);
        setReferencedConfiguration(referencedConfiguration);
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only TransportConfiguration objects are here")
    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public TransportConfiguration getReferencedConfiguration() {
        return referencedConfiguration instanceof HibernateProxy
                ? (TransportConfiguration) ((HibernateProxy) referencedConfiguration)
                .getHibernateLazyInitializer().getImplementation()
                : referencedConfiguration;
    }

    /**
     * Set referenced configuration link (link to Transport under a System).
     */
    @JsonDeserialize(using = TransportConfigurationDeserializer.class)
    public void setReferencedConfiguration(TransportConfiguration referencedConfiguration) {
        this.referencedConfiguration = referencedConfiguration;
        setTypeName(referencedConfiguration.getTypeName());
        setName(String.format("%s at %s", referencedConfiguration.getName(), getParent().getName()));
    }

    public Set<TriggerConfiguration> getTriggerConfigurations() {
        return triggerConfigurations;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setTriggerConfigurations(Set<TriggerConfiguration> triggerConfigurations) {
        this.triggerConfigurations = triggerConfigurations;
    }

    public void fillTriggerConfigurations(Set<TriggerConfiguration> triggerConfigurations) {
        StorableUtils.fillCollection(getTriggerConfigurations(), triggerConfigurations);
    }

    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public ServerHB getParent() {
        return (ServerHB) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = ServerHB.class)
    public void setParent(ServerHB parent) {
        super.setParent(parent);
    }

    @Override
    public void unbindEntityWithHierarchy() {
        setEciParameters(null, null);
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        triggerConfigurations.forEach(
                triggerConfiguration -> triggerConfiguration.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId, projectUuid, needToUpdateProjectId,
                        needToGenerateNewId)
        );
    }
}
