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

package org.qubership.automation.itf.core.model.jpa.instance.chain;

import java.math.BigInteger;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractContainerInstance;
import org.qubership.automation.itf.core.model.testcase.TestCase;
import org.qubership.automation.itf.core.util.annotation.JsonRef;
import org.qubership.automation.itf.core.util.iterator.CallChainStepIterator;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@JsonFilter("reportWorkerFilter_CallChainInstance")
public class CallChainInstance extends AbstractContainerInstance {
    private static final long serialVersionUID = 20240812L;

    private BigInteger testCaseId;
    private String datasetName;
    private boolean isDatasetDefault;
    private String callchainExecutionData;

    public CallChainInstance() {
    }

    @JsonRef
    @Override
    public Storable getSource() {
        return getTestCaseById();
    }

    @Override
    public CallChainStepIterator iterator() {
        return new CallChainStepIterator(CoreObjectManager.getInstance().getManager(CallChain.class)
                .getById(getTestCaseId()), this);
    }

    @Override
    public StepContainer getStepContainer() {
        return getTestCaseById();
    }

    @Override
    public void setStepContainer(StepContainer container) {
        this.testCaseId = (BigInteger) container.getID();
    }

    public boolean stepsIsDisabled() {
        return !iterator().hasNext();
    }

    //@JsonRef
    public BigInteger getTestCaseId() {
        return testCaseId;
    }

    public void setTestCaseId(BigInteger testCaseId) {
        this.testCaseId = testCaseId;
    }

    @Transient
    @JsonIgnore
    public TestCase getTestCaseById() {
        return CoreObjectManager.getInstance().getManager(CallChain.class).getById(getTestCaseId());
    }

    public String getDatasetName() {
        return datasetName;
    }

    public void setDatasetName(String datasetName) {
        this.datasetName = datasetName;
    }

    public boolean isDatasetDefault() {
        return isDatasetDefault;
    }

    public void setDatasetDefault(boolean datasetDefault) {
        isDatasetDefault = datasetDefault;
    }

    public String getCallchainExecutionData() {
        return callchainExecutionData;
    }

    public void setCallchainExecutionData(String callchainExecutionData) {
        this.callchainExecutionData = callchainExecutionData;
    }
}
