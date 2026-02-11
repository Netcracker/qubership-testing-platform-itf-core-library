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

package org.qubership.automation.itf.core.model.jpa.message.template;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.common.LabeledStorable;
import org.qubership.automation.itf.core.model.common.Named;
import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.jpa.interceptor.TemplateInterceptor;
import org.qubership.automation.itf.core.model.jpa.template.OutboundTemplateTransportConfiguration;
import org.qubership.automation.itf.core.util.annotation.ProduceNewObject;
import org.qubership.automation.itf.core.util.helper.StorableUtils;
import org.qubership.automation.itf.core.util.holder.ActiveInterceptorHolder;
import org.qubership.automation.itf.core.util.provider.InterceptorProvider;
import org.qubership.automation.itf.core.util.provider.TemplateProvider;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public abstract class AbstractTemplate<T extends TemplateProvider>
        extends LabeledStorable implements Template<T>, Named, InterceptorProvider {

    private String text;

    private Object contentFile;

    private Map<String, String> headers = Maps.newHashMapWithExpectedSize(5);
    private Collection<OutboundTemplateTransportConfiguration> transportProperties = Lists.newArrayList();
    private Set<String> compatibleWithTransports = Sets.newHashSetWithExpectedSize(2);
    private List<Interceptor> interceptors = Lists.newLinkedList();
    private BigInteger projectId;
    private T parent;

    /**
     * TODO: Add JavaDoc.
     */
    public AbstractTemplate(T parent) {
        if (parent != null) {
            setName("New Template");
            parent.returnTemplates().add(this);
            setParent(parent);
            setProjectId(parent.getProjectId());
        }
    }

    @JsonManagedReference
    public Collection<OutboundTemplateTransportConfiguration> getTransportProperties() {
        return transportProperties;
    }

    @JsonIgnore
    public @Nullable OutboundTemplateTransportConfiguration getTransportProperties(@Nonnull String typeName) {
        return Iterables.tryFind(this.getTransportProperties(), input -> typeName.equals(input.getTypeName())).orNull();
    }

    @JsonSerialize(contentAs = TemplateInterceptor.class)
    @JsonManagedReference
    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    @JsonDeserialize(contentAs = TemplateInterceptor.class)
    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public void setText(String text) {
        this.text = text != null ? text : StringUtils.EMPTY;
    }

    @Override
    @ProduceNewObject
    public BigInteger getNaturalId() {
        return super.getNaturalId();
    }

    public void fillHeaders(Map<String, String> headers) {
        StorableUtils.fillMap(getHeaders(), headers);
    }

    public void fillTransportProperties(Collection<OutboundTemplateTransportConfiguration> transportProperties) {
        StorableUtils.fillCollection(getTransportProperties(), transportProperties);
    }

    public void fillCompatibleWithTransports(Set<String> compatibleWithTransports) {
        StorableUtils.fillCollection(getCompatibleWithTransports(), compatibleWithTransports);
    }

    @Override
    public void performPostCopyActions(boolean statusOff) {
        if (statusOff) {
            for (Interceptor interceptor : interceptors) {
                interceptor.setActive(false);
            }
        } else {
            ActiveInterceptorHolder.getInstance().updateActiveInterceptorHolder(getID(), interceptors);
        }
    }

    @Override
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        getTransportProperties().forEach(
                transportProperty ->
                        transportProperty.performActionsForImportIntoAnotherProject(
                                replacementMap, projectId,
                                projectUuid, needToUpdateProjectId,
                                needToGenerateNewId)
        );
        getInterceptors().forEach(
                interceptor -> interceptor.performActionsForImportIntoAnotherProject(
                        replacementMap, projectId,
                        projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
    }
}
