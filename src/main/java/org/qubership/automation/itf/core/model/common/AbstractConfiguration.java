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

package org.qubership.automation.itf.core.model.common;

import java.beans.Transient;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.google.common.collect.Maps;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public abstract class AbstractConfiguration<T, V> extends LabeledStorable implements Map<T, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConfiguration.class);

    private Map<T, V> configuration = Maps.newHashMapWithExpectedSize(20);
    private String typeName;

    public AbstractConfiguration() {
        super();
    }

    public AbstractConfiguration(String typeName) {
        super();
        this.typeName = typeName;
    }

    @Transient
    @Override
    public int size() {
        return configuration.size();
    }

    @Transient
    @Override
    public boolean isEmpty() {
        return configuration.isEmpty();
    }

    @Transient
    @Override
    public boolean containsKey(Object key) {
        return configuration.containsKey(key);
    }

    @Transient
    @Override
    public boolean containsValue(Object value) {
        return configuration.containsValue(value);
    }

    @Transient
    @Override
    public V get(Object key) {
        return configuration.get(key);
    }

    @Transient
    @Override
    public V put(T key, V value) {
        return configuration.put(key, value);
    }

    @Transient
    @Override
    public V remove(Object key) {
        return configuration.remove(key);
    }

    @Transient
    @Override
    public void putAll(Map<? extends T, ? extends V> m) {
        configuration.putAll(m);
    }

    @Transient
    @Override
    public void clear() {
        configuration.clear();
    }

    @Transient
    @Override
    public Set<T> keySet() {
        return configuration.keySet();
    }

    @Transient
    @Override
    public Collection<V> values() {
        return configuration.values();
    }

    @Transient
    @Override
    public Set<Map.Entry<T, V>> entrySet() {
        return configuration.entrySet();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Map<T, V> getConfiguration() {
        return this.configuration;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setConfiguration(Map<T, V> configuration) {
        this.configuration = configuration;
    }

    @Transient
    public void fillConfiguration(Map<T, V> configuration) {
        StorableUtils.fillMap(getConfiguration(), configuration);
    }
}
