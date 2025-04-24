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

package org.qubership.automation.itf.core.model.common;

import java.util.ArrayList;
import java.util.List;

import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.util.helper.StorableUtils;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public abstract class LabeledStorable extends AbstractStorable implements Labeled {
    private List<String> labels = new ArrayList<>(1);

    public void fillLabels(List<String> labels) {
        StorableUtils.fillCollection(getLabels(), labels);
    }
}
