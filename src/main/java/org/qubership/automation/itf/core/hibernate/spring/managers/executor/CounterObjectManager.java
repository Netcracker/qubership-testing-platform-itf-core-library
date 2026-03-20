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

package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.CounterRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.counter.Counter;
import org.qubership.automation.itf.core.model.counter.CounterImpl;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

@Service
public class CounterObjectManager extends AbstractObjectManager<Counter, Counter> {

    private Map<String, Class<? extends Counter>> subclasses;

    public CounterObjectManager(CounterRepository repository) {
        super(Counter.class, repository);
    }

    @Override
    public Counter create(Storable parent, String type, Map parameters) {
        Class<? extends Counter> counterClass = subclasses.get(type);
        if (counterClass == null) {
            throw new IllegalArgumentException("Cannot create counter of type " + type);
        }
        Counter result;
        try {
            result = counterClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
                 | InvocationTargetException e) {
            throw new RuntimeException("Cannot create counter of type " + type + " with class "
                    + counterClass.getCanonicalName(), e);
        }
        result = repository.save(result);
        result.setIndex(0);
        return result;
    }

    @Override
    protected void protectedOnRemove(Counter object) {

    }

    @PostConstruct
    protected void init() {
        subclasses = new HashMap<>() {
            {
                put(CounterImpl.class.getName(), CounterImpl.class);
            }
        };
    }
}
