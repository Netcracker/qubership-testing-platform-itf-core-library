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

package org.qubership.automation.itf.core.model.jpa.project;

import java.util.Map;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.transport.Configuration;
import org.qubership.automation.itf.core.util.annotation.RefCopy;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.google.common.collect.Maps;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = IntegrationConfig.class)
public class IntegrationConfig extends Configuration {
    private static final long serialVersionUID = 20240812L;

    private String toolName;
    private Map<String, String> properties = Maps.newHashMapWithExpectedSize(10);

    public IntegrationConfig() {
    }

    /**
     * IntegrationConfig constructor using parent project, name and type of config.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only StubProject objects are here")
    public IntegrationConfig(Storable parent, String name, String type) {
        setName(name);
        setTypeName(type);
        setParent(parent);
        ((StubProject) parent).getIntegrationConfs().add(this);
    }

    /**
     * IntegrationConfig constructor using parent project and parameters Map.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only StubProject objects are here")
    public IntegrationConfig(Storable parent, Map parameters) {
        setParent(parent);
        ((StubProject) parent).getIntegrationConfs().add(this);
        if (parameters != null) {
            putAll(parameters);
        }
    }

    @RefCopy
    @Override
    @JsonIgnore
    public StubProject getParent() {
        return (StubProject) super.getParent();
    }

    @JsonIgnore
    public void setParent(StubProject parent) {
        super.setParent(parent);
    }

    @Override
    public String getName() {
        return toolName;
    }

    @Override
    public void setName(String name) {
        setToolName(name);
    }
}

