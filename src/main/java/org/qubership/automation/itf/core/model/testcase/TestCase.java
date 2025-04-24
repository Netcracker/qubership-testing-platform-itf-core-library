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

package org.qubership.automation.itf.core.model.testcase;

import java.util.Set;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.container.StepContainer;
import org.qubership.automation.itf.core.model.dataset.DataSetList;
import org.qubership.automation.itf.core.model.dataset.IDataSet;

public interface TestCase extends Storable, StepContainer {

    void addCompatibleDataSetList(DataSetList compatibleDataSetList);

    Set<DataSetList> getCompatibleDataSetLists(Object projectId);

    IDataSet findDataSetByName(String name, Object projectId);

    IDataSet findDataSetById(String id, Object projectId);

    void fillCompatibleDataSetLists(Set<DataSetList> compatibleDataSetLists);
}
