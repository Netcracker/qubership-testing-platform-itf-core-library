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

package org.qubership.automation.itf.core.util.copier;

import static org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants.COPY_OBJECT_SET_STATUS_OFF;
import static org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants.COPY_OBJECT_SET_STATUS_OFF_DEFAULT_VALUE;

import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.common.LabeledStorable;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.message.parser.OperationParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.step.IntegrationStep;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.util.annotation.NoCopy;
import org.qubership.automation.itf.core.util.annotation.OperationRefCopyAsNewObject;
import org.qubership.automation.itf.core.util.annotation.ProduceNewObject;
import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.annotation.RefCopyAsNewObject;
import org.qubership.automation.itf.core.util.annotation.TemplateRefCopyAsNewObject;
import org.qubership.automation.itf.core.util.constants.Mep;
import org.qubership.automation.itf.core.util.exception.CopyException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.provider.ParsingRuleProvider;
import org.qubership.automation.itf.core.util.provider.TemplateProvider;
import org.qubership.automation.itf.core.util.services.CoreServices;
import org.springframework.beans.BeanUtils;

import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class StorableCopier {

    private static final String USECASE1 = "UC.1";//UC1. Copy System
    private static final String USECASE2 = "UC.2";//UC2. Copy Operation to the same System
    private static final String USECASE3 = "UC.3";//UC3. Copy Operation to another System
    private static final String USECASE4 = "UC.4";//UC4. Copy Situation to the same Operation
    private static final String USECASE5 = "UC.5";//UC5. Copy Situation to another Operation in the same System
    private static final String USECASE6 = "UC.6";//UC6. Copy Situation to another Operation to another System
    private static final Map<String, Boolean> mapCopyFlag = new HashMap<>();
    private final String sessionId;

    public StorableCopier(String sessionId) {
        this.sessionId = sessionId;
    }

    public static boolean getValueMapCopyFlag(String key) {
        return mapCopyFlag.get(key);
    }

    public static void setKeyAndValueMapCopyFlag(String key, boolean value) {
        mapCopyFlag.put(key, value);
    }

    private static Method getSetOrFillMethod(PropertyDescriptor descriptor, Class owner) {
        Method method = descriptor.getWriteMethod();
        if (method == null) {
            String methodName = "fill" + StringUtils.capitalize(descriptor.getName());
            try {
                method = owner.getMethod(methodName, descriptor.getPropertyType());
            } catch (NoSuchMethodException ignored) {
                //ok, no such method - no setter
            }
        }
        return method;
    }

    private static void copyReferenceOrValue(PropertyDescriptor descriptor, Storable source, Storable dest)
            throws CopyException {
        try {
            Object value = descriptor.getReadMethod().isAnnotationPresent(ProduceNewObject.class)
                    ? source.getID().toString() : descriptor.getReadMethod().invoke(source);
            getSetOrFillMethod(descriptor, dest.getClass()).invoke(dest, value);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CopyException(String.format("Error while copying property %s from %s to %s",
                    descriptor.getName(), source, dest), e);
        }
    }

    private static Map produceNewMap(Map ethalon) {
        if (ethalon.getClass().getName().contains("java.util")) {
            try {
                return ethalon.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                return produceBasicMap(ethalon);
            }
        } else {
            return produceBasicMap(ethalon);
        }
    }

    private static Map produceBasicMap(Map ethalon) {
        return Maps.newHashMapWithExpectedSize(ethalon.size());
    }

    private static Collection produceNewCollection(Collection ethalon) {
        if (ethalon.getClass().getName().contains("java.util")) {
            try {
                return ethalon.getClass().newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                return produceBasicCollection(ethalon);
            }
        } else {
            return produceBasicCollection(ethalon);
        }
    }

    private static Collection produceBasicCollection(Collection ethalon) {
        if (Set.class.isAssignableFrom(ethalon.getClass())) {
            return Sets.newHashSetWithExpectedSize(ethalon.size());
        } else if (Queue.class.isAssignableFrom(ethalon.getClass())) {
            return Lists.newLinkedList();
        } else {
            return Lists.newArrayListWithExpectedSize(ethalon.size());
        }
    }

    private static void copyCollection(PropertyDescriptor descriptor, Storable source, Storable dest)
            throws CopyException {
        try {
            Collection collection = (Collection) descriptor.getReadMethod().invoke(source);
            Collection newCollection = produceNewCollection(collection);
            newCollection.addAll(collection);
            getSetOrFillMethod(descriptor, dest.getClass()).invoke(dest, newCollection);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CopyException(String.format("Error while copying property %s from %s to %s",
                    descriptor.getName(), source, dest), e);
        }
    }

    private static void copyMap(PropertyDescriptor descriptor, Storable source, Storable dest) throws CopyException {
        try {
            Map map = (Map) descriptor.getReadMethod().invoke(source);
            Map newCollection = (map == null) ? new HashMap() : produceNewMap(map);
            if (map != null) {
                newCollection.putAll(map);
            }
            getSetOrFillMethod(descriptor, dest.getClass()).invoke(dest, newCollection);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CopyException(String.format("Error while copying property %s from %s to %s",
                    descriptor.getName(), source, dest), e);
        }
    }

    /**
     * Copy from storable to destination.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST", justification = "Casted objects have proper types")
    public Storable copy(Storable storable, Storable destination, String projectId, String prefix)
            throws CopyException {
        String propertyName;
        propertyName = CoreObjectManager.getInstance().getManager(destination.getClass()).acceptsTo(storable);
        if (propertyName == null) {
            throw new CopyException(String.format("Destination %s cannot accept child %s", destination, storable));
        }
        Storable newStorable;
        boolean sameParent = Objects.equals(storable.getParent(), destination);
        String useCaseForTemplate = getUseCase(storable, destination, sameParent);
        PropertyDescriptor descriptor = BeanUtils.getPropertyDescriptor(destination.getClass(), propertyName);
        if (Collection.class.isAssignableFrom(descriptor.getReadMethod().getReturnType())) {
            try {
                Collection collection = (Collection) descriptor.getReadMethod().invoke(destination);
                newStorable = createCopy(storable, destination, prefix, sameParent, useCaseForTemplate);
                collection.add(newStorable);
            } catch (IllegalAccessException | InvocationTargetException e) {
                OriginalCopyMap.getInstance().clear(sessionId);
                throw new CopyException(String.format("Error while adding copy of %s to parent %s",
                        storable, destination));
            }
        } else if (descriptor.getReadMethod().getReturnType().isAssignableFrom(storable.getClass())) {
            if (descriptor.getWriteMethod() != null) {
                try {
                    newStorable = createCopy(storable, destination, prefix, sameParent, "");
                    descriptor.getWriteMethod().invoke(destination, newStorable);
                } catch (IllegalAccessException | InvocationTargetException e) {
                    OriginalCopyMap.getInstance().clear(sessionId);
                    throw new CopyException(String.format("Error while setting copy of %s as child to parent %s",
                            storable, destination));
                }
            } else {
                OriginalCopyMap.getInstance().clear(sessionId);
                throw new CopyException(String.format("Property %s is read-only at %s",
                        descriptor.getName(), destination));
            }
        } else {
            OriginalCopyMap.getInstance().clear(sessionId);
            throw new CopyException(String.format("Parent %s cannot accept %s", destination, storable));
        }
        if (newStorable instanceof Template) {
            ((Template) newStorable).setParent((TemplateProvider) destination);
        } else if (newStorable instanceof ParsingRule) {
            ((ParsingRule) newStorable).setParent((ParsingRuleProvider) destination);
        } else if (newStorable instanceof TransportConfiguration) {
            ((TransportConfiguration) newStorable).setParent((System) destination);
        } else {
            newStorable.setParent(destination);
        }
        if (prefix.equals("copy") && !useCaseForTemplate.isEmpty()) {
            // Execute these extra actions only for USECASE1-USECASE6
            postActionsForUpdateParent(newStorable, destination, useCaseForTemplate);
            postActionsForUpdateTriggerOnSituation(newStorable, useCaseForTemplate);
        }
        newStorable.store();
        newStorable.performPostCopyActions(Boolean.parseBoolean(CoreServices.getProjectSettingsService().get(projectId,
                COPY_OBJECT_SET_STATUS_OFF, COPY_OBJECT_SET_STATUS_OFF_DEFAULT_VALUE)));
        return newStorable;
    }

    private Storable createCopy(Storable storable, Storable destination, String prefix, boolean sameParent,
                                String useCaseForTemplate) throws CopyException {
        Storable newStorable;
        try {
            if (Iterables.tryFind(Arrays.asList(storable.getClass().getInterfaces()),
                            (Predicate<Class>) input -> input.getName().equals("org.hibernate.proxy.HibernateProxy"))
                    .isPresent()) {
                newStorable = storable.getClass().getSuperclass().asSubclass(Storable.class).newInstance();
            } else {
                newStorable = storable.getClass().newInstance();
            }
        } catch (InstantiationException | IllegalAccessException e) {
            throw new CopyException(String.format("Error creating copy of %s", storable));
        }
        PropertyDescriptor[] descriptors = BeanUtils.getPropertyDescriptors(newStorable.getClass());
        for (PropertyDescriptor descriptor : descriptors) {
            if (descriptor.getReadMethod() != null
                    && getSetOrFillMethod(descriptor, newStorable.getClass()) != null
                    && !descriptor.getReadMethod().isAnnotationPresent(NoCopy.class)) {
                if (Storable.class.isAssignableFrom(descriptor.getReadMethod().getReturnType())) {
                    if (descriptor.getReadMethod().isAnnotationPresent(RefCopyAsNewObject.class)) {
                        if (sameParent) {
                            copyReferenceOrValue(descriptor, storable, newStorable);
                        } else {
                            copyReferenceAsNewEntity(descriptor, storable, destination, newStorable,
                                    prefix, useCaseForTemplate);
                        }
                    } else if (descriptor.getReadMethod().isAnnotationPresent(RefCopy.class)) {
                        copyReferenceOrValue(descriptor, storable, newStorable);
                    } else if (descriptor.getReadMethod().isAnnotationPresent(OperationRefCopyAsNewObject.class)) {
                        forOperation_copyReferenceAsNewEntity(descriptor, storable, destination, newStorable,
                                prefix, useCaseForTemplate);
                    } else if (descriptor.getReadMethod().isAnnotationPresent(TemplateRefCopyAsNewObject.class)) {
                        forTemplate_copyReferenceAsNewEntity(descriptor, storable, destination, newStorable,
                                prefix, useCaseForTemplate);
                    } else {
                        copyChild(descriptor, storable, newStorable, prefix, useCaseForTemplate);
                    }
                } else if (Collection.class.isAssignableFrom(descriptor.getReadMethod().getReturnType())) {
                    if (descriptor.getReadMethod().getGenericReturnType() instanceof ParameterizedType) {
                        Type[] types =
                                ((ParameterizedType) descriptor.getReadMethod().getGenericReturnType())
                                        .getActualTypeArguments();
                        if (types != null && types.length > 0) {
                            if (types[0] instanceof Class) {
                                if (Storable.class.isAssignableFrom((Class<?>) types[0])
                                        && !descriptor.getReadMethod().isAnnotationPresent(RefCopy.class)) {
                                    copyChildCollection(descriptor, storable, newStorable,
                                            prefix, useCaseForTemplate);
                                } else {
                                    copyCollection(descriptor, storable, newStorable);
                                }
                            } else if (types[0] instanceof ParameterizedType) {
                                if (Storable.class.isAssignableFrom((Class<?>) ((ParameterizedType) types[0])
                                        .getRawType())
                                        && !descriptor.getReadMethod().isAnnotationPresent(RefCopy.class)) {
                                    copyChildCollection(descriptor, storable, newStorable,
                                            prefix, useCaseForTemplate);
                                } else {
                                    copyCollection(descriptor, storable, newStorable);
                                }
                            } else if (types[0] instanceof TypeVariable) {
                                if (((TypeVariable) types[0]).getBounds()[0] instanceof Class
                                        && Storable.class.isAssignableFrom((Class<?>) ((TypeVariable) types[0])
                                        .getBounds()[0])
                                        && !descriptor.getReadMethod().isAnnotationPresent(RefCopy.class)) {
                                    copyChildCollection(descriptor, storable, newStorable,
                                            prefix, useCaseForTemplate);
                                } else {
                                    copyCollection(descriptor, storable, newStorable);
                                }
                            } else {
                                throw new CopyException("Don't know what to do with property. "
                                        + "Please, contact Mockingbird support");
                            }
                        } else {
                            copyCollection(descriptor, storable, newStorable);
                        }
                    }
                } else if (Map.class.isAssignableFrom(descriptor.getReadMethod().getReturnType())) {
                    copyMap(descriptor, storable, newStorable);
                } else {
                    copyReferenceOrValue(descriptor, storable, newStorable);
                }
            }
        }
        if (storable instanceof Map) {
            ((Map) newStorable).putAll((Map) storable);
        }
        newStorable.setName(storable.getName());
        newStorable.setDescription(storable.getDescription());
        if (newStorable instanceof LabeledStorable) {
            LabeledStorable labeledStorable = (LabeledStorable) newStorable;
            labeledStorable.getLabels().clear();
            if (storable instanceof LabeledStorable) {
                LabeledStorable provider = (LabeledStorable) storable;
                labeledStorable.getLabels().addAll(provider.getLabels());
            }
        }
        return newStorable;
    }

    private void copyReferenceAsNewEntity(PropertyDescriptor descriptor, Storable source, Storable dest,
                                          Storable entity, String prefix, String useCaseForTemplate)
            throws CopyException {
        try {
            Storable value = (Storable) descriptor.getReadMethod().invoke(source);
            if (value == null) {
                return;
            }
            Storable cachedCopiedEntity = OriginalCopyMap.getInstance().get(sessionId, value.getID());
            if (cachedCopiedEntity == null) {
                cachedCopiedEntity = createCopy(value, dest, prefix, false, useCaseForTemplate);
                cachedCopiedEntity.setParent(dest);
                cachedCopiedEntity.store();
                OriginalCopyMap.getInstance().put(sessionId, value.getID(), cachedCopiedEntity);
            }
            getSetOrFillMethod(descriptor, entity.getClass()).invoke(entity, cachedCopiedEntity);
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CopyException(String.format("Error while copying property %s from %s to %s",
                    descriptor.getName(), source, entity), e);
        }
    }

    private void forOperation_copyReferenceAsNewEntity(PropertyDescriptor descriptor, Storable source, Storable dest,
                                                       Storable entity, String prefix, String useCaseForTemplate)
            throws CopyException {
        try {
            Storable value = (Storable) descriptor.getReadMethod().invoke(source);
            if (value == null) {
                return;
            }
            Storable cachedCopiedEntity = OriginalCopyMap.getInstance().get(sessionId, value.getID());
            switch (useCaseForTemplate) {
                case "UC.3":
                case "UC.6": {
                    TransportConfiguration transportConfiguration = ((Operation) source.getParent().getParent())
                            .getTransport();
                    if (Mep.INBOUND_REQUEST_ASYNCHRONOUS.equals(transportConfiguration.getMep())) {
                        if (cachedCopiedEntity == null) {
                            cachedCopiedEntity = createCopy(value, dest.getParent().getParent(), prefix,
                                    false, useCaseForTemplate);
                            cachedCopiedEntity.store();
                            getSetOrFillMethod(descriptor, entity.getClass()).invoke(entity, cachedCopiedEntity);
                            OriginalCopyMap.getInstance().put(sessionId, value.getID(), cachedCopiedEntity);
                        }
                    } else {
                        getSetOrFillMethod(descriptor, entity.getClass()).invoke(entity, value);
                        break;
                    }
                    break;
                }
                default: {
                    getSetOrFillMethod(descriptor, entity.getClass()).invoke(entity, value);
                    break;
                }
            }
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new CopyException(String.format("Error while copying property %s from %s to %s",
                    descriptor.getName(), source, entity), e);
        }
    }

    private void doSpecificActions(PropertyDescriptor descriptor,
                                   Storable dest,
                                   Storable entity,
                                   String prefix,
                                   Storable cachedCopiedEntity,
                                   Storable value,
                                   Storable parent) throws Exception {
        if (cachedCopiedEntity == null) {
            cachedCopiedEntity = createCopy(value, dest, prefix, false, "");
            if (parent instanceof Operation) {
                cachedCopiedEntity.setParent(dest.getParent());
            } else if (parent instanceof System) {
                cachedCopiedEntity.setParent(dest.getParent().getParent());
            }
            cachedCopiedEntity.store();
            OriginalCopyMap.getInstance().put(sessionId, value.getID(), cachedCopiedEntity);
        }
        getSetOrFillMethod(descriptor, entity.getClass()).invoke(entity, cachedCopiedEntity);
    }

    private void forTemplate_copyReferenceAsNewEntity(PropertyDescriptor descriptor, Storable source,
                                                      Storable dest, Storable entity, String prefix,
                                                      String useCaseForTemplate) throws CopyException {
        try {
            Storable value = (Storable) descriptor.getReadMethod().invoke(source);
            if (value == null) {
                return;
            }
            Storable cachedCopiedEntity = OriginalCopyMap.getInstance().get(sessionId, value.getID());
            boolean copyFlag = getValueMapCopyFlag(sessionId);
            Storable parent = value.getParent();
            switch (useCaseForTemplate) {
                case "UC.1":
                    //UC1. Copy System
                case "UC.3":
                    //UC3. Copy Operation to another System
                case "UC.6": {
                    //UC6. Copy Situation to another Operation to another System
                    doSpecificActions(descriptor, dest, entity, prefix, cachedCopiedEntity, value, parent);
                    break;
                }
                case "UC.2":
                    //UC2. Copy Operation to the same System
                case "UC.5": {
                    //UC5. Copy Situation to another Operation in the same System
                    if (!copyFlag) {
                        if (parent instanceof Operation) {
                            if (cachedCopiedEntity == null) {
                                cachedCopiedEntity = createCopy(value, dest, prefix, false, "");
                                cachedCopiedEntity.setParent(dest.getParent());
                                cachedCopiedEntity.store();
                                OriginalCopyMap.getInstance().put(sessionId, value.getID(), cachedCopiedEntity);
                            }
                            getSetOrFillMethod(descriptor, entity.getClass()).invoke(entity, cachedCopiedEntity);
                        } else if (parent instanceof System) {
                            copyReferenceOrValue(descriptor, source, entity);
                        }
                    } else {
                        doSpecificActions(descriptor, dest, entity, prefix, cachedCopiedEntity, value, parent);
                    }
                    break;
                }
                case "UC.4": {
                    //UC4. Copy Situation to the same Operation
                    if (!copyFlag) {
                        copyReferenceOrValue(descriptor, source, entity);
                    } else {
                        doSpecificActions(descriptor, dest, entity, prefix, cachedCopiedEntity, value, parent);
                    }
                    break;
                }
                default:
                    log.error("Unknown copy/move use case {}!", useCaseForTemplate);
            }
        } catch (Exception e) {
            throw new CopyException(String.format("Error while copying property %s from %s to %s",
                    descriptor.getName(), source, entity), e);
        }
    }

    private void copyChild(PropertyDescriptor descriptor, Storable source, Storable dest,
                           String prefix, String useCaseForTemplate) throws CopyException {
        try {
            Storable child = (Storable) descriptor.getReadMethod().invoke(source);
            if (child != null) {
                Storable childCopy = createCopy(child, dest, prefix, false, useCaseForTemplate);
                getSetOrFillMethod(descriptor, dest.getClass()).invoke(dest, childCopy);
                childCopy.setParent(dest);
            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CopyException(String.format("Error while copying property %s from %s to %s",
                    descriptor.getName(), source, dest), e);
        }
    }

    private void copyChildCollection(PropertyDescriptor descriptor, Storable source, Storable dest,
                                     String prefix, String useCaseForTemplate) throws CopyException {
        try {
            Collection<Storable> collection = (Collection<Storable>) descriptor.getReadMethod().invoke(source);
            Collection newCollection = produceNewCollection(collection);
            for (Storable storable : collection) {
                if (storable != null) {
                    Storable cachedCopiedEntity = OriginalCopyMap.getInstance().get(sessionId, storable.getID());
                    if (cachedCopiedEntity == null) {
                        cachedCopiedEntity = createCopy(storable, dest, prefix, false, useCaseForTemplate);
                        cachedCopiedEntity.setParent(dest);
                        OriginalCopyMap.getInstance().put(sessionId, storable.getID(), cachedCopiedEntity);
                    }
                    newCollection.add(cachedCopiedEntity);
                }
            }
            getSetOrFillMethod(descriptor, dest.getClass()).invoke(dest, newCollection);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("Exception while child collection {} copying from {} to {}",
                    descriptor.getName(), source, dest, e);
        }
    }

    private String getUseCase(Storable storable, Storable destination, boolean sameParent) {
        String useCaseForTemplate = "";
        if (storable instanceof System) {
            useCaseForTemplate = USECASE1;
        } else if (storable instanceof Operation) {
            useCaseForTemplate = sameParent ? USECASE2 : USECASE3;
        } else if (storable instanceof Situation) {
            //sameParentSystem - flag for copying situations ("UC.4, UC.5, UC.6")
            boolean sameParentSystem = Objects.equals(storable.getParent().getParent(), destination.getParent());
            useCaseForTemplate = sameParentSystem ? (sameParent ? USECASE4 : USECASE5) : USECASE6;
        }
        return useCaseForTemplate;
    }

    private void postActionsForUpdateParent(Storable newStorable, Storable destination, String useCase) {
        if (newStorable.getID() == null) {
            newStorable.store();
        }
        boolean copyFlag = getValueMapCopyFlag(sessionId);
        switch (useCase) {
            case USECASE1: {
                System copiedSystem = (System) newStorable;
                copiedSystem.getTransports().forEach(
                        transportConfiguration -> transportConfiguration.setParent(copiedSystem));
                for (Operation operation : copiedSystem.getOperations()) {
                    if (operation.getID() == null) {
                        operation.store();
                    }
                    TransportConfiguration transport = getTransport(operation, copiedSystem);
                    for (Situation situation : operation.getSituations()) {
                        IntegrationStep integrationStep = situation.getIntegrationStep();
                        if (integrationStep != null) {
                            integrationStep.setSender(copiedSystem);
                            if (transport != null) {
                                if (Mep.INBOUND_REQUEST_ASYNCHRONOUS.equals(transport.getMep())) {
                                    Operation copyOperation = integrationStep.getOperation();
                                    if (copyOperation != null) {
                                        Storable cachedCopiedEntity = OriginalCopyMap.getInstance()
                                                .get(sessionId, copyOperation.getID());
                                        integrationStep.setOperation((Operation) cachedCopiedEntity);
                                    }
                                } else {
                                    integrationStep.setOperation(operation);
                                }
                            }
                            determineTemplateParent(integrationStep.returnStepTemplate(), operation, newStorable);
                        }
                    }
                    operation.getOperationTemplates().forEach(
                            operationTemplate -> operationTemplate.setParent(operation));
                    operation.getOperationParsingRules().forEach(
                            operationParsingRule -> operationParsingRule.setParent(operation));
                }
                copiedSystem.getSystemTemplates().forEach(systemTemplate -> systemTemplate.setParent(copiedSystem));
                copiedSystem.getSystemParsingRules().forEach(
                        systemParsingRule -> systemParsingRule.setParent(copiedSystem));
                break;
            }
            case USECASE2: {
                System system = (System) destination;
                Operation operation = (Operation) newStorable;
                TransportConfiguration transport = getTransport(operation, system);
                for (Situation situation : operation.getSituations()) {
                    IntegrationStep integrationStep = situation.getIntegrationStep();
                    if (integrationStep != null) {
                        if (transport != null && !Mep.INBOUND_REQUEST_ASYNCHRONOUS.equals(transport.getMep())) {
                            integrationStep.setOperation(operation);
                        }
                        determineTemplateParent(integrationStep.returnStepTemplate(), operation, system, copyFlag);
                    }
                }
                operation.getOperationTemplates().forEach(operationTemplate -> operationTemplate.setParent(operation));
                operation.getOperationParsingRules().forEach(
                        operationParsingRule -> operationParsingRule.setParent(operation));
                break;
            }
            case USECASE3: {
                System system = (System) destination;
                Operation operation = (Operation) newStorable;
                TransportConfiguration transport = getTransport(operation, system);
                if (transport != null) {
                    transport.setParent(system);
                    system.addTransport(transport);
                }
                for (Situation situation : operation.getSituations()) {
                    IntegrationStep integrationStep = situation.getIntegrationStep();
                    if (integrationStep != null) {
                        setReferencesForIntegrationStep(system, operation, transport, integrationStep);
                    }
                }
                operation.getOperationParsingRules().forEach(
                        operationParsingRule -> operationParsingRule.setParent(operation));
                operation.getOperationTemplates().forEach(operationTemplate -> operationTemplate.setParent(operation));
                break;
            }
            case USECASE4: {
                Storable system = destination.getParent();
                IntegrationStep integrationStep = ((Situation) newStorable).getIntegrationStep();
                if (copyFlag && integrationStep != null) {
                    determineTemplateParent(integrationStep.returnStepTemplate(), destination, system);
                }
                break;
            }
            case USECASE5: {
                Operation operation = (Operation) destination;
                System system = operation.getParent();
                IntegrationStep integrationStep = ((Situation) newStorable).getIntegrationStep();
                if (integrationStep != null) {
                    TransportConfiguration transport = getTransport(operation, system);
                    if (transport != null && !Mep.INBOUND_REQUEST_ASYNCHRONOUS.equals(transport.getMep())) {
                        integrationStep.setOperation(operation);
                    }
                    determineTemplateParent(integrationStep.returnStepTemplate(), operation, system, copyFlag);
                }
                break;
            }
            case USECASE6: {
                Operation operation = (Operation) destination;
                System system = operation.getParent();
                IntegrationStep integrationStep = ((Situation) newStorable).getIntegrationStep();
                if (integrationStep != null) {
                    TransportConfiguration transport = getTransport(operation, system);
                    setReferencesForIntegrationStep(system, operation, transport, integrationStep);
                }
                break;
            }
            default:
                log.error("");
        }
    }

    private void setReferencesForIntegrationStep(System system, Operation operation, TransportConfiguration transport,
                                                 IntegrationStep integrationStep) {
        integrationStep.setSender(system);
        setOperationToStepAccorgingToTransportMep(system, operation, transport, integrationStep);
        determineTemplateParent(integrationStep.returnStepTemplate(), operation, system);
    }

    private void setOperationToStepAccorgingToTransportMep(System system, Operation operation,
                                                           TransportConfiguration transport,
                                                           IntegrationStep integrationStep) {
        if (transport != null) {
            if (Mep.INBOUND_REQUEST_ASYNCHRONOUS.equals(transport.getMep())) {
                Operation copyOperationOnStep = integrationStep.getOperation();
                if (copyOperationOnStep != null) {
                    copyOperationOnStep.setParent(system);
                    //added a copy of the operation under the system
                    system.addOperation(copyOperationOnStep);
                    TransportConfiguration copyTransport = copyOperationOnStep.getTransport();
                    copyTransport.setParent(system);
                    //added a copy of the transport under the system
                    system.addTransport(copyTransport);
                    //We go through the copied operation,
                    // take its situation and put the correct parent on the templates
                    for (Situation sit : copyOperationOnStep.getSituations()) {
                        IntegrationStep copyIntegrationStep = sit.getIntegrationStep();
                        if (copyIntegrationStep != null) {
                            copyIntegrationStep.setSender(system);
                            copyIntegrationStep.setOperation(copyOperationOnStep);
                            determineTemplateParent(copyIntegrationStep.returnStepTemplate(),
                                    copyOperationOnStep, system);
                        }
                        Set<OperationParsingRule> parsingRulesForSet = Sets.newHashSet();
                        for (OperationParsingRule parsingRule : copyOperationOnStep.getOperationParsingRules()) {
                            for (ParsingRule sitParsingRule : sit.getParsingRules()) {
                                if (parsingRule.getNaturalId() != null && sitParsingRule.getID().toString()
                                        .equals(parsingRule.getNaturalId().toString())) {
                                    parsingRulesForSet.add(parsingRule);
                                }
                            }
                        }
                        sit.setParsingRules(parsingRulesForSet);
                    }
                    copyOperationOnStep.getOperationParsingRules().forEach(
                            operationParsingRule -> operationParsingRule.setParent(copyOperationOnStep));
                    copyOperationOnStep.getOperationTemplates().forEach(
                            operationTemplate -> operationTemplate.setParent(copyOperationOnStep));
                }
            } else {
                integrationStep.setOperation(operation);
            }
        }
    }

    private void postActionsForUpdateTriggerOnSituation(Storable newStorable, String useCase) {
        if (USECASE1.equals(useCase)) {
            for (Operation operation : ((System) newStorable).getOperations()) {
                TransportConfiguration transportConfiguration = operation.getTransport();
                if (transportConfiguration == null) {
                    log.error("TransportConfiguration is null on operation: " + operation.getName() + ", system: "
                            + newStorable.getName());
                    continue;
                }
                if (transportConfiguration.getMep().isOutbound()) {
                    for (Situation situation : operation.getSituations()) {
                        for (SituationEventTrigger situationEventTrigger : situation.getSituationEventTriggers()) {
                            // Check not fully configured trigger
                            if (situationEventTrigger.getSituation() != null) {
                                Situation situationOnTrigger = (Situation) OriginalCopyMap.getInstance()
                                        .get(sessionId, situationEventTrigger.getSituation().getID());
                                if (situationOnTrigger != null) {
                                    situationEventTrigger.setSituation(situationOnTrigger);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void determineTemplateParent(Template template, Storable operation, Storable system) {
        /*
            'Template == null' and even 'integrationStep == null' - these situations are valid.
            For example, it's asynchronous transport, or it's not fully configured situation yet.
            We should not report errors in that case.
            We simply continue processing.
         */
        if (template == null) {
            return;
        }
        fixTemplateParent(template, operation, system);
    }

    private void determineTemplateParent(Template template, Storable operation, Storable system, boolean copyFlag) {
        if (template == null) {
            return;
        }
        if (copyFlag) {
            fixTemplateParent(template, operation, system);
        } else {
            if (template.getParent() instanceof Operation) {
                template.setParent((Operation) operation);
            }
        }
    }

    private void fixTemplateParent(Template template, Storable operation, Storable system) {
        if (template.getParent() instanceof Operation) {
            template.setParent((Operation) operation);
        } else if (template.getParent() instanceof System) {
            template.setParent((System) system);
        }
    }

    private TransportConfiguration getTransport(Operation operation, System system) {
        TransportConfiguration transportConfiguration = operation.getTransport();
        if (transportConfiguration == null) {
            log.error("TransportConfiguration is null on operation: {}, system: {}", operation.getName(),
                    system.getName());
        }
        return transportConfiguration;
    }
}
