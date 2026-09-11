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

import jakarta.inject.Inject;
import jakarta.inject.Named;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
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

    private static final Supplier<PlatformTransactionManager> DEFAULT = () -> {
        throw new IllegalStateException("TxExecutor is not initialized: no \"transactionManager\" "
                + "PlatformTransactionManager has been set via init(...)");
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
        nestedReadonly.setReadOnly(true);
        NESTED_READ_ONLY_DEFINITION = nestedReadonly;
    }

    /**
     * Installs {@code manager} as the shared transaction manager, unless one is already installed.
     */
    @Inject
    public TxExecutor(@Named("transactionManager") PlatformTransactionManager manager) {
        init(manager);
    }

    /**
     * Returns the transaction manager installed through {@link #init}.
     *
     * @throws IllegalStateException if no manager has been installed yet
     */
    public static PlatformTransactionManager get() {
        init(DEFAULT);
        return INSTANCE;
    }

    /**
     * Installs the manager {@code instance} supplies as the shared transaction manager, unless one
     * is already installed.
     *
     * <p>Thread-safe: concurrent callers race to install exactly one manager, and once one is
     * installed, no later call invokes {@code instance.get()}.</p>
     *
     * @param instance supplies the manager to install
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

    /**
     * Installs {@code instance} as the shared transaction manager, unless one is already installed.
     *
     * @param instance the manager to install
     * @see #init(Supplier)
     */
    public static void init(PlatformTransactionManager instance) {
        init(Suppliers.ofInstance(instance)::get);
    }

    /**
     * Returns the default writable transaction definition: {@code PROPAGATION_REQUIRED}, the same
     * propagation {@link Transactional @Transactional} defaults to.
     */
    public static TransactionDefinition defaultWritableTransaction() {
        return DEFAULT_DEFINITION;
    }

    /**
     * Returns the read-only transaction definition: {@code PROPAGATION_REQUIRED} with
     * {@code readOnly} set.
     */
    public static TransactionDefinition readOnlyTransaction() {
        return READ_ONLY_DEFINITION;
    }

    /**
     * Returns the nested read-only transaction definition: {@code PROPAGATION_REQUIRES_NEW} with
     * {@code readOnly} set.
     */
    public static TransactionDefinition nestedReadOnlyTransaction() {
        return NESTED_READ_ONLY_DEFINITION;
    }

    /**
     * Returns the nested writable transaction definition: {@code PROPAGATION_REQUIRES_NEW}.
     *
     * <p>Use it to run an inner transaction that commits or rolls back independently of the
     * caller's transaction.</p>
     */
    public static TransactionDefinition nestedWritableTransaction() {
        return NESTED_WRITABLE;
    }

    /**
     * Runs {@code callable} inside a transaction and returns its result.
     *
     * <p>Obtains a transaction from {@link #get()} using {@code def} and commits it once
     * {@code callable} returns normally. If {@code callable} throws, the transaction is rolled
     * back and the original exception propagates, except that a rollback failure other than
     * {@link UnexpectedRollbackException} replaces it with a new {@link Exception} carrying the
     * original as its cause.</p>
     *
     * @param callable the work to run inside the transaction
     * @param def the transaction definition to open
     * @return the value {@code callable} returns
     * @throws Exception propagated from {@code callable}, or from a failed rollback
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

    /**
     * Same as {@link #execute(Callable, TransactionDefinition)}, using
     * {@link #defaultWritableTransaction()}.
     *
     * @param callable the work to run inside the transaction
     * @return the value {@code callable} returns
     * @throws Exception propagated from {@code callable}, or from a failed rollback
     */
    public static <T> T execute(Callable<T> callable) throws Exception {
        return execute(callable, defaultWritableTransaction());
    }

    /**
     * Runs {@code callable} inside a transaction opened with {@code def}.
     *
     * <p>The transaction comes from {@link #get()}. If {@code callable} throws, the transaction
     * is rolled back and the original exception propagates; a rollback failure is thrown instead,
     * with the original exception attached to it via {@link Throwable#addSuppressed}. On success,
     * the transaction is left open: unlike {@link #execute(Callable, TransactionDefinition)}, this
     * method never calls {@link PlatformTransactionManager#commit}.</p>
     *
     * @param callable the work to run inside the transaction
     * @param def the transaction definition to open
     */
    public static void executeVoid(TxCallable callable, TransactionDefinition def) {
        PlatformTransactionManager instance = get();
        TransactionStatus status = instance.getTransaction(def);
        try {
            callable.execute();
            instance.commit(status);
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

    /**
     * Commits the transaction of the method currently running under Spring's
     * {@code @Transactional} aspect.
     *
     * <p>Commits {@link TransactionAspectSupport#currentTransactionStatus()} directly on the
     * installed manager, ahead of the aspect's own commit when the method returns.</p>
     *
     * @throws NullPointerException if no transaction manager has been installed yet
     * @throws org.springframework.transaction.NoTransactionException if no aspect-managed
     *     transaction is active on the current thread
     */
    public static void commit() {
        INSTANCE.commit(TransactionAspectSupport.currentTransactionStatus());
    }

    /**
     * Same as {@link #execute(Callable, TransactionDefinition)}, without the checked exception.
     *
     * <p>A {@link RuntimeException} or {@link Error} from {@code callable} or from a failed
     * rollback propagates unchanged. A checked exception is silently discarded and {@code null}
     * is returned instead, because {@link Throwables#throwIfUnchecked} only rethrows an unchecked
     * throwable.</p>
     *
     * @param callable the work to run inside the transaction
     * @param def the transaction definition to open
     * @return the value {@code callable} returns, or {@code null} if a checked exception was thrown
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
     * Runs {@code callable} inside a transaction opened with {@code definition}, propagating any
     * failure unchanged.
     *
     * <p>Delegates to {@link #executeVoid(TxCallable, TransactionDefinition)}. Because
     * {@link TxCallable#execute()} declares no checked exception, every exception it or a failed
     * rollback can throw is already unchecked, so {@link Throwables#throwIfUnchecked} always
     * rethrows it.</p>
     *
     * @param callable the work to run inside the transaction
     * @param definition the transaction definition to open
     */
    public static void executeUnchecked(TxCallable callable, TransactionDefinition definition) {
        try {
            executeVoid(callable, definition);
        } catch (Exception e) {
            Throwables.throwIfUnchecked(e);
        }
    }

    /**
     * Same as {@link #executeUnchecked(Callable, TransactionDefinition)}, using
     * {@link #defaultWritableTransaction()}.
     *
     * @param callable the work to run inside the transaction
     * @return the value {@code callable} returns, or {@code null} if a checked exception was thrown
     */
    public static <T> T executeUnchecked(Callable<T> callable) {
        return executeUnchecked(callable, defaultWritableTransaction());
    }

}
