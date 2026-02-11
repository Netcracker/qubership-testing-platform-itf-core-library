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

package org.qubership.automation.itf.core.model.jpa.system.stub;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.provider.TriggerProvider;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = OperationEventTrigger.class)
public class OperationEventTrigger extends EventTriggerImpl {

    private static final long serialVersionUID = 20241125L;

    public static final String TYPE = "Operation Event Trigger";
    private int priority;

    public OperationEventTrigger() {
        super(TYPE);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public OperationEventTrigger(Situation parent) {
        super(TYPE);
        setParent(parent);
        ((TriggerProvider) parent).getOperationEventTriggers().add(this);
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Situation getParent() {
        return (Situation) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Situation.class)
    public void setParent(Situation parent) {
        super.setParent(parent);
    }

    @JsonIgnore
    @Override
    public String getType() {
        return TYPE;
    }
}
