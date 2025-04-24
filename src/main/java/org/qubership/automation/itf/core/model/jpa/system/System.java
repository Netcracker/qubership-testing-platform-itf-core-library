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

package org.qubership.automation.itf.core.model.jpa.system;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.hibernate.spring.managers.custom.NativeManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.eci.AbstractEciConfigurable;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.folder.SystemFolder;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.template.SystemTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.project.StubContainer;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.util.annotation.ProduceNewObject;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.constants.SystemMode;
import org.qubership.automation.itf.core.util.ei.deserialize.SystemFolderDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.exception.OperationDefinitionException;
import org.qubership.automation.itf.core.util.helper.KeyHelper;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.provider.KeyDefinitionProvider;
import org.qubership.automation.itf.core.util.provider.ParsingRuleProvider;
import org.qubership.automation.itf.core.util.provider.TemplateProvider;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = System.class)
public class System extends AbstractEciConfigurable
        implements ParsingRuleProvider, KeyDefinitionProvider, TemplateProvider {
    private static final long serialVersionUID = 20240812L;

    private Set<TransportConfiguration> transports = Sets.newHashSetWithExpectedSize(10);
    private Set<SystemParsingRule> systemParsingRules = Sets.newHashSetWithExpectedSize(20);

    private String outgoingContextKeyDefinition;
    private String incomingContextKeyDefinition;
    private String operationKeyDefinition;

    private Set<Operation> operations = Sets.newHashSetWithExpectedSize(20);
    private Set<SystemTemplate> systemTemplates = Sets.newHashSetWithExpectedSize(50);
    private SystemMode mode;
    private BigInteger projectId;

    public System() {
    }

    /**
     * Constructor from parent.
     */
    public System(Storable parent) {
        Folder<System> actualParent =  determineActualParent(parent);
        setParent(actualParent);
        setProjectId(actualParent.getProjectId());
        actualParent.getObjects().add(this);
    }

    /**
     * Constructor from parent and visual primitive attributes.
     */
    public System(Storable parent, String name, String type, String description, List<String> labels) {
        Folder<System> actualParent = determineActualParent(parent);
        setParent(actualParent);
        setProjectId(actualParent.getProjectId());
        this.setName(name);
        this.setDescription(description);
        this.setLabels(labels);
        actualParent.getObjects().add(this);
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public SystemFolder getParent() {
        return (SystemFolder) super.getParent();
    }

    @JsonDeserialize(using = SystemFolderDeserializer.class)
    public void setParent(SystemFolder parent) {
        super.setParent(parent);
    }

    @Override
    @JsonIgnore
    public Set<ParsingRule> returnParsingRules() {
        return systemParsingRules.stream().map(ParsingRule.class::cast).collect(Collectors.toSet());
    }

    @Override
    public void addParsingRule(ParsingRule parsingRule) {
        if (parsingRule instanceof SystemParsingRule) {
            systemParsingRules.add((SystemParsingRule) parsingRule);
        }
    }

    public void addParsingRule(SystemParsingRule rule) {
        systemParsingRules.add(rule);
    }

    @Override
    public void removeParsingRule(ParsingRule parsingRule) {
        systemParsingRules.remove(parsingRule);
    }

    @Override
    @JsonIgnore
    public Set<Template> returnTemplates() {
        return systemTemplates.stream().map(Template.class::cast).collect(Collectors.toSet());
    }

    private Folder<System> determineActualParent(Storable parent) {
        Folder<System> actualParent = null;
        if (parent instanceof StubContainer) {
            actualParent = ((StubContainer) parent).getSystems();
        } else if (parent instanceof Folder) {
            Optional<Folder<System>> systemFolder = ((Folder<? extends Storable>) parent).of(System.class);
            if (systemFolder.isPresent()) {
                actualParent = systemFolder.get();
            }
        }
        if (actualParent == null) {
            throw new RuntimeException("SystemFolder or StubContainer are expected, but given: " + parent);
        }
        return actualParent;
    }

    /**
     * Add operation into operations collection.
     */
    public void addOperation(Operation operation) {
        if (operations == null) {
            this.operations = Sets.newHashSetWithExpectedSize(20);
        }
        operations.add(operation);
    }

    public void fillTransports(Set<TransportConfiguration> transports) {
        StorableUtils.fillCollection(getTransports(), transports);
    }

    public void addTransport(TransportConfiguration transports) {
        this.transports.add(transports);
    }

    public void fillOperations(Set<Operation> operations) {
        StorableUtils.fillCollection(getOperations(), operations);
    }

    /**
     * Define operation via Operation Definition.
     */
    public Operation defineOperation(InstanceContext context)
            throws OperationDefinitionException {
        try {
            if (operationKeyDefinition == null) {
                throw new OperationDefinitionException("Cannot define operation! "
                        + "Operation key definition is null! System: " + getName());
            }
            String key = KeyHelper.defineKey(operationKeyDefinition, context, this);
            if (Strings.isNullOrEmpty(key)) {
                throw new OperationDefinitionException(String.format("Cannot define operation! Key defined by "
                        + "definition %s in empty! System: %s", operationKeyDefinition, getName()));
            }
            Operation operation = (Operation) CoreObjectManager.getInstance()
                    .getSpecialManager(System.class, NativeManager.class)
                    .getChildByClass(this, Operation.class, key);
            if (!Objects.isNull(operation)) {
                return operation;
            }
            throw new OperationDefinitionException(String.format("No operation definition found for key [%s]", key));
        } catch (Exception e) {
            throw new OperationDefinitionException("Cannot process operation definition", e);
        }
    }

    @Override
    @ProduceNewObject
    public Object getNaturalId() {
        return super.getNaturalId();
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        for (TransportConfiguration transport : getTransports()) {
            transport.performPostImportActions(projectId, sessionId);
        }
        for (SystemTemplate template : getSystemTemplates()) {
            template.performPostImportActions(projectId, sessionId);
        }
        for (Operation operation : getOperations()) {
            operation.performPostImportActions(projectId, sessionId);
        }
        for (SystemParsingRule parsingRule : getSystemParsingRules()) {
            parsingRule.performPostImportActions(projectId, sessionId);
        }
    }

    @Override
    public void unbindEntityWithHierarchy() {
        setEciParameters(null, null);
        setEcLabel(null);
        for (TransportConfiguration transportConfiguration : getTransports()) {
            transportConfiguration.unbindEntityWithHierarchy();
        }
    }

    @Override
    public void upStorableVersion() {
        super.upStorableVersion();
        for (TransportConfiguration transport : getTransports()) {
            transport.upStorableVersion();
        }
        for (SystemTemplate template : getSystemTemplates()) {
            template.upStorableVersion();
        }
        for (Operation operation : getOperations()) {
            operation.upStorableVersion();
        }
        for (SystemParsingRule parsingRule : getSystemParsingRules()) {
            parsingRule.upStorableVersion();
        }
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        getTransports().forEach(transport -> transport.performActionsForImportIntoAnotherProject(
                            replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
        getSystemTemplates().forEach(template -> template.performActionsForImportIntoAnotherProject(
                            replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
        getOperations().forEach(operation -> operation.performActionsForImportIntoAnotherProject(
                            replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
        getSystemParsingRules().forEach(parsingRule -> parsingRule.performActionsForImportIntoAnotherProject(
                            replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
    }
}
