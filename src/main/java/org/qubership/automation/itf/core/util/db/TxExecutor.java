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

package org.qubership.automation.itf.core.util.db;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

import javax.inject.Inject;
import javax.inject.Named;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.transaction.support.DefaultTransactionDefinition;

import com.google.common.base.Suppliers;
import com.google.common.base.Throwables;

/**
 * Factory with an ability to pass an instance thru constructor using DI.
 * If instance is not set before 'get' method invoked, the DEFAULT strategy is used
 * You can {@link #init(Supplier)} singleton manually
 */

@Service
public class TxExecutor {

    private static final Supplier<PlatformTransactionManager> DEFAULT = () -> new PlatformTransactionManager() {
        @Override
        public TransactionStatus getTransaction(TransactionDefinition transactionDefinition)
                throws TransactionException {
            return null;
        }

        @Override
        public void commit(TransactionStatus transactionStatus) throws TransactionException {

        }

        @Override
        public void rollback(TransactionStatus transactionStatus) throws TransactionException {

        }
    };

    /**
     * same as {@link Transactional}.
     */
    private static final TransactionDefinition DEFAULT_DEFINITION;
    private static final TransactionDefinition READ_ONLY_DEFINITION;
    private static final TransactionDefinition NESTED_WRITABLE;
    private static final TransactionDefinition NESTED_READ_ONLY_DEFINITION;
    private static volatile PlatformTransactionManager INSTANCE;

    static {
        DEFAULT_DEFINITION = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        DefaultTransactionDefinition readonly =
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRED);
        readonly.setReadOnly(true);
        READ_ONLY_DEFINITION = readonly;
        NESTED_WRITABLE = new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        DefaultTransactionDefinition nestedReadonly =
                new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        readonly.setReadOnly(true);
        NESTED_READ_ONLY_DEFINITION = nestedReadonly;
    }

    @Inject
    public TxExecutor(@Named("transactionManager") PlatformTransactionManager manager) {
        init(manager);
    }

    public static PlatformTransactionManager get() {
        init(DEFAULT);
        return INSTANCE;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static void init(Supplier<PlatformTransactionManager> instance) {
        if (INSTANCE == null) {
            synchronized (TxExecutor.class) {
                if (INSTANCE == null) {
                    INSTANCE = instance.get();
                }
            }
        }
    }

    public static void init(PlatformTransactionManager instance) {
        init(Suppliers.ofInstance(instance)::get);
    }

    public static TransactionDefinition defaultWritableTransaction() {
        return DEFAULT_DEFINITION;
    }

    public static TransactionDefinition readOnlyTransaction() {
        return READ_ONLY_DEFINITION;
    }

    public static TransactionDefinition nestedReadOnlyTransaction() {
        return NESTED_READ_ONLY_DEFINITION;
    }

    /**
     * used when you want to do extra nested writable transaction.
     */
    public static TransactionDefinition nestedWritableTransaction() {
        return NESTED_WRITABLE;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static <T> T execute(Callable<T> callable, TransactionDefinition def) throws Exception {
        PlatformTransactionManager instance = get();
        TransactionStatus status = instance.getTransaction(def);
        try {
            T result = callable.call();
            instance.commit(status);
            return result;
        } catch (Exception ex) {
            try {
                instance.rollback(status);
            } catch (UnexpectedRollbackException ex2) {
                // Silently go away with source exception
            } catch (Exception ex2) {
                throw new Exception("Exception while rolling back: " + ex2.getMessage()
                        + "; initial exception is: ", ex);
            }
            throw ex;
        }
    }

    public static <T> T execute(Callable<T> callable) throws Exception {
        return execute(callable, defaultWritableTransaction());
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static void executeVoid(TxCallable callable, TransactionDefinition def) {
        PlatformTransactionManager instance = get();
        TransactionStatus status = instance.getTransaction(def);
        try {
            callable.execute();
        } catch (Exception ex) {
            try {
                instance.rollback(status);
            } catch (Exception ex2) {
                ex2.addSuppressed(ex);
                throw ex2;
            }
            throw ex;
        }
    }

    public static void commit() {
        INSTANCE.commit(TransactionAspectSupport.currentTransactionStatus());
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static <T> T executeUnchecked(Callable<T> callable, TransactionDefinition def) {
        try {
            return execute(callable, def);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
        }
        return null; //stub, will never be called.
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static void executeUnchecked(TxCallable callable, TransactionDefinition definition) {
        try {
            executeVoid(callable, definition);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
        }
    }

    public static <T> T executeUnchecked(Callable<T> callable) {
        return executeUnchecked(callable, defaultWritableTransaction());
    }

}
