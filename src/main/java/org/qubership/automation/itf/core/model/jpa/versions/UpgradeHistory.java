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

package org.qubership.automation.itf.core.model.jpa.versions;

import java.sql.Timestamp;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;

@Entity
public class UpgradeHistory extends AbstractStorable {
    private static final long serialVersionUID = 20240812L;

    private Timestamp upgradeDatetime;

    public UpgradeHistory(Timestamp upgradeDatetime, String name) {
        this.upgradeDatetime = upgradeDatetime;
        super.setName(name);
    }

    public UpgradeHistory() {
    }

    public Timestamp getUpgradeDatetime() {
        return upgradeDatetime;
    }

    public void setUpgradeDatetime(Timestamp upgradeDatetime) {
        this.upgradeDatetime = upgradeDatetime;
    }
}
