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

package org.qubership.automation.itf.core.hibernate.spring.repositories.base;

import java.util.List;

import org.qubership.automation.itf.core.model.common.Storable;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface StorableRepository<T extends Storable> extends RootRepository<T> {

    List<T> findByParentIDAndName(Object parentId, String name);

    List<T> findByParentID(Object parentId);

    List<T> findByParentName(String name);
}
