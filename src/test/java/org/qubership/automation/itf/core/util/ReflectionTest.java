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

import org.qubership.automation.itf.core.util.helper.Reflection;
import org.junit.Test;
import org.springframework.util.StopWatch;

import static org.junit.Assert.assertTrue;

public class ReflectionTest {
    private static StopWatch watch = new StopWatch();

    @Test
    public void testSplitProviderURLNewLine() {
        watch.start("ReflectionTest.testSplitProviderURLNewLine");
        assertTrue(Reflection.toCollection(String.class, "key\nvalue").size() == 2);
        //System.out.println(watch.);
        watch.stop();
       System.out.println(watch.prettyPrint());
    }

    @Test
    public void testSplitProviderURLDot() {
        assertTrue(Reflection.toCollection(String.class, "\nkey;value\n").size() == 2);
    }

    @Test
    public void testSplitProviderURLMixed() {
        assertTrue(Reflection.toCollection(String.class, "\nkey;value\nsome\n").size() == 3);
    }

}
