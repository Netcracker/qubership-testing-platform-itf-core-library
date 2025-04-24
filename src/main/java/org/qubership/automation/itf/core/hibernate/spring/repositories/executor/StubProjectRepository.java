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
import java.util.UUID;

import org.javers.spring.annotation.JaversSpringDataAuditable;
import org.qubership.automation.itf.core.hibernate.spring.repositories.base.RootRepository;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@JaversSpringDataAuditable
@Repository
public interface StubProjectRepository extends RootRepository<StubProject> {

    @Modifying
    @Query(value = "SET session_replication_role = 'replica'", nativeQuery = true)
    void setReplicationRoleReplica();

    @Modifying
    @Query(value = "SET session_replication_role = 'origin'", nativeQuery = true)
    void setReplicationRoleOrigin();

    @Query(value = "select id from mb_projects where uuid = :projectUuid", nativeQuery = true)
    BigInteger getEntityInternalIdByUuid(@Param("projectUuid") UUID projectUuid);

    @Query(value = "select project from StubProject as project where uuid = :projectUuid")
    StubProject getByUuid(@Param("projectUuid") UUID projectUuid);

    @Query(value = "select value from mb_userdata "
            + "where userkey = :keyParam and project_id = :projectId", nativeQuery = true)
    List<String> getData(@Param("keyParam") String keyParam, @Param("projectId") BigInteger projectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "insert into mb_userdata (project_id, userkey, value) "
            + "values (:projectId, :keyParam, :valueParam)", nativeQuery = true)
    void setData(@Param("keyParam") String keyParam,
                 @Param("valueParam") String valueParam,
                 @Param("projectId") BigInteger projectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update mb_userdata SET value = :valueParam where userkey = :keyParam and project_id = :projectId",
            nativeQuery = true)
    void updateData(@Param("keyParam") String keyParam,
                    @Param("valueParam") String valueParam,
                    @Param("projectId") BigInteger projectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "insert into mb_userdata (project_id, userkey, value) "
            + "values (:projectId, :keyParam, :valueParam) "
            + "on conflict (userkey, project_id) do "
            + "update set value = :valueParam where EXCLUDED.userkey = :keyParam and EXCLUDED.project_id = :projectId",
            nativeQuery = true)
    void upsertData(@Param("keyParam") String keyParam,
                    @Param("valueParam") String valueParam,
                    @Param("projectId") BigInteger projectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "delete from mb_userdata where userkey = :keyParam and project_id = :projectId",
            nativeQuery = true)
    void deleteData(@Param("keyParam") String keyParam, @Param("projectId") BigInteger projectId);

    @Query(value = "select clear_user_data_func(:leaveDays)", nativeQuery = true)
    String clearUserData(@Param("leaveDays") Integer leaveDays);

    /*
     *  Query to get project id and uuid having system.id.
     *  mb_projects.uuid select list item is forcibly converted to char to avoid Hibernate exception:
     *      org.springframework.orm.jpa.JpaSystemException: No Dialect mapping for JDBC type: 1111;
     *      nested exception is org.hibernate.MappingException: No Dialect mapping for JDBC type: 1111
     *  See: https://github.com/spring-projects/spring-data-jpa/issues/1796?ysclid=ldkh7lkctv992737561,
     *          https://hibernate.atlassian.net/browse/HHH-12926
     *
     *  Returns: [project.id, project.uuid]
     */
    @Query(value = "select mb_systems.project_id as id, ''||mb_projects.uuid as project_uuid "
            + "from mb_systems "
            + "inner join mb_projects on mb_systems.project_id = mb_projects.id "
            + "where mb_systems.id = :systemId",
            nativeQuery = true)
    List<String[]> determineProjectIdsBySystemId(@Param("systemId") BigInteger systemId);

    @Query(value = "select prop_value from mb_project_settings "
            + "where project_id = :projectId and prop_short_name = :propertyName",
            nativeQuery = true)
    String getProjectSetting(@Param("projectId") BigInteger projectId, @Param("propertyName") String propertyName);

    @Query(value = "select prop_short_name, prop_value from mb_project_settings "
            + "where project_id = :projectId",
            nativeQuery = true)
    List<Object[]> getAllProjectSettingsByProjectId(@Param("projectId") BigInteger projectId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query(value = "update mb_project_settings set prop_value = :propValue "
            + "where project_id = :projectId and prop_short_name = :propShortName", nativeQuery = true)
    void updateProjectSetting(@Param("projectId") BigInteger projectId, @Param("propShortName") String propShortName,
                              @Param("propValue") String propValue);

}
