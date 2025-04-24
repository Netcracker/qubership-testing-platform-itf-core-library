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

package org.qubership.automation.itf.core.util.report;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;
import org.qubership.automation.itf.core.hibernate.spring.managers.executor.EnvironmentObjectManager;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.report.LinkCollectorConfiguration;
import org.qubership.automation.itf.core.util.descriptor.Extractor;
import org.qubership.automation.itf.core.util.descriptor.PropertyDescriptor;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class ReportLinkCollector {

    private Map<String, LinkCollector> collectors = Maps.newConcurrentMap();
    private org.springframework.core.env.Environment env;
    private final LoadingCache<String, String> urls = CacheBuilder.newBuilder()
            .build(new CacheLoader<String, String>() {
                @Override
                public String load(@NotNull String key) {
                    return env.getProperty(key);
                }
            });

    private static final LoadingCache<BigInteger, Set<LinkCollectorConfiguration>> CONF_CACHE = CacheBuilder
            .newBuilder()
            .expireAfterWrite(60, TimeUnit.MINUTES)
            .build(new CacheLoader<BigInteger, Set<LinkCollectorConfiguration>>() {
                @Override
                public Set<LinkCollectorConfiguration> load(@Nonnull BigInteger id) {
                    try {
                        Set<LinkCollectorConfiguration> result = CoreObjectManager.getInstance()
                                .getSpecialManager(Environment.class, EnvironmentObjectManager.class)
                                .getLinkCollectorsByEnvId(id);
                        return result == null ? new HashSet<>() : result;
                    } catch (Throwable anyException) {
                        return new HashSet<>();
                    }
                }
            });

    private ReportLinkCollector() {
    }

    @Autowired
    public ReportLinkCollector(org.springframework.core.env.Environment env) {
        this.env = env;
    }

    public void cacheCleanup() {
        CONF_CACHE.cleanUp();
    }

    /**
     * Collect links from collectors configured for tcContext and its Environment.
     */
    public Map<String, String> collect(TcContext tcContext) {
        Map<String, String> links = Maps.newHashMapWithExpectedSize(collectors.size());
        try {
            for (Map.Entry<String, LinkCollector> entry : collectors.entrySet()) {
                if (entry.getValue().common()) {
                    Pair<String, String> pair = entry.getValue().collect(tcContext, null);
                    links.put(pair.getLeft(), Strings.nullToEmpty(pair.getRight()));
                }
            }
            Set<LinkCollectorConfiguration> reportCollectors = CONF_CACHE.get(tcContext.getEnvironmentId());
            for (LinkCollectorConfiguration configuration : reportCollectors) {
                LinkCollector collector = collectors.computeIfAbsent(configuration.getTypeName(), key -> {
                    try {
                        return Class.forName(key).asSubclass(LinkCollector.class).newInstance();
                    } catch (IllegalAccessException | InstantiationException | ClassNotFoundException e) {
                        log.error("Error initializing link collector for {}: ", key, e);
                        return null;
                    }
                });
                if (collector != null && collector.getClass().getName().equals(configuration.getTypeName())) {
                    Pair<String, String> pair = collector.collect(tcContext, configuration);
                    if (pair.getValue().startsWith("unknownhost")) {
                        continue;
                    }
                    links.put(pair.getLeft(), Strings.nullToEmpty(pair.getRight()));
                }
            }
        } catch (Exception e) {
            log.error("Error collecting link...", e);
        }
        tcContext.getReportLinks().putAll(links);
        return links;
    }

    /**
     * Register collector for LinkCollector given.
     */
    public void registerCollector(LinkCollector collector) {
        collectors.put(collector.getClass().getName(), collector);
    }

    /**
     * Register collector for class.
     */
    public void registerCollector(Class<? extends LinkCollector> collectorClass) {
        collectors.computeIfAbsent(collectorClass.getName(), key -> {
            try {
                return collectorClass.newInstance();
            } catch (IllegalAccessException | InstantiationException e) {
                log.error("Error while registering collector class {}: ", collectorClass, e);
                return null;
            }
        });
    }

    /**
     * Register collectors for classNames.
     */
    @SuppressWarnings("unchecked")
    public void registerCollectors(Collection<String> classNames) {
        for (String className : classNames) {
            try {
                registerCollector((Class<? extends LinkCollector>) Class.forName(className));
            } catch (ClassNotFoundException e) {
                log.error("Collector class {} not found", className);
            }
        }
    }

    public void unregisterCollector(LinkCollector collector) {
        collectors.remove(collector.getClass().getName());
    }

    public void unregisterCollectors(Class<? extends LinkCollector> collectorClass) {
        collectors.remove(collectorClass.getName());
    }

    public Collection<PropertyDescriptor> getProperties(String className) {
        return Extractor.extractProperties(collectors.get(className));
    }

    /**
     * Get link to a configuration object or reported object.
     */
    public String getLinkToObject(BigInteger projectId, UUID projectUuid, Object objectId,
                                  String endpoint, boolean standalone) {
        return standalone
                ? urls.getUnchecked("configurator.url") + "/project/" + projectId + endpoint + objectId
                : urls.getUnchecked("atp.catalogue.url") + "/project/" + projectUuid + "/itf" + endpoint + objectId;
    }
}
