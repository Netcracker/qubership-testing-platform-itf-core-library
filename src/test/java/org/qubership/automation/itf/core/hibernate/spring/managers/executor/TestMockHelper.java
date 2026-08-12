package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;

import java.math.BigInteger;
import java.util.concurrent.Callable;

import org.mockito.MockedStatic;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxCallable;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

/**
 * Helper class for setting up common mocks in ObjectManager tests.
 * Use in @BeforeEach method of test classes.
 */
public class TestMockHelper {
    private TestMockHelper() {
        // Utility class
    }

    /**
     * Setup mocks for TxExecutor and IdConverter static methods.
     *
     * @param txExecutorMock MockedStatic for TxExecutor (to be closed in @AfterEach)
     * @param idConverterMock MockedStatic for IdConverter (to be closed in @AfterEach)
     * @param mockTxDef TransactionDefinition instance.
     */
    public static void setupCommonMocks(
            MockedStatic<TxExecutor> txExecutorMock,
            MockedStatic<IdConverter> idConverterMock,
            TransactionDefinition mockTxDef) {

        txExecutorMock.when(TxExecutor::defaultWritableTransaction).thenReturn(mockTxDef);
        txExecutorMock.when(TxExecutor::readOnlyTransaction).thenReturn(mockTxDef);
        txExecutorMock.when(TxExecutor::nestedWritableTransaction).thenReturn(mockTxDef);

        // For methods with Callable (return value)
        txExecutorMock.when(() -> TxExecutor.executeUnchecked(any(Callable.class), any(TransactionDefinition.class)))
                .thenAnswer(invocation -> {
                    Callable<?> callable = invocation.getArgument(0);
                    return callable.call();
                });

        // For methods with TxCallable (void)
        txExecutorMock.when(() -> TxExecutor.executeUnchecked(any(TxCallable.class), any(TransactionDefinition.class)))
                .thenAnswer(invocation -> {
                    TxCallable txCallable = invocation.getArgument(0);
                    txCallable.execute();
                    return null;
                });

        // For methods with Callable (without TransactionDefinition)
        txExecutorMock.when(() -> TxExecutor.executeUnchecked(any(Callable.class)))
                .thenAnswer(invocation -> {
                    Callable<?> callable = invocation.getArgument(0);
                    return callable.call();
                });

        idConverterMock.when(() -> IdConverter.toBigInt(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            switch (arg) {
                case null -> {
                    return null;
                }
                case BigInteger bigInteger -> {
                    return bigInteger;
                }
                case Number number -> {
                    return BigInteger.valueOf(number.longValue());
                }
                case String s -> {
                    try {
                        return new BigInteger(s);
                    } catch (NumberFormatException e) {
                        return null;
                    }
                }
                default -> {
                }
            }
            return null;
        });
    }
}
