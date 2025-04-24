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

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.NotImplementedException;
import org.qubership.automation.itf.core.hibernate.spring.managers.base.AbstractObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.ByProject;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.LabeledObjectManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.NativeManager;
import org.qubership.automation.itf.core.hibernate.spring.managers.custom.SearchByProjectIdManager;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OperationTemplateRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OutboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.TemplateRepository;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.message.template.OperationTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.SystemTemplate;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.template.OutboundTemplateTransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.provider.TemplateProvider;

import com.google.common.collect.Sets;

public abstract class TemplateObjectManager<K extends TemplateProvider, T extends Template<K>>
        extends AbstractObjectManager<T, T>
        implements NativeManager<T>, LabeledObjectManager<T>, ByProject<T>, SearchByProjectIdManager<T> {

    private final TemplateRepository templateRepository;
    private final StepRepository stepRepository;
    private final OutboundTransportConfigurationRepository outboundTransportConfigurationRepository;

    /**
     * Constructor.
     */
    public TemplateObjectManager(Class<T> clazz,
                                 TemplateRepository<K, T> templateRepository,
                                 StepRepository stepRepository,
                                 OutboundTransportConfigurationRepository outboundTransportConfigurationRepository) {
        super(clazz, templateRepository);
        this.templateRepository = templateRepository;
        this.stepRepository = stepRepository;
        this.outboundTransportConfigurationRepository = outboundTransportConfigurationRepository;
    }

    @Override
    public void protectedOnRemove(T object) {
        if (object instanceof SystemTemplate) {
            stepRepository.onDeleteSystemTemplate(object);
        } else {
            stepRepository.onDeleteOperationTemplate(object);
        }
    }

    @Override
    public Collection<UsageInfo> findUsages(Storable storable) {
        Iterable<Step> all = storable instanceof SystemTemplate
                ? stepRepository.getIntegrationStepsBySystemTemplate((SystemTemplate)storable)
                : storable instanceof OperationTemplate
                ? stepRepository.getIntegrationStepsByOperationTemplate((OperationTemplate)storable)
                : new ArrayList<>();
        Collection<UsageInfo> result = Sets.newHashSet();
        addToUsages(result, "template", all);
        return result;
    }

    /**
     * Find usages of storable (template) by Id in diamerer transport configurations
     * under Env/Outbounds sections of the project (by projectId).
     */
    public List<Map<String, Object>> findUsagesOnOutboundDiameterConfiguration(Storable storable) {
        return templateRepository
                .findUsagesOnOutboundDiameterConfiguration((BigInteger) storable.getID(), storable.getProjectId());
    }

    @Override
    public Storable getChildByClass(T parent, Class childrenClass, Object... param) {
        throw new NotImplementedException("");
    }

    /**
     * Find child transport configurations under template,
     * with extra condition on transport type (if given).
     */
    @Override
    public Collection<? extends Storable> getChildrenByClass(T parent, Class childrenClass, Object... param) {
        if (childrenClass.getName().equals(OutboundTemplateTransportConfiguration.class.getName())) {
            if (param[0] != null) {
                return outboundTransportConfigurationRepository
                        .findCfgByTemplateAndType(toBigInt(parent.getID()), String.valueOf(param[0]));
            } else {
                return outboundTransportConfigurationRepository.findAllCfgByTemplate(toBigInt(parent.getID()));
            }
        }
        throw new NotImplementedException("Implemented only for "
                + OutboundTemplateTransportConfiguration.class.getName() + " class");
    }

    @Override
    public Collection<T> getAllByProject(Object projectId) {
        throw new NotImplementedException("Not implemented for project");
    }

    @Override
    public Collection<T> getByLabel(String label) {
        throw new NotImplementedException("Method getByLabel is not implemented");
    }

    @Override
    public Collection<T> getByLabel(String label, BigInteger projectId) {
        throw new NotImplementedException("Method getByLabel is not implemented");
    }

    @Override
    public Set<String> getAllLabels(BigInteger projectId) {
        return TxExecutor.executeUnchecked(() -> ((TemplateRepository<K, T>) repository).getAllLabels(projectId),
                TxExecutor.readOnlyTransaction());
    }

    @Override
    public List<?> getReceiverSystemsFromCallChainSteps(Object chainId) {
        throw new NotImplementedException("Method getReceiverSystemsFromCallChainSteps is not implemented");
    }

    @Override
    public List<T> getByNameAndProjectId(String name, BigInteger projectId) {
        return ((TemplateRepository<K,T>) repository).findByNameAndProjectId(name, toBigInt(projectId));
    }

    @Override
    public Collection<T> getByProjectId(BigInteger projectId) {
        return ((TemplateRepository<K,T>) repository).findByProjectId(toBigInt(projectId));
    }

    /**
     * Get Templates List (ids and names only) for the project.
     * findIdAndNameByProjectId method in the OperationTemplateRepository performs *native* query,
     * which select all templates (id, name) by the project_id.
     * So, we can't use constructor new IdNamePair(id, name) directly in the query.
     * And, we can't change the query from native to HQL, due to performance reasons.
     *
     * @param projectId - project id,
     * @return - list of templates ids and names.
     */
    public List<IdNamePair> getIdAndNameByProjectId(BigInteger projectId) {
        List<Object[]> list = ((OperationTemplateRepository) repository).findIdAndNameByProjectId(toBigInt(projectId));
        List<IdNamePair> result = new ArrayList<>();
        if (list != null) {
            for (Object[] item : list) {
                result.add(new IdNamePair(item[0], String.valueOf(item[1])));
            }
        }
        return result;
    }
}
