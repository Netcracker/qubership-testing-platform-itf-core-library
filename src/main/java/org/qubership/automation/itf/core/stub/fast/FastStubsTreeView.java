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

package org.qubership.automation.itf.core.stub.fast;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FastStubsTreeView {

    private String name;
    private String endpoint;
    private StubEndpointConfig.TransportTypes transportType;

    /**
     * Constructor.
     */
    public FastStubsTreeView(String endpoint, StubEndpointConfig.TransportTypes transportType) {
        this.name = String.format("%s %s", transportType.toString(), endpoint);
        this.endpoint = endpoint;
        this.transportType = transportType;
    }
}
