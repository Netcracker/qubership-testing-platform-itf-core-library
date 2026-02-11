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

package org.qubership.automation.itf.core.util.descriptor;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Objects;
import java.util.UUID;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

public class StorableDescriptor implements Serializable {
    private static final long serialVersionUID = 20240812L;

    private Object id;
    private String name;
    private UUID projectUuid;
    private BigInteger projectId;

    public StorableDescriptor() {
    }

    /**
     * Creates StorableDescriptor object by properties.
     * It's used currently only in atp-itf-stubs service to hold configuration info at triggers.
     * projectUuid and projectId fields are mandatory in order to use them in atp-itf-executor service,
     * in TriggerExecutor class, avoiding a necessity to get them from Db/cache.
     */
    public StorableDescriptor(Object id, String name, UUID projectUuid, BigInteger projectId) {
        this.id = id;
        this.name = name;
        this.projectUuid = projectUuid;
        this.projectId = projectId;
    }

    @Nonnull
    public Object getId() {
        return id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    @Nonnull
    public UUID getProjectUuid() {
        return projectUuid;
    }

    @Nonnull
    public BigInteger getProjectId() {
        return projectId;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof StorableDescriptor)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        return Objects.equals(id, ((StorableDescriptor) obj).getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
