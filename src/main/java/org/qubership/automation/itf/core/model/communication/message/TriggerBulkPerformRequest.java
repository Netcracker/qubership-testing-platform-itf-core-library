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

package org.qubership.automation.itf.core.model.communication.message;

import java.math.BigInteger;
import java.util.List;
import java.util.UUID;

import org.qubership.automation.itf.core.model.communication.StubUser;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class TriggerBulkPerformRequest {

    private UUID projectUuid;
    private String type;
    private String action;
    private StubUser user;
    private String sessionId;
    private boolean isSelectedAll;
    private List<IdentifiedEntity> objects;

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class IdentifiedEntity {
        BigInteger id;
        String className;
    }
}


