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

package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.annotation.Nonnull;
import jakarta.annotation.PostConstruct;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.FolderManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.FolderRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.folder.ChainFolder;
import org.qubership.automation.itf.core.model.jpa.folder.DsListFolder;
import org.qubership.automation.itf.core.model.jpa.folder.EnvFolder;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.folder.ServerFolder;
import org.qubership.automation.itf.core.model.jpa.folder.SystemFolder;
import org.qubership.automation.itf.core.util.copier.StorableCopier;
import org.qubership.automation.itf.core.util.exception.CopyException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@Service
public class FolderObjectManager extends AbstractObjectManager<Folder, Folder> implements FolderManager {

    private final FolderRepository folderRepository;
    private Map<String, Class<? extends Folder>> subclasses;

    @Autowired
    public FolderObjectManager(FolderRepository repository) {
        super(Folder.class, repository);
        this.folderRepository = repository;
    }

    @Override
    public Folder create(Storable parent, String name, String type) {
        Class<? extends Folder> folderClass = subclasses.get(type);
        if (folderClass == null) {
            throw new IllegalArgumentException("Cannot create folder of type " + type);
        }
        Folder result;
        try {
            result = folderClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot create folder of type " + type + " with class "
                    + folderClass.getCanonicalName(), e);
        }
        Folder parentFolder = null;
        if (parent instanceof Folder) {
            parentFolder = (Folder) parent;
        }
        result.setParent(parentFolder);
        result.setTypeName(type);
        result.setName(name);
        result = repository.save(result);
        if (parentFolder != null) {
            parentFolder.getSubFolders().add(result);
        }
        return result;
    }

    @Override
    public Folder create() {
        throw new IllegalArgumentException("Cannot create step of unknown type!");
    }

    @PostConstruct
    protected void init() {
        subclasses = new HashMap<String, Class<? extends Folder>>() {
            {
                put(EnvFolder.TYPE.getSimpleName(), EnvFolder.class);
                put(ChainFolder.TYPE.getSimpleName(), ChainFolder.class);
                put(DsListFolder.TYPE.getSimpleName(), DsListFolder.class);
                put(ServerFolder.TYPE.getSimpleName(), ServerFolder.class);
                put(SystemFolder.TYPE.getSimpleName(), SystemFolder.class);
            }
        };
    }

    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Only Folder objects are here")
    @Nonnull
    @Override
    public Storable copy(Storable dst, Storable obj, String projectId, String sessionId) throws CopyException {
        Folder<? extends Storable> sourceFolder = (Folder<? extends Storable>) obj;
        Storable folderCopy = super.copy(dst, obj, projectId, sessionId);
        for (Storable subElement : sourceFolder.getObjects()) {
            StorableCopier storableCopier = new StorableCopier(sessionId);
            storableCopier.copy(subElement, folderCopy, projectId, "copy");
        }
        for (Folder<? extends Storable> subFolder : ((Folder<? extends Storable>) obj).getSubFolders()) {
            //preventing infinity recursive copying
            if (!subFolder.getID().equals(folderCopy.getID())) {
                this.copy(folderCopy, subFolder, projectId, sessionId);
            }
        }
        return folderCopy;
    }

    /**
     * Find folder of specified type inside project (identified by projectId) by piece of name.
     *
     * @return List of folders found
     */
    public List<Folder> findFolderByPieceOfName(String classShortName, String pieceOfName, BigInteger projectId) {
        switch (classShortName) {
            case "ChainFolder":
                return folderRepository.findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, projectId,
                        "chains");
            case "EnvFolder":
                return folderRepository.findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, projectId,
                        "envs");
            case "SystemFolder":
                return folderRepository.findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, projectId,
                        "systems");
            default:
                return new ArrayList<>();
        }
    }

    @Override
    public void afterDelete(Storable object) {
        if (object.getParent() instanceof Folder) {
            synchronized (object.getParent()) {
                ((Folder<?>) object.getParent()).getSubFolders().remove(object);
            }
        }
    }
}
