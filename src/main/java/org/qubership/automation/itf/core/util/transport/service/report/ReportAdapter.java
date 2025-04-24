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

import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.model.jpa.context.SpContext;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractInstance;
import org.qubership.automation.itf.core.model.jpa.instance.chain.CallChainInstance;
import org.qubership.automation.itf.core.util.constants.Status;

public interface ReportAdapter {

    void openSection(final AbstractInstance instance, final String title);

    void closeSection(final AbstractInstance containerInstance);

    void info(final AbstractInstance containerInstance, final String title, final SpContext spContext);

    void info(final AbstractInstance containerInstance, final String title, final String message);

    void warn(final AbstractInstance containerInstance, final String title, final String message);

    void error(final AbstractInstance containerInstance, final String title,
               final SpContext spContext, final Throwable exception);

    void error(final AbstractInstance containerInstance, final String title,
               final String message, final Throwable exception);

    void terminated(final AbstractInstance containerInstance, final String title,
                    final String message, final Throwable exception);

    void startRun(TcContext context);

    void startAtpRun(String testRunId);

    void stopRun(InstanceContext context, Status status);

    void stopAllRuns();

    void terminate();

    void reportCallChainInfo(CallChainInstance instance);

    boolean needToReport(TcContext context);
}

