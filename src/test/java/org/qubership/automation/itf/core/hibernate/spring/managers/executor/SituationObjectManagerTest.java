package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
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
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SituationEventTriggerRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SituationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.Situation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SituationObjectManagerTest {

    private static final BigInteger SITUATION_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(200L);
    private static final BigInteger OPERATION_ID = BigInteger.valueOf(300L);
    private static final String SITUATION_NAME = "TestSituation";
    private static final String BV_CASE_ID = "test-bv-case-id";

    @Mock
    private SituationRepository situationRepository;

    @Mock
    private StepRepository stepRepository;

    @Mock
    private SituationEventTriggerRepository situationEventTriggerRepository;

    @Mock
    private Situation situation;

    @Mock
    private Operation parentOperation;

    @Mock
    private System parentSystem;

    @Mock
    private Step step;

    @Mock
    private SituationEventTrigger trigger;

    @Mock
    private Storable nonSituationStorable;

    @InjectMocks
    private SituationObjectManager manager;

    private MockedStatic<TxExecutor> txExecutorMock;
    private MockedStatic<IdConverter> idConverterMock;
    private TransactionDefinition mockTxDef;

    @BeforeEach
    void setUp() {
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

    // ==================== remove TESTS ====================

    @Test
    void remove_ShouldRemoveStepsAndTriggers() {
        // given
        List<Step> steps = new ArrayList<>();
        steps.add(step);
        Set<SituationEventTrigger> triggers = new HashSet<>();
        triggers.add(trigger);

        when(situation.getSteps()).thenReturn(steps);
        when(situation.getSituationEventTriggers()).thenReturn(triggers);
        when(situation.getOperationEventTriggers()).thenReturn(new HashSet<>());

        // when
        manager.remove(situation, false);

        // then
        verify(step).remove();
        verify(trigger).remove();
    }

    // ==================== protectedOnRemove TESTS ====================

    @Test
    void protectedOnRemove_ShouldCallRepositories() {
        // when
        manager.protectedOnRemove(situation);

        // then
        verify(stepRepository).onDeleteSituation(situation);
        verify(situationEventTriggerRepository).onDeleteSituation(situation);
    }

    // ==================== findUsages TESTS ====================

    @Test
    void findUsages_WhenStorableIsSituation_ShouldReturnUsages() {
        // given
        when(situation.getID()).thenReturn(SITUATION_ID);
        List<BigInteger> stepIds = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        when(stepRepository.getIdsSteps(SITUATION_ID)).thenReturn(stepIds);
        when(stepRepository.getOne(BigInteger.valueOf(1L))).thenReturn(step);
        when(stepRepository.getOne(BigInteger.valueOf(2L))).thenReturn(step);
        when(situationEventTriggerRepository.getTriggersBySituation(situation))
                .thenReturn(List.of(trigger));

        // when
        Collection<UsageInfo> result = manager.findUsages(situation);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        verify(stepRepository).getIdsSteps(SITUATION_ID);
        verify(situationEventTriggerRepository).getTriggersBySituation(situation);
    }

    @Test
    void findUsages_WhenStorableIsNotSituation_ShouldReturnEmptyCollection() {
        // when
        Collection<UsageInfo> result = manager.findUsages(nonSituationStorable);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== findAllByProjectIdOfNameAndId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.


    // ==================== getAllByProject TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByPieceOfNameAndProject TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByNameAndProjectId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByParentNameAndProject TESTS ====================

    @Test
    void getByParentNameAndProject_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getByParentNameAndProject("test", PROJECT_ID));
        Assertions.assertTrue(ex.getMessage().contains("Not implemented method"));
    }

    // ==================== countBvCaseUsages TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getAllLabels TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByLabel TESTS ====================

    @Test
    void getByLabel_WithoutProjectId_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getByLabel("label"));
        Assertions.assertTrue(ex.getMessage().contains("Method getByLabel is not implemented"));
    }

    @Test
    void getByLabel_WithProjectId_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getByLabel("label", PROJECT_ID));
        Assertions.assertTrue(ex.getMessage().contains("Method getByLabel is not implemented"));
    }

    // ==================== getObjectsWithBvLinks TESTS ====================

    @Test
    void getObjectsWithBvLinks_ShouldReturnFormattedResults() {
        // given
        List<Object[]> objects = new ArrayList<>();
        objects.add(new Object[]{BigInteger.valueOf(1L), "name1", "desc1", null, null, null, BigInteger.valueOf(10L)});
        when(situationRepository.getSituationsWithBvLinks(PROJECT_ID)).thenReturn(objects);

        // when
        List<Object[]> result = manager.getObjectsWithBvLinks(PROJECT_ID);

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertInstanceOf(String.class, result.getFirst()[0]);
    }

    @Test
    void getObjectsWithBvLinks_WhenNoResults_ShouldReturnEmptyList() {
        // given
        when(situationRepository.getSituationsWithBvLinks(PROJECT_ID)).thenReturn(null);

        // when
        List<Object[]> result = manager.getObjectsWithBvLinks(PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getReceiverSystemsFromCallChainSteps TESTS ====================

    @Test
    void getReceiverSystemsFromCallChainSteps_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getReceiverSystemsFromCallChainSteps(SITUATION_ID));
        Assertions.assertTrue(ex.getMessage().contains("Method getReceiverSystemsFromCallChainSteps is not implemented"));
    }

    // ==================== getFastStubsCandidates TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

}