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

import java.util.Collection;
import java.util.List;

import org.qubership.automation.itf.core.hibernate.spring.managers.custom.IDataSetListManager;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.dataset.DataSetList;
import org.qubership.automation.itf.core.model.dataset.DataSetListsSource;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.google.common.collect.ImmutableList;

public class DsListFolder extends Folder<DataSetListsSource> {
    private static final long serialVersionUID = 20240812L;
    public static final Class<DataSetListsSource> TYPE = DataSetListsSource.class;

    public DsListFolder() {
        super(TYPE);
    }

    public DsListFolder(Storable parent) {
        this();
        setParent(parent);
    }

    @Override
    public List<DataSetListsSource> getObjects() {
        return ImmutableList.copyOf(CoreObjectManager.getInstance()
                .getSpecialManager(DataSetList.class, IDataSetListManager.class).getAllSources());
    }

    @Override
    protected void setObjects(List<DataSetListsSource> objects) {
    }

    @Override
    public void fillObjects(Collection<DataSetListsSource> objects) {
    }
}
