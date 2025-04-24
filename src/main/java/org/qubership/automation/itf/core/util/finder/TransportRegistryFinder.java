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

package org.qubership.automation.itf.core.util.finder;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import org.qubership.automation.itf.core.util.transport.registry.base.TransportRegistry;

public class TransportRegistryFinder {

    private static TransportRegistryFinder INSTANCE = new TransportRegistryFinder();

    public static TransportRegistryFinder getInstance() {
        return INSTANCE;
    }

    private TransportRegistry transportRegistry;

    private TransportRegistryFinder() {
    }

    public void setTransportRegistry(TransportRegistry transportRegistry) {
        this.transportRegistry = transportRegistry;
    }

    public TransportRegistry find() throws NotBoundException, RemoteException {
        return findByLoader();
    }

    private TransportRegistry findByLoader() throws NotBoundException {
        if (transportRegistry != null) {
            return transportRegistry;
        }
        throw new NotBoundException("No Transport Registry");
    }
}
