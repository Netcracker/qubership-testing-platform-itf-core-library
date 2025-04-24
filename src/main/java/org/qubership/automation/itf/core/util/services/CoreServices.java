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

package org.qubership.automation.itf.core.util.services;

import static org.qubership.automation.itf.core.util.services.CoreServicesNames.PROJECT_SETTINGS_SERVICE;

import java.util.HashMap;
import java.util.Map;

import org.qubership.automation.itf.core.util.services.projectsettings.IProjectSettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Service;

@Service
@DependsOn(value = "projectSettingsService")
public class CoreServices {

    private static final Map<String, Object> coreServices = new HashMap<>();

    @Autowired
    public CoreServices(@Qualifier("projectSettingsService") IProjectSettingsService projectSettingsService) {
        coreServices.put(PROJECT_SETTINGS_SERVICE, projectSettingsService);
    }

    public static IProjectSettingsService getProjectSettingsService() {
        return (IProjectSettingsService) coreServices.get(PROJECT_SETTINGS_SERVICE);
    }
}
