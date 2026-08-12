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

import java.util.ArrayList;
import java.util.List;

import jakarta.annotation.Nonnull;

import org.qubership.automation.itf.core.model.jpa.interceptor.InterceptorParams;
import org.qubership.automation.itf.core.model.jpa.message.Message;

public class InterceptorChain {
    private List<Interceptor> interceptors = new ArrayList<>(10);

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    public void setInterceptors(List<Interceptor> interceptors) {
        this.interceptors = interceptors;
    }

    public void add(Interceptor interceptor) {
        interceptors.add(interceptor);
    }

    public void remove(Interceptor interceptor) {
        interceptors.remove(interceptor);
    }

    /**
     * You can find usage in test class 'InterceptorChainTest'.
     *
     * @param message which will be modified by all of interceptors from collection {@link #interceptors}
     * @return any data which modified by interceptors
     */
    @Nonnull
    public Message apply(@Nonnull Message message) throws Exception {
        Message data = message;
        for (Interceptor interceptor : interceptors) {
            if (interceptor.isActive()) {
                TransportInterceptor transportInterceptor =
                        (TransportInterceptor) Class.forName(interceptor.getTypeName())
                                .getConstructor(InterceptorParams.class).newInstance(interceptor);
                data = transportInterceptor.apply(data);
            }
        }
        return data;
    }
}
