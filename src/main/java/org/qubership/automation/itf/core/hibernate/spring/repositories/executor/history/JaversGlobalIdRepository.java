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

package org.qubership.automation.itf.core.hibernate.spring.repositories.executor.history;

import java.util.Collection;
import java.util.List;

import org.qubership.automation.itf.core.model.jpa.history.JvGlobalIdEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface JaversGlobalIdRepository extends JpaRepository<JvGlobalIdEntity, Long> {

    List<JvGlobalIdEntity> findAllByOwnerIdIn(Collection<Long> ownerIds);

    @Modifying
    @Query("DELETE FROM JvGlobalIdEntity e WHERE e.id IN (:ids)")
    void deleteByIdIn(@Param("ids") Collection<Long> ids);
}
