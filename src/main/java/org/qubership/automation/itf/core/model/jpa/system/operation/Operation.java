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

package org.qubership.automation.itf.core.model.jpa.system.operation;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.message.parser.OperationParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.template.OperationTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.util.annotation.NoCopy;
import org.qubership.automation.itf.core.util.annotation.ProduceNewObject;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.annotation.RefCopyAsNewObject;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.provider.KeyDefinitionProvider;
import org.qubership.automation.itf.core.util.provider.ParsingRuleProvider;
import org.qubership.automation.itf.core.util.provider.TemplateProvider;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Operation.class)
public class Operation extends AbstractStorable
        implements ParsingRuleProvider, KeyDefinitionProvider, TemplateProvider {
    private static final long serialVersionUID = 20240812L;

    private TransportConfiguration transport;
    private String operationDefinitionKey;
    private String outgoingContextKeyDefinition;
    private String incomingContextKeyDefinition;
    private Set<OperationParsingRule> operationParsingRules = Sets.newHashSetWithExpectedSize(10);
    private Set<OperationTemplate> operationTemplates = Sets.newHashSetWithExpectedSize(50);
    private Set<Situation> situations = Sets.newHashSetWithExpectedSize(20);
    private Situation errorInboundSituation;
    private Situation defaultInboundSituation;
    BigInteger projectId;

    public Operation() {
    }

    /**
     * Constructor for Operation.
     * @param parent - System
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only System objects are here")
    public Operation(Storable parent) {
        setParent(parent);
        setProjectId(parent.getProjectId());
        ((System) parent).getOperations().add(this);
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public System getParent() {
        return (System) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = System.class)
    public void setParent(System parent) {
        super.setParent(parent);
    }

    @RefCopyAsNewObject
    @JsonSerialize(using = IdSerializer.class)
    public TransportConfiguration getTransport() {
        return transport;
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id", scope = TransportConfiguration.class)
    public void setTransport(TransportConfiguration transport) {
        this.transport = transport;
    }

    public void fillSituations(Collection<Situation> situations) {
        StorableUtils.fillCollection(getSituations(), situations);
    }

    @Override
    @ProduceNewObject
    public Object getNaturalId() {
        return super.getNaturalId();
    }

    /**
     * Get MEP of linked transport.
     */
    @JsonIgnore
    public Mep getMep() {
        if (transport != null && transport.getMep() != null) {
            return transport.getMep();
        } else if (getName().contains("Callback")) {
            return Mep.OUTBOUND_REQUEST_ASYNCHRONOUS;
        } else {
            return Mep.INBOUND_REQUEST_ASYNCHRONOUS;
        }
    }

    /**
     * Return default inbound situation (create if not exists).
     */
    @NoCopy
    @JsonIgnore
    public Situation getDefaultIfInbound() {
        if (getMep().isInbound() && getMep().isRequest()) {
            if (defaultInboundSituation == null) {
                createDefault();
            }
            //defaultInboundSituation.setParent(this);
            return defaultInboundSituation;
        } else {
            return null;
        }
    }

    /**
     * Get default inbound situation.
     */
    @NoCopy
    @JsonIgnore
    public Situation getDefaultInboundSituation() {
        return defaultInboundSituation;
    }

    /**
     * Set default inbound situation.
     */
    @NoCopy
    @JsonIgnore
    public void setDefaultInboundSituation(Situation defaultInboundSituation) {
        this.defaultInboundSituation = defaultInboundSituation;
    }

    @Override
    @JsonIgnore
    public Set<ParsingRule> returnParsingRules() {
        return operationParsingRules.stream().map(ParsingRule.class::cast).collect(Collectors.toSet());
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only OperationParsingRule objects are here")
    @Override
    public void addParsingRule(ParsingRule parsingRule) {
        operationParsingRules.add((OperationParsingRule) parsingRule);
    }

    @Override
    public void removeParsingRule(ParsingRule parsingRule) {
        operationParsingRules.remove(parsingRule);
    }

    @Override
    @JsonIgnore
    public Set<Template> returnTemplates() {
        return operationTemplates.stream().map(Template.class::cast).collect(Collectors.toSet());
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Storable returnSimpleParent() {
        Storable system = super.returnSimpleParent();
        if (getTransport() != null) {
            ((System) system).getTransports().add(getTransport());
        }
        return system;
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        if (getTransport() != null) {
            getTransport().performPostImportActions(projectId, sessionId);
        }
        for (OperationParsingRule parsingRule : getOperationParsingRules()) {
            parsingRule.performPostImportActions(projectId, sessionId);
        }
        for (OperationTemplate template : getOperationTemplates()) {
            template.performPostImportActions(projectId, sessionId);
        }
        for (Situation situation : getSituations()) {
            situation.performPostImportActions(projectId, sessionId);
            if (getDefaultInboundSituation() != null
                    && getDefaultInboundSituation().getID().equals(situation.getID())) {
                setDefaultInboundSituation(situation);
            }
            if (getErrorInboundSituation() != null
                    && getErrorInboundSituation().getID().equals(situation.getID())) {
                setErrorInboundSituation(situation);
            }
        }
    }

    private void createDefault() {
        TxExecutor.executeUnchecked((Callable<Void>) () -> {
            defaultInboundSituation = CoreObjectManager.getInstance().getManager(Situation.class).create();
            defaultInboundSituation.setName(String.format("[ *** DEFAULT (autocreated) *** ] [%s] "
                    + "receives [%s] request",
                    getParent().getName(), getName())); // Requirement from DT: name should be unusual
            defaultInboundSituation.store();
            this.store();
            return null;
        }, TxExecutor.nestedWritableTransaction());
    }

    @NoCopy
    @JsonIgnore
    public Situation getErrorInboundSituation() {
        return errorInboundSituation;
    }

    @NoCopy
    @JsonIgnore
    public void setErrorInboundSituation(Situation errorInboundSituation) {
        this.errorInboundSituation = errorInboundSituation;
    }

    @Override
    @JsonIgnore
    public Storable getExtendsParameters() {
        return this.getTransport();
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        getOperationParsingRules().forEach(parsingRule -> parsingRule.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
        getOperationTemplates().forEach(template -> template.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
        getSituations().forEach(situation -> situation.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
    }
}
