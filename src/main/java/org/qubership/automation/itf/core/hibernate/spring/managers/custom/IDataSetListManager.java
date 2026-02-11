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

package org.qubership.automation.itf.core.hibernate.spring.managers.custom;

import java.util.Collection;
import java.util.List;

import jakarta.annotation.Nonnull;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.dataset.DataSetList;
import org.qubership.automation.itf.core.model.dataset.DataSetListsSource;
import org.qubership.automation.itf.core.model.dataset.IDataSet;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;

public interface IDataSetListManager extends ObjectManager<DataSetList> {
    Collection<? extends DataSetListsSource> getAllSources();

    Collection<? extends DataSetListsSource> getAllSources(Object projectId);

    DataSetListsSource getSourceById(Object id, Object projectId);

    //DataSetListsSource getSourceByNatureId(Object id);

    DataSetListsSource getSourceByNatureId(Object id, Object projectId);

    List<IDataSet> getDataSetsWithLabel(DataSetList list, String label, Object projectId);

    Folder<DataSetListsSource> getFolder();

    DataSetList getById(@Nonnull Object id, Object projectId);
}
