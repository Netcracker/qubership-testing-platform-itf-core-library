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

package org.qubership.automation.itf.core.hibernate.spring.repositories.executor;

import java.math.BigInteger;
import java.util.List;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.StorableRepository;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FolderRepository extends StorableRepository<Folder>, QuerydslPredicateExecutor<Folder> {

    @Query(value = "select folder.* from mb_folders folder "
                 + "where upper(folder.name) like concat('%', upper(:pieceOfName), '%') "
                 + "and folder.\"type\" = :typeName "
                 + "and folder.project_id = :projectId",
            nativeQuery = true)
    List<Folder> findFolderByProjectAndNameContainingIgnoreCase(@Param("pieceOfName") String pieceOfName,
                                                                @Param("projectId") BigInteger projectId,
                                                                @Param("typeName") String typeName);
}
