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

package org.qubership.automation.itf.core.hibernate.spring.repositories.reports;

import org.qubership.automation.itf.core.hibernate.spring.repositories.base.SearchRepository;
import org.qubership.automation.itf.core.model.jpa.context.TcContext;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TcContextRepository extends SearchRepository<TcContext> {

    @Modifying
    @Query(value = "truncate mb_context cascade", nativeQuery = true)
    void truncateContexts();

    @Query(value = "select clear_monitoring_data_func(:clear_hours)", nativeQuery = true)
    String clearMonitoringData(@Param("clear_hours") int clearHours);

    @Modifying
    @Query(value = "update mb_context "
            + "set status = 'STOPPED', "
            + "end_time = LOCALTIMESTAMP(3) "
            + "where \"type\" = 'TcContext' "
            + "  and status = 'IN_PROGRESS' "
            + "  and (EXTRACT(epoch from LOCALTIMESTAMP(0)) * 1000 - last_update_time > time_to_live)",
            nativeQuery = true)
    int updateStatusContextWithStatusInProgress();
}

