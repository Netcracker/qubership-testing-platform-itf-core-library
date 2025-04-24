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

import java.util.regex.Pattern;

public interface PropertyConstants {

    Pattern FILE_DIRECTORY_PATTERN = Pattern.compile("data/"
            + "(wsdl-xsd|diameter-dictionary|ei-session|dataset|keystore|fast-stub)"
            + "/([0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12})(/.*)");

    int FILE_DIRECTORY_PROJECT_UUID_GROUP_NUMBER = 2;
    int FILE_DIRECTORY_RELATIVE_PATH_GROUP_NUMBER = 3;

    interface Jms {

        String DESTINATION_TYPE_DESCRIPTION = "Queue or Topic";
        String DESTINATION_DESCRIPTION = "For cluser:NCJMSServer_clust1/NCJMSModule!queue_xxx\n"
                + "For non cluster:NCJMSModule!queue_xxx";
        String CONNECTION_FACTORY_DESCRIPTION = "jms_xxx_xxx";
        String INITIAL_CONTEXT_FACTORY_DESCRIPTION = "weblogic.jndi.WLInitialContextFactory";
        String ADDITIONAL_JNDI_PROPERTIES_DESCRIPTION = "aaa=bbb\nccc=ddd";
        String MESSAGE_SELECTOR_DESCRIPTION = "For example: test=%A ...or empty\n";
        String JMS_HEADERS_DESCRIPTION = "aaa=bbb\nccc=ddd";
        String RECOVERY_INTERVAL_DESCRIPTION = "Time between reconnection attempts in milliseconds";
        String MAX_ATTEMPTS_DESCRIPTION = "Total maximum reconnection attempts: unlimited or numerical value";
    }

    interface Commons {

        String PRINCIPAL_DESCRIPTION = "UserName";
        String AUTHENTICATION_DESCRIPTION = "Simple or empty";
        String PROVIDER_URL_DESCRIPTION = "URL or empty\nURL...";
        String CREDENTIALS_DESCRIPTION = "Password or empty";
        String ENDPOINT_PROPERTIES = "endpointProperties";
        String ENDPOINT_PROPERTIES_DESCRIPTION = "Extra Endpoint Properties (name=value pairs delimited by newlines)";
    }

    interface Http {

        String ENDPOINT_URI = "URI endpoint: /mb/test/enpoint";
        String METHOD = "HTTP Method";
        String RESPONSE_CODE = "Response code: 200";
        String ALLOW_STATUS = "Allowed status code for response (range like '200-299' or '200-200')";
        String HEADERS = "SomeHeader=SomeValue\nAuthorization=Basic some-token";
        String BASE_URL = "Base URL: http://some-machine.our-company.com:6330";
        String CONTENT_TYPE = "Content-Type; Default=text/html. Header overrides it.";
        String PROPERTIES = "properties";
        String PROPERTIES_DESCRIPTION = "Properties description";
        String IS_STUB = "Is Stub: Default=No";
        String CACHE_RESPONSE_FOR_SECONDS = "cacheResponseForSeconds";
        String CACHE_RESPONSE_FOR_SECONDS_DESCRIPTION =
                "Cache response; expiry time (in seconds). Default=Empty (means: Do NOT cache a response)";
    }

    interface File {

        String FILE_NAME_DESCRIPTION = "File Name";
        String HOST = "host";
        String HOST_DESCRIPTION = "Host";
        String PATH = "path";
        String PATH_DESCRIPTION = "Path";
        String TYPE = "type";
        String TYPE_DESCRIPTION = "Protocol type";
        String PRINCIPAL = "principal";
        String CREDENTIALS = "credentials";
        String DESTINATION_FILE_NAME = "destinationFileName";
        String SSH_KEY = "ssh_key";
        String SSH_KEY_DESCRIPTION = "SSH private key string (pem format only)";
    }

    interface Cli {

        String REMOTE_IP = "remote_ip";
        String CONNECTION_TYPE = "type";
        String REMOTE_PORT = "remote_port";
        String USER = "user";
        String PASSWORD = "password";
        String SSH_KEY = "ssh_key";
        String WAIT_RESPONSE = "wait_response";

        interface Inbound {

            String COMMAND_DELIMITER = "command_delimiter";
            String GREETING = "greeting";
            String ALLOWED_EMPTY = "empty_commands_allowed";
        }
    }

    interface Soap {

        // Parameters
        String WSDL_PATH = "wsdlPath";
        String WSDL_CONTAINS_XSD = "isWsdlContains";
        String REQUEST_XSD_PATH = "requestXSDPath";
        String RESPONSE_XSD_PATH = "responseXSDPath";
        // Descriptions
        String WSDL_PATH_DESCRIPTION = "Path to WSDL file";
        String XSD_PATH_DESCRIPTION = "Path to XSD file (not required)";
    }

    interface Kafka {

        // Parameters
        String BROKERS = "brokers";
        String TOPIC = "topic";
        String GROUP = "group";
        String HEADERS = "headers";
        String MESSAGE_KEY = "key";
        // Descriptions
        String BROKERS_DESCRIPTION = "Brokers to consume (comma-separated list in format host:port)";
        String TOPIC_DESCRIPTION = "Topics to consume (comma-separated list)";
        String GROUP_DESCRIPTION = "Group of consumers";
        String HEADERS_DESCRIPTION = "SomeHeader=SomeValue\nContent-Type=text/html";
        String MESSAGE_KEY_DESCRIPTION = "Message Key";
    }

    interface Applicability {

        String ENVIRONMENT = "Environment";
        String SYSTEM = "System";
    }

    interface DiameterTransportConstants {

        String HOST = "host";
        String HOST_DESCRIPTION = "Remote host name";

        String PORT = "port";
        String PORT_DESCRIPTION = "Remote host port";

        String DWA = "dwa";
        String DWA_DESCRIPTION = "Watchdog default template";

        String CONFIG_PATH = "configPath";
        String CONFIG_PATH_LONG_NAME = "AVP configurations path";
        String CONFIG_PATH_DESCRIPTION = "AVP configurations path (relative path, example: '/dictionary')";
        String CONFIG_PATH_VALIDATE_PATTERN = "^((\\/[a-zA-Z0-9]+)+|\\/)$";

        String WAIT_RESPONSE_TIMEOUT = "waitResponseTimeout";
        String WAIT_RESPONSE_TIMEOUT_DESCRIPTION = "Wait response timeout. Default: 3000ms (MILLISECONDS)";

        String CONNECTION_TYPE = "connectionType";
        String CONNECTION_TYPE_DESCRIPTION = "TCP/SCTP connection layer";

        String INTERCEPTOR_NAME = "interceptorName";
        String INTERCEPTOR_NAME_DESCRIPTION = "What response you are expecting";

        String MESSAGE_FORMAT_NAME = "messageFormat";
        String MESSAGE_FORMAT_DESCRIPTION = "Message format";

        String PROPERTIES = "properties";
        String PROPERTIES_DESCRIPTION = "Properties like Origin-Host=localhost";

        String CER = "CER";
        String CER_DESCRIPTION = "CER template (to send automatically just after connection is opened)";

        String DPA = "DPA";
        String DPA_DESCRIPTION = "DPA template";

        String WAIT_RESPONSE = "waitResponse";
        String WAIT_RESPONSE_DESCRIPTION = "Wait response for message sent";

        String CUSTOM_DPR = "customDpr";
        String CUSTOM_DPR_DESCRIPTION = "Send DPR by yourself?";

        String DPR = "DPR";
        String DPR_DESCRIPTION = "DPR template (to send automatically just before the channel is closed)";

        String SESSION_ID = "sessionID";
        String SESSION_ID_DESCRIPTION = "Diameter session ID (Can be dynamic)";

        String DICTIONARY_TYPE = "dictionary type";
        String DICTIONARY_TYPE_DESCRIPTION = "Dictionary type";
    }
}

