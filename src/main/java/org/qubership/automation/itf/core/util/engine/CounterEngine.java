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
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang3.time.DateUtils;
import org.qubership.automation.itf.core.model.counter.Counter;
import org.qubership.automation.itf.core.model.counter.CounterImpl;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.exception.CounterLimitIsExhaustedException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;

/**
 * Issues per-day, per-owner sequential indexes backed by {@link Counter} rows.
 *
 * <p>{@link #getInstance()} commits the singleton only once construction succeeds, so a call
 * reaching it before {@link CoreObjectManager} is wired leaves the singleton unset and a later
 * call retries construction instead of failing permanently.</p>
 */
public class CounterEngine {

    private static final Logger LOGGER = LoggerFactory.getLogger(CounterEngine.class);

    private static volatile CounterEngine instance;

    private final ConcurrentMap<Set<Object>, Counter> counterMap = Maps.newConcurrentMap();

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

    /**
     * Returns the singleton, building it lazily on first call.
     *
     * @throws NullPointerException if {@link CoreObjectManager} has not been wired yet; the
     *     singleton stays unset so a later call can retry
     */
    public static CounterEngine getInstance() {
        CounterEngine result = instance;
        if (result == null) {
            synchronized (CounterEngine.class) {
                result = instance;
                if (result == null) {
                    instance = result = new CounterEngine();
                }
            }
        }
        return result;
    }

    /**
     * Returns the next formatted index for {@code owners}, creating a counter for today when none
     * exists yet or the stored one predates today.
     *
     * @param owners the counter's identity
     * @param counterFormat the index format; {@code null} is logged and returns {@code null}
     * @return the formatted next index, or {@code null} when {@code counterFormat} is {@code null}
     * @throws CounterLimitIsExhaustedException if the index already reached its format's limit, or
     *     the counter could not be stored
     */
    public String nextIndex(Set<Object> owners, String counterFormat) throws CounterLimitIsExhaustedException {
        if (counterFormat == null) {
            LOGGER.warn("Counter format is null");
            return null;
        }
        Counter counter = counterMap.get(owners);
        if (counter != null) {
            if (DateUtils.isSameDay(Calendar.getInstance().getTime(), counter.getDate())) {
                return getNextIndexAndStore(counter);
            }
            // Only the thread that wins this conditional remove deletes the row, so a concurrent
            // evictor for the same owners never double-deletes it.
            if (counterMap.remove(owners, counter)) {
                counter.remove();
            }
        }
        //TODO if we will have more one Impl for counter then we will need edit signature
        return newCounter(owners, counterFormat, CounterImpl.class);
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
        // computeIfAbsent runs its function at most once per owners key even under concurrent
        // callers, so only one Counter row is ever created for a given owners set, and the map's
        // per-bucket locking never blocks a call for a different owners key.
        String[] freshIndex = new String[1];
        Counter counter;
        try {
            counter = counterMap.computeIfAbsent(owners, key -> {
                try {
                    return TxExecutor.execute(() -> {
                        Counter created = CoreObjectManager.getInstance().getManager(Counter.class).create();
                        created.setOwners(key);
                        created.setDate(Calendar.getInstance().getTime());
                        created.setFormat(format);
                        created.setIndex(1); // Set starting value to 1 (old variant: 0) in order to avoid
                        // subsequent call of 'getNextIndexAndStore'
                        created.store();
                        freshIndex[0] = prepareIndex(1, format);
                        return created;
                    }, TxExecutor.nestedWritableTransaction());
                } catch (Exception e) {
                    throw new CounterCreationFailure(e);
                }
            });
        } catch (CounterCreationFailure e) {
            throw new CounterLimitIsExhaustedException("Unable to create new counter", e.getCause());
        }
        return freshIndex[0] != null ? freshIndex[0] : getNextIndexAndStore(counter);
    }

    // Synchronized per counter, not per map: CounterImpl.getNextIndex() increments a plain field,
    // so two threads sharing one counter must not run this concurrently, while unrelated owners
    // must still run their own DB round trip without waiting on this one.
    private String getNextIndexAndStore(Counter counter) throws CounterLimitIsExhaustedException {
        synchronized (counter) {
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

    /** Wraps a checked failure from {@link #newCounter} so it can cross {@link ConcurrentMap#computeIfAbsent}. */
    private static final class CounterCreationFailure extends RuntimeException {
        CounterCreationFailure(Throwable cause) {
            super(cause);
        }
    }
}
