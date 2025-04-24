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

package org.qubership.automation.itf.core.hibernate.spring.managers.custom;

import java.math.BigInteger;
import java.util.Collection;

import org.qubership.automation.itf.core.hibernate.spring.managers.base.ObjectManager;
import org.qubership.automation.itf.core.model.eci.EciConfigurable;

public interface EnvConfigurationManager<T extends EciConfigurable> extends ObjectManager<T> {
    T getByEcId(String ecId, Object... objects);

    Collection<T> getByEcProjectId(String ecProjectId);

    Collection<String> getEcProjectIds(BigInteger projectId);

    void unbindByEcProject(String ecProjectId);

    T findByEcLabel(String ecLabel, BigInteger projectId);
}


