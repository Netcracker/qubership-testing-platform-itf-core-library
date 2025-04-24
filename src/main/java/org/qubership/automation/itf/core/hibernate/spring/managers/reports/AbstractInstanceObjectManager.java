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

package org.qubership.automation.itf.core.hibernate.spring.managers.reports;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.InstanceManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.reports.AbstractInstanceRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractInstance;
import org.qubership.automation.itf.core.model.jpa.instance.SituationInstance;
import org.qubership.automation.itf.core.model.jpa.instance.chain.CallChainInstance;
import org.qubership.automation.itf.core.model.jpa.instance.step.StepInstance;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.generator.id.UniqueIdGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AbstractInstanceObjectManager<T extends AbstractInstance> extends AbstractObjectManager<AbstractInstance,
        AbstractInstance> implements InstanceManager {

    private Map<String, Class<? extends AbstractInstance>> subclasses;

    @Autowired
    public AbstractInstanceObjectManager(AbstractInstanceRepository repository) {
        super(AbstractInstance.class, repository);
    }

    @Override
    public void protectedOnRemove(AbstractInstance object) {
    }

    @Override
    public AbstractInstance create(final Storable parent, final String type, Map parameters) {
        return createInMemory(parent, type, parameters, true);
    }

    @Override
    public AbstractInstance create() {
        throw new IllegalArgumentException("Cannot create instance of unknown type!");
    }

    private AbstractInstance createInMemory(final Storable parent, final String type, Map parameters, boolean setId) {
        Class<? extends AbstractInstance> instanceClass = subclasses.get(type);
        if (null == instanceClass) {
            throw new IllegalArgumentException("Cannot create instance of type " + type);
        }
        AbstractInstance result;
        try {
            result = instanceClass.newInstance();
            if (setId) {
                result.setID(UniqueIdGenerator.generate());
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Cannot create instance of type " + type + " with class "
                    + instanceClass.getCanonicalName(), e);
        }
        result.setParent(parent);
        return result;
    }

    @PostConstruct
    protected void init() {
        subclasses = new HashMap<String, Class<? extends AbstractInstance>>() {
            {
                put(CallChainInstance.class.getSimpleName(), CallChainInstance.class);
                put(SituationInstance.class.getSimpleName(), SituationInstance.class);
                put(StepInstance.class.getSimpleName(), StepInstance.class);
            }
        };
    }

    @Override
    public void storeInNestedTransaction(final Storable storable) {
        TxExecutor.executeUnchecked(() -> {
            AbstractInstance object = repository.save((AbstractInstance) storable);
            storable.setVersion(object.getVersion());
            storable.setID(object.getID());
            return object;
        }, TxExecutor.nestedWritableTransaction());
    }

    public T getByIDAndPartNum(BigInteger id, Integer partNum) {
        return ((AbstractInstanceRepository<T>)repository).findByIDAndPartNum(id, partNum);
    }

}
