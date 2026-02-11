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

package org.qubership.automation.itf.core.model.jpa.callchain;

import java.math.BigInteger;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.folder.ChainFolder;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.project.StubContainer;
import org.qubership.automation.itf.core.model.jpa.step.SituationStep;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.testcase.AbstractTestCase;
import org.qubership.automation.itf.core.util.annotation.NoCopy;
import org.qubership.automation.itf.core.util.annotation.ProduceNewObject;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.ei.deserialize.ChainFolderDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = CallChain.class)
public class CallChain extends AbstractTestCase {
    private static final long serialVersionUID = 20240812L;

    private Set<String> keys = Sets.newHashSet();
    private Map<String, String> bvCases = Maps.newHashMap();
    private String datasetId;
    private BigInteger projectId;

    public CallChain() {
    }

    /**
     * TODO: Add JavaDoc.
     */
    public CallChain(Storable parent) {
        this();
        Folder<CallChain> actualParent = null;
        if (parent instanceof StubContainer) {
            actualParent = ((StubContainer) parent).getCallchains();
        } else if (parent instanceof Folder) {
            Optional<Folder<CallChain>> callChainFolder = ((Folder<? extends Storable>) parent).of(CallChain.class);
            if (callChainFolder.isPresent()) {
                actualParent = callChainFolder.get();
            }
        }
        if (actualParent == null) {
            throw new RuntimeException("ER: SystemFolder or StubContainer; AR:" + parent);
        }
        setParent(parent);
        actualParent.getObjects().add(this);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public SituationStep addStep(Situation situation) {
        SituationStep situationStep =
                CoreObjectManager.getInstance().getManager(SituationStep.class)
                        .create(this, situation.getName(), SituationStep.TYPE);
        situationStep.setSituation(situation);
        return situationStep;
    }

    public Set<String> getKeys() {
        return keys;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setKeys(Set<String> keys) {
        this.keys = keys;
    }

    public void fillKeys(Set<String> keys) {
        StorableUtils.fillCollection(getKeys(), keys);
    }

    @NoCopy
    public Map<String, String> getBvCases() {
        return bvCases;
    }

    public void setBvCases(Map<String, String> bvCases) {
        this.bvCases = bvCases;
    }


    public String getDatasetId() {
        return datasetId;
    }

    public void setDatasetId(String datasetId) {
        this.datasetId = datasetId;
    }

    public BigInteger getProjectId() {
        return projectId;
    }

    public void setProjectId(BigInteger projectId) {
        this.projectId = projectId;
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public ChainFolder getParent() {
        return (ChainFolder) super.getParent();
    }

    @JsonDeserialize(using = ChainFolderDeserializer.class)
    public void setParent(ChainFolder parent) {
        super.setParent(parent);
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        setProjectId(projectId);
        for (Step step : getSteps()) {
            if (step != null) {
                step.performPostImportActions(projectId, sessionId);
            }
        }
    }

    @ProduceNewObject
    @Override
    public BigInteger getNaturalId() {
        return super.getNaturalId();
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        getSteps().forEach(
                step -> step.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId, projectUuid, needToUpdateProjectId,
                        needToGenerateNewId)
        );
    }
}
