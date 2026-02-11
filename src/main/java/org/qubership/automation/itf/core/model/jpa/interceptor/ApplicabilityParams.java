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

package org.qubership.automation.itf.core.model.jpa.interceptor;

import java.math.BigInteger;
import java.util.Map;
import java.util.UUID;

import jakarta.persistence.Entity;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.jpa.transport.Configuration;
import org.qubership.automation.itf.core.util.constants.PropertyConstants;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ApplicabilityParams extends Configuration {
    private static final long serialVersionUID = 20240812L;

    @JsonBackReference
    public void setParent(Interceptor parent) {
        super.setParent(parent);
    }

    public ApplicabilityParams(Storable parent) {
        super();
        setParent(parent);
    }

    @Override
    public void performActionsForImportIntoAnotherProject(
            Map<BigInteger, BigInteger> replacementMap,
            BigInteger projectId, UUID projectUuid, boolean needToUpdateProjectId, boolean needToGenerateNewId) {
        /*
        TODO: it's not a good idea to call super here,
         because we don't need to update paths for applicability groups,
         but here were are ...
         */
        super.performActionsForImportIntoAnotherProject(
                replacementMap, projectId, projectUuid, needToUpdateProjectId, needToGenerateNewId
        );
        String applicabiltySystemId = get(PropertyConstants.Applicability.SYSTEM);
        if (StringUtils.isNotEmpty(applicabiltySystemId)) {
            BigInteger replacedSystemId = replacementMap.get(new BigInteger(applicabiltySystemId));
            if (replacedSystemId != null) {
                put(PropertyConstants.Applicability.SYSTEM, replacedSystemId.toString());
            }
        }
    }
}
