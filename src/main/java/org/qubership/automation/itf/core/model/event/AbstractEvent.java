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

package org.qubership.automation.itf.core.model.event;

import org.qubership.automation.itf.core.model.jpa.instance.AbstractInstance;

public abstract class AbstractEvent<T extends AbstractInstance> extends Event {
    private T instance;

    public AbstractEvent(String parentId, T instance) {
        this.setParentId(parentId);
        this.instance = instance;
    }

    public AbstractEvent(String id) {
        this.setID(id);
    }

    public AbstractEvent(AbstractEvent event) {
        super(event);
        this.instance = (T) event.getInstance();
    }

    public T getInstance() {
        return instance;
    }

    public void setInstance(T instance) {
        this.instance = instance;
    }

    @Override
    public String toString() {
        return "Event{"
                + "instance=" + instance
                + '}';
    }
}
