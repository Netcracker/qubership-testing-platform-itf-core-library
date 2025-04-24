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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.message.parser.ParsingRule;
import org.qubership.automation.itf.core.model.jpa.message.template.Template;
import org.qubership.automation.itf.core.model.jpa.server.ServerHB;
import org.qubership.automation.itf.core.model.jpa.step.EmbeddedStep;
import org.qubership.automation.itf.core.model.jpa.step.SituationStep;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;

public class EiConstants {

    public static final String SESSION_TYPE_SHORT_NAME = "session.type";
    public static final String SESSION_TYPE_LONG_NAME = "Type of session";
    public static final String LINK_TO_THE_FILE_SHORT_NAME = "link.to.the.file";
    public static final String LINK_TO_THE_FILE_LONG_NAME = "Link to the file";
    public static final String SESSION_STATUS_SHORT_NAME = "session.status";
    public static final String SESSION_STATUS_LONG_NAME = "Status";
    public static final String SESSION_START_DATE_SHORT_NAME = "session.start.date";
    public static final String SESSION_START_DATE_LONG_NAME = "Start Date";
    public static final String SESSION_FINISH_DATE_SHORT_NAME = "session.finish.date";
    public static final String SESSION_FINISH_DATE_LONG_NAME = "Finish Date";

    public static final String SESSION_ID = "sessionId";
    public static final String ZIP_FILE_PATH = "zipFilePath";
    public static final String ZIP_NAME = "zipName";
    public static final String ENTITY_TYPE = "entityType";
    public static final String PARENT_ID = "parentId";
    public static final String PARENT_CLASS = "parentClass";
    public static final String ID = "id";

    public static final String SYSTEM = "System";
    public static final String CALLCHAIN = "CallChain";
    public static final String OPERATION = "Operations";
    public static final String TEMPLATE = "Template";
    public static final String TRANSPORT = "Transports";
    public static final String PARSING_RULE = "Parsing Rules";
    public static final String SITUATION_STEP = "Situation Step";
    public static final String EMBEDDED_STEP = "Embedded Step";
    public static final String ENVIRONMENT = "Environment";
    public static final String CALLCHAIN_FOLDER = "ChainFolder";
    public static final String ENVIRONMENT_FOLDER = "EnvFolder";
    public static final String SYSTEM_FOLDER = "SystemFolder";
    public static final String SITUATION = "Situations";
    public static final String SERVER = "ServerHB";
    public static final String SERVER_FOLDER = "ServerFolder";
    // TODO: Remove after correct ExportImport implementation
    //  This  constant used as WA in HC map as key - we have only one Import session at this time (Projects Migration
    //  to Multi-replica blocker)
    public static final BigInteger EI_ACTIVE_SESSION_ID = new BigInteger("123");

    public static final Map<String, Class<? extends Storable>> importMapping = new HashMap<>();

    static {
        importMapping.put(PARSING_RULE, ParsingRule.class);
        importMapping.put(TRANSPORT, TransportConfiguration.class);
        importMapping.put(TEMPLATE, Template.class);
        importMapping.put(OPERATION, Operation.class);
        importMapping.put(SYSTEM, System.class);
        importMapping.put(CALLCHAIN, CallChain.class);
        importMapping.put(SITUATION_STEP, SituationStep.class);
        importMapping.put(EMBEDDED_STEP, EmbeddedStep.class);
        importMapping.put(ENVIRONMENT, Environment.class);
        importMapping.put(CALLCHAIN_FOLDER, Folder.class);
        importMapping.put(SYSTEM_FOLDER, Folder.class);
        importMapping.put(ENVIRONMENT_FOLDER, Folder.class);
        importMapping.put(SITUATION, Situation.class);
        importMapping.put(SERVER, ServerHB.class);
        importMapping.put(SERVER_FOLDER, Folder.class);
    }
}

