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

package org.qubership.automation.itf.core.model.dataset;

import java.math.BigInteger;
import java.util.Set;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.qubership.automation.itf.core.model.common.Identified;
import org.qubership.automation.itf.core.model.common.Named;
import org.qubership.automation.itf.core.model.common.OptimisticLockable;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;

public interface DataSetList extends Storable, Named, Identified<BigInteger>, OptimisticLockable<Object> {

    @Override
    DataSetListsSource getParent();

    @Nonnull
    Set<IDataSet> getDataSets(Object projectId);

    @Nonnull
    Set<IDataSet> getDataSetsWithLabel(String label, Object projectId);

    @Nonnull
    Set<String> getVariables();

    @Nullable
    IDataSet getDataSet(String dataSetName, Object projectId);

    @Nullable
    IDataSet getDataSetById(String dataSetId, Object projectId);

    @Nullable
    // ITF-CR-18; Alexander Kapustin, 2017-12-28, This method - may be - will be removed.
    // I will re-think all around it and make changes after ITF 4.2.15 release.
    JsonContext getDataSetContextById(String dataSetId, Object projectId);
}
