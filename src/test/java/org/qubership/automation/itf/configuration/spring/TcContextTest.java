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

package org.qubership.automation.itf.configuration.spring;

import org.qubership.automation.itf.core.model.jpa.context.JsonContext;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;

import org.json.simple.JSONArray;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.transaction.Transactional;

import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.create;
import static org.qubership.automation.itf.configuration.spring.ObjectManagerUtils.renameStoreValidate;
import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertTrue;

@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:hibernate-configuration-test-context.xml"})
public class TcContextTest {

//    @Autowired
//    private TCContextObjectManager contextManager;


//    @Test
//    public void workedCacheInObjectManager() throws Exception {
//        TCContext tcContext = create(TCContext.class);
//        Environment environment = create(Environment.class);
//        tcContext.setEnvironment(environment);
//        tcContext.start();
//        Thread.sleep(3000);
//        tcContext.finish();
//        InstanceContext instanceContext = CoreObjectManager.managerFor(InstanceContext.class).create();
//        instanceContext.setTC(tcContext);
//        SPContext sp = new SPContext();
//        sp.setIncomingMessage(new Message("123"));
//        sp.setOutgoingMessage(new Message("321"));
//        instanceContext.setSP(sp);
//        instanceContext.store();
//        Object instanceContextID = instanceContext.getID();
//        TxExecutor.execute(() -> {
//            InstanceContext instContext = CoreObjectManager.managerFor(InstanceContext.class).getById(instanceContextID);
//            TCContext tc = instContext.getTC();
//            assertNotNull(tc.getEnvironment());
//            return null;
//        });
//    }

    @Test
    public void workedRenameFind() {
        TcContext tcContext = create(TcContext.class);
        renameStoreValidate(tcContext);
    }

    @Test //NITP-3919
    public void testContextSaveOrder() throws Exception {
        TcContext context = new TcContext();
        context.put("portnumber[0]", 23);
        context.put("portnumber[1]", 24);
        context.put("portnumber[2]", 25);
        context.put("portnumber[3]", 26);
        Object portnumber = context.get("portnumber");
        assertTrue(portnumber instanceof JSONArray); //JSONArray is order safe
    }

    @Test //NITP-4147
    public void testIsContextContainsKey() throws Exception {
        TcContext context = new TcContext();
        context.put("group", new JsonContext());
        String value = "value";
        context.get("group", JsonContext.class).put("param", value);
        assertEquals(value, context.get("group.param"));
        assertTrue(context.containsKey("group.param"));
    }
}
