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

package org.qubership.automation.itf.configuration.spring;

import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.create;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.managerFor;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.project;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.renameStoreValidate;

import java.util.Map;

import javax.transaction.Transactional;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.Assert;

import com.google.common.collect.Maps;
import com.google.common.reflect.TypeToken;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:hibernate-configuration-test-context.xml"})
public class FoldersTest {

    public static Map<Class, TypeToken<? extends Folder<? extends Storable>>> REGISTRY = Maps.newHashMap();

    @SuppressWarnings("UnstableApiUsage")
    @BeforeClass
    public static void before() {
        register(new TypeToken<Folder<CallChain>>() {
        });
        register(new TypeToken<Folder<Environment>>() {
        });
        /*register(new TypeToken<Folder<DataSetListsSource>>() {
        });*/
        register(new TypeToken<Folder<Server>>() {
        });
        register(new TypeToken<Folder<Server>>() {
        });
        register(new TypeToken<Folder<System>>() {
        });
    }

    private static void register(TypeToken<? extends Folder<? extends Storable>> folder) {
        Class folderGeneric = folder.resolveType(Folder.class.getTypeParameters()[0]).getRawType();
        REGISTRY.put(folderGeneric, folder);
    }

    @Test
    public void createFolders() {
        for (Class clazz : REGISTRY.keySet()) {
            oldStyleCreate(clazz);
        }
    }

    @Test
    public void deleteFolderContainingCallChains() {
        Object id = null;
        try {
            id = TxExecutor.execute(() -> {
                StubProject project = project();
                Folder<CallChain> rootCallChainFolder = project.getCallchains();
                Folder callChainFolder = managerFor(Folder.class).create(rootCallChainFolder, "New CallChain Folder",
                        CallChain.class.getSimpleName());
                create(callChainFolder, CallChain.class, null);
                create(callChainFolder, CallChain.class, null);
                create(callChainFolder, CallChain.class, null);
                return callChainFolder.getID();
            }, TxExecutor.nestedWritableTransaction());
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.notNull(id);
        managerFor(Folder.class).getById(id).remove();
        Assert.isNull(managerFor(Folder.class).getById(id));
    }

    private <T extends Storable> Folder<T> oldStyleCreate(Class<T> clazz) {
        ObjectManager<Folder> folderOM = CoreObjectManager.getInstance().getManager(Folder.class);
        StubProject proj = project();
        Folder<T> root = folderOM.create(null, "New Folder", clazz.getSimpleName());
        root.setProject(proj);
        renameStoreValidate(folderOM, root);
        Folder<T> second = folderOM.create(root, "New Folder", clazz.getSimpleName());
        renameStoreValidate(folderOM, second);
        return second;
    }
}
