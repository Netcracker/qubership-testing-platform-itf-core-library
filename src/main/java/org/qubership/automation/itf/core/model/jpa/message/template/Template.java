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

import jakarta.annotation.Nonnull;
import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Identified;
import org.qubership.automation.itf.core.model.common.Labeled;
import org.qubership.automation.itf.core.model.common.Named;
import org.qubership.automation.itf.core.model.common.OptimisticLockable;
import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.jpa.template.OutboundTemplateTransportConfiguration;
import org.qubership.automation.itf.core.util.provider.TemplateProvider;

@Entity
public interface Template<T extends TemplateProvider>
        extends Labeled, Named, Identified<BigInteger>, OptimisticLockable<Object> {
    OutboundTemplateTransportConfiguration getTransportProperties(@Nonnull String typeName);

    Collection<OutboundTemplateTransportConfiguration> getTransportProperties();

    String getText();

    Map<String, String> getHeaders();

    List<Interceptor> getInterceptors();

    Set<String> getCompatibleWithTransports();

    BigInteger getProjectId();

    T getParent();

    void setTransportProperties(Collection<OutboundTemplateTransportConfiguration> transportProperties);

    void setText(String text);

    void setHeaders(Map<String, String> headers);

    void setInterceptors(List<Interceptor> interceptors);

    void setCompatibleWithTransports(Set<String> compatibleWithTransports);

    void setProjectId(BigInteger projectId);

    void setParent(T parent);

    void fillHeaders(Map<String, String> headers);

    void fillTransportProperties(Collection<OutboundTemplateTransportConfiguration> transportProperties);

}
