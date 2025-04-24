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

package org.qubership.automation.itf.core.util.constants;

public class ProjectSettingsConstants {

    public static final String PROJECT_UUID = "uuid";

    public static final String FOLDER_DELETE_NOT_EMPTY_ALLOW = "folder.delete.notempty.allow";
    public static final String FOLDER_DELETE_NOT_EMPTY_ALLOW_LONG_NAME = "Delete non-empty folders";
    public static final String FOLDER_DELETE_NOT_EMPTY_ALLOW_DESCRIPTION =
            "If this parameter = true (deletion is allowed) - an additional message is issued that the user is trying"
                    + " to delete a non-empty folder. If the user confirms the action, the folder is deleted; If this "
                    + "parameter is false (deletion is prohibited) - an "
                    + "additional message is issued that it is forbidden to delete non-empty folders.";

    public static final String TCPDUMP_PACKET_COUNT_DEFAULT = "tcpdump.packet.count.default";
    public static final String TCPDUMP_PACKET_COUNT_DEFAULT_LONG_NAME = "Number of TCP dump packets";
    public static final String TCPDUMP_PACKET_COUNT_DEFAULT_DESCRIPTION = "Count of packets to capture. Default: 50";
    public static final String TCPDUMP_PACKET_COUNT_DEFAULT_DEFAULT_VALUE = "50";

    public static final String TCPDUMP_CAPTURING_FILTER_DEFAULT = "tcpdump.capturing.filter.default";
    public static final String TCPDUMP_CAPTURING_FILTER_DEFAULT_LONG_NAME = "Default TCP dump capturing filter";
    public static final String TCPDUMP_CAPTURING_FILTER_DEFAULT_DESCRIPTION = "Capturing Filter. \n"
            + "Example value: port 3840 or port 3867 or port 3868 or port 3869 or port 3870 or port 3871. \n Default:"
            + "empty string";
    public static final String TCPDUMP_CAPTURING_FILTER_DEFAULT_DEFAULT_VALUE = "";

    public static final String TCP_DUMP_NI_DEFAULT = "tcpdump.ni.default";
    public static final String TCP_DUMP_NI_DEFAULT_LONG_NAME = "Default TCP dump NetworkInterface";
    public static final String TCP_DUMP_NI_DEFAULT_DESCRIPTION = "Network adapter to capture packets.\n Example "
            + "value: \\\\Device\\\\NPF_{FBA5FC6B-29EF-48ED-9D35-7FFB46A95D30}\nCheck this value via commands: "
            + "ipconfig /all, then getmac\nDefault: empty string";
    public static final String TCP_DUMP_NI_DEFAULT_VALUE = "";

    public static final String COPY_OBJECT_SET_STATUS_OFF = "copyObjects.setStatusOff";
    public static final String COPY_OBJECT_SET_STATUS_OFF_LONG_NAME = "Set status off after copy object";
    public static final String COPY_OBJECT_SET_STATUS_OFF_DESCRIPTION =
            "If true, then when copying the situation, the new situation assumes the status InActive. It is used in "
                    + "cases when it is necessary for the user to configure it first and then manually turn it on.\n "
                    + "The default value is false. The statuses of situations are copied from the environment. "
                    + "Default: false";
    public static final String COPY_OBJECT_SET_STATUS_OFF_DEFAULT_VALUE = "false";

    public static final String COPY_OBJECT_IS_SMART = "copyObjects.isSmart";
    public static final String COPY_OBJECT_IS_SMART_LONG_NAME = "Smart copy";
    public static final String COPY_OBJECT_IS_SMART_DESCRIPTION =
            "If true, the Environment is copied along with the server in which the triggers are stored. If false, "
                    + "then smart copying does not work. Default value = false";
    public static final String COPY_OBJECT_IS_SMART_DEFAULT_VALUE = "false";

    public static final String SITUATION_IGNORE_ERRORS_ENABLED = "situation.ignoreErrors.enabled";
    public static final String SITUATION_IGNORE_ERRORS_ENABLED_LONG_NAME = "Situation ignore errors";
    public static final String SITUATION_IGNORE_ERRORS_ENABLED_DESCRIPTION =
            "Turn on/off ignoring transport errors checkbox. Values: true/false. (Default: false)";
    public static final String SITUATION_IGNORE_ERRORS_ENABLED_DEFAULT_VALUE = "false";

    public static final String MESSAGE_PRETTY_FORMAT = "message.pretty.format";
    public static final String MESSAGE_PRETTY_FORMAT_LONG_NAME = "Message pretty format";
    public static final String MESSAGE_PRETTY_FORMAT_DESCRIPTION = "Message pretty format. Default: false";
    public static final String MESSAGE_PRETTY_FORMAT_DEFAULT_VALUE = "false";

    public static final String ATP_WSDL_PATH = "atp.wsdl.path";
    public static final String ATP_WSDL_PATH_LONG_NAME = "ATP WSDL path";
    public static final String ATP_WSDL_PATH_DESCRIPTION =
            "If the project plans to report to ATP, you need to set the server address\n"
                    + "Default: http://atp.our-company.com/solutions/atp/integration/jsp/AtpRamWebService?wsdl";
    public static final String ATP_WSDL_PATH_DEFAULT_VALUE = "http://atp.our-company.com/solutions/atp/integration/jsp"
            + "/AtpRamWebService?wsdl";

    public static final String ATP_ACCOUNT_NAME = "atp.account.name";
    public static final String ATP_ACCOUNT_NAME_LONG_NAME = "ATP account name";
    public static final String ATP_ACCOUNT_NAME_DESCRIPTION =
            "The account name is taken from the \"Project Name\" in ATP parameters of the project\n"
                    + "Default: empty string";
    public static final String ATP_ACCOUNT_NAME_DEFAULT_VALUE = "";

    public static final String ATP_TEST_PLAN = "atp.test.plan";
    public static final String ATP_TEST_PLAN_LONG_NAME = "ATP test plan";
    public static final String ATP_TEST_PLAN_DESCRIPTION = "Test plan, taken from the column \"Object Type\".  "
            + "Default: empty string";
    public static final String ATP_TEST_PLAN_DEFAULT_VALUE = "";

    public static final String ATP_PROJECT_NAME = "atp.project.name";
    public static final String ATP_PROJECT_NAME_LONG_NAME = "ATP project name";
    public static final String ATP_PROJECT_NAME_DESCRIPTION = "The name of the project, taken from the column \"Test "
            + "Plan Name\". Default: empty string";
    public static final String ATP_PROJECT_NAME_DEFAULT_VALUE = "";

    public static final String ENABLE_REPORT_ADAPTER_ATP = "enable.report.adapter.atp";
    public static final String ENABLE_REPORT_ADAPTER_ATP_LONG_NAME = "Enable ATP report adapter";
    public static final String ENABLE_REPORT_ADAPTER_ATP_DESCRIPTION = "Enable / disable ATP report adapter. "
            + "Default: false";
    public static final String ENABLE_REPORT_ADAPTER_ATP_DEFAULT_VALUE = "false";

    public static final String ENABLE_REPORT_ADAPTER_ATP2 = "enable.report.adapter.atp2";
    public static final String ENABLE_REPORT_ADAPTER_ATP2_LONG_NAME = "Enable ATP2 report adapter";
    public static final String ENABLE_REPORT_ADAPTER_ATP2_DESCRIPTION = "Enable / disable ATP2 report adapter. "
            + "Default: false";
    public static final String ENABLE_REPORT_ADAPTER_ATP2_DEFAULT_VALUE = "false";

    public static final String ENABLE_ITF_REPORTING = "enable.itf.reporting";
    public static final String ENABLE_ITF_REPORTING_LONG_NAME = "Enable ITF reporting of callchains";
    public static final String ENABLE_ITF_REPORTING_DESCRIPTION = "Enable ITF reporting of callchains, "
            + "invoked from ATP/ATP2. Default: false";
    public static final String ENABLE_ITF_REPORTING_DEFAULT_VALUE = "false";

    public static final String TC_TIMEOUT_FAIL = "tc.timeout.fail";
    public static final String TC_TIMEOUT_FAIL_LONG_NAME = "TC timeout fail";
    public static final String TC_TIMEOUT_FAIL_DESCRIPTION =
            "The maximum execution time of the test case. If it runs longer than the specified time, it will fail.\n"
                    + "The parameter is not required, since the code has a default value. Default: 20";
    public static final String TC_TIMEOUT_FAIL_DEFAULT_VALUE = "20";

    public static final String TC_TIMEOUT_FAIL_TIME_UNIT = "tc.timeout.fail.timeunit";
    public static final String TC_TIMEOUT_FAIL_TIME_UNIT_LONG_NAME = "TC timeout fail (Time unit)";
    public static final String TC_TIMEOUT_FAIL_TIME_UNIT_DESCRIPTION = "Timeout units. The default is minutes.\n"
            + "The parameter is not required, since the code has a default value. Default: MINUTES";
    public static final String TC_TIMEOUT_FAIL_TIME_UNIT_DEFAULT_VALUE = "MINUTES";

    public static final String REPORT_EXECUTION_ENABLED = "report.execution.enabled";
    public static final String REPORT_EXECUTION_ENABLED_LONG_NAME = "ITF reporting enabled";
    public static final String REPORT_EXECUTION_ENABLED_DESCRIPTION = "Enable/disable ITF reporting. Default: true";
    public static final String REPORT_EXECUTION_ENABLED_DEFAULT_VALUE = "true";

    public static final String REPORT_TO_ATP_ENABLED = "report.to.atp.enabled";
    public static final String REPORT_TO_ATP_ENABLED_LONG_NAME = "Report to ATP enabled";
    public static final String REPORT_TO_ATP_ENABLED_DESCRIPTION = "Enable/disable reporting in ATP. Default: false";
    public static final String REPORT_TO_ATP_ENABLED_DEFAULT_VALUE = "false";

    public static final String REPORT_TO_ATP_DEFAULT_CONTEXT_ENABLED = "report.to.atp.default.context.enabled";
    public static final String REPORT_TO_ATP_DEFAULT_CONTEXT_ENABLED_LONG_NAME = "Report to ATP default context "
            + "enabled";
    public static final String REPORT_TO_ATP_DEFAULT_CONTEXT_ENABLED_DESCRIPTION =
            "Enabling / disabling reporting in ATP default contexts. Default: false";
    public static final String REPORT_TO_ATP_DEFAULT_CONTEXT_ENABLED_DEFAULT_VALUE = "false";

    public static final String CALL_CHAIN_PARALLEL_RUNNING_THREAD_COUNT = "callchain.parallel.running.thread.count";
    public static final String CALL_CHAIN_PARALLEL_RUNNING_THREAD_COUNT_LONG_NAME = "Callchain parallel thread number";
    public static final String CALL_CHAIN_PARALLEL_RUNNING_THREAD_COUNT_DESCRIPTION = "Callchain parallel thread "
            + "number. Default: 10";
    public static final String CALL_CHAIN_PARALLEL_RUNNING_THREAD_COUNT_DEFAULT_VALUE = "10";

    public static final String EXPRESSION_VAR = "expression.var";
    public static final String EXPRESSION_VAR_LONG_NAME = "Allow variables in parsing rule expressions";
    public static final String EXPRESSION_VAR_DESCRIPTION =
            "Allow variables in parsing rule expressions. Default: false";
    public static final String EXPRESSION_VAR_DEFAULT_VALUE = "false";

    public static final String TEST_SERVER_AVAILABILITY = "test.server.availability";
    public static final String TEST_SERVER_AVAILABILITY_LONG_NAME = "Test server availability";
    public static final String TEST_SERVER_AVAILABILITY_DECRIPTION = "Checking server availability before activating "
            + "triggers.\n This functionality work at ITF startup.\nIf server not available, other triggers on this "
            + "server won't activate.\nDefault: false";
    public static final String TEST_SERVER_AVAILABILITY_DEFAULT_VALUE = "false";

    public static final String START_TRIGGERS_AT_STARTUP = "start.triggers.at.startup";
    public static final String START_TRIGGERS_AT_STARTUP_SHORT_NAME = "Start triggers at startup";
    public static final String START_TRIGGERS_AT_STARTUP_DESCRIPTION =
            "Determines whether triggers are triggered when the ITF starts.\nIf true, then all triggers will be "
                    + "activated whose active field in the database is true. Default: false";
    public static final String START_TRIGGERS_AT_STARTUP_DEFAULT_VALUE = "false";

    public static final String START_TRANSPORT_TRIGGERS_AT_STARTUP = "start.transport.triggers.at.startup";
    public static final String START_TRANSPORT_TRIGGERS_AT_STARTUP_LONG_NAME = "Start transport triggers at startup";
    public static final String START_TRANSPORT_TRIGGERS_AT_STARTUP_DESCRIPTION = "Start transport triggers at startup"
            + ".  Default: false";
    public static final String START_TRANSPORT_TRIGGERS_AT_STARTUP_DEFAULT_VALUE = "false";

    public static final String MONITORING_PAGINATION_SIZE = "monitoring.pagination.size";
    public static final String MONITORING_PAGINATION_SIZE_LONG_NAME = "Monitoring pagination size";
    public static final String MONITORING_PAGINATION_SIZE_DESCRIPTION = "Page size on monitoring.\nThe default value "
            + "is 20.";
    public static final int MONITORING_PAGINATION_SIZE_DEFAULT_VALUE_INT = 20;
    public static final String MONITORING_PAGINATION_SIZE_DEFAULT_VALUE_STRING = "20";

    public static final String DATA_SET_SERVICE_DS_FORMAT = "dataset.service.datasetFormat";
    public static final String DATA_SET_SERVICE_DS_FORMAT_LONG_NAME = "Dataset Format";
    public static final String DATA_SET_SERVICE_DS_FORMAT_DESCRIPTION = "Algorithm how dataset contents are filtered "
            + "on  the Dataset Service side. Values are: Default, Object, ObjectExtended, Optimized, Itf (deprecated)"
            + ".  Default value = Itf";
    public static final String DATA_SET_SERVICE_DS_FORMAT_DEFAULT_VALUE = "Itf";

    public static final String MANY_OBJECTS_UI_MODE = "many.objects.ui.mode";
    public static final String MANY_OBJECTS_UI_MODE_LONG_NAME = "Many configurations objects";
    public static final String MANY_OBJECTS_UI_MODE_DESCRIPTION =
            "false = standart mb EditableSelects are used for choosing object in UI (templates on situations, "
                    + "situations on situations triggers) \n true = input filter, go to backend, then receive "
                    + "filtered list of objects" + "Default value: false";
    public static final String MANY_OBJECTS_UI_MODE_DEFAULT_VALUE = "false";

    public static final String START_PARAM_RUN_BV_CASE = "startParam.starterObject.runBvCase";
    public static final String START_PARAM_RUN_BV_CASE_LONG_NAME = "[Run callchain window defaults]\nRun BV Case "
            + "after execution for the context (if available)";
    public static final String START_PARAM_RUN_BV_CASE_DESCRIPTION =
            "This parameter is responsible for selecting the option \"Run BV Case after execution for context (if "
                    + "available)\" in run popup by default. Default value: false (off)";
    public static final String START_PARAM_RUN_BV_CASE_DEFAULT_VALUE = "false";

    public static final String START_PARAM_SVT = "startParam.starterObject.svt";
    public static final String START_PARAM_SVT_LONG_NAME = "[Run callchain window defaults]\nRun with SVT parameters";
    public static final String START_PARAM_SVT_DESCRIPTION = "This parameter is responsible for selecting the option "
            + "\"SVT\" in run popup by default. Default value: false (off)";
    public static final String START_PARAM_SVT_DEFAULT_VALUE = "false";

    public static final String START_PARAM_COLLECT_LOGS = "startParam.starterObject.collectLogs";
    public static final String START_PARAM_COLLECT_LOGS_LONG_NAME =
            "[Run callchain window defaults]\nCollect Logs via logCollector";
    public static final String START_PARAM_COLLECT_LOGS_DESCRIPTION =
            "This parameter is responsible for selecting the option \"Collect Logs via logCollector\" in run popup by"
                    + " default. Default value: false (off)";
    public static final String START_PARAM_COLLECT_LOGS_DEFAULT_VALUE = "false";

    public static final String START_PARAM_NEED_TO_LOG_IN_ATP = "startParam.starterObject.needToLogInAtp";
    public static final String START_PARAM_NEED_TO_LOG_IN_ATP_LONG_NAME = "[Run callchain window defaults]\nRun "
            + "with logging to ATP";
    public static final String START_PARAM_NEED_TO_LOG_IN_ATP_DESCRIPTION = "This parameter is responsible for "
            + "selecting the option \"With logging to ATP\" in run popup by default. Default value: false (off)";
    public static final String START_PARAM_NEED_TO_LOG_IN_ATP_DEFAULT_VALUE = "false";

    public static final String START_PARAM_MAKE_DEFAULT_DATASET = "startParam.starterObject.makeDefaultDataset";
    public static final String START_PARAM_MAKE_DEFAULT_DATASET_LONG_NAME = "[Run callchain window defaults]\n Set "
            + "selected dataset as 'Default' for the callChain";
    public static final String START_PARAM_MAKE_DEFAULT_DATASET_DESCRIPTION = "This parameter is responsible for "
            + "selecting the option \"Set selected dataset as 'Default' for the callChain\" in run popup by default"
            + ". Default value: false (off)";
    public static final String START_PARAM_MAKE_DEFAULT_DATASET_DEFAULT_VALUE = "false";

    public static final String START_PARAM_RUN_VALIDATION = "startParam.starterObject.runValidation";
    public static final String START_PARAM_RUN_VALIDATION_LONG_NAME = "[Run callchain window defaults]\n Run "
            + "BulkValidator Case after situation step (if turned ON) for response messages\"";
    public static final String START_PARAM_RUN_VALIDATION_DESCRIPTION = "This parameter is responsible for selecting "
            + "the option \"Run BulkValidator Case after situation step (if turned ON) for response messages\" in run"
            + " popup by default. Default value: false (off)";
    public static final String START_PARAM_RUN_VALIDATION_DEFAULT_VALUE = "false";

    public static final String START_PARAM_TEST_DATA = "startParam.starterObject.testData";
    public static final String START_PARAM_TEST_DATA_LONG_NAME = "[Run callchain window defaults]\n Send request to "
            + "TestDataManagement Service after execution PASSED\"";
    public static final String START_PARAM_TEST_DATA_DESCRIPTION =
            "This parameter is responsible for selecting the option \"Send request to TestDataManagement Service "
                    + "after execution PASSED\" in run popup by default. Default value: false (off)";
    public static final String START_PARAM_TEST_DATA_DEFAULT_VALUE = "false";

    public static final String START_PARAM_CREATE_TCP_DUMP = "startParam.starterObject.createTcpDump";
    public static final String START_PARAM_CREATE_TCP_DUMP_LONG_NAME =
            "[Run callchain window defaults]\nCreate TCP " + "Dump";
    public static final String START_PARAM_CREATE_TCP_DUMP_DESCRIPTION = "This parameter is responsible for selecting"
            + " the option \"Create TCP Dump\" in run popup by default Default value: false (off)";
    public static final String START_PARAM_CREATE_TCP_DUMP_DEFAULT_VALUE = "false";

    public static final String BV_DEFAULT_ACTION = "bv.default.action";
    public static final String BV_DEFAULT_ACTION_LONG_NAME = "Default BV action";
    public static final String BV_DEFAULT_ACTION_DESCRIPTION = "Alias of the default BulkValidator endpoint when "
            + "creating a testrun. Default value: CreateNewTestRun";
    public static final String BV_DEFAULT_ACTION_DEFAULT_VALUE = "CreateNewTestRun";
    public static final String BV_DEFAULT_ACTION_OPTIONS_CREATENEWTESTRUN_VALUE = "CreateNewTestRun";
    public static final String BV_DEFAULT_ACTION_OPTIONS_READCOMPARE_VALUE = "ReadCompare";
    public static final String[] BV_DEFAULT_ACTION_OPTIONS_VALUE =
            {BV_DEFAULT_ACTION_OPTIONS_CREATENEWTESTRUN_VALUE, BV_DEFAULT_ACTION_OPTIONS_READCOMPARE_VALUE};

    public static final String MAX_CONNECTION_TIMEOUT = "max.connection.timeout";
    public static final String MAX_CONNECTION_TIMEOUT_LONG_NAME = "Max Connection Timeout";
    public static final String MAX_CONNECTION_TIMEOUT_DESCRIPTION = "Max thread timeout to wait while activating "
            + "triggers at ITF startup (milliseconds).\n Default: 5000";
    public static final String MAX_CONNECTION_TIMEOUT_DEFAULT_VALUE = "5000";

    public static final String CONTEXT_FORMAT_PRETTY_PRINT = "context.format.prettyPrint";
    public static final String CONTEXT_FORMAT_PRETTY_PRINT_LONG_NAME = "Pretty print for messages in context";
    public static final String CONTEXT_FORMAT_PRETTY_PRINT_DESCRIPTION = "Default setting of 'Pretty print' checkbox "
            + "in the context popup -> steps tree --> Incoming/Outgoing message accordion. Default: true";
    public static final String CONTEXT_FORMAT_PRETTY_PRINT_DEFAULT_VALUE = "true";

    public static final String CONTEXT_FORMAT_MESSAGE_TYPE = "context.format.messageType";
    public static final String CONTEXT_FORMAT_MESSAGE_TYPE_LONG_NAME = "Type for messages in context";
    public static final String CONTEXT_FORMAT_MESSAGE_TYPE_DESCRIPTION = "Default setting of 'Message Type' "
            + "select-list in the context popup -> steps tree --> Incoming/Outgoing message accordion. Values: XML, "
            + "JSON. Default: XML";
    public static final String CONTEXT_FORMAT_MESSAGE_TYPE_DEFAULT_VALUE = "XML";

    public static final String CONTEXT_FORMAT_WORDWRAP = "context.format.wordWrap";
    public static final String CONTEXT_FORMAT_WORDWRAP_LONG_NAME = "Word wrap for messages in context";
    public static final String CONTEXT_FORMAT_WORDWRAP_DESCRIPTION = "Default setting of 'Word wrap' checkbox in the "
            + "context popup -> steps tree --> Incoming/Outgoing message accordion. Values: true/false. Default: false";
    public static final String CONTEXT_FORMAT_WORDWRAP_DEFAULT_VALUE = "false";

    public static final String CONTEXT_FORMAT_EXPAND_ALL = "context.format.expandAll";
    public static final String CONTEXT_FORMAT_EXPAND_ALL_LONG_NAME = "Expand all steps in the context tree";
    public static final String CONTEXT_FORMAT_EXPAND_ALL_DESCRIPTION = "Expand all steps in the context tree. Values:"
            + " true/false. Default: true";
    public static final String CONTEXT_FORMAT_EXPAND_ALL_DEFAULT_VALUE = "true";

    public static final String ATP_REPORTING_WAIT_MAX = "atp.reporting.wait.max";
    public static final String ATP_REPORTING_WAIT_MAX_LONG_NAME =
            "A maximum wait time before sending the response " + "to" + " ATP";
    public static final String ATP_REPORTING_WAIT_MAX_DESCRIPTION = "A maximum wait time (in seconds) before "
            + "'Execution Request is Finished' response is sent to ATP in case async reporting mode. Default: 30 "
            + "(seconds)";
    public static final String ATP_REPORTING_WAIT_MAX_DEFAULT_VALUE = "30";

    public static final String ATP_REPORTING_MODE = "atp.reporting.mode";
    public static final String ATP_REPORTING_MODE_LONG_NAME = "Mode of reporting to ATP ";
    public static final String ATP_REPORTING_MODE_DESCRIPTION = "Mode of reporting to ATP in case ExecuteStepRequest "
            + "from ATP is processed. Values: sync/async. (Default: sync)";
    public static final String ATP_REPORTING_MODE_DEFAULT_VALUE = "sync";

    public static final String LDAP_URL = "ldap.url";
    public static final String LDAP_URL_LONG_NAME = "URL of LDAP-server";
    public static final String LDAP_URL_DESCRIPTION = "URL of LDAP-server for domain authentication";

    public static final String LDAP_LOGIN_PREFIX = "ldap.login.prefix";
    public static final String LDAP_LOGIN_PREFIX_LONG_NAME = "Prefix for LDAP-auth";
    public static final String LDAP_LOGIN_PREFIX_DESCRIPTION = "Prefix for authentication via LDAP";

    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_KEY = "report.link.KeyBasedLinkCollector.key";
    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_KEY_LONG_NAME = "Link Collector Key";
    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_KEY_DESCRIPTION = "Link Collector Key";

    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_VIEW_NAME =
            "report.link.KeyBasedLinkCollector.view" + ".name";
    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_VIEW_NAME_LONG_NAME = "Link Collector View Name";
    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_VIEW_NAME_DESCRIPTION = "Link Collector View Name";

    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_URL_FORMAT =
            "report.link.KeyBasedLinkCollector.url" + ".format";
    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_URL_FORMAT_LONG_NAME = "Link Collector Url Format";
    public static final String REPORT_LINK_KEYBASEDLINKCOLLECTOR_URL_FORMAT_DESCRIPTION = "Link Collector Url Format";

    public static final String CONDITIONS_STYLE_LEGACY = "conditions.style.legacy";
    public static final String CONDITIONS_STYLE_LEGACY_LONG_NAME = "Conditions style";
    public static final String CONDITIONS_STYLE_LEGACY_DESCRIPTION =
            "If variable is absent in the context, then:\n 1. EXISTS returns false,\n2. NOTEXISTS returns true,\n"
                    + " 3. All rest conditions:\n In case conditions.style.legacy = false (default):\n return false "
                    + "for all rest conditions,\n In case conditions.style.legacy = true:\n return true for "
                    + "{NOTEQUALS, NOTMATCHES},\n otherwise - return false. \n Default: false";
    public static final String CONDITIONS_STYLE_LEGACY_DEFAULT_VALUE = "false";

    public static final String REPORT_IN_DIFFERENT_THREAD = "report.in.different.thread";
    public static final String REPORT_IN_DIFFERENT_THREAD_LONG_NAME = "Report in different thread";
    public static final String REPORT_IN_DIFFERENT_THREAD_DESCRIPTION = "Report in different thread. Default: false";
    public static final String REPORT_IN_DIFFERENT_THREAD_DEFAULT_VALUE = "false";

    public static final String REPORT_EXECUTION_SENDER_THREAD_POOL_SIZE = "report.execution.sender.thread.pool.size";
    public static final String REPORT_EXECUTION_SENDER_THREAD_POOL_SIZE_LONG_NAME = "Execution sender thread pool size";
    public static final String REPORT_EXECUTION_SENDER_THREAD_POOL_SIZE_DESCRIPTION =
            "Execution sender thread pool " + "size. Default: 10";
    public static final String REPORT_EXECUTION_SENDER_THREAD_POOL_SIZE_DEFAULT_VALUE = "10";

    public static final String SCHEDULED_CLEANUP_ENABLED = "scheduled.cleanup.enabled";
    public static final String SCHEDULED_CLEANUP_ENABLED_LONG_NAME = "Scheduled cleanup enabled";
    public static final String SCHEDULED_CLEANUP_ENABLED_DESCRIPTION = "Scheduled cleanup enabled";
    public static final String[] SCHEDULED_CLEANUP_ENABLED_DESCRIPTION_ARRAY = {"true", "false"};

    public static final String SCHEDULED_CLEANUP_DAYS_REMAINING = "scheduled.cleanup.daysRemaining";
    public static final String SCHEDULED_CLEANUP_DAYS_REMAINING_LONG_NAME = "Scheduled cleanup days remaining";
    public static final String SCHEDULED_CLEANUP_DAYS_REMAINING_DESCRIPTION = "Scheduled cleanup days remaining";
    public static final String SCHEDULED_CLEANUP_DAYS_REMAINING_DEFAULT_VALUE = "14";

    public static final String SCHEDULED_CLEANUP_HOURS_TO_DELETE = "scheduled.cleanup.hoursToDelete";
    public static final String SCHEDULED_CLEANUP_HOURS_TO_DELETE_LONG_NAME = "Scheduled cleanup hours to delete";
    public static final String SCHEDULED_CLEANUP_HOURS_TO_DELETE_DESCRIPTION = "Scheduled cleanup hours to delete";
    public static final String SCHEDULED_CLEANUP_HOURS_TO_DELETE_DEFAULT_VALUE = "1";

    public static final String SCHEDULED_CLEANUP_INITIAL_DELAY_MINUTES = "scheduled.cleanup.initialDelayMinutes";
    public static final String SCHEDULED_CLEANUP_INITIAL_DELAY_MINUTES_LONG_NAME =
            "Scheduled cleanup initial delay " + "minutes";
    public static final String SCHEDULED_CLEANUP_INITIAL_DELAY_MINUTES_DESCRIPTION = "Scheduled cleanup initial delay"
            + " minutes";
    public static final String SCHEDULED_CLEANUP_INITIAL_DELAY_MINUTES_DEFAULT_VALUE = "20";

    public static final String SCHEDULED_CLEANUP_DELAY_MINUTES = "scheduled.cleanup.delayMinutes";
    public static final String SCHEDULED_CLEANUP_DELAY_MINUTES_LONG_NAME = "Scheduled cleanup delay minutes";
    public static final String SCHEDULED_CLEANUP_DELAY_MINUTES_DESCRIPTION = "Scheduled cleanup delay minutes";
    public static final String SCHEDULED_CLEANUP_DELAY_MINUTES_DEFAULT_VALUE = "15";

    public static final String TC_CONTEXT_CLIENT_ADDRESS = "tc.context.client_address";
    public static final String TC_CONTEXT_CLIENT_ADDRESS_LONG_NAME = "Context variable storing client address";
    public static final String TC_CONTEXT_CLIENT_ADDRESS_DESCRIPTION =
            "Context variable (or even Velocity expression) storing client address. \n"
                + "(Useful mostly for stubs) \n"
                + "Example value: ${tc.saved.customField}. \n"
                + "Default: empty string";
    public static final String TC_CONTEXT_CLIENT_ADDRESS_DEFAULT_VALUE = "";

    public static final String ENABLE_HISTORY_AND_VERSIONING = "enable.history.versioning";
    public static final String ENABLE_HISTORY_AND_VERSIONING_LONG_NAME = "Enable history and versioning";
    public static final String ENABLE_HISTORY_AND_VERSIONING_DESCRIPTION =
            "Enable history and versioning for ITF entities. It might \naffect ITF performance. Default: false.";
    public static final String ENABLE_HISTORY_AND_VERSIONING_DEFAULT_VALUE = "false";

    public static final String ENABLE_FAST_STUBS = "enable.fastStubs";
    public static final String ENABLE_FAST_STUBS_LONG_NAME = "Enable Fast Stubs";
    public static final String ENABLE_FAST_STUBS_DESCRIPTION = "Enable Fast Stubs. Default: true.";
    public static final String ENABLE_FAST_STUBS_DEFAULT_VALUE = "true";

}
