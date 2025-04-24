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

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.qubership.automation.itf.core.model.jpa.system.System;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FastConfigurationRequest implements Serializable {
    private static final long serialVersionUID = 20250303L;

    private List<FastTransportConfig> transportConfigs;
    private String projectUuid;

    @Getter
    @Setter
    public static class FastTransportConfig implements Serializable {
        private static final long serialVersionUID = 20250303L;

        private StubEndpointConfig.TransportTypes transportType;
        private LinkedList<FastSystem> systems;
    }

    @Getter
    @Setter
    public static class FastSystem implements Serializable {
        private static final long serialVersionUID = 20250303L;

        private String id;
        private List<FastOperation> operations;
        private System storableSystem;
    }

    @Getter
    @Setter
    public static class FastOperation implements Serializable {
        private static final long serialVersionUID = 20250303L;

        private String id;
        private List<FastSituation> situations;
    }

    @Getter
    @Setter
    public static class FastSituation implements Serializable {
        private static final long serialVersionUID = 20250303L;

        private String id;
    }
}
