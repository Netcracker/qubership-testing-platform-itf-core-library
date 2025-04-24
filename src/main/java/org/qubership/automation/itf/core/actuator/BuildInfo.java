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

package org.qubership.automation.itf.core.actuator;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.qubership.automation.itf.core.hibernate.spring.managers.executor.UpgradeHistoryObjectManager;
import org.qubership.automation.itf.core.model.jpa.versions.UpgradeHistory;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import lombok.SneakyThrows;

@Component
@PropertySource(value = "file:./buildVersion.properties", ignoreResourceNotFound = true)
public class BuildInfo implements InfoContributor {

    @Autowired
    private Environment env;

    @SneakyThrows
    @Override
    public void contribute(Info.Builder builder) {
        String currentBuildVersion = env.getProperty("application.version");
        if (currentBuildVersion != null) {
            builder.withDetail("build", Collections.singletonMap("version", currentBuildVersion));
        }
        Collection<? extends UpgradeHistory> allBuildsHistory = CoreObjectManager.getInstance()
                .getSpecialManager(UpgradeHistory.class, UpgradeHistoryObjectManager.class).getAll();
        Collections.reverse((List<?>) allBuildsHistory);
        Map<String, Map<String, String>> buildsHistory = new LinkedHashMap<>();
        int count = allBuildsHistory.size();
        for (UpgradeHistory upgradeHistory : allBuildsHistory) {
            Map<String, String> id = new HashMap<>();
            id.put("update_date", upgradeHistory.getUpgradeDatetime().toString());
            id.put("version", upgradeHistory.getName());
            buildsHistory.put(String.valueOf(count), id);
            count--;
        }
        builder.withDetail("last_builds", buildsHistory);
    }
}
