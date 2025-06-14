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

package org.qubership.automation.itf.core.util.hazelcast.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.client.config.ClientConfig;
import com.hazelcast.client.config.ClientConnectionStrategyConfig;

@ConditionalOnProperty(value = "hazelcast.cache.enabled")
@EnableCaching
@Configuration
public class HazelcastClientConfig {

    @Value("${hazelcast.cluster-name}")
    private String clusterName;

    @Value("${hazelcast.address}")
    private String hazelcastAddress;

    @Value("${hazelcast.client.name}")
    private String hazelcastClientName;

    /**
     * Create {@link ClientConfig} bean.
     *
     * @return bean
     */
    @Bean(name = "clientConfig")
    public ClientConfig clientConfig() {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setClusterName(clusterName);
        clientConfig.setInstanceName(hazelcastClientName);
        clientConfig.getNetworkConfig().addAddress(hazelcastAddress);
        clientConfig.getConnectionStrategyConfig().setReconnectMode(ClientConnectionStrategyConfig.ReconnectMode.ASYNC);
        return clientConfig;
    }
}
