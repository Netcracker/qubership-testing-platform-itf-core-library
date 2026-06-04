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
import java.util.Map;
import java.util.Set;

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
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.EnvironmentRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SystemRepository;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SystemObjectManagerTest {

    private static final BigInteger SYSTEM_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(300L);
    private static final String SYSTEM_NAME = "TestSystem";
    private static final String EC_ID = "ec-id-123";
    private static final String EC_PROJECT_ID = "ec-project-456";
    private static final String EC_LABEL = "test-label";
    private static final String OPERATION_NAME = "TestOperation";

    @Mock
    private SystemRepository systemRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private System system;

    @Mock
    private Storable nonSystemStorable;

    @Mock
    private Environment environment;

    @Mock
    private Step step;

    @Mock
    private Operation operation;

    @InjectMocks
    private SystemObjectManager manager;

    private MockedStatic<TxExecutor> txExecutorMock;
    private MockedStatic<IdConverter> idConverterMock;
    private TransactionDefinition mockTxDef;

    @BeforeEach
    void setUp() throws Exception {
        txExecutorMock = mockStatic(TxExecutor.class);
        idConverterMock = mockStatic(IdConverter.class);
        mockTxDef = mock(TransactionDefinition.class);

        TestMockHelper.setupCommonMocks(txExecutorMock, idConverterMock, mockTxDef);
    }

    @AfterEach
    void tearDown() {
        txExecutorMock.close();
        idConverterMock.close();
    }

    // ==================== protectedOnRemove TESTS ====================

    @Test
    void protectedOnRemove_ShouldDeleteStepReferencesAndRemoveFromEnvironments() {
        // given
        when(systemRepository.getReferenceById(any())).thenReturn(system);

        Set<Environment> outboundEnvs = new HashSet<>();
        outboundEnvs.add(environment);
        when(environmentRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                .thenReturn(outboundEnvs)
                .thenReturn(new HashSet<>());

        // when
        manager.protectedOnRemove(system);

        // then
        verify(stepRepository).onDeleteSystemSender(system);
        verify(stepRepository).onDeleteSystemReceiver(system);
        verify(environment).getOutbound();
        verify(environment).store();
    }

    // ==================== findUsages TESTS ====================

    @Test
    void findUsages_WhenStorableIsSystem_ShouldReturnAllUsages() {
        // given
        List<Step> stepsWithSender = List.of(mock(Step.class), mock(Step.class));
        List<Step> stepsWithReceiver = List.of(mock(Step.class));
        Set<Environment> envsOut = Set.of(mock(Environment.class), mock(Environment.class));
        Set<Environment> envsIn = Set.of(mock(Environment.class));

        when(stepRepository.getIntegrationStepsBySender(system)).thenReturn(stepsWithSender);
        when(stepRepository.getIntegrationStepsByReceiver(system)).thenReturn(stepsWithReceiver);
        when(environmentRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                .thenReturn(envsOut)
                .thenReturn(envsIn);

        // when
        Collection<UsageInfo> result = manager.findUsages(system);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        verify(stepRepository).getIntegrationStepsBySender(system);
        verify(stepRepository).getIntegrationStepsByReceiver(system);
    }

    @Test
    void findUsages_WhenStorableIsNotSystem_ShouldReturnEmptyCollection() {
        // when
        Collection<UsageInfo> result = manager.findUsages(nonSystemStorable);

        // then
        verify(stepRepository, never()).getIntegrationStepsBySender(any());
        verify(stepRepository, never()).getIntegrationStepsByReceiver(any());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== findImportantChildren TESTS ====================

    @Test
    void findImportantChildren_WhenStorableIsSystem_ShouldReturnTriggerIds() {
        // given
        List<BigInteger> transportTriggers = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        List<BigInteger> seTriggers = List.of(BigInteger.valueOf(3L), BigInteger.valueOf(4L));

        when(system.getID()).thenReturn(SYSTEM_ID);
        when(systemRepository.getTransportTriggersBySystemId(SYSTEM_ID)).thenReturn(transportTriggers);
        when(systemRepository.getSituationEventTriggersBySystemId(SYSTEM_ID)).thenReturn(seTriggers);

        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(system);

        // then
        Assertions.assertTrue(result.containsKey("TransportTriggers"));
        Assertions.assertTrue(result.containsKey("SituationEventTriggers"));
        Assertions.assertEquals(transportTriggers, result.get("TransportTriggers"));
        Assertions.assertEquals(seTriggers, result.get("SituationEventTriggers"));
    }

    @Test
    void findImportantChildren_WhenStorableIsNotSystem_ShouldDelegateToSuper() {
        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(nonSystemStorable);

        // then
        verify(systemRepository, never()).getTransportTriggersBySystemId(any());
        verify(systemRepository, never()).getSituationEventTriggersBySystemId(any());
        Assertions.assertTrue(result == null || result.isEmpty());
    }

    // ==================== getChildByClass TESTS ====================

    @Test
    void getChildByClass_WithOperationClassAndValidParam_ShouldReturnOperation() {
        // given
        when(system.getID()).thenReturn(SYSTEM_ID);
        when(systemRepository.findFirstByDefineOperation(SYSTEM_ID, OPERATION_NAME))
                .thenReturn(operation);

        // when
        Storable result = manager.getChildByClass(system, Operation.class, OPERATION_NAME);

        // then
        Assertions.assertSame(operation, result);
        verify(systemRepository).findFirstByDefineOperation(SYSTEM_ID, OPERATION_NAME);
    }

    @Test
    void getChildByClass_WithNonOperationClass_ShouldThrowNotImplementedException() {
        // when & then
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getChildByClass(system, Environment.class, "param"));
        Assertions.assertTrue(ex.getMessage().contains("Not implemented for classes other than"));
    }

    @Test
    void getChildByClass_WithOperationClassButNullParam_ShouldThrowException() {
        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.getChildByClass(system, Operation.class, null));
        Assertions.assertTrue(ex.getMessage().contains("The 1st param should not be null"));
    }

    // ==================== getChildrenByClass TESTS ====================

    @Test
    void getChildrenByClass_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getChildrenByClass(system, Operation.class));
        Assertions.assertNotNull(ex);
    }

    // ==================== getByLabel TESTS ====================

    @Test
    void getByLabel_WithoutProjectId_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getByLabel(EC_LABEL));
        Assertions.assertTrue(ex.getMessage().contains("Method getByLabel is not implemented"));
    }

    @Test
    void getByLabel_WithProjectId_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getByLabel(EC_LABEL, PROJECT_ID));
        Assertions.assertTrue(ex.getMessage().contains("Method getByLabel is not implemented"));
    }

    // ==================== getAllLabels TESTS ====================

    @Test
    void getAllLabels_ShouldReturnSetFromRepository() {
        // given
        Set<String> expectedLabels = Set.of("label1", "label2");
        when(systemRepository.getAllLabels(PROJECT_ID)).thenReturn(expectedLabels);

        // when
        Set<String> result = manager.getAllLabels(PROJECT_ID);

        // then
        Assertions.assertSame(expectedLabels, result);
        verify(systemRepository).getAllLabels(PROJECT_ID);
    }

    // ==================== getByEcId TESTS ====================

    @Test
    void getByEcId_ShouldReturnSystemFromRepository() {
        // given
        when(systemRepository.findSystemByEcId(EC_ID)).thenReturn(system);

        // when
        System result = manager.getByEcId(EC_ID);

        // then
        Assertions.assertSame(system, result);
        verify(systemRepository).findSystemByEcId(EC_ID);
    }

    // ==================== getByEcProjectId TESTS ====================

    @Test
    void getByEcProjectId_ShouldReturnSystemsFromRepository() {
        // given
        Collection<System> expectedSystems = List.of(system);
        when(systemRepository.getSystemsByEcProject(EC_PROJECT_ID)).thenReturn(expectedSystems);

        // when
        Collection<System> result = manager.getByEcProjectId(EC_PROJECT_ID);

        // then
        Assertions.assertSame(expectedSystems, result);
        verify(systemRepository).getSystemsByEcProject(EC_PROJECT_ID);
    }

    // ==================== getEcProjectIds TESTS ====================

    @Test
    void getEcProjectIds_ShouldReturnCollectionFromRepository() {
        // given
        Collection<String> expectedIds = List.of("project1", "project2");
        when(systemRepository.getEcProjectIds(PROJECT_ID)).thenReturn(expectedIds);

        // when
        Collection<String> result = manager.getEcProjectIds(PROJECT_ID);

        // then
        Assertions.assertSame(expectedIds, result);
        verify(systemRepository).getEcProjectIds(PROJECT_ID);
    }

    // ==================== unbindByEcProject TESTS ====================

    @Test
    void unbindByEcProject_ShouldDelegateToRepository() {
        // when
        manager.unbindByEcProject(EC_PROJECT_ID);

        // then
        verify(systemRepository).unbindByEcProject(EC_PROJECT_ID);
    }

    // ==================== findByEcLabel TESTS ====================

    @Test
    void findByEcLabel_ShouldReturnSystemFromRepository() {
        // given
        when(systemRepository.findByEcLabel(EC_LABEL, PROJECT_ID)).thenReturn(system);

        // when
        System result = manager.findByEcLabel(EC_LABEL, PROJECT_ID);

        // then
        Assertions.assertSame(system, result);
        verify(systemRepository).findByEcLabel(EC_LABEL, PROJECT_ID);
    }

    // ==================== getByPieceOfNameAndProject TESTS ====================

    @Test
    void getByPieceOfNameAndProject_ShouldReturnSystemsFromRepository() {
        // given
        String name = "test";
        Collection<System> expectedSystems = List.of(system);
        when(systemRepository.findByPieceOfNameAndProject(name, PROJECT_ID)).thenReturn(expectedSystems);

        // when
        Collection<System> result = manager.getByPieceOfNameAndProject(name, PROJECT_ID);

        // then
        Assertions.assertSame(expectedSystems, result);
        verify(systemRepository).findByPieceOfNameAndProject(name, PROJECT_ID);
    }

    // ==================== getByProjectId TESTS ====================

    @Test
    void getByProjectId_ShouldReturnSystemsFromRepository() {
        // given
        Collection<System> expectedSystems = List.of(system);
        when(systemRepository.findByProject(PROJECT_ID)).thenReturn(expectedSystems);

        // when
        Collection<System> result = manager.getByProjectId(PROJECT_ID);

        // then
        Assertions.assertSame(expectedSystems, result);
        verify(systemRepository).findByProject(PROJECT_ID);
    }

    // ==================== getSimpleListByProject TESTS ====================

    @Test
    void getSimpleListByProject_ShouldReturnIdNamePairs() {
        // given
        List<IdNamePair> expectedPairs = List.of(
                new IdNamePair(BigInteger.valueOf(1L), "System1"),
                new IdNamePair(BigInteger.valueOf(2L), "System2")
        );
        when(systemRepository.getSimpleListByProject(PROJECT_ID)).thenReturn(expectedPairs);

        // when
        List<IdNamePair> result = manager.getSimpleListByProject(PROJECT_ID);

        // then
        Assertions.assertSame(expectedPairs, result);
        verify(systemRepository).getSimpleListByProject(PROJECT_ID);
    }

    // ==================== getReceiverSystemsFromCallChainSteps TESTS ====================

    @Test
    void getReceiverSystemsFromCallChainSteps_WhenIdsExist_ShouldReturnSystems() {
        // given
        BigInteger chainId = BigInteger.valueOf(500L);
        List<BigInteger> systemIds = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        System system1 = mock(System.class);
        System system2 = mock(System.class);

        when(systemRepository.getReceiverSystemIdsFromCallChainSteps(chainId)).thenReturn(systemIds);
        when(systemRepository.getOne(BigInteger.valueOf(1L))).thenReturn(system1);
        when(systemRepository.getOne(BigInteger.valueOf(2L))).thenReturn(system2);

        // when
        List<System> result = manager.getReceiverSystemsFromCallChainSteps(chainId);

        // then
        Assertions.assertEquals(2, result.size());
        Assertions.assertTrue(result.contains(system1));
        Assertions.assertTrue(result.contains(system2));
        verify(systemRepository).getOne(BigInteger.valueOf(1L));
        verify(systemRepository).getOne(BigInteger.valueOf(2L));
    }

    @Test
    void getReceiverSystemsFromCallChainSteps_WhenNoIds_ShouldReturnEmptyList() {
        // given
        BigInteger chainId = BigInteger.valueOf(500L);
        when(systemRepository.getReceiverSystemIdsFromCallChainSteps(chainId)).thenReturn(new ArrayList<>());

        // when
        List<System> result = manager.getReceiverSystemsFromCallChainSteps(chainId);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(systemRepository, never()).getOne(any());
    }

    @Test
    void getReceiverSystemsFromCallChainSteps_WhenIdsIsNull_ShouldReturnEmptyList() {
        // given
        BigInteger chainId = BigInteger.valueOf(500L);
        when(systemRepository.getReceiverSystemIdsFromCallChainSteps(chainId)).thenReturn(null);

        // when
        List<System> result = manager.getReceiverSystemsFromCallChainSteps(chainId);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(systemRepository, never()).getOne(any());
    }

    @Test
    void getReceiverSystemsFromCallChainSteps_WhenIdIsNull_ShouldSkipIt() {
        // given
        BigInteger chainId = BigInteger.valueOf(500L);
        List<BigInteger> systemIds = new ArrayList<>();
        systemIds.add(BigInteger.valueOf(1L));
        systemIds.add(null);
        systemIds.add(BigInteger.valueOf(2L));

        System system1 = mock(System.class);
        System system2 = mock(System.class);

        when(systemRepository.getReceiverSystemIdsFromCallChainSteps(chainId)).thenReturn(systemIds);
        when(systemRepository.getOne(BigInteger.valueOf(1L))).thenReturn(system1);
        when(systemRepository.getOne(BigInteger.valueOf(2L))).thenReturn(system2);

        // when
        List<System> result = manager.getReceiverSystemsFromCallChainSteps(chainId);

        // then
        Assertions.assertEquals(2, result.size());
    }

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_ShouldNotThrowException() {
        // when & then
        Assertions.assertDoesNotThrow(() -> manager.afterDelete(system));
    }
}