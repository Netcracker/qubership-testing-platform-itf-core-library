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

package org.qubership.automation.itf.core.util.feign.http;

import javax.annotation.Nonnull;

import org.qubership.automation.itf.core.util.feign.FeignHttpClient;
import org.qubership.automation.itf.core.util.feign.impl.BvApiResourceFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.BvPublicApiResourceFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.BvTestCaseResourceFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsAttachmentFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsAttributeFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsDatasetFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsDatasetListFeignClient;
import org.qubership.automation.itf.core.util.feign.impl.DatasetsVisibilityAreaFeignClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class HttpClientFactory implements EnvironmentAware {

    private static FeignHttpClient feignClient;
    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public static Environment env;

    @Autowired
    public void setFeignHttpClient(FeignHttpClient feignClient) {
        HttpClientFactory.feignClient = feignClient;
    }

    public static DatasetsAttachmentFeignClient getDatasetsAttachmentFeignClient() {
        return feignClient.getDatasetsAttachmentFeignClient();
    }

    public static DatasetsAttributeFeignClient getDatasetsAttributeFeignClient() {
        return feignClient.getDatasetsAttributeFeignClient();
    }

    public static DatasetsDatasetFeignClient getDatasetsDatasetFeignClient() {
        return feignClient.getDatasetsDatasetFeignClient();
    }

    public static DatasetsDatasetListFeignClient getDatasetsDatasetListFeignClient() {
        return feignClient.getDatasetsDatasetListFeignClient();
    }

    public static DatasetsVisibilityAreaFeignClient getDatasetsVisibilityAreaFeignClient() {
        return feignClient.getDatasetsVisibilityAreaFeignClient();
    }

    public static BvTestCaseResourceFeignClient getBvTestCaseResourceFeignClient() {
        return feignClient.getBvTestCaseResourceFeignClient();
    }

    public static BvPublicApiResourceFeignClient getBvPublicApiResourceFeignClient() {
        return feignClient.getBvPublicApiResourceFeignClient();
    }

    public static BvApiResourceFeignClient getBvApiResourceFeignClient() {
        return feignClient.getBvApiResourceFeignClient();
    }

    @Override
    public void setEnvironment(@Nonnull final Environment environment) {
        env = environment;
        eventIsReady();
    }

    private void eventIsReady() {
        HttpClientReadyEvent readyEvent = new HttpClientReadyEvent(this);
        eventPublisher.publishEvent(readyEvent);
    }
}
