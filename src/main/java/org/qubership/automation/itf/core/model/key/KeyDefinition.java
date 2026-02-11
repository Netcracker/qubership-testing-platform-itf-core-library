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

package org.qubership.automation.itf.core.model.key;

import java.math.BigInteger;

import org.qubership.automation.itf.core.model.common.Identified;
import org.qubership.automation.itf.core.model.common.Named;
import org.qubership.automation.itf.core.model.common.OptimisticLockable;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.context.InstanceContext;
import org.qubership.automation.itf.core.util.exception.KeyDefinitionException;

public interface KeyDefinition extends Storable, Named, Identified<BigInteger>, OptimisticLockable<Object> {

    String defineKey(InstanceContext context) throws KeyDefinitionException;

    String getExpression();

    void setExpression(String expression);
}
