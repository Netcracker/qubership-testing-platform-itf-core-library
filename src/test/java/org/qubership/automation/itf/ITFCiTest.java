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

package org.qubership.automation.itf;

import org.qubership.automation.itf.core.hibernate.spring.managers.custom.MonitoringManager;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.util.StopWatch;

import javax.persistence.EntityManagerFactory;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.Map;

@Transactional
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath*:ta-prod-hibernate-configuration-context.xml"})
public class ITFCiTest {
    @Autowired
    private EntityManagerFactory emf;

    private MonitoringManager manager;

    @Before
    public void setUp() throws Exception {
        manager = CoreObjectManager.getInstance()
                .getSpecialManager(InstanceContext.class, MonitoringManager.class);
    }

    @Test
    public void testLoadMonitoringMessages() {
//        LoggerFactory.getLogger(ITFCiTest.class).info(manager.getInstanceContextId("400000396").toString());
        StopWatch stopWatch = new StopWatch();
        System.out.println("Start getting data from database");
        stopWatch.start();
        HashMap<String,Object> messageParameters = manager.getSpMessageParameters("363891874",1);
        for (Map.Entry<String, Object> parameter : messageParameters.entrySet()) {
            String singleValue = parameter.getKey();
        }
        stopWatch.stop();
        System.out.println(stopWatch);
        for (Map.Entry<String, Object> parameter : messageParameters.entrySet()) {
            System.out.printf("Param: %s, Value: %s", parameter.getKey(), parameter.getValue());
        }
    }
}
