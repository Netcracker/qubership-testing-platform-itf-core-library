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

package org.qubership.automation.itf.core.util.generator.id;

import java.io.Serializable;
import java.math.BigInteger;
import java.sql.SQLException;
import java.util.concurrent.LinkedBlockingQueue;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SharedSessionContractImplementor;
import org.hibernate.id.IdentifierGenerator;
import org.qubership.automation.itf.core.util.generator.tools.InternalDataBaseSqlExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class UniqueIdGenerator implements IdentifierGenerator {

    private static final LinkedBlockingQueue<BigInteger> IDS = new LinkedBlockingQueue<>();
    private static final LinkedBlockingQueue<BigInteger> IDS_REPORTING = new LinkedBlockingQueue<>();
    public static InternalDataBaseSqlExecutor INTERNAL_DATABASE_SQL_EXECUTOR;

    public UniqueIdGenerator() {
    }

    @Autowired
    public UniqueIdGenerator(InternalDataBaseSqlExecutor internalDataBaseSqlExecutor) {
        INTERNAL_DATABASE_SQL_EXECUTOR = internalDataBaseSqlExecutor;
    }

    /**
     * Get next BigInteger id from pool.
     *
     * @return BigInteger id
     */
    public static Serializable generate() {
        return getNextIdFromQueue();
    }

    /**
     * Hibernate is using this method to get ids (UniqueIdGenerator is set as id generator in hibernate mapping).
     * @param sharedSessionContractImplementor sharedSessionContractImplementor.
     * @param o object.
     * @return BigInteger id.
     * @throws HibernateException if Hibernate can't get\generate id via getNextIdFromQueue() method.
     */
    @Override
    public Serializable generate(SharedSessionContractImplementor sharedSessionContractImplementor, Object o)
            throws HibernateException {
        return getNextIdFromQueue();
    }

    /**
     * Get next BigInteger id from pool, for reporting objects (messages, contexts etc.).
     *
     * @return BigInteger id
     */
    public static Serializable generateReportingId() {
        return getNextReportingIdFromQueue();
    }

    private static synchronized Serializable getNextIdFromQueue() {
        if (IDS.isEmpty()) {
            try {
                IDS.addAll(INTERNAL_DATABASE_SQL_EXECUTOR.selectArrayViaNonParameterizedFunction(true));
            } catch (SQLException e) {
                log.error("Error while generating the next id: " + e);
                throw new HibernateException(e);
            }
        }
        return IDS.poll();
    }

    private static synchronized Serializable getNextReportingIdFromQueue() {
        if (IDS_REPORTING.isEmpty()) {
            try {
                IDS_REPORTING.addAll(INTERNAL_DATABASE_SQL_EXECUTOR.selectArrayViaNonParameterizedFunction(false));
            } catch (SQLException e) {
                log.error("Error while generating the next reporting id: " + e);
                throw new HibernateException(e);
            }
        }
        return IDS_REPORTING.poll();
    }
}
