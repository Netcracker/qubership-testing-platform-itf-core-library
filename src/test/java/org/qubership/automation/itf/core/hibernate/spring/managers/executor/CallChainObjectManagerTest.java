package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import org.apache.commons.lang3.NotImplementedException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.CallChainRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CallChainObjectManagerTest {

    private static final BigInteger CALL_CHAIN_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(300L);
    private static final String CHAIN_NAME = "TestCallChain";
    private static final String LABEL = "test-label";
    private static final String BV_CASE_ID = "test-bv-case-id";

    @Mock
    private CallChainRepository callChainRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private CallChain callChain;

    @Mock
    private Storable nonCallChainStorable;

    @InjectMocks
    private CallChainObjectManager manager;

    private MockedStatic<TxExecutor> txExecutorMock;
    private MockedStatic<IdConverter> idConverterMock;
    private TransactionDefinition mockReadOnlyDef;

    @BeforeEach
    void setUp() throws Exception {
        txExecutorMock = mockStatic(TxExecutor.class);
        idConverterMock = mockStatic(IdConverter.class);

        // Mock readOnlyTransaction() to return a mock TransactionDefinition
        mockReadOnlyDef = mock(TransactionDefinition.class);
        txExecutorMock.when(TxExecutor::readOnlyTransaction).thenReturn(mockReadOnlyDef);

        // Mock executeUnchecked(Callable, TransactionDefinition) to execute the callable
        txExecutorMock.when(() -> TxExecutor.executeUnchecked(any(Callable.class), any(TransactionDefinition.class)))
                .thenAnswer(invocation -> {
                    Callable<?> callable = invocation.getArgument(0);
                    return callable.call();
                });

        // Default behavior for ID conversion
        idConverterMock.when(() -> IdConverter.toBigInt(any())).thenAnswer(invocation -> {
            Object arg = invocation.getArgument(0);
            if (arg instanceof BigInteger) {
                return arg;
            }
            if (arg instanceof Number) {
                return BigInteger.valueOf(((Number) arg).longValue());
            }
            return null;
        });
    }

    @AfterEach
    void tearDown() {
        txExecutorMock.close();
        idConverterMock.close();
    }

    // ==================== protectedOnRemove TESTS ====================

    @Test
    void protectedOnRemove_ShouldCallStepRepositoryOnDeleteCallChain() {
        // given
        when(callChainRepository.getReferenceById(any())).thenReturn(callChain);

        // when
        manager.protectedOnRemove(callChain);

        // then
        verify(stepRepository).onDeleteCallChain(callChain);
    }

    // ==================== findUsages TESTS ====================

    @Test
    void findUsages_WhenStorableIsCallChain_ShouldReturnCallChainsUsages() {
        // given
        when(callChain.getID()).thenReturn(CALL_CHAIN_ID);

        List<BigInteger> callChainIds = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        when(callChainRepository.getIdsCallchains(CALL_CHAIN_ID)).thenReturn(callChainIds);

        CallChain foundChain1 = mock(CallChain.class);
        CallChain foundChain2 = mock(CallChain.class);
        when(callChainRepository.getOne(BigInteger.valueOf(1L))).thenReturn(foundChain1);
        when(callChainRepository.getOne(BigInteger.valueOf(2L))).thenReturn(foundChain2);

        // when
        Collection<UsageInfo> result = manager.findUsages(callChain);

        // then
        verify(callChainRepository).getIdsCallchains(CALL_CHAIN_ID);
        verify(callChainRepository).getOne(BigInteger.valueOf(1L));
        verify(callChainRepository).getOne(BigInteger.valueOf(2L));
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
    }

    @Test
    void findUsages_WhenStorableIsNotCallChain_ShouldReturnEmptyCollection() {
        // given
        when(nonCallChainStorable.getID()).thenReturn(CALL_CHAIN_ID);

        // when
        Collection<UsageInfo> result = manager.findUsages(nonCallChainStorable);

        // then
        verify(callChainRepository, never()).getIdsCallchains(any());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findUsages_WhenNoCallChainsFound_ShouldReturnEmptyCollection() {
        // given
        when(callChain.getID()).thenReturn(CALL_CHAIN_ID);
        when(callChainRepository.getIdsCallchains(CALL_CHAIN_ID)).thenReturn(new ArrayList<>());

        // when
        Collection<UsageInfo> result = manager.findUsages(callChain);

        // then
        verify(callChainRepository, never()).getOne(any());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findUsages_WhenStorableIsNull_ShouldReturnEmptyCollection() {
        // when
        Collection<UsageInfo> result = manager.findUsages(null);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getByLabel TESTS ====================

    @Test
    void getByLabel_WithProjectId_ShouldReturnCallChainsByLabel() {
        // given
        Collection<BigInteger> ids = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        when(callChainRepository.getCallchainIdsByLabel(LABEL, PROJECT_ID)).thenReturn(ids);

        CallChain chain1 = mock(CallChain.class);
        CallChain chain2 = mock(CallChain.class);
        when(callChainRepository.getOne(BigInteger.valueOf(1L))).thenReturn(chain1);
        when(callChainRepository.getOne(BigInteger.valueOf(2L))).thenReturn(chain2);

        // when
        Collection<CallChain> result = manager.getByLabel(LABEL, PROJECT_ID);

        // then
        verify(callChainRepository).getCallchainIdsByLabel(LABEL, PROJECT_ID);
        verify(callChainRepository).getOne(BigInteger.valueOf(1L));
        verify(callChainRepository).getOne(BigInteger.valueOf(2L));
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains(chain1));
        Assertions.assertTrue(result.contains(chain2));
    }

    @Test
    void getByLabel_WithProjectId_WhenNoIdsFound_ShouldReturnEmptyCollection() {
        // given
        when(callChainRepository.getCallchainIdsByLabel(LABEL, PROJECT_ID)).thenReturn(new ArrayList<>());

        // when
        Collection<CallChain> result = manager.getByLabel(LABEL, PROJECT_ID);

        // then
        verify(callChainRepository, never()).getOne(any());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getByLabel_WithProjectId_WhenIdsIsNull_ShouldReturnEmptyCollection() {
        // given
        when(callChainRepository.getCallchainIdsByLabel(LABEL, PROJECT_ID)).thenReturn(null);

        // when
        Collection<CallChain> result = manager.getByLabel(LABEL, PROJECT_ID);

        // then
        verify(callChainRepository, never()).getOne(any());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getByLabel_Deprecated_ShouldReturnNull() {
        // when
        Collection<? extends CallChain> result = manager.getByLabel(LABEL);

        // then
        Assertions.assertNull(result);
    }

    // ==================== getSimpleListByProject TESTS ====================

    @Test
    void getSimpleListByProject_ShouldReturnIdNamePairs() {
        // given
        List<IdNamePair> expectedPairs = List.of(
                new IdNamePair(BigInteger.valueOf(1L), "Chain1"),
                new IdNamePair(BigInteger.valueOf(2L), "Chain2")
        );
        when(callChainRepository.getSimpleListByProject(PROJECT_ID)).thenReturn(expectedPairs);

        // when
        List<IdNamePair> result = manager.getSimpleListByProject(PROJECT_ID);

        // then
        verify(callChainRepository).getSimpleListByProject(PROJECT_ID);
        Assertions.assertSame(expectedPairs, result);
    }

    @Test
    void getSimpleListByProject_WhenNoChains_ShouldReturnEmptyList() {
        // given
        when(callChainRepository.getSimpleListByProject(PROJECT_ID)).thenReturn(new ArrayList<>());

        // when
        List<IdNamePair> result = manager.getSimpleListByProject(PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getSimpleListByProject_WhenProjectIdIsNull_ShouldDelegate() {
        // given
        when(callChainRepository.getSimpleListByProject(null)).thenReturn(new ArrayList<>());

        // when
        List<IdNamePair> result = manager.getSimpleListByProject(null);

        // then
        verify(callChainRepository).getSimpleListByProject(null);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== countBvCaseUsages TESTS ====================

    @Test
    void countBvCaseUsages_ShouldReturnCount() {
        // given
        int expectedCount = 5;
        when(callChainRepository.countBvCaseUsages(BV_CASE_ID)).thenReturn(expectedCount);

        // when
        int result = manager.countBvCaseUsages(BV_CASE_ID);

        // then
        verify(callChainRepository).countBvCaseUsages(BV_CASE_ID);
        Assertions.assertEquals(expectedCount, result);
    }

    @Test
    void countBvCaseUsages_WhenNoUsages_ShouldReturnZero() {
        // given
        when(callChainRepository.countBvCaseUsages(BV_CASE_ID)).thenReturn(0);

        // when
        int result = manager.countBvCaseUsages(BV_CASE_ID);

        // then
        Assertions.assertEquals(0, result);
    }

    @Test
    void countBvCaseUsages_WhenBvCaseIdIsNull_ShouldDelegate() {
        // given
        when(callChainRepository.countBvCaseUsages(null)).thenReturn(0);

        // when
        int result = manager.countBvCaseUsages(null);

        // then
        verify(callChainRepository).countBvCaseUsages(null);
        Assertions.assertEquals(0, result);
    }

    // ==================== getAllLabels TESTS ====================

    @Test
    void getAllLabels_ShouldReturnSetOfLabels() {
        // given
        Set<String> expectedLabels = Set.of("label1", "label2", "label3");
        when(callChainRepository.getAllLabels(PROJECT_ID)).thenReturn(expectedLabels);

        // when
        Set<String> result = manager.getAllLabels(PROJECT_ID);

        // then
        verify(callChainRepository).getAllLabels(PROJECT_ID);
        Assertions.assertSame(expectedLabels, result);
    }

    @Test
    void getAllLabels_WhenNoLabels_ShouldReturnEmptySet() {
        // given
        when(callChainRepository.getAllLabels(PROJECT_ID)).thenReturn(new HashSet<>());

        // when
        Set<String> result = manager.getAllLabels(PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getAllLabels_WhenProjectIdIsNull_ShouldDelegate() {
        // given
        when(callChainRepository.getAllLabels(null)).thenReturn(new HashSet<>());

        // when
        Set<String> result = manager.getAllLabels(null);

        // then
        verify(callChainRepository).getAllLabels(null);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getByNameAndProjectId TESTS ====================

    @Test
    void getByNameAndProjectId_ShouldReturnCallChains() {
        // given
        List<CallChain> expectedChains = List.of(callChain);
        when(callChainRepository.findByNameAndProjectId(CHAIN_NAME, PROJECT_ID)).thenReturn(expectedChains);

        // when
        List<CallChain> result = manager.getByNameAndProjectId(CHAIN_NAME, PROJECT_ID);

        // then
        verify(callChainRepository).findByNameAndProjectId(CHAIN_NAME, PROJECT_ID);
        Assertions.assertSame(expectedChains, result);
    }

    @Test
    void getByNameAndProjectId_WhenNotFound_ShouldReturnEmptyList() {
        // given
        when(callChainRepository.findByNameAndProjectId(CHAIN_NAME, PROJECT_ID)).thenReturn(new ArrayList<>());

        // when
        List<CallChain> result = manager.getByNameAndProjectId(CHAIN_NAME, PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getByNameAndProjectId_WhenNameIsNull_ShouldDelegate() {
        // given
        when(callChainRepository.findByNameAndProjectId(null, PROJECT_ID)).thenReturn(new ArrayList<>());

        // when
        List<CallChain> result = manager.getByNameAndProjectId(null, PROJECT_ID);

        // then
        verify(callChainRepository).findByNameAndProjectId(null, PROJECT_ID);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getByPieceOfNameAndProjectId TESTS ====================

    @Test
    void getByPieceOfNameAndProjectId_ShouldReturnMatchingCallChains() {
        // given
        String pieceOfName = "test";
        List<CallChain> expectedChains = List.of(callChain);
        when(callChainRepository.findByPieceOfNameAndProjectId(pieceOfName, PROJECT_ID))
                .thenReturn(expectedChains);

        // when
        List<CallChain> result = manager.getByPieceOfNameAndProjectId(pieceOfName, PROJECT_ID);

        // then
        verify(callChainRepository).findByPieceOfNameAndProjectId(pieceOfName, PROJECT_ID);
        Assertions.assertSame(expectedChains, result);
    }

    @Test
    void getByPieceOfNameAndProjectId_WhenNotFound_ShouldReturnEmptyList() {
        // given
        String pieceOfName = "nonexistent";
        when(callChainRepository.findByPieceOfNameAndProjectId(pieceOfName, PROJECT_ID))
                .thenReturn(new ArrayList<>());

        // when
        List<CallChain> result = manager.getByPieceOfNameAndProjectId(pieceOfName, PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getByPieceOfNameAndProjectId_WhenNameIsNull_ShouldDelegate() {
        // given
        when(callChainRepository.findByPieceOfNameAndProjectId(null, PROJECT_ID))
                .thenReturn(new ArrayList<>());

        // when
        List<CallChain> result = manager.getByPieceOfNameAndProjectId(null, PROJECT_ID);

        // then
        verify(callChainRepository).findByPieceOfNameAndProjectId(null, PROJECT_ID);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getByProjectId TESTS ====================

    @Test
    void getByProjectId_ShouldReturnCallChainsByProject() {
        // given
        List<CallChain> expectedChains = List.of(callChain);
        when(callChainRepository.findByProjectId(PROJECT_ID)).thenReturn(expectedChains);

        // when
        Collection<CallChain> result = manager.getByProjectId(PROJECT_ID);

        // then
        verify(callChainRepository).findByProjectId(PROJECT_ID);
        Assertions.assertSame(expectedChains, result);
    }

    @Test
    void getByProjectId_WhenNoChains_ShouldReturnEmptyCollection() {
        // given
        when(callChainRepository.findByProjectId(PROJECT_ID)).thenReturn(new ArrayList<>());

        // when
        Collection<CallChain> result = manager.getByProjectId(PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getByProjectId_WhenProjectIdIsNull_ShouldDelegate() {
        // given
        when(callChainRepository.findByProjectId(null)).thenReturn(new ArrayList<>());

        // when
        Collection<CallChain> result = manager.getByProjectId(null);

        // then
        verify(callChainRepository).findByProjectId(null);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getObjectsWithBvLinks TESTS ====================

    @Test
    void getObjectsWithBvLinks_ShouldReturnEmptyList() {
        // given - метод всегда возвращает пустой список согласно реализации

        // when
        List<Object[]> result = manager.getObjectsWithBvLinks(PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(callChainRepository, never()).getCallChainsWithBvLinks(any());
    }

    @Test
    void getObjectsWithBvLinks_WhenProjectIdIsNull_ShouldReturnEmptyList() {
        // when
        List<Object[]> result = manager.getObjectsWithBvLinks(null);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getReceiverSystemsFromCallChainSteps TESTS ====================

    @Test
    void getReceiverSystemsFromCallChainSteps_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getReceiverSystemsFromCallChainSteps(CALL_CHAIN_ID));
        Assertions.assertTrue(ex.getMessage().contains("Method getReceiverSystemsFromCallChainSteps is not implemented"));
    }

    // ==================== getAllIdsAndNamesByProjectId TESTS ====================

    @Test
    void getAllIdsAndNamesByProjectId_ShouldReturnListFromStepRepository() {
        // given
        List<Object[]> expectedResult = List.of(
                new Object[]{BigInteger.valueOf(1L), "Chain1"},
                new Object[]{BigInteger.valueOf(2L), "Chain2"}
        );
        when(stepRepository.findIdAndNameByProjectId(PROJECT_ID)).thenReturn(expectedResult);

        // when
        List<Object[]> result = manager.getAllIdsAndNamesByProjectId(PROJECT_ID);

        // then
        verify(stepRepository).findIdAndNameByProjectId(PROJECT_ID);
        Assertions.assertSame(expectedResult, result);
    }

    @Test
    void getAllIdsAndNamesByProjectId_WhenProjectIdIsNull_ShouldDelegate() {
        // given
        when(stepRepository.findIdAndNameByProjectId(null)).thenReturn(new ArrayList<>());

        // when
        List<Object[]> result = manager.getAllIdsAndNamesByProjectId(null);

        // then
        verify(stepRepository).findIdAndNameByProjectId(null);
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getAllIdsAndNamesByProjectId_WhenNoResults_ShouldReturnEmptyList() {
        // given
        when(stepRepository.findIdAndNameByProjectId(PROJECT_ID)).thenReturn(new ArrayList<>());

        // when
        List<Object[]> result = manager.getAllIdsAndNamesByProjectId(PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_ShouldNotThrowException() {
        // when & then - метод не должен бросать исключение
        Assertions.assertDoesNotThrow(() -> manager.afterDelete(callChain));
    }

    // ==================== Edge Cases ====================

    @Test
    void getByLabel_WithEmptyStringLabel_ShouldReturnEmptyCollection() {
        // given
        when(callChainRepository.getCallchainIdsByLabel("", PROJECT_ID)).thenReturn(new ArrayList<>());

        // when
        Collection<CallChain> result = manager.getByLabel("", PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getByPieceOfNameAndProjectId_WithEmptyStringName_ShouldReturnEmptyList() {
        // given
        when(callChainRepository.findByPieceOfNameAndProjectId("", PROJECT_ID))
                .thenReturn(new ArrayList<>());

        // when
        List<CallChain> result = manager.getByPieceOfNameAndProjectId("", PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}