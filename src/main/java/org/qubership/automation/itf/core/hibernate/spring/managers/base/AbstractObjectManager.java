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

package org.qubership.automation.itf.core.hibernate.spring.managers.base;

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.function.Function;

import jakarta.annotation.Nonnull;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.apache.commons.lang3.NotImplementedException;
import org.apache.commons.lang3.tuple.Triple;
import org.hibernate.ReplicationMode;
import org.hibernate.Session;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.RootRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.dataset.DataSetListsSource;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.message.parser.OperationParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.SystemParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.template.OperationTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.SystemTemplate;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.model.jpa.step.AbstractCallChainStep;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.EventTrigger;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.constants.Match;
import org.qubership.automation.itf.core.util.copier.StorableCopier;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.exception.CopyException;
import org.qubership.automation.itf.core.util.helper.PropertyHelper;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.TypeMismatchException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.repository.query.FluentQuery;

import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Striped;
import lombok.Getter;

public abstract class AbstractObjectManager<T extends Storable, V extends T> implements ObjectManager<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractObjectManager.class);
    private static final Set<Object> SO_CREATION_PREVENTER = Sets.newCopyOnWriteArraySet();
    private static final Set<Object> SO_UPDATE_PREVENTER = Sets.newCopyOnWriteArraySet();
    private static final Set<Object> SO_REMOVAL_PREVENTER = Sets.newCopyOnWriteArraySet();
    private static final int STRIPES = 256;
    private static final Striped<Lock> LOCK_STRIPED = Striped.lazyWeakLock(STRIPES);
    protected final StorableRepository<V> repository;
    protected final Class<T> myType;

    @Getter
    @PersistenceContext
    protected EntityManager entityManager;

    protected AbstractObjectManager(StorableRepository<V> repository) {
        this.myType = null;
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    protected AbstractObjectManager(Class<T> type, StorableRepository<V> repository) {
        this.myType = type;
        this.repository = repository;
    }

    @SuppressWarnings("unchecked")
    protected AbstractObjectManager(Class<T> type, RootRepository<V> repository) {
        this.myType = type;
        this.repository = new RootRepositoryWrapper<>(repository);
    }

    protected static void addToUsages(Collection<UsageInfo> collection,
                                      String propertyName, Iterable<? extends Storable> referers) {
        for (Storable step : referers) {
            UsageInfo usage = new UsageInfo();
            usage.setProperty(propertyName);
            usage.setReferer(step);
            collection.add(usage);
        }
    }

    public Collection<? extends T> getAll() {
        return repository.findAll();
    }

    @Override
    public T getById(@Nonnull Object id) {
        BigInteger identifier = toBigInt(id);
        try {
            // TODO avoid orElse(null) for optionals
            return repository.findById(identifier).orElse(null);
        } catch (Exception e) {
            LOGGER.warn("Object with ID {} not found", identifier);
            return null;
        }
    }

    @Override
    public Collection<? extends T> getByNatureId(@Nonnull Object id, @Nonnull Object projectId) {
        return repository.findByNaturalId(id.toString());
    }

    @Override
    public Collection<? extends T> getAllByParentId(@Nonnull Object id) {
        return repository.findByParentID(toBigInt(id));
    }

    @Override
    public Collection<? extends T> getByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Collection<? extends T> getByPieceOfName(String pieceOfName) {
        return repository.findByNameContainingIgnoreCase(pieceOfName);
    }

    @Override
    public Collection<? extends T> getAllByParentName(String name) {
        return repository.findByParentName(name);
    }

    @Override
    public Collection<T> getByProperties(BigInteger projectId, Triple<String, Match, ?>... properties) {
        Collection<T> toReturn;
        Collection<? extends T> all = getAll();
        try {
            toReturn = all.getClass().newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            toReturn = Sets.newHashSetWithExpectedSize(all.size());
        }
        for (T t : all) {
            if (PropertyHelper.meetsAllProperties(t, properties)) {
                toReturn.add(t);
            }
        }
        return toReturn;
    }

    @Override
    public Collection<? extends T> getByParentAndName(Storable parent, String name) {
        return repository.findByParentIDAndName(parent.getID(), name);
    }

    @Override
    public Collection<UsageInfo> remove(Storable object, boolean force) {
        if (force) {
            repository.delete((V) object);
            afterDelete(object);
            return null;
        }
        Collection<UsageInfo> usages = findUsages(object);
        if (usages == null || usages.isEmpty()) {
            repository.delete((V) object);
            afterDelete(object);
            return null;
        } else {
            return usages;
        }
    }

    public void afterDelete(Storable object) {
    }

    /**
     * Synchronized action(s) on object create.
     */
    @Override
    public void onCreate(T object) {
        if (!SO_CREATION_PREVENTER.contains(object.getID())) {
            synchronized (LOCK_STRIPED.get(object.getID())) {
                if (!SO_CREATION_PREVENTER.contains(object.getID())) {
                    try {
                        TxExecutor.execute(() -> {
                            try {
                                SO_CREATION_PREVENTER.add(object.getID());
                                protectedOnCreate(object);
                            } finally {
                                SO_CREATION_PREVENTER.remove(object.getID());
                            }
                            return null;
                        }, TxExecutor.nestedWritableTransaction());
                    } catch (Exception e) {
                        LOGGER.error("Object {} creation is failed", object, e);
                    }
                }
            }
        }
    }

    /**
     * Synchronized action(s) on object update.
     */
    public void onUpdate(T object) {
        if (!SO_UPDATE_PREVENTER.contains(object.getID())) {
            synchronized (LOCK_STRIPED.get(object.getID())) {
                if (!SO_UPDATE_PREVENTER.contains(object.getID())) {
                    try {
                        TxExecutor.execute(() -> {
                            try {
                                SO_UPDATE_PREVENTER.add(object.getID());
                                protectedOnUpdate(object);
                            } finally {
                                SO_UPDATE_PREVENTER.remove(object.getID());
                            }
                            return null;
                        }, TxExecutor.nestedWritableTransaction());
                    } catch (Exception e) {
                        LOGGER.error("Object {} updating is failed", object, e);
                    }
                }
            }
        }
    }

    /**
     * Synchronized action(s) on object delete.
     */
    @Override
    public void onRemove(T object) {
        if (!SO_REMOVAL_PREVENTER.contains(object.getID())) {
            synchronized (LOCK_STRIPED.get(object.getID())) {
                if (!SO_REMOVAL_PREVENTER.contains(object.getID())) {
                    try {
                        TxExecutor.execute(() -> {
                            try {
                                SO_REMOVAL_PREVENTER.add(object.getID());
                                protectedOnRemove(object);
                            } finally {
                                SO_REMOVAL_PREVENTER.remove(object.getID());
                            }
                            return null;
                        }, TxExecutor.nestedWritableTransaction());
                    } catch (Exception e) {
                        LOGGER.error("Object {} deletion is failed", object, e);
                    }
                }
            }
        }
    }

    protected void protectedOnCreate(T object) {
    }

    protected void protectedOnUpdate(T object) {
    }

    protected void protectedOnRemove(T object) {
    }

    @Override
    public void store(Storable storable) {
        try {
            TxExecutor.execute(() -> {
                T object = repository.save((V) storable);
                storable.setVersion(object.getVersion());
                storable.setID(object.getID());
                return null;
            }, TxExecutor.defaultWritableTransaction());
        } catch (Exception e) {
            LOGGER.error("Error while object {} storing", storable, e);
        }
    }

    /**
     * Replicate (add new or update existing) object.
     */
    public void replicate(Storable object) {
        try {
            ((Session) entityManager.getDelegate()).replicate(object, ReplicationMode.OVERWRITE);
        } catch (Exception e) {
            LOGGER.error("Error while object {} replicating", object, e);
        }
    }

    @Override
    public void evict(Storable object) {
        ((Session) entityManager.getDelegate()).evict(object);
    }

    /**
     * Update object.
     */
    public void update(Storable object) {
        try {
            TxExecutor.execute(() -> {
                ((Session)entityManager.getDelegate()).merge(object);
                return null;
            } , TxExecutor.defaultWritableTransaction());
        } catch (Exception e) {
            LOGGER.error("Error while object {} updating", object, e);
        }
    }

    /**
     * Check if entityManager objects collection contains the object or not.
     *
     * @return true/false
     */
    public boolean contains(Storable object) {
        try {
            return TxExecutor.execute(() -> ((Session)entityManager.getDelegate()).contains(object),
                    TxExecutor.defaultWritableTransaction());
        } catch (Exception e) {
            LOGGER.error("Error while contains check {}: ", object, e);
            return false;
        }
    }

    /**
     * Flush entityManager objects' changes into database.
     */
    public void flush() {
        try {
            TxExecutor.execute(() -> {
                ((Session)entityManager.getDelegate()).flush();
                return null;
            }, TxExecutor.defaultWritableTransaction());
        } catch (Exception e) {
            LOGGER.error("Error while flush: ", e);
        }
    }

    @Override
    public T create() {
        try {
            return repository.save((V) myType.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            LOGGER.error("Error while creating the storable: ", e);
        }
        return null;
    }

    @Override
    public T create(Storable parent) {
        try {
            return repository.save((V) myType.getConstructor(Storable.class).newInstance(parent));
        } catch (InstantiationException | IllegalAccessException
                 | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Error while creating the storable: ", e);
        }
        return null;
    }

    @Override
    public T create(Storable parent, String type) {
        try {
            return repository.save((V) myType.getConstructor(Storable.class, String.class).newInstance(parent, type));
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Error while creating the storable: ", e);
        }
        return null;
    }

    @Override
    public T create(Storable parent, String type, Map parameters) {
        try {
            return repository.save((V) myType.getConstructor(Storable.class, Map.class)
                    .newInstance(parent, parameters));
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Error while creating the storable: ", e);
        }
        return null;
    }

    @Override
    public T create(Storable parent, String name, String type) {
        try {
            return repository.save((V) myType.getConstructor(Storable.class, String.class, String.class)
                    .newInstance(parent, name, type));
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Error while creating the storable: ", e);
        }
        return null;
    }

    @Override
    public T create(Storable parent, String name, String type, String description) {
        try {
            return repository.save((V) myType.getConstructor(Storable.class, String.class, String.class, String.class)
                    .newInstance(parent, name, type, description));
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Error while creating the storable: ", e);
        }
        return null;
    }

    @Override
    public T create(Storable parent, String name, String type, String description, List<String> labels) {
        try {
            return repository.save((V) myType.getConstructor(Storable.class, String.class, String.class,
                    String.class, List.class).newInstance(parent, name, type, description, labels));
        } catch (InstantiationException | IllegalAccessException
                | NoSuchMethodException | InvocationTargetException e) {
            LOGGER.error("Error while creating the storable: ", e);
        }

        return null;
    }

    @Nonnull
    @Override
    public Storable copy(Storable dst, Storable obj, String projectId, String sessionId) throws CopyException {
        if (myType.isAssignableFrom(obj.getClass())) {
            return new StorableCopier(sessionId).copy(obj, dst, projectId, "copy");
        }
        throw new TypeMismatchException(obj, myType);
    }

    @Override
    public void move(Storable dst, Storable obj, String sessionId) {
        /*
            ANKU - condition below was added during fixing the problems with copy/move actions for
            operation parsing rules/system templates/operation templates.
            Moving the operation parsing rules/templates breaks the data right now - situations will have
            references to parsing rules/templates from another operation after moving.

            TODO: later this condition should be deleted and fixed correctly.
         */
        if (!(obj instanceof OperationParsingRule) && !(obj instanceof SystemTemplate)
                && !(obj instanceof OperationTemplate)) {
            if (CoreObjectManager.getInstance().getManager(dst.getClass()).acceptsTo(obj) != null) {
                obj.setParent(dst);
                CoreObjectManager.getInstance().getManager(obj.getClass()).additionalMoveActions(obj, sessionId);
                store(obj);
            } else {
                throw new IllegalArgumentException(String.format("Destination %s cannot accept object %s",
                        dst.getName(), obj.getName()));
            }
        }
    }

    public void additionalMoveActions(Storable storable, String sessionId) {
    }

    /**
     * Find usages for the storable object.
     *
     * @param storable Storable object to find usages, as a rule, checked before deletion.
     * @return List of UsageInfo objects.
     */
    @Override
    public Collection<UsageInfo> findUsages(Storable storable) {
        return null;
    }

    /**
     * Find so-called 'important children' for the storable object.
     * 'important children' are object types preventing deletion of the object (by some decision).
     * For example, "A system can't be deleted if it has transport triggers".
     *
     * @param storable Storable object to find 'important children', as a rule, checked before deletion.
     * @return Map of objects found. Map key is String object type, value is a list of object ids.
     */
    @Override
    public Map<String, List<BigInteger>> findImportantChildren(Storable storable) {
        return null;
    }

    @Override
    public String acceptsTo(Storable storable) {
        if (myType.isAssignableFrom(System.class)) {
            if (storable instanceof TransportConfiguration) {
                return "transports";
            } else if (storable instanceof SystemParsingRule) {
                return "systemParsingRules";
            } else if (storable instanceof Operation) {
                return "operations";
            } else if (storable instanceof SystemTemplate) {
                return "systemTemplates";
            }
        } else if (myType.isAssignableFrom(CallChain.class)) {
            if (storable instanceof AbstractCallChainStep) {
                return "steps";
            }
        } else if (myType.isAssignableFrom(Operation.class)) {
            if (storable instanceof OperationParsingRule) {
                return "operationParsingRules";
            } else if (storable instanceof OperationTemplate) {
                return "operationTemplates";
            } else if (storable instanceof Situation) {
                return "situations";
            }
        } else if (myType.isAssignableFrom(StubProject.class)) {
            if (storable instanceof Environment) {
                return "environments";
            } else if (storable instanceof DataSetListsSource) {
                return "dataSetLists";
            }
        } else if (myType.isAssignableFrom(Situation.class)) {
            if (storable instanceof IntegrationStep) {
                return "steps";
            } else if (storable instanceof EventTrigger) {
                return "triggers";
            }
        } else if (myType.isAssignableFrom(Folder.class)) {
            if (storable instanceof Folder) {
                return "subFolders";
            } else {
                return "objects";
            }
        }
        return null;
    }

    @Override
    public void setReplicationRole(String roleName) {
        throw new NotImplementedException("Not implemented yet");
    }

    private String generateName(@Nonnull Storable storable) {
        return "New " + storable.getClass().getSimpleName();
    }

    protected static class RootRepositoryWrapper<T extends Storable> implements StorableRepository<T> {

        private final RootRepository<T> rootRepository;

        public RootRepositoryWrapper(RootRepository<T> rootRepository) {
            this.rootRepository = rootRepository;
        }

        @Override
        public List<T> findByParentIDAndName(BigInteger parentId, String name) {
            return Collections.emptyList();
        }

        @Override
        public List<T> findByParentID(BigInteger parentId) {
            return Collections.emptyList();
        }

        @Override
        public List<T> findByParentName(String name) {
            return Collections.emptyList();
        }

        @Override
        public List<T> findByName(String name) {
            return rootRepository.findByName(name);
        }

        @Override
        public List<T> findByNameContainingIgnoreCase(String name) {
            return rootRepository.findByNameContainingIgnoreCase(name);
        }

        @Override
        public List<T> findByNaturalId(String naturalId) {
            return rootRepository.findByNaturalId(naturalId);
        }

        @Override
        public List<T> findAll() {
            return rootRepository.findAll();
        }

        @Override
        public List<T> findAll(Sort sort) {
            return rootRepository.findAll(sort);
        }

        @Override
        public Page<T> findAll(Pageable pageable) {
            return rootRepository.findAll(pageable);
        }

        @Override
        public <S extends T> List<S> findAll(Example<S> example) {
            return rootRepository.findAll(example);
        }

        @Override
        public <S extends T> List<S> findAll(Example<S> example, Sort sort) {
            return rootRepository.findAll(example, sort);
        }

        @Override
        public <S extends T> Page<S> findAll(Example<S> example, Pageable pageable) {
            return rootRepository.findAll(example, pageable);
        }

        @Override
        public List<T> findAllById(Iterable<BigInteger> iterable) {
            return rootRepository.findAllById(iterable);
        }

        @Override
        public long count() {
            return rootRepository.count();
        }

        @Override
        public <S extends T> long count(Example<S> example) {
            return rootRepository.count(example);
        }

        @Override
        public void deleteById(BigInteger bigInteger) {
            rootRepository.deleteById(bigInteger);
        }

        @Override
        public void delete(T t) {
            rootRepository.delete(t);
        }

        @Override
        public void deleteAllById(Iterable<? extends BigInteger> bigIntegers) {
            //TODO: should be revised using "deleteAll" as a basis
        }

        @Override
        public void deleteAll(Iterable<? extends T> iterable) {
            rootRepository.deleteAll(iterable);
        }

        @Override
        public void deleteAll() {
            rootRepository.deleteAll();
        }

        @Override
        public <S extends T> S save(S s) {
            return rootRepository.save(s);
        }

        @Override
        public <S extends T> List<S> saveAll(Iterable<S> iterable) {
            return rootRepository.saveAll(iterable);
        }

        @Override
        public Optional<T> findById(BigInteger bigInteger) {
            return rootRepository.findById(bigInteger);
        }

        @Override
        public <S extends T> Optional<S> findOne(Example<S> example) {
            return rootRepository.findOne(example);
        }

        @Override
        public boolean existsById(BigInteger bigInteger) {
            return rootRepository.existsById(bigInteger);
        }

        @Override
        public <S extends T> boolean exists(Example<S> example) {
            return rootRepository.exists(example);
        }

        @Override
        public <S extends T, R> R findBy(Example<S> example,
                                         Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
            //TODO: should be revised using "findById" as a basis
            return null;
        }

        @Override
        public void flush() {
            rootRepository.flush();
        }

        @Override
        public <S extends T> S saveAndFlush(S s) {
            return rootRepository.saveAndFlush(s);
        }

        @Override
        public <S extends T> List<S> saveAllAndFlush(Iterable<S> entities) {
            //TODO: should be revised using "saveAndFlush" as a basis
            return null;
        }

        @Override
        public void deleteInBatch(Iterable<T> iterable) {
            rootRepository.deleteInBatch(iterable);
        }

        @Override
        public void deleteAllInBatch(Iterable<T> entities) {
            //TODO: should be revised using "deleteInBatch" as a basis; the previous method must be removed
        }

        @Override
        public void deleteAllInBatch() {
            rootRepository.deleteAllInBatch();
        }

        @Override
        public void deleteAllByIdInBatch(Iterable<BigInteger> bigIntegers) {
            //TODO: should be revised using "deleteInBatch" as a basis; the previous method must be removed
        }

        @Override
        public T getOne(BigInteger bigInteger) {
            return rootRepository.getOne(bigInteger);
        }

        @Override
        public T getById(BigInteger bigInteger) {
            //TODO: should be revised using "getOne" as a basis;
            return null;
        }

        @Override
        public T getReferenceById(BigInteger bigInteger) {
            //TODO: should be revised using "getOne" as a basis;
            return null;
        }
    }
}
