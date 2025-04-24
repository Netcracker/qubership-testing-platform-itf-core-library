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

package org.qubership.automation.itf.core.extension;

import org.junit.Assert;
import org.junit.Test;
import org.qubership.automation.itf.core.model.extension.Extension;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.util.manager.ExtensionManager;

public class ExtensionManagerTest {

    @Test
    public void extend() {
        TcContext contextToExtend = new TcContext();
        ExtensionManager.getInstance().extend(contextToExtend, new TestExtension());
        TestExtension extension = ExtensionManager.getInstance().getExtension(contextToExtend, TestExtension.class);
        Assert.assertNotNull(extension);
        contextToExtend.put("aaa", "bbb");
        Assert.assertEquals("bbb", contextToExtend.get("aaa"));
    }

    private static class TestExtension implements Extension {
    }

}
