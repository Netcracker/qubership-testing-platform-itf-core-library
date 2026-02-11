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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.annotation.Nonnull;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.common.LabeledStorable;
import org.qubership.automation.itf.core.model.dataset.DataSetList;
import org.qubership.automation.itf.core.model.dataset.IDataSet;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public abstract class AbstractTestCase extends LabeledStorable implements TestCase {
    protected List<Step> steps = Lists.newLinkedList();
    @JsonIgnore
    private Set<String> compatibleDataSetLists = Sets.newHashSetWithExpectedSize(10);

    public AbstractTestCase() {
    }

    @RefCopy
    @Override
    public Set<DataSetList> getCompatibleDataSetLists(Object projectId) {
        Throwable throwable = null;
        ObjectManager<DataSetList> dsMan = CoreObjectManager.getInstance().getManager(DataSetList.class);
        HashSet<DataSetList> result = new HashSet<>();
        for (String natureId : compatibleDataSetLists) {
            try {
                result.addAll(dsMan.getByNatureId(natureId, projectId));
            } catch (Throwable th) {
                // Silently ignore now; may be thrown later
                throwable = th;
            }
        }
        result.removeIf(Objects::isNull);  // Filter to process deleted/renamed DSLs, or unreachable Dataset Service
        if (result.isEmpty() && throwable != null) {
            throw new RuntimeException(throwable.getMessage(), throwable.getCause());
        }
        return result;
    }

    @Override
    public void addCompatibleDataSetList(DataSetList compatibleDataSetList) {
        compatibleDataSetLists.add(Objects.toString(compatibleDataSetList.getNaturalId()));
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setCompatibleDataSetLists(Set<DataSetList> compatibleDataSetLists) {
        this.compatibleDataSetLists = compatibleDataSetLists.stream()
                .map(dsList -> Objects.toString(dsList.getNaturalId()))
                .collect(Collectors.toSet());
    }

    @Override
    public void fillCompatibleDataSetLists(Set<DataSetList> compatibleDataSetLists) {
        this.compatibleDataSetLists.clear();
        if (compatibleDataSetLists != null) {
            this.compatibleDataSetLists.addAll(
                    compatibleDataSetLists.stream()
                            .map(dsList -> Objects.toString(dsList.getNaturalId()))
                            .collect(Collectors.toSet()));
        }
    }

    @Nonnull
    public Set<String> getCompatibleDataSetListIds() {
        return compatibleDataSetLists;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    public void setCompatibleDataSetListIds(Set<String> naturalIds) {
        this.compatibleDataSetLists = naturalIds;
    }

    @Override
    public List<Step> getSteps() {
        return steps;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setSteps(List<Step> steps) {
        this.steps = steps;
    }

    @Override
    public void fillSteps(List<Step> steps) {
        StorableUtils.fillCollection(getSteps(), steps);
    }

    @Override
    public IDataSet findDataSetByName(String name, Object projectId) {
        for (DataSetList dataSetList : getCompatibleDataSetLists(projectId)) {
            IDataSet dataSet = dataSetList.getDataSet(name, projectId);
            if (dataSet != null) {
                return dataSet;
            }
        }
        return null;
    }

    //for remote DataSet
    @Override
    public IDataSet findDataSetById(String id, Object projectId) {
        for (DataSetList dataSetList : getCompatibleDataSetLists(projectId)) {
            IDataSet dataSet = dataSetList.getDataSetById(id, projectId);
            if (dataSet != null) {
                return dataSet;
            }
        }
        return null;
    }
}
