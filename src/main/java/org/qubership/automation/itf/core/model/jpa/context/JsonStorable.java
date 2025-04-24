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

package org.qubership.automation.itf.core.model.jpa.context;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.Entity;

import org.apache.commons.lang3.NotImplementedException;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.exception.CopyException;
import org.qubership.automation.itf.core.util.exception.StorageException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.storage.StoreDelegateFactory;
import org.qubership.automation.itf.core.util.storage.StoreInformationDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonSubTypes({@JsonSubTypes.Type(value = InstanceContext.class, name = "InstanceContext"),
        @JsonSubTypes.Type(value = TcContext.class, name = "TcContext"),
        @JsonSubTypes.Type(value = SpContext.class, name = "SpContext")})
@Entity
public class JsonStorable extends JsonContext implements Storable {
    private static final long serialVersionUID = 20240812L;

    private String name;
    private String prefix;
    private String description;
    private StoreInformationDelegate delegate;
    private Storable parent;
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorable.class);
    protected Date startTime;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public void store() throws StorageException {
        CoreObjectManager.getInstance().getManager(this.getClass()).store(this);
        LOGGER.info("Storable {} stored", this);
    }

    @Override
    public Collection<UsageInfo> remove() throws StorageException {
        return CoreObjectManager.getInstance().getManager(this.getClass()).remove(this, true);
    }

    @Override
    public void move(Storable newParent) {
        CoreObjectManager.getInstance().getManager(this.getClass()).move(newParent, this, "");
    }

    @Override
    public Storable copy(Storable newParent) throws CopyException {
        return CoreObjectManager.getInstance().getManager(this.getClass()).copy(newParent, this, "", "");
    }

    @Override
    public Storable getParent() {
        return parent;
    }

    @Override
    public Storable returnSimpleParent() {
        return null;
    }

    @Override
    public void setParent(Storable parent) {
        this.parent = parent;
    }

    @Override
    public StoreInformationDelegate getStoreInformationDelegate() {
        return delegate;
    }

    @Override
    public Collection<UsageInfo> findUsages() {
        return CoreObjectManager.getInstance().getManager(this.getClass()).findUsages(this);
    }

    @Override
    public Object getNaturalId() {
        return null;
    }

    @Override
    public void setNaturalId(Object id) {
    }

    @Override
    public Object getID() {
        return delegate.getID();
    }

    @Override
    public void setID(Object id) {
        delegate.setID(id);
    }

    @Override
    public Object getVersion() {
        return delegate.getVersion();
    }

    @Override
    public void setVersion(Object version) {
        delegate.setVersion(version);
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @SuppressWarnings("Duplicates")
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(this.getClass().isInstance(o))) {
            return false;
        }
        if (getID() != null) {
            return Objects.equals(getID(), ((Storable) o).getID());
        } else {
            return false;
        }
    }

    @SuppressWarnings("Duplicates")
    @Override
    public int hashCode() {
        return Objects.hash(getID());
    }

    @SuppressWarnings("Duplicates")
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        Storable parent = this.getParent();
        while (parent != null) {
            builder.insert(0, '>')
                    .insert(0, parent.getName())
                    .insert(0, ']')
                    .insert(0, parent.getClass().getSimpleName())
                    .insert(0, '[');
            parent = parent.getParent();
        }
        builder.append('\n').append("Name: '").append(getName()).append('\'')
                .append(", ID: '").append(getID()).append('\'');
        return builder.toString();
    }

    public JsonStorable() {
        delegate = StoreDelegateFactory.getInstance().newDelegate();
        setStartTime(new Date());
    }

    @Override
    public void performPostCopyActions(boolean statusOff) {
        // Should be overrided in specific classes which objects' status should be set to InActive (or Off) while
        // copying
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
    }

    @Override
    public void replicate() {
        throw new NotImplementedException("Export/import functionality is not implemented for the class.");
    }

    @Override
    @JsonIgnore
    public Map<String, String> getStorableProp() {
        throw new NotImplementedException("Get storable properties functionality is not implemented for the class.");
    }

    @Override
    public void setStorableProp(Map<String, String> properties) {
    }

    @Override
    public Storable findRootObject(BigInteger projectId) {
        throw new NotImplementedException("getRootObject is not applicable for the class");
    }

    @Override
    public boolean contains() {
        return false;
    }

    @Override
    public void flush() {
        throw new NotImplementedException("Export/import functionality is not implemented for the class.");
    }

    @Override
    public void performPostImportActionsParent(BigInteger projectId, BigInteger sessionId) {
    }

    @Override
    public Storable getExtendsParameters() {
        return null;
    }

    @Override
    public void upStorableVersion() {
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
    }
}
