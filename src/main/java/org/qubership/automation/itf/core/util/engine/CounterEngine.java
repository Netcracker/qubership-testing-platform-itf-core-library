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

package org.qubership.automation.itf.core.util.engine;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateUtils;
import org.qubership.automation.itf.core.model.counter.Counter;
import org.qubership.automation.itf.core.model.counter.CounterImpl;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.exception.CounterLimitIsExhaustedException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

public class CounterEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterEngine.class);

    private static CounterEngine INSTANCE = new CounterEngine();

    private final Map<Set<Object>, Counter> counterMap = Maps.newHashMap();

    //TODO need optimisation the constructor. getAll() for all counters is a bed idea.
    private CounterEngine() {
        Collection<? extends Counter> counters = CoreObjectManager.getInstance().getManager(Counter.class).getAll();
        for (Counter counter : counters) {
            if (DateUtils.isSameDay(Calendar.getInstance().getTime(), counter.getDate())) {
                counterMap.put(counter.getOwners(), counter);
            } else {
                counter.remove();
            }
        }
    }

    public static CounterEngine getInstance() {
        return INSTANCE;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public String nextIndex(Set<Object> owners, String counterFormat) throws CounterLimitIsExhaustedException {
        if (counterFormat == null) {
            LOGGER.warn("Counter format is null");
            return null;
        }
        synchronized (counterMap) {
            for (Map.Entry<Set<Object>, Counter> entry : counterMap.entrySet()) {
                if (entry.getKey().equals(owners)) {
                    return getNextIndexAndStore(entry.getValue());
                }
            }
            //TODO if we will have more one Impl for counter then we will need edit signature
            return newCounter(owners, counterFormat, CounterImpl.class);
        }
    }

    //TODO Need to implement other format if it's needed
    private String prepareIndex(Integer index, String format) {
        if (format.length() == 2) {
            if (index.toString().length() == 1) {
                return "0" + index.toString();
            }
        } else {
            if (format.length() == 3) {
                switch (index.toString().length()) {
                    case 1:
                        return "00" + index.toString();
                    case 2:
                        return "0" + index.toString();
                    default:
                        LOGGER.error("Index length is not 1 or 2.");
                        break;
                }
            }
        }
        return index.toString();
    }

    private String newCounter(Set<Object> owners, String format, Class clazz) throws CounterLimitIsExhaustedException {
        try {
            return TxExecutor.execute(() -> {
                Counter counter = CoreObjectManager.getInstance().getManager(Counter.class).create();
                counter.setOwners(owners);
                counter.setDate(Calendar.getInstance().getTime());
                counter.setFormat(format);
                counter.setIndex(1); // Set starting value to 1 (old variant: 0) in order to avoid subsequent call of
                // 'getNextIndexAndStore'
                counter.store();
                counterMap.put(owners, counter);
                return prepareIndex(1, format);
            }, TxExecutor.nestedWritableTransaction());
        } catch (Exception e) {
            throw new CounterLimitIsExhaustedException("Unable to create new counter", e);
        }
    }

    private String getNextIndexAndStore(Counter counter) throws CounterLimitIsExhaustedException {
        try {
            return TxExecutor.execute(() -> {
                String index = prepareIndex(counter.getNextIndex(), counter.getFormat());
                counter.store();
                return index;
            }, TxExecutor.nestedWritableTransaction());
        } catch (Exception e) {
            throw new CounterLimitIsExhaustedException("Unable to store counter", e);
        }
    }
}
