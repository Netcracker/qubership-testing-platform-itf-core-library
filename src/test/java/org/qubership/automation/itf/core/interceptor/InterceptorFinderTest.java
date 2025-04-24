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

package org.qubership.automation.itf.core.interceptor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Set;

import org.junit.Test;
import org.qubership.automation.itf.core.model.interceptor.CommonInterceptor;
import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.config.Config;
import org.qubership.automation.itf.core.util.finder.InterceptorFinder;

public class InterceptorFinderTest {

    @Test
    public void testSimpleEmptyInterceptorIsFoundByInterceptorFinder() throws Exception {
        Set<Class<? extends Interceptor>> interceptorClasses = InterceptorFinder.find("org.qubership.automation.itf.core.interceptor");
        assertTrue(interceptorClasses.contains(SimpleEmptyInterceptor.class));
        assertEquals(1, interceptorClasses.size());
    }

    @Test
    public void testSimpleEmptyInterceptorIsFoundByInterceptorFinderFromConfig() throws Exception {
        Config.getConfig().addProperty("interceptor.path", "org.qubership.automation.itf.core.interceptor");
        Set<Class<? extends Interceptor>> interceptorClasses = InterceptorFinder.find();
        assertTrue(interceptorClasses.contains(SimpleEmptyInterceptor.class));
        assertEquals(1, interceptorClasses.size());
    }

    @Test
    public void testSimpleEmptyInterceptorIsNotFoundInInstancePackage() throws Exception {
        Set<Class<? extends Interceptor>> interceptorClasses = InterceptorFinder.find("org.qubership.automation.itf.core.integration");
        assertTrue(interceptorClasses.isEmpty());
    }

    private class SimpleEmptyInterceptor extends CommonInterceptor {
        public Message apply(Message data) {
            return data;
        }
    }
}
