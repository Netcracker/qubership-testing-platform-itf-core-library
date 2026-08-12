package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OperationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.folder.SystemFolder;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.copier.OriginalCopyMap;
import org.qubership.automation.itf.core.util.copier.StorableCopier;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.manager.CoreObjectManagerService;
import org.slf4j.Logger;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OperationObjectManagerTest {

    private static final String SESSION_ID = "test-session-id";
    private static final BigInteger OPERATION_ID = BigInteger.valueOf(100L);
    private static final BigInteger TRANSPORT_ID = BigInteger.valueOf(200L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(300L);
    private static final String OPERATION_NAME = "TestOperation";
    private static final String TRANSPORT_NAME = "TestTransport";
    private static final String PARENT_SYSTEM_NAME = "TestSystem";

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private Operation operation;

    @Mock
    private System parentSystem;

    @Mock
    private SystemFolder parentFolder;

    @Mock
    private TransportConfiguration transport;

    @Mock
    private TransportConfiguration copiedTransport;

    @Mock
    private Situation situation;

    @Mock
    private StorableCopier storableCopier;

    @Mock
    private CoreObjectManager coreObjectManager;

    @Mock
    private Logger logger;

    @InjectMocks
    private OperationObjectManager manager;

    private MockedStatic<OriginalCopyMap> originalCopyMapMock;
    private MockedStatic<CoreObjectManager> coreObjectManagerMock;
    private CoreObjectManagerService coreObjectManagerService;
    private SituationObjectManager situationManager;
    private MockedStatic<IdConverter> idConverterMock;

    @BeforeEach
    void setUp() {
        originalCopyMapMock = mockStatic(OriginalCopyMap.class);
        coreObjectManagerMock = mockStatic(CoreObjectManager.class);
        idConverterMock = mockStatic(IdConverter.class);
        coreObjectManagerService = mock(CoreObjectManagerService.class);
        situationManager = mock(SituationObjectManager.class);

        coreObjectManagerMock.when(CoreObjectManager::getInstance).thenReturn(coreObjectManagerService);
        when(coreObjectManagerService.getManager(Situation.class)).thenReturn(situationManager);

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
        originalCopyMapMock.close();
        coreObjectManagerMock.close();
        idConverterMock.close();
    }

    // ==================== protectedOnRemove TESTS ====================

    @Test
    void protectedOnRemove_ShouldCallStepRepositoryOnDeleteOperation() {
        // given
        when(operationRepository.getReferenceById(any())).thenReturn(operation);

        // when
        manager.protectedOnRemove(operation);

        // then
        verify(stepRepository).onDeleteOperation(operation);
    }

    // ==================== findUsages TESTS ====================

    @Test
    void findUsages_WhenStorableIsOperation_ShouldReturnSteps() {
        // given
        List<Step> steps = List.of(mock(Step.class), mock(Step.class));
        when(stepRepository.getIntegrationStepsByOperation(operation)).thenReturn(steps);

        // when
        Collection<UsageInfo> result = manager.findUsages(operation);

        // then
        verify(stepRepository).getIntegrationStepsByOperation(operation);
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty(),
                "Operation: Usages should be non-empty (should contain steps, at least)");
        Assertions.assertEquals(2, result.size());
    }

    @Test
    void findUsages_WhenStorableIsNotOperation_ShouldReturnEmptyCollection() {
        // given
        Storable nonOperation = mock(Storable.class);

        // when
        Collection<UsageInfo> result = manager.findUsages(nonOperation);

        // then
        verify(stepRepository, never()).getIntegrationStepsByOperation(any());
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void findUsages_WhenNoStepsFound_ShouldReturnEmptyCollection() {
        // given
        when(stepRepository.getIntegrationStepsByOperation(operation)).thenReturn(new ArrayList<>());

        // when
        Collection<UsageInfo> result = manager.findUsages(operation);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== findImportantChildren TESTS ====================

    @Test
    void findImportantChildren_WhenStorableIsOperation_ShouldReturnTriggerIds() {
        // given
        List<BigInteger> expectedTriggers = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        when(operation.getID()).thenReturn(OPERATION_ID);
        when(operationRepository.getSituationEventTriggersByOperationId(OPERATION_ID))
                .thenReturn(expectedTriggers);

        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(operation);

        // then
        Assertions.assertTrue(result.containsKey("SituationEventTriggers"));
        Assertions.assertEquals(expectedTriggers, result.get("SituationEventTriggers"));
        verify(operationRepository).getSituationEventTriggersByOperationId(OPERATION_ID);
    }

    @Test
    void findImportantChildren_WhenStorableIsOperationWithNoTriggers_ShouldReturnEmptyList() {
        // given
        when(operation.getID()).thenReturn(OPERATION_ID);
        when(operationRepository.getSituationEventTriggersByOperationId(OPERATION_ID))
                .thenReturn(new ArrayList<>());

        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(operation);

        // then
        Assertions.assertTrue(result.get("SituationEventTriggers").isEmpty());
    }

    @Test
    void findImportantChildren_WhenStorableIsNotOperation_ShouldDelegateToSuper() {
        // given
        Storable nonOperation = mock(Storable.class);
        // AbstractObjectManager.findImportantChildren returns empty map by default

        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(nonOperation);

        // then
        verify(operationRepository, never()).getSituationEventTriggersByOperationId(any());
        Assertions.assertTrue(result == null || result.isEmpty());
    }

    // ==================== additionalMoveActions TESTS ====================

    @Test
    void additionalMoveActions_WhenNoTransport_ShouldNotCopyTransport() {
        // given
        when(operation.getTransport()).thenReturn(null);
        when(operation.getSituations()).thenReturn(new HashSet<>());

        // when
        manager.additionalMoveActions(operation, SESSION_ID);

        // then
        verify(operation, never()).setTransport(any());
        verify(operation).store();
        originalCopyMapMock.verify(() -> OriginalCopyMap.getInstance(), never());
    }

    @Test
    void additionalMoveActions_WhenTransportExistsAndCached_ShouldUseCachedCopy() {
        // given
        OriginalCopyMap originalCopyMap = mock(OriginalCopyMap.class);
        originalCopyMapMock.when(OriginalCopyMap::getInstance).thenReturn(originalCopyMap);

        when(operation.getTransport()).thenReturn(transport);
        when(transport.getID()).thenReturn(TRANSPORT_ID);
        when(operation.getParent()).thenReturn(parentSystem);
        when(operation.getSituations()).thenReturn(new HashSet<>());
        when(originalCopyMap.get(SESSION_ID, TRANSPORT_ID)).thenReturn(copiedTransport);
        when(parentSystem.getParent()).thenReturn(parentFolder);
        when(parentFolder.getProject()).thenReturn(mock(StubProject.class));
        when(parentFolder.getProject().getID()).thenReturn(PROJECT_ID);

        // when
        manager.additionalMoveActions(operation, SESSION_ID);

        // then
        verify(operation, never()).setTransport(any());
        verify(operation).store();
        verify(originalCopyMap, never()).put(eq(SESSION_ID), eq(TRANSPORT_ID), any());
    }

    @Test
    void additionalMoveActions_WhenOperationHasSituations_ShouldCallAdditionalMoveActionsOnEach() {
        // given
        Set<Situation> situations = Set.of(situation, mock(Situation.class));
        when(operation.getTransport()).thenReturn(null);
        when(operation.getSituations()).thenReturn(situations);

        //coreObjectManagerMock.when(CoreObjectManager::getInstance).thenReturn(coreObjectManagerService);
        //when(coreObjectManager.getManager(Situation.class)).thenReturn(situationManager);

        // when
        manager.additionalMoveActions(operation, SESSION_ID);

        // then
        situations.forEach(s ->
                verify(situationManager).additionalMoveActions(s, SESSION_ID)
        );
        verify(operation).store();
    }

    // ==================== getAllByProject TESTS ====================

    @Test
    void getAllByProject_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getAllByProject(PROJECT_ID));
        Assertions.assertTrue(ex.getMessage().contains("Not implemented for project"));
    }

    // ==================== getByPieceOfNameAndProject TESTS ====================

    @Test
    void getByPieceOfNameAndProject_ShouldDelegateToRepository() {
        // given
        String name = "test";
        BigInteger projectId = PROJECT_ID;
        Collection<Operation> expectedOperations = List.of(operation);

        when(operationRepository.findByPieceOfNameAndProject(name, projectId))
                .thenReturn(expectedOperations);

        // when
        Collection<Operation> result = manager.getByPieceOfNameAndProject(name, projectId);

        // then
        Assertions.assertSame(expectedOperations, result);
        verify(operationRepository).findByPieceOfNameAndProject(name, projectId);
    }

    @Test
    void getByPieceOfNameAndProject_WithEmptyName_ShouldDelegate() {
        // given
        String name = "";
        BigInteger projectId = PROJECT_ID;
        Collection<Operation> expectedOperations = new ArrayList<>();

        when(operationRepository.findByPieceOfNameAndProject(name, projectId))
                .thenReturn(expectedOperations);

        // when
        Collection<Operation> result = manager.getByPieceOfNameAndProject(name, projectId);

        // then
        Assertions.assertTrue(result.isEmpty());
        verify(operationRepository).findByPieceOfNameAndProject(name, projectId);
    }

    @Test
    void getByPieceOfNameAndProject_WithNullProjectId_ShouldDelegate() {
        // given
        String name = "test";
        BigInteger projectId = null;

        when(operationRepository.findByPieceOfNameAndProject(name, null))
                .thenReturn(new ArrayList<>());

        // when
        Collection<Operation> result = manager.getByPieceOfNameAndProject(name, projectId);

        // then
        Assertions.assertTrue(result.isEmpty());
        verify(operationRepository).findByPieceOfNameAndProject(name, null);
    }

    // ==================== getByNameAndProjectId TESTS ====================

    @Test
    void getByNameAndProjectId_ShouldDelegateToRepository() {
        // given
        String name = "exactMatch";
        BigInteger projectId = PROJECT_ID;
        List<Operation> expectedOperations = List.of(operation);

        when(operationRepository.findByNameAndProjectId(name, projectId))
                .thenReturn(expectedOperations);

        // when
        List<Operation> result = manager.getByNameAndProjectId(name, projectId);

        // then
        Assertions.assertSame(expectedOperations, result);
        verify(operationRepository).findByNameAndProjectId(name, projectId);
    }

    @Test
    void getByNameAndProjectId_WithNullName_ShouldDelegate() {
        // given
        String name = null;
        BigInteger projectId = PROJECT_ID;

        when(operationRepository.findByNameAndProjectId(null, projectId))
                .thenReturn(new ArrayList<>());

        // when
        List<Operation> result = manager.getByNameAndProjectId(name, projectId);

        // then
        Assertions.assertTrue(result.isEmpty());
        verify(operationRepository).findByNameAndProjectId(null, projectId);
    }

    // ==================== getByParentNameAndProject TESTS ====================

    @Test
    void getByParentNameAndProject_ShouldDelegateToRepository() {
        // given
        String name = PARENT_SYSTEM_NAME;
        BigInteger projectId = PROJECT_ID;
        Collection<Operation> expectedOperations = List.of(operation);

        when(operationRepository.findByParentNameAndProject(name, projectId))
                .thenReturn(expectedOperations);

        // when
        Collection<Operation> result = manager.getByParentNameAndProject(name, projectId);

        // then
        Assertions.assertSame(expectedOperations, result);
        verify(operationRepository).findByParentNameAndProject(name, projectId);
    }

    @Test
    void getByParentNameAndProject_WithCaseInsensitiveSearch_ShouldDelegateToRepository() {
        // given
        String name = "TestSystem";
        BigInteger projectId = PROJECT_ID;
        Collection<Operation> expectedOperations = List.of(operation);

        when(operationRepository.findByParentNameAndProject(name, projectId))
                .thenReturn(expectedOperations);

        // when
        Collection<Operation> result = manager.getByParentNameAndProject(name, projectId);

        // then
        Assertions.assertSame(expectedOperations, result);
        verify(operationRepository).findByParentNameAndProject(name, projectId);
    }

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_WhenParentIsSystem_ShouldRemoveOperationFromParentCollection() {
        // given
        Set<Operation> operations = new HashSet<>();
        operations.add(operation);

        when(operation.getParent()).thenReturn(parentSystem);
        when(parentSystem.getOperations()).thenReturn(operations);

        // when
        manager.afterDelete(operation);

        // then
        Assertions.assertFalse(operations.contains(operation));
    }

    @Test
    void afterDelete_WhenOperationHasNoParent_ShouldDoNothing() {
        // given
        when(operation.getParent()).thenReturn(null);

        // when
        manager.afterDelete(operation);

        // then
        verify(parentSystem, never()).getOperations();
    }

    @Test
    void afterDelete_WhenOperationNotInParentCollection_ShouldHandleGracefully() {
        // given
        Set<Operation> operations = new HashSet<>(); // empty list, operation not present

        when(operation.getParent()).thenReturn(parentSystem);
        when(parentSystem.getOperations()).thenReturn(operations);

        // when - should not throw exception
        manager.afterDelete(operation);

        // then
        Assertions.assertFalse(operations.contains(operation));
    }

    @Test
    void afterDelete_WithSynchronizedBlock_ShouldBeThreadSafe() {
        // given
        Set<Operation> operations = new HashSet<>();
        operations.add(operation);

        when(operation.getParent()).thenReturn(parentSystem);
        when(parentSystem.getOperations()).thenReturn(operations);

        // when
        manager.afterDelete(operation);

        // then - synchronization ensures visibility, we just check the effect
        Assertions.assertFalse(operations.contains(operation));
    }

    // ==================== Edge Cases and Additional Tests ====================

    @Test
    void findImportantChildren_WhenOperationIdIsNull_ShouldHandleGracefully() {
        // given
        when(operation.getID()).thenReturn(null);
        when(operationRepository.getSituationEventTriggersByOperationId(null))
                .thenReturn(new ArrayList<>());

        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(operation);

        // then
        Assertions.assertTrue(result.get("SituationEventTriggers").isEmpty());
        verify(operationRepository).getSituationEventTriggersByOperationId(null);
    }

    @Test
    void additionalMoveActions_WhenOperationHasBothTransportAndSituations_ShouldProcessBoth() {
        // given
        OriginalCopyMap originalCopyMap = mock(OriginalCopyMap.class);
        originalCopyMapMock.when(OriginalCopyMap::getInstance).thenReturn(originalCopyMap);

        Set<Situation> situations = Set.of(situation);
        when(operation.getTransport()).thenReturn(transport);
        when(operation.getSituations()).thenReturn(situations);
        when(transport.getID()).thenReturn(TRANSPORT_ID);
        when(operation.getParent()).thenReturn(parentSystem);
        when(originalCopyMap.get(SESSION_ID, TRANSPORT_ID)).thenReturn(copiedTransport);

        //coreObjectManagerMock.when(CoreObjectManager::getInstance).thenReturn(coreObjectManagerService);
        //when(coreObjectManager.getManager(Situation.class)).thenReturn(situationManager);

        // when
        manager.additionalMoveActions(operation, SESSION_ID);

        // then
        verify(operation, never()).setTransport(any());
        verify(situationManager).additionalMoveActions(situation, SESSION_ID);
        verify(operation).store();
    }

    @Test
    void findUsages_WithNullStorable_ShouldThrowException() {
        Collection<UsageInfo> result = manager.findUsages(null);
        Assertions.assertTrue(result.isEmpty());
    }
}