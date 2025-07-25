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

import java.util.List;

import javax.annotation.Nonnull;

import org.qubership.automation.itf.core.model.common.Named;
import org.qubership.automation.itf.core.model.jpa.context.JsonContext;

public interface IDataSet extends Named {
    JsonContext read(Object projectId);

    /**
     * Reads dataset parameters and overrides params with params from custom dataset.
     *
     * @param overriddenValues custom dataset which can contain parameters with DS formulas.
     *                         In case it contains formulas, and plain text params
     *                         DO NOT FORGET REMOVE overridden params from custom DS.
     * @return JsonContext
     */
    JsonContext read(@Nonnull JsonContext overriddenValues, Object projectId);

    String getIdDs();

    String getDsServiceUri();

    void addModifiedToName(boolean flag);

    List<String> getLabels();
}
