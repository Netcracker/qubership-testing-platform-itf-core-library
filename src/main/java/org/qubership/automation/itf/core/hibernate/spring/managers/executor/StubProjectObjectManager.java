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

import static org.qubership.automation.itf.core.util.constants.InstanceSettingsConstants.WORKING_DIRECTORY;
import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

import org.apache.logging.log4j.util.Strings;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.UserDataManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StubProjectRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.config.ApplicationConfig;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.exception.StorageException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class StubProjectObjectManager extends AbstractObjectManager<StubProject, StubProject>
        implements SearchManager<StubProject>, UserDataManager {

    private final StubProjectRepository stubProjectRepository;

    @Autowired
    public StubProjectObjectManager(StubProjectRepository repository) {
        super(StubProject.class, repository);
        this.stubProjectRepository = repository;
    }

    @Override
    public StubProject create(Storable parent, String type, Map parameters) {
        StubProject project = create();
        project.setStorableProp(parameters);
        return project;
    }

    @Override
    public StubProject create() {
        StubProject project = super.create();
        prepareRootFolders(project);
        createDatasetFolder((BigInteger) project.getID());
        return stubProjectRepository.save(project);
    }

    @Override
    public void setReplicationRole(String roleName) {
        if (roleName.equals("replica")) {
            stubProjectRepository.setReplicationRoleReplica();
        } else {
            stubProjectRepository.setReplicationRoleOrigin();
        }
    }

    @Override
    public String setUserData(String action, String key, String value, BigInteger projectId) {
        if (action == null) {
            return "#set_userdata directive error: 'Action' parameter is null or empty!";
        }
        try {
            return TxExecutor.execute(() -> {
                switch (action) {
                    case "SELECT":
                        List<String> objs = stubProjectRepository.getData(key, projectId);
                        return !objs.isEmpty() ? objs.get(0) : "";
                    case "INSERT":
                        stubProjectRepository.setData(key, value, projectId);
                        break;
                    case "UPDATE":
                        stubProjectRepository.updateData(key, value, projectId);
                        break;
                    case "UPSERT":
                        stubProjectRepository.upsertData(key, value, projectId);
                        break;
                    case "DELETE":
                        stubProjectRepository.deleteData(key, projectId);
                        break;
                    default:
                        return "#set_userdata directive error: Unknown 'Action': " + action + "!";
                }
                return "";
            }, TxExecutor.nestedWritableTransaction());
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public String clearUserData(Integer leaveDays) {
        return stubProjectRepository.clearUserData(leaveDays);
    }

    @Override
    public BigInteger getEntityInternalIdByUuid(UUID uuid) {
        return stubProjectRepository.getEntityInternalIdByUuid(uuid);
    }

    @Override
    public StubProject getByUuid(UUID uuid) {
        return stubProjectRepository.getByUuid(uuid);
    }

    @Override
    public Map<String, Object> getProjectIdsBySystem(Object systemId) {
        List<String[]> results = stubProjectRepository.determineProjectIdsBySystemId(toBigInt(systemId));
        if (results.isEmpty()) {
            return null;
        }
        Map<String, Object> map = new HashMap<>();
        map.put("projectId", toBigInt(results.get(0)[0]));
        map.put("projectUuid", UUID.fromString(results.get(0)[1]));
        return map;
    }

    /**
     * Get project setting by projectId and propertyShortName.
     */
    @Transactional(readOnly = true)
    public String getProjectSettingByShortName(BigInteger projectId, String propertyShortName) {
        try {
            return stubProjectRepository.getProjectSetting(projectId, propertyShortName);
        } catch (Exception e) {
            log.warn("Error getting project setting from DB by projectId {} and property name '{}', root cause: {}",
                    projectId, propertyShortName, (e.getCause() == null ? e : e.getCause()));
            return Strings.EMPTY;
        }
    }

    /**
     * Get all project settings by projectId.
     */
    @Transactional(readOnly = true)
    public Map<String, String> getAllProjectSettingsByProjectId(BigInteger projectId) {
        try {
            List<Object[]> arraysWithProjectSettingsShortNamesAndValues =
                    stubProjectRepository.getAllProjectSettingsByProjectId(projectId);
            return formatProjectSettingsAsMap(arraysWithProjectSettingsShortNamesAndValues);
        } catch (Exception e) {
            log.error("Error getting project settings from DB by projectId {}, root cause: {}", projectId,
                    (e.getCause() == null ? e : e.getCause()));
            return new HashMap<>();
        }
    }

    @Transactional
    public void updateProjectSetting(@Param("projectId") BigInteger projectId,
                                     @Param("propShortName") String propShortName,
                                     @Param("propValue") String propValue) {
        stubProjectRepository.updateProjectSetting(projectId, propShortName, propValue);
    }

    private Map<String, String> formatProjectSettingsAsMap(List<Object[]> nameValueArrays) {
        Map<String, String> projectSettings = new HashMap<>();
        for (Object[] item : nameValueArrays) {
            projectSettings.put(item[0].toString(), item[1] != null ? item[1].toString() : Strings.EMPTY);
        }
        return projectSettings;
    }

    private void prepareRootFolders(StubProject project) {
        if (project.getEnvironments() == null) {
            project.setEnvironments(createRootFolder(project, Environment.class));
        }
        if (project.getCallchains() == null) {
            project.setCallchains(createRootFolder(project, CallChain.class));
        }
        if (project.getServers() == null) {
            project.setServers(createRootFolder(project, Server.class));
        }
        if (project.getSystems() == null) {
            project.setSystems(createRootFolder(project, System.class));
        }
    }

    private <T extends Storable> Folder<T> createRootFolder(StubProject project, Class<T> forClass) {
        Folder<T> result = (Folder<T>) CoreObjectManager.getInstance().getManager(Folder.class)
                .create(null, "ROOT", forClass.getSimpleName()).of(forClass).get();
        result.setProject(project);
        return result;
    }

    private <T extends Storable> Folder<T> createFolder(Storable parent, Class<T> forClass) {
        return (Folder<T>) CoreObjectManager.getInstance().getManager(Folder.class)
                .create(parent, forClass.getSimpleName()).of(forClass).get();
    }

    private void createDatasetFolder(BigInteger projectId) {
        Path newDataSetFolder = Paths.get(Objects.requireNonNull(ApplicationConfig.env.getProperty(WORKING_DIRECTORY)),
                "dataset").resolve(String.valueOf(projectId));
        if (!newDataSetFolder.toFile().mkdirs()) {
            throw new StorageException(String.format("DataSet folder for project %d hasn't been created.", projectId));
        }
    }
}
