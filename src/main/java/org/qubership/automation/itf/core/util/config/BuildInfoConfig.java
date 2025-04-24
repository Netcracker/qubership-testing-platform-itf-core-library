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

package org.qubership.automation.itf.core.util.config;

public class BuildInfoConfig extends AbstractConfig {

    private static final String CONFIG_FILENAME = "buildInfo.info";
    private static volatile BuildInfoConfig config;

    BuildInfoConfig(boolean withLoading) {
        super(withLoading, CONFIG_FILENAME);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static BuildInfoConfig getConfig() {
        if (config == null) {
            synchronized (BuildInfoConfig.class) {
                if (config == null) {
                    config = new BuildInfoConfig(true);
                }
            }
        }
        return config;
    }
}
