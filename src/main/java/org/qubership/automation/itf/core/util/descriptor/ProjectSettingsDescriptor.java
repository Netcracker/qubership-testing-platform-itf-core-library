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

package org.qubership.automation.itf.core.util.descriptor;

import java.util.Map;

import org.qubership.automation.itf.core.util.annotation.DefaultValue;
import org.qubership.automation.itf.core.util.annotation.Options;
import org.qubership.automation.itf.core.util.annotation.Parameter;
import org.qubership.automation.itf.core.util.constants.ProjectSettingsCategory;
import org.qubership.automation.itf.core.util.constants.ProjectSettingsConstants;
import org.qubership.automation.itf.core.util.provider.PropertyProvider;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;

@Component
public class ProjectSettingsDescriptor implements PropertyProvider {

    @Parameter(shortName = ProjectSettingsConstants.ATP_WSDL_PATH,
            longName = ProjectSettingsConstants.ATP_WSDL_PATH_LONG_NAME,
            description = ProjectSettingsConstants.ATP_WSDL_PATH_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.ATP_WSDL_PATH_DEFAULT_VALUE)
    String atpWsdlPath;

    @Parameter(shortName = ProjectSettingsConstants.ATP_ACCOUNT_NAME,
            longName = ProjectSettingsConstants.ATP_ACCOUNT_NAME_LONG_NAME,
            description = ProjectSettingsConstants.ATP_ACCOUNT_NAME_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.ATP_ACCOUNT_NAME_DEFAULT_VALUE)
    String atpAccName;

    @Parameter(shortName = ProjectSettingsConstants.ATP_TEST_PLAN,
            longName = ProjectSettingsConstants.ATP_TEST_PLAN_LONG_NAME,
            description = ProjectSettingsConstants.ATP_TEST_PLAN_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.ATP_TEST_PLAN_DEFAULT_VALUE)
    String atpTestPlan;

    @Parameter(shortName = ProjectSettingsConstants.ATP_PROJECT_NAME,
            longName = ProjectSettingsConstants.ATP_PROJECT_NAME_LONG_NAME,
            description = ProjectSettingsConstants.ATP_PROJECT_NAME_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.ATP_PROJECT_NAME_DEFAULT_VALUE)
    String atpProjectName;

    @Parameter(shortName = ProjectSettingsConstants.ENABLE_REPORT_ADAPTER_ATP,
            longName = ProjectSettingsConstants.ENABLE_REPORT_ADAPTER_ATP_LONG_NAME,
            description = ProjectSettingsConstants.ENABLE_REPORT_ADAPTER_ATP_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.ENABLE_REPORT_ADAPTER_ATP_DEFAULT_VALUE)
    String reportAdapterAtp;

    @Parameter(shortName = ProjectSettingsConstants.ENABLE_REPORT_ADAPTER_ATP2,
            longName = ProjectSettingsConstants.ENABLE_REPORT_ADAPTER_ATP2_LONG_NAME,
            description = ProjectSettingsConstants.ENABLE_REPORT_ADAPTER_ATP2_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_2_INTEGRATION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.ENABLE_REPORT_ADAPTER_ATP2_DEFAULT_VALUE)
    String reportAdapterAtp2;

    @Parameter(shortName = ProjectSettingsConstants.ENABLE_ITF_REPORTING,
            longName = ProjectSettingsConstants.ENABLE_ITF_REPORTING_LONG_NAME,
            description = ProjectSettingsConstants.ENABLE_ITF_REPORTING_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.INNER_REPORTING_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.ENABLE_ITF_REPORTING_DEFAULT_VALUE)
    String itfReportingOfCallchains;

    @Parameter(shortName = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_KEY,
            longName = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_KEY_LONG_NAME,
            description = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_KEY_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.INNER_REPORTING_SETTINGS)
    @DefaultValue(value = "tc.Keys.processId")
    String reportLinkKeyBasedLinkCollectorKey;

    @Parameter(shortName = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_VIEW_NAME,
            longName = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_VIEW_NAME_LONG_NAME,
            description = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_VIEW_NAME_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.INNER_REPORTING_SETTINGS)
    @DefaultValue(value = "Integration Sessions Log")
    String reportLinkKeyBasedLinkCollectorViewName;

    @Parameter(shortName = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_URL_FORMAT,
            longName = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_URL_FORMAT_LONG_NAME,
            description = ProjectSettingsConstants.REPORT_LINK_KEYBASEDLINKCOLLECTOR_URL_FORMAT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.INNER_REPORTING_SETTINGS)
    @DefaultValue(value = "/solutions/jumpToIntegrationSession.jsp?processId=%s")
    String reportLinkKeyBasedLinkCollectorUrlFormat;

    @Parameter(shortName = ProjectSettingsConstants.START_TRIGGERS_AT_STARTUP,
            longName = ProjectSettingsConstants.START_TRIGGERS_AT_STARTUP_SHORT_NAME,
            description = ProjectSettingsConstants.START_TRIGGERS_AT_STARTUP_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SERVICE_STARTUP_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_TRIGGERS_AT_STARTUP_DEFAULT_VALUE)
    String startTriggersAtStartup;

    @Parameter(shortName = ProjectSettingsConstants.START_TRANSPORT_TRIGGERS_AT_STARTUP,
            longName = ProjectSettingsConstants.START_TRANSPORT_TRIGGERS_AT_STARTUP_LONG_NAME,
            description = ProjectSettingsConstants.START_TRANSPORT_TRIGGERS_AT_STARTUP_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SERVICE_STARTUP_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_TRANSPORT_TRIGGERS_AT_STARTUP_DEFAULT_VALUE)
    String startTranspotrtTriggersAtStartup;

    @Parameter(shortName = ProjectSettingsConstants.MAX_CONNECTION_TIMEOUT,
            longName = ProjectSettingsConstants.MAX_CONNECTION_TIMEOUT_LONG_NAME,
            description = ProjectSettingsConstants.MAX_CONNECTION_TIMEOUT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SERVICE_STARTUP_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.MAX_CONNECTION_TIMEOUT_DEFAULT_VALUE)
    Integer maxConnectionTimeout;

    @Parameter(shortName = ProjectSettingsConstants.COPY_OBJECT_SET_STATUS_OFF,
            longName = ProjectSettingsConstants.COPY_OBJECT_SET_STATUS_OFF_LONG_NAME,
            description = ProjectSettingsConstants.COPY_OBJECT_SET_STATUS_OFF_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CONFIGURATION_INTERFACE_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.COPY_OBJECT_SET_STATUS_OFF_DEFAULT_VALUE)
    String statusOff;

    @Parameter(shortName = ProjectSettingsConstants.COPY_OBJECT_IS_SMART,
            longName = ProjectSettingsConstants.COPY_OBJECT_IS_SMART_LONG_NAME,
            description = ProjectSettingsConstants.COPY_OBJECT_IS_SMART_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CONFIGURATION_INTERFACE_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.COPY_OBJECT_IS_SMART_DEFAULT_VALUE)
    String isSmartCopy;

    @Parameter(shortName = ProjectSettingsConstants.SITUATION_IGNORE_ERRORS_ENABLED,
            longName = ProjectSettingsConstants.SITUATION_IGNORE_ERRORS_ENABLED_LONG_NAME,
            description = ProjectSettingsConstants.SITUATION_IGNORE_ERRORS_ENABLED_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.SITUATION_IGNORE_ERRORS_ENABLED_DEFAULT_VALUE)
    String situationIgnoreErrors;

    @Parameter(shortName = ProjectSettingsConstants.TC_TIMEOUT_FAIL,
            longName = ProjectSettingsConstants.TC_TIMEOUT_FAIL_LONG_NAME,
            description = ProjectSettingsConstants.TC_TIMEOUT_FAIL_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.TC_TIMEOUT_FAIL_DEFAULT_VALUE)
    Integer tcFailTimeout;

    @Parameter(shortName = ProjectSettingsConstants.TC_TIMEOUT_FAIL_TIME_UNIT,
            longName = ProjectSettingsConstants.TC_TIMEOUT_FAIL_TIME_UNIT_LONG_NAME,
            description = ProjectSettingsConstants.TC_TIMEOUT_FAIL_TIME_UNIT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @Options({"MILLISECONDS", "SECONDS", "MINUTES", "HOURS"})
    @DefaultValue(value = ProjectSettingsConstants.TC_TIMEOUT_FAIL_TIME_UNIT_DEFAULT_VALUE)
    String tcFailTimeoutUnit;

    @Parameter(shortName = ProjectSettingsConstants.MONITORING_PAGINATION_SIZE,
            longName = ProjectSettingsConstants.MONITORING_PAGINATION_SIZE_LONG_NAME,
            description = ProjectSettingsConstants.MONITORING_PAGINATION_SIZE_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_MONITORING_INTERFACE_SETTINGS)
    @Options({"10", "20", "50", "100"})
    @DefaultValue(value = ProjectSettingsConstants.MONITORING_PAGINATION_SIZE_DEFAULT_VALUE_STRING)
    String paginationSize;

    @Parameter(shortName = ProjectSettingsConstants.FOLDER_DELETE_NOT_EMPTY_ALLOW,
            longName = ProjectSettingsConstants.FOLDER_DELETE_NOT_EMPTY_ALLOW_LONG_NAME,
            description = ProjectSettingsConstants.FOLDER_DELETE_NOT_EMPTY_ALLOW_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CONFIGURATION_INTERFACE_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = "false")
    String isDeleteNotEmptyFolders;

    @Parameter(shortName = ProjectSettingsConstants.DATA_SET_SERVICE_DS_FORMAT,
            longName = ProjectSettingsConstants.DATA_SET_SERVICE_DS_FORMAT_LONG_NAME,
            description = ProjectSettingsConstants.DATA_SET_SERVICE_DS_FORMAT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_2_INTEGRATION_SETTINGS)
    @Options({"Itf", "Default", "Object", "ObjectExtended", "Optimized"})
    @DefaultValue(value = ProjectSettingsConstants.DATA_SET_SERVICE_DS_FORMAT_DEFAULT_VALUE)
    String datasetServiceDatasetFormat;

    @Parameter(shortName = ProjectSettingsConstants.REPORT_EXECUTION_ENABLED,
            longName = ProjectSettingsConstants.REPORT_EXECUTION_ENABLED_LONG_NAME,
            description = ProjectSettingsConstants.REPORT_EXECUTION_ENABLED_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.INNER_REPORTING_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.REPORT_EXECUTION_ENABLED_DEFAULT_VALUE)
    String isReportExecutionEnabled;

    @Parameter(shortName = ProjectSettingsConstants.BV_DEFAULT_ACTION,
            longName = ProjectSettingsConstants.BV_DEFAULT_ACTION_LONG_NAME,
            description = ProjectSettingsConstants.BV_DEFAULT_ACTION_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_2_INTEGRATION_SETTINGS)
    @Options({ProjectSettingsConstants.BV_DEFAULT_ACTION_OPTIONS_CREATENEWTESTRUN_VALUE,
            ProjectSettingsConstants.BV_DEFAULT_ACTION_OPTIONS_READCOMPARE_VALUE})
    @DefaultValue(value = ProjectSettingsConstants.BV_DEFAULT_ACTION_DEFAULT_VALUE)
    String bvDefaultAction;

    @Parameter(shortName = ProjectSettingsConstants.REPORT_TO_ATP_ENABLED,
            longName = ProjectSettingsConstants.REPORT_TO_ATP_ENABLED_LONG_NAME,
            description = ProjectSettingsConstants.REPORT_TO_ATP_ENABLED_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.REPORT_TO_ATP_ENABLED_DEFAULT_VALUE)
    String isReportToAtpEnabled;

    @Parameter(shortName = ProjectSettingsConstants.REPORT_TO_ATP_DEFAULT_CONTEXT_ENABLED,
            longName = ProjectSettingsConstants.REPORT_TO_ATP_DEFAULT_CONTEXT_ENABLED_LONG_NAME,
            description = ProjectSettingsConstants.REPORT_TO_ATP_DEFAULT_CONTEXT_ENABLED_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.REPORT_TO_ATP_DEFAULT_CONTEXT_ENABLED_DEFAULT_VALUE)
    String isAtpDefaultContextEnabled;

    @Parameter(shortName = ProjectSettingsConstants.TCPDUMP_PACKET_COUNT_DEFAULT,
            longName = ProjectSettingsConstants.TCPDUMP_PACKET_COUNT_DEFAULT_LONG_NAME,
            description = ProjectSettingsConstants.TCPDUMP_PACKET_COUNT_DEFAULT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.TCPDUMP_PACKET_COUNT_DEFAULT_DEFAULT_VALUE)
    Integer packetCount;

    @Parameter(shortName = ProjectSettingsConstants.TCPDUMP_CAPTURING_FILTER_DEFAULT,
            longName = ProjectSettingsConstants.TCPDUMP_CAPTURING_FILTER_DEFAULT_LONG_NAME,
            description = ProjectSettingsConstants.TCPDUMP_CAPTURING_FILTER_DEFAULT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.TCPDUMP_CAPTURING_FILTER_DEFAULT_DEFAULT_VALUE)
    Integer capturingFilter;

    @Parameter(shortName = ProjectSettingsConstants.TCP_DUMP_NI_DEFAULT,
            longName = ProjectSettingsConstants.TCP_DUMP_NI_DEFAULT_LONG_NAME,
            description = ProjectSettingsConstants.TCP_DUMP_NI_DEFAULT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.TCP_DUMP_NI_DEFAULT_VALUE)
    String ni;

    @Parameter(shortName = ProjectSettingsConstants.START_PARAM_RUN_BV_CASE,
            longName = ProjectSettingsConstants.START_PARAM_RUN_BV_CASE_LONG_NAME,
            description = ProjectSettingsConstants.START_PARAM_RUN_BV_CASE_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CALLCHAIN_RUN_WINDOW_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_PARAM_RUN_BV_CASE_DEFAULT_VALUE)
    String isRunBvCase;

    @Parameter(shortName = ProjectSettingsConstants.START_PARAM_SVT,
            longName = ProjectSettingsConstants.START_PARAM_SVT_LONG_NAME,
            description = ProjectSettingsConstants.START_PARAM_SVT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CALLCHAIN_RUN_WINDOW_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_PARAM_SVT_DEFAULT_VALUE)
    String isSvt;

    @Parameter(shortName = ProjectSettingsConstants.START_PARAM_COLLECT_LOGS,
            longName = ProjectSettingsConstants.START_PARAM_COLLECT_LOGS_LONG_NAME,
            description = ProjectSettingsConstants.START_PARAM_COLLECT_LOGS_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CALLCHAIN_RUN_WINDOW_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_PARAM_COLLECT_LOGS_DEFAULT_VALUE)
    String isCollectLogs;

    @Parameter(shortName = ProjectSettingsConstants.START_PARAM_NEED_TO_LOG_IN_ATP,
            longName = ProjectSettingsConstants.START_PARAM_NEED_TO_LOG_IN_ATP_LONG_NAME,
            description = ProjectSettingsConstants.START_PARAM_NEED_TO_LOG_IN_ATP_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CALLCHAIN_RUN_WINDOW_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_PARAM_NEED_TO_LOG_IN_ATP_DEFAULT_VALUE)
    String isLogToAtp;

    @Parameter(shortName = ProjectSettingsConstants.START_PARAM_MAKE_DEFAULT_DATASET,
            longName = ProjectSettingsConstants.START_PARAM_MAKE_DEFAULT_DATASET_LONG_NAME,
            description = ProjectSettingsConstants.START_PARAM_MAKE_DEFAULT_DATASET_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CALLCHAIN_RUN_WINDOW_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_PARAM_MAKE_DEFAULT_DATASET_DEFAULT_VALUE)
    String isMakeDefaultDataset;

    @Parameter(shortName = ProjectSettingsConstants.START_PARAM_RUN_VALIDATION,
            longName = ProjectSettingsConstants.START_PARAM_RUN_VALIDATION_LONG_NAME,
            description = ProjectSettingsConstants.START_PARAM_RUN_VALIDATION_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CALLCHAIN_RUN_WINDOW_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_PARAM_RUN_VALIDATION_DEFAULT_VALUE)
    String isRunValidation;

    @Parameter(shortName = ProjectSettingsConstants.START_PARAM_TEST_DATA,
            longName = ProjectSettingsConstants.START_PARAM_TEST_DATA_LONG_NAME,
            description = ProjectSettingsConstants.START_PARAM_TEST_DATA_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CALLCHAIN_RUN_WINDOW_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_PARAM_TEST_DATA_DEFAULT_VALUE)
    String isRunTestData;

    @Parameter(shortName = ProjectSettingsConstants.START_PARAM_CREATE_TCP_DUMP,
            longName = ProjectSettingsConstants.START_PARAM_CREATE_TCP_DUMP_LONG_NAME,
            description = ProjectSettingsConstants.START_PARAM_CREATE_TCP_DUMP_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CALLCHAIN_RUN_WINDOW_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.START_PARAM_CREATE_TCP_DUMP_DEFAULT_VALUE)
    String isRunTcpDump;

    @Parameter(shortName = ProjectSettingsConstants.CONTEXT_FORMAT_PRETTY_PRINT,
            longName = ProjectSettingsConstants.CONTEXT_FORMAT_PRETTY_PRINT_LONG_NAME,
            description = ProjectSettingsConstants.CONTEXT_FORMAT_PRETTY_PRINT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_MONITORING_INTERFACE_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.CONTEXT_FORMAT_PRETTY_PRINT_DEFAULT_VALUE)
    String contextFormatPrettyPrint;

    @Parameter(shortName = ProjectSettingsConstants.CONTEXT_FORMAT_MESSAGE_TYPE,
            longName = ProjectSettingsConstants.CONTEXT_FORMAT_MESSAGE_TYPE_LONG_NAME,
            description = ProjectSettingsConstants.CONTEXT_FORMAT_MESSAGE_TYPE_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_MONITORING_INTERFACE_SETTINGS)
    @Options({"XML", "JSON"})
    @DefaultValue(value = ProjectSettingsConstants.CONTEXT_FORMAT_MESSAGE_TYPE_DEFAULT_VALUE)
    String contextFormatMessageType;

    @Parameter(shortName = ProjectSettingsConstants.CONTEXT_FORMAT_WORDWRAP,
            longName = ProjectSettingsConstants.CONTEXT_FORMAT_WORDWRAP_LONG_NAME,
            description = ProjectSettingsConstants.CONTEXT_FORMAT_WORDWRAP_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_MONITORING_INTERFACE_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.CONTEXT_FORMAT_WORDWRAP_DEFAULT_VALUE)
    String contextFormatWordwrap;

    @Parameter(shortName = ProjectSettingsConstants.CONTEXT_FORMAT_EXPAND_ALL,
            longName = ProjectSettingsConstants.CONTEXT_FORMAT_EXPAND_ALL_LONG_NAME,
            description = ProjectSettingsConstants.CONTEXT_FORMAT_EXPAND_ALL_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_MONITORING_INTERFACE_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.CONTEXT_FORMAT_EXPAND_ALL_DEFAULT_VALUE)
    String contextFormatExpandAll;

    @Parameter(shortName = ProjectSettingsConstants.CALL_CHAIN_PARALLEL_RUNNING_THREAD_COUNT,
            longName = ProjectSettingsConstants.CALL_CHAIN_PARALLEL_RUNNING_THREAD_COUNT_LONG_NAME,
            description = ProjectSettingsConstants.CALL_CHAIN_PARALLEL_RUNNING_THREAD_COUNT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_2_INTEGRATION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.CALL_CHAIN_PARALLEL_RUNNING_THREAD_COUNT_DEFAULT_VALUE)
    Integer callchainParallelThreads;

    @Parameter(shortName = ProjectSettingsConstants.EXPRESSION_VAR,
            longName = ProjectSettingsConstants.EXPRESSION_VAR_LONG_NAME,
            description = ProjectSettingsConstants.EXPRESSION_VAR_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.EXPRESSION_VAR_DEFAULT_VALUE)
    String expressionVar;

    @Parameter(shortName = ProjectSettingsConstants.TEST_SERVER_AVAILABILITY,
            longName = ProjectSettingsConstants.TEST_SERVER_AVAILABILITY_LONG_NAME,
            description = ProjectSettingsConstants.TEST_SERVER_AVAILABILITY_DECRIPTION,
            uiCategory = ProjectSettingsCategory.SERVICE_STARTUP_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.TEST_SERVER_AVAILABILITY_DEFAULT_VALUE)
    String testServerAvailability;

    @Parameter(shortName = ProjectSettingsConstants.MESSAGE_PRETTY_FORMAT,
            longName = ProjectSettingsConstants.MESSAGE_PRETTY_FORMAT_LONG_NAME,
            description = ProjectSettingsConstants.MESSAGE_PRETTY_FORMAT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.MESSAGE_PRETTY_FORMAT_DEFAULT_VALUE)
    String prettyFormat;

    @Parameter(shortName = ProjectSettingsConstants.LDAP_URL,
            longName = ProjectSettingsConstants.LDAP_URL_LONG_NAME,
            description = ProjectSettingsConstants.LDAP_URL_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SECURITY_SETTINGS)
    @DefaultValue(value = "ldap://domain-controller.our-company.com:389")
    String ldapUrl;

    @Parameter(shortName = ProjectSettingsConstants.LDAP_LOGIN_PREFIX,
            longName = ProjectSettingsConstants.LDAP_LOGIN_PREFIX_LONG_NAME,
            description = ProjectSettingsConstants.LDAP_LOGIN_PREFIX_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SECURITY_SETTINGS)
    @DefaultValue(value = "our-company.com")
    String ldapLoginPrefix;

    @Parameter(shortName = ProjectSettingsConstants.MANY_OBJECTS_UI_MODE,
            longName = ProjectSettingsConstants.MANY_OBJECTS_UI_MODE_LONG_NAME,
            description = ProjectSettingsConstants.MANY_OBJECTS_UI_MODE_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_CONFIGURATION_INTERFACE_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.MANY_OBJECTS_UI_MODE_DEFAULT_VALUE)
    String isManyObjects;

    @Parameter(shortName = ProjectSettingsConstants.CONDITIONS_STYLE_LEGACY,
            longName = ProjectSettingsConstants.CONDITIONS_STYLE_LEGACY_LONG_NAME,
            description = ProjectSettingsConstants.CONDITIONS_STYLE_LEGACY_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.CONDITIONS_STYLE_LEGACY_DEFAULT_VALUE)
    String conditionsStyleLegacy;

    @Parameter(shortName = ProjectSettingsConstants.ATP_REPORTING_WAIT_MAX,
            longName = ProjectSettingsConstants.ATP_REPORTING_WAIT_MAX_LONG_NAME,
            description = ProjectSettingsConstants.ATP_REPORTING_WAIT_MAX_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.ATP_REPORTING_WAIT_MAX_DEFAULT_VALUE)
    Integer atpReportingWaitMax;

    @Parameter(shortName = ProjectSettingsConstants.ATP_REPORTING_MODE,
            longName = ProjectSettingsConstants.ATP_REPORTING_MODE_LONG_NAME,
            description = ProjectSettingsConstants.ATP_REPORTING_MODE_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.ATP_1_INTEGRATION_SETTINGS)
    @Options({"sync", "async"})
    @DefaultValue(value = ProjectSettingsConstants.ATP_REPORTING_MODE_DEFAULT_VALUE)
    String atpReportingMode;

    @Parameter(shortName = ProjectSettingsConstants.REPORT_IN_DIFFERENT_THREAD,
            longName = ProjectSettingsConstants.REPORT_IN_DIFFERENT_THREAD_LONG_NAME,
            description = ProjectSettingsConstants.REPORT_IN_DIFFERENT_THREAD_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.INNER_REPORTING_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.REPORT_IN_DIFFERENT_THREAD_DEFAULT_VALUE)
    String reportInDifferentThread;

    @Parameter(shortName = ProjectSettingsConstants.REPORT_EXECUTION_SENDER_THREAD_POOL_SIZE,
            longName = ProjectSettingsConstants.REPORT_EXECUTION_SENDER_THREAD_POOL_SIZE_LONG_NAME,
            description = ProjectSettingsConstants.REPORT_EXECUTION_SENDER_THREAD_POOL_SIZE_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.INNER_REPORTING_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.REPORT_EXECUTION_SENDER_THREAD_POOL_SIZE_DEFAULT_VALUE)
    Integer reportExecutionSenderThreadPoolSize;

    @Parameter(shortName = ProjectSettingsConstants.SCHEDULED_CLEANUP_ENABLED,
            longName = ProjectSettingsConstants.SCHEDULED_CLEANUP_ENABLED_LONG_NAME,
            description = ProjectSettingsConstants.SCHEDULED_CLEANUP_ENABLED_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SCHEDULED_CLEANUP_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = "false")
    String scheduledCleanupEnabled;

    @Parameter(shortName = ProjectSettingsConstants.SCHEDULED_CLEANUP_DAYS_REMAINING,
            longName = ProjectSettingsConstants.SCHEDULED_CLEANUP_DAYS_REMAINING_LONG_NAME,
            description = ProjectSettingsConstants.SCHEDULED_CLEANUP_DAYS_REMAINING_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SCHEDULED_CLEANUP_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.SCHEDULED_CLEANUP_DAYS_REMAINING_DEFAULT_VALUE)
    Integer scheduledCleanupDaysRemaining;

    @Parameter(shortName = ProjectSettingsConstants.SCHEDULED_CLEANUP_HOURS_TO_DELETE,
            longName = ProjectSettingsConstants.SCHEDULED_CLEANUP_HOURS_TO_DELETE_LONG_NAME,
            description = ProjectSettingsConstants.SCHEDULED_CLEANUP_HOURS_TO_DELETE_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SCHEDULED_CLEANUP_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.SCHEDULED_CLEANUP_HOURS_TO_DELETE_DEFAULT_VALUE)
    Integer scheduledCleanupHoursToDelete;

    @Parameter(shortName = ProjectSettingsConstants.SCHEDULED_CLEANUP_INITIAL_DELAY_MINUTES,
            longName = ProjectSettingsConstants.SCHEDULED_CLEANUP_INITIAL_DELAY_MINUTES_LONG_NAME,
            description = ProjectSettingsConstants.SCHEDULED_CLEANUP_INITIAL_DELAY_MINUTES_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SCHEDULED_CLEANUP_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.SCHEDULED_CLEANUP_INITIAL_DELAY_MINUTES_DEFAULT_VALUE)
    Integer scheduledCleanupInitialDelayMinutes;

    @Parameter(shortName = ProjectSettingsConstants.SCHEDULED_CLEANUP_DELAY_MINUTES,
            longName = ProjectSettingsConstants.SCHEDULED_CLEANUP_DELAY_MINUTES_LONG_NAME,
            description = ProjectSettingsConstants.SCHEDULED_CLEANUP_DELAY_MINUTES_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SCHEDULED_CLEANUP_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.SCHEDULED_CLEANUP_DELAY_MINUTES_DEFAULT_VALUE)
    Integer scheduledCleanupDelayMinutes;

    @Parameter(shortName = ProjectSettingsConstants.TC_CONTEXT_CLIENT_ADDRESS,
            longName = ProjectSettingsConstants.TC_CONTEXT_CLIENT_ADDRESS_LONG_NAME,
            description = ProjectSettingsConstants.TC_CONTEXT_CLIENT_ADDRESS_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.UI_MONITORING_INTERFACE_SETTINGS)
    @DefaultValue(value = ProjectSettingsConstants.TC_CONTEXT_CLIENT_ADDRESS_DEFAULT_VALUE)
    String tcContextClientAddress;

    @Parameter(shortName = ProjectSettingsConstants.ENABLE_HISTORY_AND_VERSIONING,
            longName = ProjectSettingsConstants.ENABLE_HISTORY_AND_VERSIONING_LONG_NAME,
            description = ProjectSettingsConstants.ENABLE_HISTORY_AND_VERSIONING_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.SERVICE_STARTUP_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.ENABLE_HISTORY_AND_VERSIONING_DEFAULT_VALUE)
    String enableHistoryAndVersioning;

    @Parameter(shortName = ProjectSettingsConstants.ENABLE_FAST_STUBS,
            longName = ProjectSettingsConstants.ENABLE_FAST_STUBS_LONG_NAME,
            description = ProjectSettingsConstants.ENABLE_FAST_STUBS_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.ENABLE_FAST_STUBS_DEFAULT_VALUE)
    String enableFastStubs;

    @Parameter(shortName = ProjectSettingsConstants.ENABLE_EVENT_TRIGGERS_ACTIVATION_AFTER_IMPORT,
            longName = ProjectSettingsConstants.ENABLE_EVENT_TRIGGERS_ACTIVATION_AFTER_IMPORT_LONG_NAME,
            description = ProjectSettingsConstants.ENABLE_EVENT_TRIGGERS_ACTIVATION_AFTER_IMPORT_DESCRIPTION,
            uiCategory = ProjectSettingsCategory.EXECUTION_SETTINGS)
    @Options({"true", "false"})
    @DefaultValue(value = ProjectSettingsConstants.ENABLE_EVENT_TRIGGERS_ACTIVATION_AFTER_IMPORT_DEFAULT_VALUE)
    String enableEventTriggersActivationAfterImport;

    /**
     * Make a Map of properties with default values (if any).
     *
     * @return Map of properties with default values
     */
    public Map<String, String> asMapWithDefaultValues() {
        Map<String, String> propertiesWithDefaultValues = Maps.newHashMap();
        for (PropertyDescriptor extractProperty : Extractor.extractProperties(this)) {
            if (extractProperty.getDefaultValue() != null) {
                propertiesWithDefaultValues.put(extractProperty.getShortName(),
                        extractProperty.getDefaultValue().toString());
            }
        }
        return propertiesWithDefaultValues;
    }

}
