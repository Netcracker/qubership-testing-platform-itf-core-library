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

package org.qubership.automation.itf.core.model.jpa.folder;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.ei.deserialize.SystemFolderDeserializer;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@Entity
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SystemFolder extends Folder<System> {
    private static final long serialVersionUID = 20240812L;
    public static final Class<System> TYPE = System.class;

    public SystemFolder() {
        super(TYPE);
    }

    public SystemFolder(Storable parent) {
        this();
        setParent(parent);
    }

    @JsonDeserialize(using = SystemFolderDeserializer.class)
    public void setParent(SystemFolder parent) {
        super.setParent(parent);
    }
}
