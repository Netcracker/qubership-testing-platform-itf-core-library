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

package org.qubership.automation.itf.core.model.common;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.annotation.JsonRef;
import org.qubership.automation.itf.core.util.annotation.NoCopy;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.exception.CopyException;
import org.qubership.automation.itf.core.util.exception.StorageException;
import org.qubership.automation.itf.core.util.storage.StoreInformationDelegate;

import com.fasterxml.jackson.annotation.JsonIgnore;

public interface Storable extends Named, Identified<Object>, Prefixed, OptimisticLockable<Object>, Serializable {

    void store() throws StorageException;

    Collection<UsageInfo> remove() throws StorageException;

    void move(Storable newParent);

    Storable copy(Storable newParent) throws CopyException;

    @JsonRef
    @RefCopy
    Storable getParent();

    Storable returnSimpleParent();

    void setParent(Storable parent);

    @JsonIgnore
    @NoCopy
    StoreInformationDelegate getStoreInformationDelegate();

    Collection<UsageInfo> findUsages();

    String getDescription();

    void setDescription(String description);

    @JsonIgnore
    Object getNaturalId();

    void setNaturalId(Object id);

    void performPostCopyActions(boolean statusOff); // While copying some types of objects we should set their statuses
    // to InActive (or Off) regardless of status of source object (NITP-4139)

    void performPostImportActions(BigInteger projectId, BigInteger sessionId);

    void performActionsForImportIntoAnotherProject(Map<BigInteger, BigInteger> replacementMap,
                                                   BigInteger projectId, UUID projectUuid,
                                                   boolean needToUpdateProjectId, boolean needToGenerateNewId);

    void replicate();

    Map<String, String> getStorableProp();

    void setStorableProp(Map<String, String> properties);

    default void setProjectId(BigInteger projectId) {
    }

    default BigInteger getProjectId() {
        return this.getParent() == null ? null : this.getParent().getProjectId();
    }

    Storable findRootObject(BigInteger projectId);

    boolean contains();

    void flush();

    void performPostImportActionsParent(BigInteger projectId, BigInteger sessionId);

    Storable getExtendsParameters();

    void upStorableVersion();
}
