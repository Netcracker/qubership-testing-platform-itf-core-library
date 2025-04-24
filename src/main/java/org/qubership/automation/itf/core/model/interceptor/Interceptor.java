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

package org.qubership.automation.itf.core.model.interceptor;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.interceptor.ApplicabilityParams;
import org.qubership.automation.itf.core.model.jpa.interceptor.InterceptorParams;

public interface Interceptor extends Storable {
    String getTypeName();

    void setTypeName(String typeName);

    int getOrder();

    void setOrder(int order);

    boolean isActive();

    void setActive(boolean active);

    String getTransportName();

    void setTransportName(String transportName);

    InterceptorParams getParameters();

    List<InterceptorParams> getInterceptorParams();

    void setInterceptorParams(List<InterceptorParams> interceptorParams);

    String getInterceptorGroup();

    void setInterceptorGroup(String interceptorGroup);

    List<ApplicabilityParams> getApplicabilityParams();

    void setApplicabilityParams(List<ApplicabilityParams> applicabilityParams);

    boolean isApplicable(String environmentId, String systemId);

    String validate() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException;
}
