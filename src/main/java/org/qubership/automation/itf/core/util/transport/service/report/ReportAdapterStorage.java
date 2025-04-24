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

package org.qubership.automation.itf.core.util.transport.service.report;

import java.util.Collections;
import java.util.List;

import org.qubership.automation.itf.core.util.helper.Reflection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class ReportAdapterStorage {
    private static final ReportAdapterStorage INSTANCE = new ReportAdapterStorage();
    private static final List<ReportAdapter> STORAGE = Lists.newArrayListWithExpectedSize(5);
    public static final Logger LOGGER = LoggerFactory.getLogger(ReportAdapterStorage.class);

    public static ReportAdapterStorage getInstance() {
        return INSTANCE;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void init() {
        for (Class<? extends ReportAdapter> clazz : Reflection.getReflections().getSubTypesOf(ReportAdapter.class)) {
            try {
                STORAGE.add(clazz.newInstance());
            } catch (Exception e) {
                LOGGER.error("Failed registration of adapter", e);
            }
        }
    }

    public List<ReportAdapter> getAdapters() {
        return Collections.unmodifiableList(STORAGE);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void terminateAll() {
        for (ReportAdapter adapter : STORAGE) {
            adapter.terminate();
        }
    }
}

