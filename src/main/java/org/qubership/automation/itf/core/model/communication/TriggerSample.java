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

package org.qubership.automation.itf.core.model.communication;

import java.math.BigInteger;
import java.util.UUID;

import org.qubership.automation.itf.core.model.transport.ConnectionProperties;
import org.qubership.automation.itf.core.util.constants.TriggerState;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TriggerSample {

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger triggerId;
    private String triggerName;
    private String triggerTypeName;
    private String transportName;
    private String serverName;
    private TriggerState triggerState;
    private TransportType transportType;
    private ConnectionProperties triggerProperties;
    private UUID projectUuid;
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigInteger projectId;

    public TriggerSample() {
    }
}
