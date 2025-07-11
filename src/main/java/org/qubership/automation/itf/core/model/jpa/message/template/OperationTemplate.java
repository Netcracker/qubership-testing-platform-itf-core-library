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

package org.qubership.automation.itf.core.model.jpa.message.template;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = OperationTemplate.class)
public class OperationTemplate extends AbstractTemplate<Operation> {
    private static final long serialVersionUID = 20240812L;

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Operation objects are here")
    public OperationTemplate(Storable parent) {
        super((Operation) parent);
    }

    @Override
    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public Operation getParent() {
        return super.getParent();
    }

    @Override
    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Operation.class)
    public void setParent(Operation parent) {
        super.setParent(parent);
    }
}
