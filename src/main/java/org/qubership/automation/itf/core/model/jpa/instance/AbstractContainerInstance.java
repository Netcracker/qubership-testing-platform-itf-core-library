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

import java.util.List;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.model.jpa.instance.step.StepInstance;
import org.qubership.automation.itf.core.util.annotation.JsonRef;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.iterator.AbstractStepIterator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;

@Entity
public abstract class AbstractContainerInstance extends AbstractInstance {

    private TcContext parentContext;
    private List<StepInstance> stepInstances = Lists.newArrayListWithExpectedSize(3);

    @JsonIgnore
    public List<StepInstance> getStepInstances() {
        return stepInstances;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setStepInstances(List<StepInstance> stepInstances) {
        this.stepInstances = stepInstances;
    }

    public void fillStepInstances(List<StepInstance> stepInstances) {
        StorableUtils.fillCollection(getStepInstances(), stepInstances);
    }

    @JsonIgnore
    public abstract AbstractStepIterator iterator();

    @JsonRef
    public abstract StepContainer getStepContainer();

    public abstract void setStepContainer(StepContainer container);

    /**
     * TODO: Add JavaDoc.
     */
    public String toString() {
        if (getStepContainer() != null) {
            return String.format("Instance: [%s]", getStepContainer().getName());
        }
        return "";
    }

    @JsonRef
    public TcContext getParentContext() {
        return parentContext;
    }

    public void setParentContext(TcContext parentContext) {
        this.parentContext = parentContext;
    }
}
