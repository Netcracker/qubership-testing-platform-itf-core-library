/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.automation.itf.core.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;

import liquibase.integration.spring.SpringLiquibase;

/**
 * Fails if {@code spring.liquibase.label-filter} or {@code spring.liquibase.contexts} stops
 * reaching the {@link SpringLiquibase} bean {@link SpringLiquibaseConfiguration} builds.
 */
class SpringLiquibaseConfigurationTest {

    private final SpringLiquibaseConfiguration configuration = new SpringLiquibaseConfiguration();

    @Test
    void labelFilterReachesTheBean() {
        LiquibaseProperties properties = new LiquibaseProperties();
        properties.setLabelFilter(List.of("!destructive", "v5"));

        SpringLiquibase bean = configuration.springLiquibaseWithDisabledMultiTenancy(mock(DataSource.class),
                properties);

        assertEquals("!destructive,v5", bean.getLabels());
    }

    @Test
    void contextsReachTheBean() {
        LiquibaseProperties properties = new LiquibaseProperties();
        properties.setContexts(List.of("ctx-a", "ctx-b"));

        SpringLiquibase bean = configuration.springLiquibaseWithDisabledMultiTenancy(mock(DataSource.class),
                properties);

        assertEquals("ctx-a,ctx-b", bean.getContexts());
    }

    @Test
    void noLabelFilterLeavesTheBeanUnset() {
        LiquibaseProperties properties = new LiquibaseProperties();

        SpringLiquibase bean = configuration.springLiquibaseWithDisabledMultiTenancy(mock(DataSource.class),
                properties);

        assertNull(bean.getLabels());
    }
}
