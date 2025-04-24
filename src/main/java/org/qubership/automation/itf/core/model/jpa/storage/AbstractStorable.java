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

package org.qubership.automation.itf.core.model.jpa.storage;

import java.math.BigInteger;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import javax.persistence.PostPersist;
import javax.persistence.PostUpdate;
import javax.persistence.PreRemove;

import org.apache.commons.lang3.NotImplementedException;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.common.AbstractNamedImpl;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.annotation.NoCopy;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.db.ItfTransactionSynchAdapter;
import org.qubership.automation.itf.core.util.ei.deserialize.DeserializedEntitiesCache;
import org.qubership.automation.itf.core.util.exception.CopyException;
import org.qubership.automation.itf.core.util.exception.NoSuchManagerException;
import org.qubership.automation.itf.core.util.exception.StorageException;
import org.qubership.automation.itf.core.util.generator.id.UniqueIdGenerator;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.storage.StoreDelegateFactory;
import org.qubership.automation.itf.core.util.storage.StoreInformationDelegate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractStorable extends AbstractNamedImpl implements Storable {
    protected static final Logger LOGGER = LoggerFactory.getLogger(AbstractStorable.class);
    // Should be the same to ImportedDataCache#SIMPLE_PARENT_MARKER
    private static final String SIMPLE_PARENT_MARKER = "#SimpleParent#";

    private Storable parent;
    private StoreInformationDelegate storeInformationDelegate;

    private String prefix;
    private String description;
    private Object naturalId;
    private Map<String, String> storableProp;

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * AbstractStorable method.
     *
     * @deprecated Use specified Object Manager to create object instead of using constructor
     */
    @Deprecated
    public AbstractStorable() {
        storeInformationDelegate = StoreDelegateFactory.getInstance().newDelegate();
    }

    @Override
    public void store() throws StorageException {
        getManager().store(this);
        LOGGER.info("Storable {} stored", this);
    }

    public void replicate() {
        getManager().replicate(this);
        LOGGER.info("Storable {} replicated", this);
    }

    public void update() {
        getManager().update(this);
        LOGGER.info("Storable {} updated", this);
    }

    public boolean contains() {
        return getManager().contains(this);
    }

    public void flush() {
        getManager().flush();
    }

    protected String printStackTrace() {
        StringBuilder builder = new StringBuilder();
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            builder.append(stackTraceElement.toString()).append('\n');
        }
        return builder.toString();
    }

    @Override
    public Collection<UsageInfo> remove() throws StorageException {
        return getManager().remove(this, true);
    }

    @Override
    public void move(Storable newParent) {
        getManager().move(newParent, this, "");
    }

    @Override
    public Storable copy(Storable newParent) throws CopyException {
        return getManager().copy(newParent, this, "", "");
    }

    @Override
    public Collection<UsageInfo> findUsages() {
        return getManager().findUsages(this);
    }

    @RefCopy
    @Override
    public Storable getParent() {
        return parent;
    }

    @Override
    public Storable returnSimpleParent() {
        if (parent != null) {
            try {
                Storable simpleStorable = parent.getClass().newInstance();
                simpleStorable.setID(parent.getID());
                simpleStorable.setName(parent.getName());
                simpleStorable.setDescription(SIMPLE_PARENT_MARKER);
                simpleStorable.setVersion(parent.getVersion());
                simpleStorable.setNaturalId(parent.getNaturalId());
                simpleStorable.setParent(parent.returnSimpleParent());
                return simpleStorable;
            } catch (IllegalAccessException | InstantiationException e) {
                LOGGER.error("Can't create the object of {}. Null is returned.", parent.getClass().getName(), e);
                return null;
            }
        }
        LOGGER.debug("Object [id {}, class {}] hasn't got a parent. Null is returned.", this.getID(),
                this.getClass().getName());
        return null;
    }

    @Override
    public Storable getExtendsParameters() {
        return null;
    }

    @Override
    public void setParent(Storable parent) {
        this.parent = parent;
    }

    @NoCopy
    @Override
    public Object getID() {
        return storeInformationDelegate.getID();
    }

    @Override
    public void setID(Object id) {
        storeInformationDelegate.setID(id);
    }

    @Override
    public Object getNaturalId() {
        return naturalId;
    }

    @Override
    public void setNaturalId(Object id) {
        this.naturalId = id;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @NoCopy
    @Override
    public Object getVersion() {
        return storeInformationDelegate.getVersion();
    }

    @Override
    public void setVersion(Object version) {
        Integer intVersion = null;
        if (version instanceof Integer) {
            intVersion = (Integer) version;
        } else if (version instanceof String) {
            intVersion = Integer.parseInt((String) version);
        }
        storeInformationDelegate.setVersion(intVersion);
    }

    @JsonIgnore
    @Override
    public StoreInformationDelegate getStoreInformationDelegate() {
        return storeInformationDelegate;
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

    protected ObjectManager getManager() {
        return CoreObjectManager.getInstance().getManager(this.getClass());
    }

    /**
     * Actions after object creation.
     */
    @PostPersist
    public void doAfterCreate() {
        TransactionSynchronizationManager.registerSynchronization(new ItfTransactionSynchAdapter(this) {
            @Override
            public void afterCommit() {
                try {
                    getManager().onCreate(getObject());
                } catch (NoSuchManagerException ignored) {
                    LOGGER.warn("NoSuchManagerException: ", ignored);
                }
            }
        });
    }

    /**
     * Actions after object updating.
     */
    @PostUpdate
    public void doAfterUpdate() {
        try {
            getManager().onUpdate(this);
        } catch (NoSuchManagerException ignored) {
            LOGGER.warn("NoSuchManagerException: ", ignored);
        }
    }

    /**
     * Actions before objet deletion.
     */
    @PreRemove
    public void doBeforeRemove() {
        try {
            getManager().onRemove(this);
        } catch (NoSuchManagerException ignored) {
            //ok, no manager, nothing to do
            // Alexander Kapustin, 2017-10-30, commented. Removing of situations or some other entities without
            // managers - is 'common' and not so rare activity.
            // These warnings (more than 100 rows each) spam logfiles.
            // LOGGER.warn("No manager on remove  ", ignored);
        }
    }

    @Override
    public void performPostCopyActions(boolean statusOff) {
        // Should be overridden in specific classes
        // which objects' status should be set to InActive (or Off) while copying
    }

    /**
     * Perform post-import actions.
     */
    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        DeserializedEntitiesCache.getInstance().getCacheBySessionId(sessionId).put((BigInteger) getID(), this);
        performPostImportActionsParent(projectId, sessionId);
    }

    @Override
    public void performPostImportActionsParent(BigInteger projectId, BigInteger sessionId) {
        if (getParent() != null) {
            Storable byId = DeserializedEntitiesCache.getInstance().getCacheBySessionId(sessionId)
                    .getById((BigInteger) getParent().getID());
            if (byId != null) {
                setParent(byId);
            } else {
                DeserializedEntitiesCache.getInstance().getCacheBySessionId(sessionId)
                        .put((BigInteger) getParent().getID(), this.getParent());
                getParent().performPostImportActionsParent(projectId, sessionId);
            }
        }
    }

    public Map<String, String> getStorableProp() {
        return storableProp;
    }

    public void setStorableProp(Map<String, String> storableProp) {
        this.storableProp = storableProp;
    }

    @Override
    public Storable findRootObject(BigInteger projectId) {
        throw new NotImplementedException("getRootObject is not applicable for the class");
    }

    public void upStorableVersion() {
        this.setVersion((Integer)this.getVersion() + 1);
    }

    /**
     * Update projectId/generate new ID if it needs.
     */
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        if (needToUpdateProjectId) {
            setProjectId(projectId);
        }
        if (needToGenerateNewId) {
            replaceStorableId(replacementMap);
        }
    }

    private void replaceStorableId(Map<BigInteger, BigInteger> replacementMap) {
        if (!replacementMap.containsValue((BigInteger) getID())) {
            BigInteger newId = (BigInteger) UniqueIdGenerator.generate();
            replacementMap.put((BigInteger) getID(), newId);
            setID(newId);
        }
    }
}
