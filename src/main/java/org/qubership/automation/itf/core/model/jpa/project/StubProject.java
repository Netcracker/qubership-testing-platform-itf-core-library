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

import java.math.BigInteger;
import java.util.Set;
import java.util.UUID;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.hibernate.spring.managers.custom.IDataSetListManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.dataset.DataSetList;
import org.qubership.automation.itf.core.model.dataset.DataSetListsSource;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.folder.ChainFolder;
import org.qubership.automation.itf.core.model.jpa.folder.EnvFolder;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.folder.ServerFolder;
import org.qubership.automation.itf.core.model.jpa.folder.SystemFolder;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.ei.deserialize.ChainFolderDeserializer;
import org.qubership.automation.itf.core.util.ei.deserialize.EnvFolderDeserializer;
import org.qubership.automation.itf.core.util.ei.deserialize.ServerFolderDeserializer;
import org.qubership.automation.itf.core.util.ei.deserialize.SystemFolderDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = StubProject.class)
public class StubProject extends AbstractStorable implements StubContainer {
    private static final long serialVersionUID = 20240812L;

    private SystemFolder systems;
    private ChainFolder callchains;
    private EnvFolder environments;
    private ServerFolder servers;
    private Folder<DataSetListsSource> dataSetLists;
    private Set<IntegrationConfig> integrationConfs = Sets.newHashSet();

    private UUID uuid;

    public StubProject() {
    }

    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Folder<System> getSystems() {
        return systems;
    }

    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    @Override
    @JsonDeserialize(using = SystemFolderDeserializer.class)
    public void setSystems(Folder<System> systems) {
        this.systems = (SystemFolder) systems;
    }

    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Folder<Environment> getEnvironments() {
        return environments;
    }

    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    @Override
    @JsonDeserialize(using = EnvFolderDeserializer.class)
    public void setEnvironments(Folder<Environment> environments) {
        this.environments = (EnvFolder) environments;
    }

    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Folder<Server> getServers() {
        return servers;
    }

    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    @Override
    @JsonDeserialize(using = ServerFolderDeserializer.class)
    public void setServers(Folder<Server> servers) {
        this.servers = (ServerFolder) servers;
    }

    @Override
    @JsonIgnore
    public Folder<DataSetListsSource> getDataSetLists() {
        return CoreObjectManager.getInstance()
                .getSpecialManager(DataSetList.class, IDataSetListManager.class).getFolder();
    }

    /** setDataSetLists method.
     * @param dataSetLists - dataSet lists
     * @deprecated {@link IDataSetListManager} manages the folder itself
     */
    @Deprecated
    @Override
    public void setDataSetLists(Folder<DataSetListsSource> dataSetLists) {
    }

    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Folder<CallChain> getCallchains() {
        return callchains;
    }

    @SuppressFBWarnings("BC_UNCONFIRMED_CAST")
    @Override
    @JsonDeserialize(using = ChainFolderDeserializer.class)
    public void setCallchains(Folder<CallChain> callchains) {
        this.callchains = (ChainFolder) callchains;
    }

    @Override
    public Storable findRootObject(BigInteger projectId) {
        return CoreObjectManager.getInstance().getManager(StubProject.class).getById(projectId);
    }

    @Override
    public BigInteger getProjectId() {
        return (BigInteger) this.getID();
    }
}
