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

public class InstanceSettingsConstants {
    public static final String WORKING_DIRECTORY = "working.directory";
    public static final String REPORT_ADAPTER_PREFIX = "report.adapter";
    public static final String BOOTSTRAP_SERVERS = "bootstrap.servers";
    public static final String ATP_LOGGER_URL = "atp.logger.url";
    public static final String INTERCEPTORS_FOLDER = "interceptors.folder";
    public static final String REPORT_EXECUTION_SCHEMA = "report.execution.schema";

    public static final String BV_SERVICE_URL = "bv.service.url";
    public static final String BV_SERVICE_URL_LONG_NAME = "BV service URL";
    public static final String BV_SERVICE_URL_DESCRIPTION =
            "BV service URL.\n"
                    + " Example value: https://bv.atp-cloud.our-company.com/";

    public static final String LOG_LEVEL = "log.level";
    public static final String LOG_APPENDER_DATE_FORMAT = "log.appender.date.format";
    public static final String TRANSPORT_FOLDER = "transport.folder";
    public static final String TRIGGER_FOLDER = "trigger.folder";
    public static final String TCP_DUMP_FOLDER = "tcpdump.folder";
    public static final String EXPORT_IMPORT_FOLDER = "ei.folder";
    public static final String EXECUTOR_THREAD_POOL_SIZE = "executor.thread.pool.size";
    public static final String BACKGROUND_EXECUTOR_THREAD_POOL_SIZE = "background.executor.thread.pool.size";
    public static final String HTTP_RESPONSE_CODE_SUCCESS = "http.response.code.success";
    public static final String TRANSPORT_LIB = "transport.lib";
    public static final String TRIGGER_LIB = "trigger.lib";
    public static final String ENVIRONMENTS_SERVICE_URL = "environments.service.url";
    public static final String AUTHENTICATION_TYPE = "authentication.type";
    public static final String EUREKA_CLIENT_ENABLED = "eureka.client.enabled";
    public static final String KAFKA_CLIENT_ENABLED = "kafka.enable";
    public static final String KAFKA_BOOTSTRAP_SERVERS = "kafka.bootstrap.servers";
    public static final String KAFKA_CLIENT_ID = "kafka.client.id";
    public static final String KAFKA_GROUP_ID = "kafka.group.id";
    public static final String KAFKA_AUTO_OFFSET_RESET = "kafka.auto.offset.reset";
    public static final String KAFKA_KEY_DESERIALIZER_CLASS = "kafka.key.deserializer.class";
    public static final String KAFKA_VALUE_DESERIALIZER_CLASS = "kafka.value.deserializer.class";
    public static final String LOAD_TRANSPORTS_AT_STARTUP = "load.transports.at.startup";
    public static final String ATP_PUBLIC_GATEWAY_URL = "atp.public.gateway.url";

    public static final String FEIGN_ATP_DATASETS_ROUTE = "feign.atp.datasets.route";
    public static final String FEIGN_ATP_ENVIRONMENTS_ROUTE = "feign.atp.environments.route";
    public static final String FEIGN_ATP_BV_ROUTE = "feign.atp.bv.route";
    public static final String FEIGN_ATP_CATALOGUE_ROUTE = "feign.atp.catalogue.route";
    public static final String FEIGN_ATP_EXECUTOR_ROUTE = "feign.atp.executor.route";
    public static final String VELOCITY_CONFIG = "velocity.config";

    public static final String LOCK_PROVIDER_PROCESS_TIMEOUT = "lock.provider.process.timeout";
    public static final String SESSION_HANDLER_PROCESS_TIMEOUT = "session.handler.process.timeout";
    public static final String INFINITE_LOOP_PROTECTION_BARRIER = "infinite.loop.protection.barrier";
    public static final String REPORT_EXECUTION_RECEIVER_THREAD_POOL_SIZE =
            "report.execution.receiver.thread.pool.size";
}
