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

package org.qubership.automation.itf.core.model.extension;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class SituationExtension implements Extension, Serializable {

    private static final long serialVersionUID = 20241125L;

    private List<String> situationInstanceIds = new LinkedList<>();

    public SituationExtension() {
    }

    public List<String> getSituationInstanceIds() {
        return situationInstanceIds;
    }

    public void setSituationInstanceIds(List<String> situationInstanceIds) {
        this.situationInstanceIds = situationInstanceIds;
    }
}
