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

package org.qubership.automation.itf.core.util.generator.prefix;

import org.qubership.automation.itf.core.model.common.Storable;

public interface IPrefixFactory {

    /** getPrefix method.
     * @param clazz - object class to generate prefix.
     * @return prefix generated according to clazz
     */
    Object getPrefix(Class<? extends Storable> clazz);

    /** getPrefix method.
     * @param object object which extends from {@link Storable}
     * @see #getPrefix(Class)
     */
    Object getPrefix(Storable object);

    Class<? extends Storable> getClassByPrefix(Object prefix);

    Object removePrefix(Object id);

}
