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

package org.qubership.automation.itf.core.util.transport.service;

import java.rmi.RemoteException;

import org.qubership.automation.itf.core.util.exception.ExportException;
import org.qubership.automation.itf.core.util.exception.NoDeployedTransportException;
import org.qubership.automation.itf.core.util.transport.access.AccessTransport;
import org.qubership.automation.itf.core.util.transport.base.Transport;
import org.qubership.automation.itf.core.util.transport.registry.base.TransportRegistry;

public class Wrapper {

    private Wrapper() {
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static AccessTransport wrap(Transport transport, TransportRegistry transportRegistry)
            throws ExportException {
        AccessTransport accessTransport;
        try {
            accessTransport = transportRegistry.find(transport.getClass().getName());
            transport.setRemote(accessTransport);
            return accessTransport;
        } catch (NoDeployedTransportException | RemoteException e) {
            throw new ExportException("Error wrapping object to export", e);
        }
    }
}
