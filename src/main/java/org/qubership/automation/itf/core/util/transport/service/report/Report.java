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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *  - Each report adapter should act alone, with no affect on execution and on other adapters,
 *      So, each adapter method invocation must be placed into separate try-catch block.
 *  - And, moreover, adapters' exceptions should not break normal execution,
 *      So, in catch blocks we must only log errors, never throw exceptions
 */
public class Report {
    public static final Logger LOGGER = LoggerFactory.getLogger(Report.class);

    /**
     * Perform 'openSection' method in all report adapters registered.
     */
    //TODO need refactoring and use thread local to avoid collisions - Please proof if this is still actual or not
    public static void openSection(AbstractInstance instance, String title) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.openSection(instance, title);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'openSection'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'closeSection' method in all report adapters registered.
     */
    public static void closeSection(AbstractInstance instance) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.closeSection(instance);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'closeSection'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'info' method in all report adapters registered.
     */
    public static void info(AbstractInstance instance, String title, SpContext spContext) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.info(instance, title, spContext);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'info'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'info' method in all report adapters registered.
     */
    public static void info(final AbstractInstance instance, final String title, final String message) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.info(instance, title, message);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'info'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'warn' method in all report adapters registered.
     */
    public static void warn(final AbstractInstance instance, final String title, final String message) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.warn(instance, title, message);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'warn'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'error' method in all report adapters registered.
     */
    public static void error(AbstractInstance instance, String title, SpContext spContext, Throwable exception) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.error(instance, title, spContext, exception);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'error'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'error' method in all report adapters registered.
     */
    public static void error(AbstractInstance instance, String title, String message, Throwable exception) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.error(instance, title, message, exception);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'error'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'terminated' method in all report adapters registered.
     */
    public static void terminated(AbstractInstance instance, String title, String message, Throwable exception) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.terminated(instance, title, message, exception);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'terminated'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'startRun' method in all report adapters registered.
     */
    public static void startRun(TcContext context) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(context)) {
                try {
                    reportAdapter.startRun(context);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'startRun'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'startAtpRun' method in all report adapters registered.
     */
    public static void startAtpRun(String testRunId) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            try {
                reportAdapter.startAtpRun(testRunId);
            } catch (Throwable ex) {
                LOGGER.error("Report Adapter {} Error while 'startAtpRun'", reportAdapter, ex);
            }
        }
    }

    /**
     * Perform 'stopRun' method in all report adapters registered.
     */
    public static void stopRun(InstanceContext context, Status status) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(context.tc())) {
                try {
                    reportAdapter.stopRun(context, status);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'stopRun'", reportAdapter, ex);
                }
            }
        }
    }

    /**
     * Perform 'stopAllRuns' method in all report adapters registered.
     */
    public static void stopAllRuns() {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            try {
                reportAdapter.stopAllRuns();
            } catch (Throwable ex) {
                LOGGER.error("Report Adapter {} Error while 'stopAllRuns'", reportAdapter, ex);
            }
        }
    }

    /**
     * Perform 'reportCallChainInfo' method in all report adapters registered.
     */
    public static void reportCallChainInfo(CallChainInstance instance) {
        for (ReportAdapter reportAdapter : ReportAdapterStorage.getInstance().getAdapters()) {
            if (reportAdapter.needToReport(instance.getContext().tc())) {
                try {
                    reportAdapter.reportCallChainInfo(instance);
                } catch (Throwable ex) {
                    LOGGER.error("Report Adapter {} Error while 'reportCallChainInfo'", reportAdapter, ex);
                }
            }
        }
    }
}
