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

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;

/**
 * Fails when {@link TxExecutor#executeVoid} leaves the transaction it opened uncommitted after
 * a {@link TxCallable} returns normally.
 *
 * <p>A transaction opened by executeVoid must be committed on the success path and rolled back,
 * never committed, when the callable throws. TxExecutor keeps its {@link PlatformTransactionManager}
 * in a static field that {@link TxExecutor#init(PlatformTransactionManager)} sets only once, so each
 * test clears that field through reflection before installing its own mock; otherwise the manager
 * one test installs would leak into the next.</p>
 */
class TxExecutorTest {

    private Field instanceField;

    @BeforeEach
    void resetStaticInstance() throws Exception {
        instanceField = TxExecutor.class.getDeclaredField("INSTANCE");
        instanceField.setAccessible(true);
        instanceField.set(null, null);
    }

    @AfterEach
    void clearStaticInstance() throws Exception {
        instanceField.set(null, null);
    }

    @Test
    void executeVoidCommitsOnSuccess() {
        PlatformTransactionManager ptm = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(ptm.getTransaction(any())).thenReturn(status);
        TxExecutor.init(ptm);

        TxExecutor.executeVoid(() -> { }, TxExecutor.defaultWritableTransaction());

        verify(ptm, times(1)).commit(status);
        verify(ptm, never()).rollback(any());
    }

    @Test
    void executeVoidRollsBackAndPropagatesOnFailure() {
        PlatformTransactionManager ptm = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(ptm.getTransaction(any())).thenReturn(status);
        TxExecutor.init(ptm);
        RuntimeException failure = new RuntimeException("write failed");

        RuntimeException thrown = assertThrows(RuntimeException.class,
                () -> TxExecutor.executeVoid(() -> {
                    throw failure;
                }, TxExecutor.defaultWritableTransaction()));

        assertSame(failure, thrown);
        verify(ptm, times(1)).rollback(status);
        verify(ptm, never()).commit(any());
    }

    @Test
    void executeUncheckedCommitsForAVoidMethodReference() {
        // Mirrors EnvironmentObjectManager.updateInitialEnvState(), where the repository method
        // returns void and overload resolution picks executeUnchecked(TxCallable, ...).
        PlatformTransactionManager ptm = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(ptm.getTransaction(any())).thenReturn(status);
        TxExecutor.init(ptm);
        VoidRepository repository = mock(VoidRepository.class);

        TxExecutor.executeUnchecked(repository::write, TxExecutor.defaultWritableTransaction());

        verify(repository, times(1)).write();
        verify(ptm, times(1)).commit(status);
    }

    private interface VoidRepository {
        void write();
    }
}
