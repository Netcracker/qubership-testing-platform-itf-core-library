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

package org.qubership.automation.itf.core.model.jpa.report;

import java.util.Map;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.transport.Configuration;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class LinkCollectorConfiguration extends Configuration {
    private static final long serialVersionUID = 20240812L;

    public LinkCollectorConfiguration() {
    }

    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Environment getParent() {
        return (Environment) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
            property = "id", scope = Environment.class)
    public void setParent(Environment parent) {
        super.setParent(parent);
    }

    /**
     * Constructor using parent and Map of parameters.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Environment objects are here")
    public LinkCollectorConfiguration(Storable parent, Map parameters) {
        this();
        if (parameters != null && parameters.containsKey("typeName")) {
            setTypeName((String) parameters.get("typeName"));
        }
        setParent(parent);
        if (((Environment) parent).getReportCollectors() != null) {
            ((Environment) parent).getReportCollectors().add(this);
        }
    }

}
