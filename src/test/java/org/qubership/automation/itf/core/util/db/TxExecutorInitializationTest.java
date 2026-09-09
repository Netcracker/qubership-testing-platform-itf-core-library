package org.qubership.automation.itf.core.util.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

/**
 * Covers {@link TxExecutor#get()} and the static {@code TransactionDefinition} factory methods.
 *
 * <p>{@link TxExecutor#get()}: a call reaching it before {@link TxExecutor#init} has installed a manager must fail,
 * and must leave the singleton unset so a manager installed afterward still takes effect.</p>
 *
 * <p>The factory methods: each must return the propagation and read-only combination its name promises. This is a
 * regression test for a copy-paste bug where {@link TxExecutor#nestedReadOnlyTransaction()} set {@code readOnly} on
 * the wrong {@code TransactionDefinition} and left its own result writable.</p>
 *
 * <p>{@code INSTANCE} is a static field shared by every caller in the JVM, so each test saves it before
 * running and restores it afterward to avoid leaking state into other tests.</p>
 */
class TxExecutorInitializationTest {

    private PlatformTransactionManager originalInstance;

    private static Field instanceField() throws NoSuchFieldException {
        Field field = TxExecutor.class.getDeclaredField("INSTANCE");
        field.setAccessible(true);
        return field;
    }

    private static PlatformTransactionManager readInstance() throws Exception {
        return (PlatformTransactionManager) instanceField().get(null);
    }

    private static void writeInstance(PlatformTransactionManager manager) throws Exception {
        instanceField().set(null, manager);
    }

    @BeforeEach
    void clearInstance() throws Exception {
        originalInstance = readInstance();
        writeInstance(null);
    }

    @AfterEach
    void restoreInstance() throws Exception {
        writeInstance(originalInstance);
    }

    @Test
    void getThrowsWhenNoManagerWasEverInstalled() {
        assertThrows(IllegalStateException.class, TxExecutor::get);
    }

    @Test
    void aFailedGetLeavesTheSingletonUnset() throws Exception {
        assertThrows(IllegalStateException.class, TxExecutor::get);

        assertNull(readInstance());
    }

    @Test
    void aRealManagerCanStillBeInstalledAfterAFailedGet() throws Exception {
        assertThrows(IllegalStateException.class, TxExecutor::get);

        PlatformTransactionManager real = mock(PlatformTransactionManager.class);
        TxExecutor.init(real);

        assertSame(real, TxExecutor.get());
    }

    @Test
    void executeUsesTheRealManagerInstalledAfterAFailedGet() throws Exception {
        assertThrows(IllegalStateException.class, TxExecutor::get);

        PlatformTransactionManager real = mock(PlatformTransactionManager.class);
        TransactionStatus status = mock(TransactionStatus.class);
        when(real.getTransaction(any())).thenReturn(status);
        TxExecutor.init(real);

        String result = TxExecutor.execute(() -> "done", TxExecutor.defaultWritableTransaction());

        assertEquals("done", result);
        verify(real).getTransaction(TxExecutor.defaultWritableTransaction());
        verify(real).commit(status);
    }

    @Test
    void transactionDefinitionsCarryThePropagationAndReadOnlyFlagTheirNamePromises() {
        assertEquals(TransactionDefinition.PROPAGATION_REQUIRED,
                TxExecutor.defaultWritableTransaction().getPropagationBehavior());
        assertFalse(TxExecutor.defaultWritableTransaction().isReadOnly());

        assertEquals(TransactionDefinition.PROPAGATION_REQUIRED,
                TxExecutor.readOnlyTransaction().getPropagationBehavior());
        assertTrue(TxExecutor.readOnlyTransaction().isReadOnly());

        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                TxExecutor.nestedWritableTransaction().getPropagationBehavior());
        assertFalse(TxExecutor.nestedWritableTransaction().isReadOnly());

        assertEquals(TransactionDefinition.PROPAGATION_REQUIRES_NEW,
                TxExecutor.nestedReadOnlyTransaction().getPropagationBehavior());
        assertTrue(TxExecutor.nestedReadOnlyTransaction().isReadOnly());
    }
}
