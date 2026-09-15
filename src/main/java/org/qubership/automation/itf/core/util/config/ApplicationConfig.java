/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import jakarta.annotation.Nonnull;

@Component
public class ApplicationConfig implements EnvironmentAware {

    public static Environment env;

    @Override
    public void setEnvironment(@Nonnull Environment environment) {
        env = environment;
    }

    /**
     * Returns {@link #env}, or throws if the Spring bean that sets it was never constructed.
     */
    public static Environment getEnv() {
        if (env == null) {
            throw new IllegalStateException("ApplicationConfig.env is not set: the Spring bean "
                    + ApplicationConfig.class.getName() + " was never constructed. Add "
                    + "\"org.qubership.automation.itf.core\" to your application's @ComponentScan (or "
                    + "@SpringBootApplication scanBasePackages) so its @Component/@Service beans, "
                    + "including this one, enter your application context.");
        }
        return env;
    }
}
