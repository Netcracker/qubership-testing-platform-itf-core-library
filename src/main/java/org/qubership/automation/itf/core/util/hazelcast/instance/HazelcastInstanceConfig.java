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

package org.qubership.automation.itf.core.util.hazelcast.instance;

import static org.qubership.automation.itf.core.CoreConstants.HIBERNATE_CACHE_HAZELCAST_INSTANCE_EUREKA_CONFIG_NAME;
import static org.qubership.automation.itf.core.CoreConstants.HIBERNATE_CACHE_HAZELCAST_INSTANCE_EUREKA_CONFIG_NAMESPACE;
import static org.qubership.automation.itf.core.CoreConstants.HIBERNATE_CACHE_HAZELCAST_INSTANCE_NAME;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.hazelcast.config.CacheSimpleConfig;
import com.hazelcast.config.Config;
import com.hazelcast.config.EurekaConfig;
import com.hazelcast.config.EvictionConfig;
import com.hazelcast.config.EvictionPolicy;
import com.hazelcast.config.MaxSizePolicy;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;

@Configuration
@ConditionalOnProperty(name = "hibernate.second.level.cache.enabled", havingValue = "true")
public class HazelcastInstanceConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(HazelcastInstanceConfig.class);

    @Value("${eureka.client.serviceUrl.defaultZone}")
    private String eurekaUrl;

    @Value("${hazelcast.cache.enabled:false}")
    private boolean hazelcastCacheEnabled;

    /**
     * Create Config object, populate its properties and return the object.
     *
     * @return Config object
     */
    @Bean(name = "instanceConfig")
    public Config getConfig() {
        Config config = new Config();
        config.setInstanceName(HIBERNATE_CACHE_HAZELCAST_INSTANCE_NAME.stringValue());
        if (hazelcastCacheEnabled) {
            config.setProperty("hazelcast.phone.home.enabled", "false");
            config.getNetworkConfig().getJoin().getMulticastConfig().setEnabled(false);
            config.getNetworkConfig().getRestApiConfig().setEnabled(true);

            EurekaConfig eurekaConfig = config.getNetworkConfig().getJoin().getEurekaConfig();
            eurekaConfig.setEnabled(true);
            eurekaConfig.setProperty("self-registration", "true");
            eurekaConfig.setProperty("namespace",
                    HIBERNATE_CACHE_HAZELCAST_INSTANCE_EUREKA_CONFIG_NAMESPACE.stringValue());
            eurekaConfig.setProperty("use-classpath-eureka-client-props","false");
            eurekaConfig.setProperty("shouldUseDns","false");
            eurekaConfig.setProperty("name",
                    HIBERNATE_CACHE_HAZELCAST_INSTANCE_EUREKA_CONFIG_NAME.stringValue());
            eurekaConfig.setProperty("serviceUrl.default", eurekaUrl);
        } else {
            config.setClusterName("local-itf-hazelcast-cluster");
        }

        config.addCacheConfig(
                initBigRegionCache("projectsCache", 300, 12, TimeUnit.HOURS));

        config.addCacheConfig(
                initBigRegionCache("configurationsCache", 300000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("parsingRulesCache",80000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("keysToRegenerateCache",15000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("stepContainersCache",50000, 120, TimeUnit.MINUTES));

        config.addCacheConfig(
                initBigRegionCache("eventTriggerCache",30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("conditionPropsCache",60000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("operationParsingRulesCache",70000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("systemParsingRulesCache",10000, 120, TimeUnit.MINUTES));

        config.addCacheConfig(
                initBigRegionCache("templateCache",35000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("operationTemplateCache",30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("systemTemplateCache",5000, 120, TimeUnit.MINUTES));

        config.addCacheConfig(
                initBigRegionCache("outboundTransportConfigurationCache",
                        30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("outboundTransportConfigurationsCollectionCache",
                        30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(
                initBigRegionCache("outboundTemplateTransportConfigurationsCollectionCache",
                        70000, 120, TimeUnit.MINUTES));

        config.addCacheConfig(initBigRegionCache("systemParsingRulesCollectionCache",
                        7000, 60, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("operationParsingRulesCollectionCache",
                        70000, 60, TimeUnit.MINUTES));

        config.addCacheConfig(initBigRegionCache("activeOperationEventTriggersCache",
                        30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("operationByDefinitionKeyCache",
                        20000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("operationSituationsCollectionCache",
                        30000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("systemTransportsCollectionCache",
                4000, 120, TimeUnit.MINUTES));
        config.addCacheConfig(initBigRegionCache("simpleSystemListByProjectCache",
                500, 240, TimeUnit.MINUTES));

        Map<String, CacheSimpleConfig> cacheConfigs = config.getCacheConfigs();
        LOGGER.info("CacheConfigs - {} entries", cacheConfigs.size());
        for (Map.Entry<String, CacheSimpleConfig> cfg : cacheConfigs.entrySet()) {
            LOGGER.info("CacheConfig: {}: \n   EvictionConfig {}\n   ExpiryPolicyFactoryConfig {}", cfg.getKey(),
                    cfg.getValue().getEvictionConfig(), cfg.getValue().getExpiryPolicyFactoryConfig());
        }

        return config;
    }

    @Bean(name = "hazelcastCacheInstance")
    public HazelcastInstance getHazelcastInstance(@Qualifier("instanceConfig") Config config) {
        return Hazelcast.newHazelcastInstance(config);
    }

    private CacheSimpleConfig initBigRegionCache(String cacheName,
                                                 int cacheSize,
                                                 int durationAmount,
                                                 TimeUnit durationTimeUnit) {
        EvictionConfig evictionConfig = new EvictionConfig();
        evictionConfig.setEvictionPolicy(EvictionPolicy.LRU);
        evictionConfig.setMaxSizePolicy(MaxSizePolicy.ENTRY_COUNT);
        evictionConfig.setSize(cacheSize);
        CacheSimpleConfig cacheConfig = new CacheSimpleConfig();
        cacheConfig.setName(cacheName);
        cacheConfig.setEvictionConfig(evictionConfig);

        CacheSimpleConfig.ExpiryPolicyFactoryConfig.DurationConfig durationConfig =
                new CacheSimpleConfig.ExpiryPolicyFactoryConfig.DurationConfig(durationAmount, durationTimeUnit);
        CacheSimpleConfig.ExpiryPolicyFactoryConfig.TimedExpiryPolicyFactoryConfig timedExpiryConfig =
                new CacheSimpleConfig.ExpiryPolicyFactoryConfig.TimedExpiryPolicyFactoryConfig(
                        CacheSimpleConfig.ExpiryPolicyFactoryConfig.TimedExpiryPolicyFactoryConfig
                                .ExpiryPolicyType.ACCESSED,
                        durationConfig
                );
        CacheSimpleConfig.ExpiryPolicyFactoryConfig expiryConfig =
                new CacheSimpleConfig.ExpiryPolicyFactoryConfig(timedExpiryConfig);
        cacheConfig.setExpiryPolicyFactoryConfig(expiryConfig);
        return cacheConfig;
    }
}
