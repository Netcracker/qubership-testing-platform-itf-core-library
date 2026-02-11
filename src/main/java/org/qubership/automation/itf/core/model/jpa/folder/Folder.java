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

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;
import jakarta.persistence.Transient;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.common.AbstractConfiguration;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdsListSerializer;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;

@Entity
public class Folder<T extends Storable> extends AbstractConfiguration<String, String> {
    private static final long serialVersionUID = 20240812L;

    private final Class<T> genericType;
    private List<T> objects = Lists.newArrayListWithExpectedSize(50);
    /**
     * WA for spring. Got recursion while resolving generic type of folder
     * in org.springframework.util.ReflectionUtils#doWithFields(java.lang.Class,
     * org.springframework.util.ReflectionUtils.FieldCallback,
     * org.springframework.util.ReflectionUtils.FieldFilter)
     */
    @Transient
    private List<Folder<T>> subFolders = Lists.newArrayListWithExpectedSize(50);

    /**
     * Get level in the hierarchy for the Folder.
    */
    @Transient
    @JsonIgnore
    public int hierarchyLevel() {
        Storable current = this;
        int level = 0;
        while (current.getParent() != null) {
            level++;
            current = current.getParent();
        }
        return level;
    }

    @JsonIgnore
    private StubProject project;

    @Deprecated
    public Folder() {
        genericType = null;
    }

    public Folder(Class<T> genericType) {
        this.genericType = genericType;
    }

    public Class<T> getGenericType() {
        return genericType;
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Storable getParent() {
        return super.getParent();
    }

    @JsonIgnore
    @Override
    public void setParent(Storable parent) {
        super.setParent(parent);
    }

    /**
     * Get parent project, going up via parents if necessary.
     * Currently, all folders have their own project property.
     */
    @RefCopy
    public StubProject getProject() {
        if (this.project == null && this.getParent() != null && this.getParent() instanceof Folder) {
            return ((Folder<?>) this.getParent()).getProject();
        } else {
            return this.project;
        }
    }

    @Override
    public BigInteger getProjectId() {
        return (BigInteger) getProject().getID();
    }

    public void setProject(StubProject project) {
        this.project = project;
    }

    @JsonSerialize(using = IdsListSerializer.class)
    public List<T> getObjects() {
        return objects;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setObjects(List<T> objects) {
        this.objects = objects;
    }

    public void fillObjects(Collection<T> objects) {
        StorableUtils.fillCollection(getObjects(), objects);
    }

    @JsonSerialize(using = IdsListSerializer.class)
    public List<Folder<T>> getSubFolders() {
        return subFolders;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setSubFolders(List<Folder<T>> subFolders) {
        this.subFolders = subFolders;
    }

    /**
     * Fill subFolders collection.
     */
    public void fillSubFolders(Collection<Folder<T>> subFolders) {
        StorableUtils.fillCollection(getSubFolders(), subFolders);
    }

    /**
     * Get full path (list of path elements), going up from the current folder.
     */
    @JsonIgnore
    public Collection<Folder<T>> getFullPath() {
        Storable parent = getParent();
        List<Folder<T>> result = Lists.newArrayListWithExpectedSize(10);
        result.add(this);
        while (parent instanceof Folder) {
            result.add((Folder<T>) parent);
            parent = parent.getParent();
        }
        return result;
    }

    /**
     * Get ObjectManager for Folder class.
     */
    @Override
    @JsonIgnore
    protected ObjectManager getManager() {
        return CoreObjectManager.getInstance().getManager(Folder.class);
    }

    /**
     * TODO: Add JavaDoc.
     */
    @Nonnull
    public <S extends Storable> Optional<Folder<S>> of(@Nonnull Class<S> objectType) {
        if (objectType.equals(this.genericType)) {
            return Optional.of((Folder<S>) this);
        }
        return Optional.absent();
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        if (this instanceof ChainFolder) {
            for (CallChain callChain : ((ChainFolder)this).getObjects()) {
                callChain.performPostImportActions(projectId, sessionId);
            }
        } else if (this instanceof EnvFolder) {
            for (Environment env : ((EnvFolder)this).getObjects()) {
                env.performPostImportActions(projectId, sessionId);
            }
        }
        for (Object subfolder : getSubFolders()) {
            ((Folder)subfolder).performPostImportActions(projectId, sessionId);
        }
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        getSubFolders().forEach(folder ->
                    folder.performActionsForImportIntoAnotherProject(
                            replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
    }

    @Override
    public Storable findRootObject(BigInteger projectId) {
        StubProject project = CoreObjectManager.getInstance().getManager(StubProject.class).getById(projectId);
        ObjectManager<Folder> folderManager = CoreObjectManager.getInstance().getManager(Folder.class);
        if (this instanceof ChainFolder) {
            return folderManager.getById(project.getCallchains().getID());
        } else if (this instanceof EnvFolder) {
            return folderManager.getById(project.getEnvironments().getID());
        } else if (this instanceof SystemFolder) {
            return folderManager.getById(project.getSystems().getID());
        } else if (this instanceof ServerFolder) {
            return project.getServers();
        } else {
            return null;
        }
    }
}
