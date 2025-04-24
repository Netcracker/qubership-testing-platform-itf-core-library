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

package org.qubership.automation.itf.core.util.registry;

import org.qubership.automation.itf.core.util.holder.ActiveInterceptorHolder;
import org.qubership.automation.itf.core.util.holder.InterceptorHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class InterceptorsRegister {
    private static final Logger LOGGER = LoggerFactory.getLogger(InterceptorsRegister.class);

    /**
     * Perform interceptors' modules init and register - InterceptorHolder.getInstance(),
     * and active (configured) interceptors register - ActiveInterceptorHolder.getInstance().
     * The method and entire class should not be used,
     * because in multi-tenant app ActiveInterceptorHolder is filled more than once, from all clusters.
     * So, ActiveInterceptorHolder was rewritten to be used in multi-tenant app.
     * Actually, it's filled in the UiContextListener#registerInterceptors method (atp-itf-executor service).
     */
    public void registrate() {
        try {
            LOGGER.info("Registration of interceptors' modules is started...");
            InterceptorHolder.getInstance();
            LOGGER.info("Registration of interceptors' modules is completed.");

            LOGGER.info("Registration of active interceptors is started...");
            ActiveInterceptorHolder.getInstance();
            LOGGER.info("Registration of active interceptors is completed.");
        } catch (Exception e) {
            LOGGER.error("Registration of interceptors is failed", e);
        }
    }
}
