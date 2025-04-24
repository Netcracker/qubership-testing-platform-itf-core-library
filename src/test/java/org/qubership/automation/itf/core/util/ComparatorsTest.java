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

package org.qubership.automation.itf.core.util;

import static org.testng.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.qubership.automation.itf.core.model.interceptor.CommonInterceptor;
import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.util.helper.Comparators;

public class ComparatorsTest {
    private List<Interceptor> interceptorList = new ArrayList<>();

    @Before
    public void setUp() throws Exception {
        interceptorList.clear();
        CommonInterceptor first = new CommonInterceptor();
        CommonInterceptor second = new CommonInterceptor();
        CommonInterceptor third = new CommonInterceptor();
        first.setName("First");
        first.setOrder(0);
        second.setName("Second");
        second.setOrder(1);
        third.setName("Third");
        third.setOrder(2);
        interceptorList.add(third);
        interceptorList.add(second);
        interceptorList.add(first);

    }

    @Test
    public void testSortByOrder() throws Exception {
        interceptorList.sort(Comparators.INTERCEPTOR_COMPARATOR);
        assertEquals(0, interceptorList.get(0).getOrder());
        assertEquals("Second", interceptorList.get(1).getName());
        assertEquals(2, interceptorList.get(2).getOrder());
    }

    @Test
    public void testSortByName() throws Exception {
        interceptorList.sort(Comparators.NAME);
        assertEquals(0, interceptorList.get(0).getOrder());
        assertEquals("Second", interceptorList.get(1).getName());
        assertEquals(2, interceptorList.get(2).getOrder());
    }
}
