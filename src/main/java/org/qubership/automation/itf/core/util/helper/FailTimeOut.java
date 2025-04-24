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

package org.qubership.automation.itf.core.util.helper;

import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.util.config.Config;
import org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.slf4j.LoggerFactory;

public class FailTimeOut {

    /**
     * Get project settings 'Fail timeout' and 'Fail timeout timeunit' and calculate timeout in msecs.
     *
     * @return {@link long} Fail timeout value in milliseconds
     */
    public static long getTimeout(BigInteger projectId) {
        try {
            return TxExecutor.execute(() -> {
                Map<String, String> props = CoreObjectManager.getInstance().getManager(StubProject.class)
                    .getById(projectId).getStorableProp();
                TimeUnit configuredTimeUnit = TimeUnit.valueOf(props.getOrDefault(
                        ProjectSettingsConstants.TC_TIMEOUT_FAIL_TIME_UNIT,
                        ProjectSettingsConstants.TC_TIMEOUT_FAIL_TIME_UNIT_DEFAULT_VALUE));
                long configuredValue = Long.parseLong(props.getOrDefault(
                        ProjectSettingsConstants.TC_TIMEOUT_FAIL,
                        ProjectSettingsConstants.TC_TIMEOUT_FAIL_DEFAULT_VALUE));
                return configuredTimeUnit.toMillis(configuredValue);
            }, TxExecutor.readOnlyTransaction());
        } catch (Exception e) {
            LoggerFactory.getLogger(Config.class).warn("FailTimeOut.getTimeout({}) exception: ", projectId, e);
            TimeUnit timeUnit = TimeUnit.valueOf(ProjectSettingsConstants.TC_TIMEOUT_FAIL_TIME_UNIT_DEFAULT_VALUE);
            return timeUnit.toMillis(Long.parseLong(ProjectSettingsConstants.TC_TIMEOUT_FAIL_DEFAULT_VALUE));
        }
    }
}
