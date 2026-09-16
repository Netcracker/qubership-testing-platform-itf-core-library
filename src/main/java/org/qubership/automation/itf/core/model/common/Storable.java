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

public interface Storable extends Named, Identified<BigInteger>, Prefixed, OptimisticLockable<Object>, Serializable {

    void store() throws StorageException;

    Collection<UsageInfo> remove() throws StorageException;

    void move(Storable newParent);

    Storable copy(Storable newParent) throws CopyException;

    @JsonRef
    @RefCopy
    Storable getParent();

    /**
     * Returns a copy of {@link #getParent()} for the export/import serialization protocol, not a
     * substitute for the parent itself.
     *
     * <p>The returned object carries the parent's id and name, but its description is replaced by
     * the fixed marker
     * {@link org.qubership.automation.itf.core.util.ei.deserialize.ImportedDataCache#SIMPLE_PARENT_MARKER},
     * which the export/import protocol uses to recognize a placeholder rather than the full object.
     * It is a separate instance from the one {@link #getParent()} returns. Some implementations
     * base {@code equals} and {@code hashCode} on the class and the id alone, in which case the
     * returned object still equals the real parent and shares its hash code even though the
     * description differs, so a {@code HashSet} or {@code List.contains} cannot tell them apart.
     * Passing the returned object to {@link #store()} overwrites the parent's real description with
     * the marker.</p>
     *
     * @return a copy of the parent for the export/import protocol; {@code null} when there is no
     *         parent to simplify, or when an implementation takes no part in the protocol
     */
    Storable returnSimpleParent();

    void setParent(Storable parent);

    @JsonIgnore
    @NoCopy
    StoreInformationDelegate getStoreInformationDelegate();

    Collection<UsageInfo> findUsages();

    String getDescription();

    void setDescription(String description);

    @JsonIgnore
    String getNaturalId();

    void setNaturalId(String id);

    /**
     * While copying some types of objects we should set their statuses
     * to InActive (or Off) regardless of status of source object.
     *
     * @param statusOff boolean value to set.
     */
    void performPostCopyActions(boolean statusOff);

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

    /**
     * Compose String representation of Storable object ID (for UI purposes).
     * Implementations can override it in specific cases.
     *
     * @return String representation of Storable object ID.
     */
    default String returnDisplayId() {
        return getID() == null ? "" : getID().toString();
    }

}
