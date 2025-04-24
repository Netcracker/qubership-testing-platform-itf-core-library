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

package org.qubership.automation.itf.core.util.iterator;

import java.util.Iterator;

import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.SpContext;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractContainerInstance;
import org.qubership.automation.itf.core.model.jpa.instance.step.StepInstance;
import org.qubership.automation.itf.core.model.jpa.step.Step;

public class AbstractStepIterator implements Iterator<StepInstance> {
    private StepInstance current;
    private Iterator<Step> iterator;
    private AbstractContainerInstance parent;

    protected void setIterator(Iterator<Step> iterator) {
        this.iterator = iterator;
    }

    protected void setParent(AbstractContainerInstance parent) {
        this.parent = parent;
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public StepInstance next() {
        current = new StepInstance();
        //CoreObjectManager.getInstance().getManager(StepInstance.class)
        // .create(null, StepInstance.class.getSimpleName(), null);
        current.init(iterator.next());
        current.setParent(parent);
        InstanceContext context = current.getContext();
        context.putAll(parent.getContext());
        context.setProjectId(parent.getContext().getProjectId());
        context.setProjectUuid(parent.getContext().getProjectUuid());
        if (context.getSP() != null) {
            context.getSP().setParent(context);
            /*TODO Need create annotation for ignore someparam in the method (putAll)*/
        } else {
            context.setSP(new SpContext(current));
        }

        return current;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Cannot remove steps from situation");
    }

    public StepInstance current() {
        return current;
    }
}
