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

package org.qubership.automation.itf.core.model.jpa.instance;

import java.math.BigInteger;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.util.iterator.StepIterator;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

@Entity
@JsonFilter("reportWorkerFilter_SituationInstance")
public class SituationInstance extends AbstractContainerInstance {
    private static final long serialVersionUID = 20240812L;

    @Getter
    @Setter
    private BigInteger situationId;
    @Getter
    @Setter
    private String operationName;
    @Getter
    @Setter
    private BigInteger operationId;
    @Getter
    @Setter
    private BigInteger systemId;
    @Getter
    @Setter
    private String systemName;
    private StepIterator iterator = null;

    public SituationInstance() {
    }

    @Override
    public Storable getSource() {
        return getStepContainer();
    }

    public SituationInstance(BigInteger situationId, InstanceContext instanceContext) {
        this.situationId = situationId;
        getContext().putAll(instanceContext);
    }

    @Transient
    @JsonIgnore
    public Situation getSituationById() {
        return Objects.nonNull(situationId)
                ? CoreObjectManager.getInstance().getManager(Situation.class).getById(getSituationId())
                : null;
    }

    @Override
    public StepIterator iterator() {
        if (iterator == null) {
            iterator = new StepIterator(getSituationById() == null
                    ? Collections.emptyList() : getSituationById().getSteps(), this);
        }
        return iterator;
    }

    public void setIterator(StepIterator iterator) {
        this.iterator = iterator;
    }

    @Override
    public StepContainer getStepContainer() {
        return getSituationById();
    }

    @Override
    public void setStepContainer(StepContainer container) {
        this.situationId = (BigInteger) container.getID();
    }

    @JsonIgnore
    public List<String> getLabels() {
        return this.getSituationById() == null ? Collections.emptyList() : this.getSituationById().getLabels();
    }
}
