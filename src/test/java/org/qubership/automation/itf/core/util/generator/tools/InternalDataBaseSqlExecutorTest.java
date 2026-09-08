/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import javax.sql.DataSource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * CONC-11: the id refill used to connect with {@code DriverManager} outside the connection pool
 * and left the statement's query timeout at 0 (wait indefinitely). It must now take its connection
 * from the configured {@link DataSource} and bound the query with a timeout.
 */
@ExtendWith(MockitoExtension.class)
class InternalDataBaseSqlExecutorTest {

    private static final int QUERY_TIMEOUT_SECONDS = 15;

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private Statement statement;

    @Mock
    private ResultSet resultSet;

    private InternalDataBaseSqlExecutor executor;

    @BeforeEach
    void setUp() throws Exception {
        executor = new InternalDataBaseSqlExecutor(dataSource);
        Field timeoutField = InternalDataBaseSqlExecutor.class.getDeclaredField("queryTimeoutSeconds");
        timeoutField.setAccessible(true);
        timeoutField.set(executor, QUERY_TIMEOUT_SECONDS);

        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.createStatement()).thenReturn(statement);
        when(statement.executeQuery(anyString())).thenReturn(resultSet);
    }

    @Test
    void selectArrayTakesTheConnectionFromTheConfiguredDataSourceAndBoundsTheQuery() throws SQLException {
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getBigDecimal(1)).thenReturn(BigDecimal.ONE);

        executor.selectArrayViaNonParameterizedFunction(true);

        verify(dataSource).getConnection();
        verify(statement).setQueryTimeout(QUERY_TIMEOUT_SECONDS);
    }

    @Test
    void selectArrayQueriesTheConfigIdSpaceWhenForConfigObjectsIsTrue() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        executor.selectArrayViaNonParameterizedFunction(true);

        verify(statement).executeQuery(eq("select getid() from (select generate_series(1,1000)) sto"));
    }

    @Test
    void selectArrayQueriesTheReportingIdSpaceWhenForConfigObjectsIsFalse() throws SQLException {
        when(resultSet.next()).thenReturn(false);

        executor.selectArrayViaNonParameterizedFunction(false);

        verify(statement).executeQuery(eq("select nextval('serial') from generate_series(1,1000)"));
    }

    @Test
    void selectArrayConvertsEveryRowToABigInteger() throws SQLException {
        when(resultSet.next()).thenReturn(true, true, false);
        when(resultSet.getBigDecimal(1)).thenReturn(BigDecimal.valueOf(7), BigDecimal.valueOf(42));

        List<BigInteger> ids = executor.selectArrayViaNonParameterizedFunction(true);

        assertEquals(List.of(BigInteger.valueOf(7), BigInteger.valueOf(42)), ids);
    }

    @Test
    void selectArrayClosesTheStatementAndConnectionEvenWhenTheQueryFails() throws SQLException {
        when(statement.executeQuery(anyString())).thenThrow(new SQLException("query failed"));

        assertThrows(SQLException.class, () -> executor.selectArrayViaNonParameterizedFunction(true));

        verify(statement).close();
        verify(connection).close();
        verify(resultSet, never()).next();
    }
}
