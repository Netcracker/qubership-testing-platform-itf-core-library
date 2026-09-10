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
import java.util.List;
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

    private static final IdPool IDS = new IdPool(true);
    private static final IdPool IDS_REPORTING = new IdPool(false);
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
        return IDS.next();
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
        return IDS.next();
    }

    /**
     * Get next BigInteger id from pool, for reporting objects (messages, contexts etc.).
     *
     * @return BigInteger id
     */
    public static Serializable generateReportingId() {
        return IDS_REPORTING.next();
    }

    /**
     * A pool of pre-fetched ids for one id space, refilled from the database on demand.
     *
     * <p>A refill releases {@link #lock} before the database round trip and reacquires it only to
     * add the fetched batch to {@link #queue} and to hand the caller doing the refill its own id, so
     * a slow or unreachable database blocks callers of this pool alone. At most one refill runs at a
     * time per pool: a caller that finds the queue empty while another refill is already in flight
     * waits for that refill instead of starting a second one.</p>
     */
    private static final class IdPool {

        private final LinkedBlockingQueue<BigInteger> queue = new LinkedBlockingQueue<>();
        private final Object lock = new Object();
        private final boolean forConfigObjects;
        private boolean refillInProgress;

        private IdPool(boolean forConfigObjects) {
            this.forConfigObjects = forConfigObjects;
        }

        private BigInteger next() {
            BigInteger id = queue.poll();
            if (id != null) {
                return id;
            }
            synchronized (lock) {
                while (true) {
                    id = queue.poll();
                    if (id != null) {
                        return id;
                    }
                    if (!refillInProgress) {
                        refillInProgress = true;
                        break;
                    }
                    waitForRefill();
                }
            }
            try {
                return refill();
            } finally {
                synchronized (lock) {
                    refillInProgress = false;
                    lock.notifyAll();
                }
            }
        }

        private void waitForRefill() {
            try {
                lock.wait();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new HibernateException(e);
            }
        }

        private BigInteger refill() {
            List<BigInteger> batch;
            try {
                batch = INTERNAL_DATABASE_SQL_EXECUTOR.selectArrayViaNonParameterizedFunction(forConfigObjects);
            } catch (SQLException e) {
                log.error("Error while generating the next id: " + e);
                throw new HibernateException(e);
            }
            synchronized (lock) {
                queue.addAll(batch);
                return queue.poll();
            }
        }
    }
}
