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

package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.lang3.tuple.Triple;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.ParsingRuleRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.util.constants.Match;
import org.qubership.automation.itf.core.util.helper.PropertyHelper;
import org.qubership.automation.itf.core.util.parser.ParsingRuleType;
import org.qubership.automation.itf.core.util.provider.ParsingRuleProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ParsingRuleObjectManager<K extends ParsingRuleProvider, T extends ParsingRule<K>>
        extends AbstractObjectManager<T, T>
        implements SearchByProjectIdManager<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ParsingRuleObjectManager.class);

    public ParsingRuleObjectManager(Class<T> clazz, ParsingRuleRepository<K, T> repository) {
        super(clazz, repository);
    }

    @Override
    public T create(Storable parent) {
        try {
            T parsingRule = myType.getConstructor(Storable.class).newInstance(parent);
            if (parent instanceof ParsingRuleProvider) {
                ((ParsingRuleProvider)parent).addParsingRule(parsingRule);
            }
            return repository.save(parsingRule);
        } catch (InstantiationException | IllegalAccessException
                | InvocationTargetException | NoSuchMethodException e) {
            LOGGER.error("Error while parsing rule creation: ", e);
        }
        return null;
    }

    @Override
    public T create(Storable parent, String type, Map parameters) {
        if (!(parent instanceof ParsingRuleProvider)) {
            throw new IllegalArgumentException(String.format("Can't add parsing rule to '%s', "
                    + " because object is not parsing rule provider!", parent));
        }
        T parsingRule = super.create(parent, type, parameters);
        parsingRule.setParsingType(ParsingRuleType.XPATH);
        parsingRule.setExpression(".");//default value
        return parsingRule;
    }

    public Collection<T> getByProjectId(BigInteger projectId) {
        return ((ParsingRuleRepository<K,T>) repository).findByProjectId(projectId);
    }

    @Override
    public Collection<T> getByProperties(BigInteger projectId, Triple<String, Match, ?>... properties) {
        Collection<T> toReturn = new LinkedList<T>() {
        };
        Collection<T> all = getByProjectId(projectId);
        for (T rule : all) {
            if (PropertyHelper.meetsAllProperties(rule, properties)) {
                toReturn.add(rule);
            }
        }
        return toReturn;
    }
}
