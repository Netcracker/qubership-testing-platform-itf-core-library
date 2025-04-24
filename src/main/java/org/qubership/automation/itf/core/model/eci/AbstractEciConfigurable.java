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

package org.qubership.automation.itf.core.model.eci;

import org.qubership.automation.itf.core.model.common.LabeledStorable;

public abstract class AbstractEciConfigurable extends LabeledStorable implements EciConfigurable {
    private String ecId;
    private String ecProjectId;
    private String ecLabel;

    public String getEcId() {
        return ecId;
    }

    public void setEcId(String ecId) {
        this.ecId = ecId;
    }

    @Override
    public String getEcProjectId() {
        return ecProjectId;
    }

    @Override
    public void setEcProjectId(String ecProjectId) {
        this.ecProjectId = ecProjectId;
    }

    public void setEciParameters(String ecId, String ecProjectId) {
        setEcId(ecId);
        setEcProjectId(ecProjectId);
    }

    public String getEcLabel() {
        return ecLabel;
    }

    public void setEcLabel(String ecLabel) {
        this.ecLabel = ecLabel;
    }
}

