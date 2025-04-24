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
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.interceptor.ApplicabilityParams;
import org.qubership.automation.itf.core.model.jpa.interceptor.InterceptorParams;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.util.constants.PropertyConstants;
import org.qubership.automation.itf.core.util.loader.InterceptorClassLoader;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommonInterceptor extends AbstractStorable implements Storable, Interceptor {
    private static final long serialVersionUID = 20240812L;

    private List<InterceptorParams> interceptorParams = Lists.newArrayList();
    private String typeName;
    private String transportName;
    private boolean active = false;
    private int order;
    private String interceptorGroup;
    private List<ApplicabilityParams> applicabilityParams = Lists.newArrayList();

    /**
     * TODO: Add JavaDoc.
     */
    @JsonIgnore
    public InterceptorParams getParameters() {
        return interceptorParams.isEmpty() ? null : interceptorParams.get(0);
    }

    @JsonManagedReference
    public List<InterceptorParams> getInterceptorParams() {
        return interceptorParams;
    }

    @JsonManagedReference
    public List<ApplicabilityParams> getApplicabilityParams() {
        return applicabilityParams;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public boolean isApplicable(String environmentId, String systemId) {
        if (applicabilityParams.isEmpty()) {
            return true;
        }

        for (ApplicabilityParams params : applicabilityParams) {
            String applicableEnvId = params.get(PropertyConstants.Applicability.ENVIRONMENT);
            String applicableSystemId = params.get(PropertyConstants.Applicability.SYSTEM);

            if (StringUtils.isEmpty(applicableEnvId) && StringUtils.isEmpty(applicableSystemId)) {
                return false;
            }

            if (StringUtils.isEmpty(applicableSystemId)) {
                if (applicableEnvId.equals(environmentId)) {
                    return true;
                }
            } else {
                if (applicableEnvId.equals(environmentId) && applicableSystemId.equals(systemId)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public String validate() throws NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException {
        try {
            return InterceptorClassLoader.getInstance().getInstanceClass(getTypeName(), this).validate();
        } catch (ClassNotFoundException e) {
            throw new InvocationTargetException(e);
        }
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        getInterceptorParams().forEach(
                interceptor ->
                        interceptor.performActionsForImportIntoAnotherProject(
                                replacementMap, projectId,
                                projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
        getApplicabilityParams().forEach(
                applicabilityParam ->
                        applicabilityParam.performActionsForImportIntoAnotherProject(
                                replacementMap, projectId,
                                projectUuid, needToUpdateProjectId, needToGenerateNewId)
        );
    }
}
