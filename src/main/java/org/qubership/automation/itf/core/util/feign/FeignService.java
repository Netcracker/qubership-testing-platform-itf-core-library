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

package org.qubership.automation.itf.core.util.feign;

import org.qubership.automation.itf.core.util.feign.impl.BvApiResourceFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.BvPublicApiResourceFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.BvTestCaseResourceFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsAttachmentFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsAttributeFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsDatasetFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsDatasetListFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsVisibilityAreaFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.stereotype.Service;

import lombok.Getter;

@EnableFeignClients(basePackages = {"org.qubership.automation.itf.core.util.feign.impl"})
@Service
@Getter
public class FeignService implements FeignHttpClient {

    @Autowired
    private DatasetsAttachmentFeignClient datasetsAttachmentFeignClient;

    @Autowired
    private DatasetsAttributeFeignClient datasetsAttributeFeignClient;

    @Autowired
    private DatasetsDatasetFeignClient datasetsDatasetFeignClient;

    @Autowired
    private DatasetsDatasetListFeignClient datasetsDatasetListFeignClient;

    @Autowired
    private DatasetsVisibilityAreaFeignClient datasetsVisibilityAreaFeignClient;

    @Autowired
    private BvTestCaseResourceFeignClient bvTestCaseResourceFeignClient;

    @Autowired
    private BvPublicApiResourceFeignClient bvPublicApiResourceFeignClient;

    @Autowired
    private BvApiResourceFeignClient bvApiResourceFeignClient;
}
