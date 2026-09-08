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

package org.qubership.automation.itf.core.util.generator.tools;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InternalDataBaseSqlExecutor {

    private static final String GET_CONFIG_IDS_QUERY = "select getid() from (select generate_series(1,1000)) sto";
    private static final String GET_REPORTING_IDS_QUERY = "select nextval('serial') from generate_series(1,1000)";
    private final DataSource dataSource;
    @Value("${itf.generator.id.query-timeout-seconds:30}")
    private int queryTimeoutSeconds;

    @Autowired
    public InternalDataBaseSqlExecutor(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    /**
     * Gets a list of BigInteger ids from the internal main database.
     *
     * @return List of BigInteger ids
     * @throws SQLException if can't connect to db or sql execution failed.
     */
    public List<BigInteger> selectArrayViaNonParameterizedFunction(boolean forConfigObjects) throws SQLException {
        List<BigInteger> values = new ArrayList<>();
        try (Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement()) {
            stmt.setQueryTimeout(queryTimeoutSeconds);
            try (ResultSet rs = stmt.executeQuery(forConfigObjects ? GET_CONFIG_IDS_QUERY : GET_REPORTING_IDS_QUERY)) {
                while (rs.next()) {
                    values.add(rs.getBigDecimal(1).toBigInteger());
                }
            }
        }
        return values;
    }
}
