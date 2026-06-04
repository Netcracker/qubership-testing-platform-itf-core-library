package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import org.qubership.automation.itf.core.model.communication.EventTriggerBriefInfo;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.system.stub.SituationEventTrigger;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SituationEventTriggerObjectManagerTest {

    private static final BigInteger TRIGGER_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(200L);
    private static final BigInteger SYSTEM_ID = BigInteger.valueOf(300L);

    @Mock
    private SituationEventTriggerRepository repository;

    @Mock
    private SituationEventTrigger trigger;

    @Mock
    private Operation operation;

    @InjectMocks
    private SituationEventTriggerObjectManager manager;

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

    // ==================== getTriggersBriefInfoBySystem TESTS (с логикой) ====================

    @Test
    void getTriggersBriefInfoBySystem_ShouldSeparateActiveAndInactiveTriggers() {
        // given
        List<Object[]> dbResult = new ArrayList<>();
        dbResult.add(new Object[]{BigInteger.valueOf(1L), "ACTIVE"});
        dbResult.add(new Object[]{BigInteger.valueOf(2L), "INACTIVE"});
        dbResult.add(new Object[]{BigInteger.valueOf(3L), "ACTIVE"});

        when(repository.getTriggersBriefInfoBySystemId(SYSTEM_ID)).thenReturn(dbResult);

        // when
        Map<String, List<EventTriggerBriefInfo>> result = manager.getTriggersBriefInfoBySystem(SYSTEM_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.containsKey("ToDeactivate"));
        Assertions.assertTrue(result.containsKey("ToReactivate"));

        List<EventTriggerBriefInfo> toDeactivate = result.get("ToDeactivate");
        List<EventTriggerBriefInfo> toReactivate = result.get("ToReactivate");

        Assertions.assertEquals(1, toDeactivate.size());
        Assertions.assertEquals(BigInteger.valueOf(2L), toDeactivate.getFirst().getId());

        Assertions.assertEquals(2, toReactivate.size());
        Assertions.assertEquals(BigInteger.valueOf(1L), toReactivate.get(0).getId());
        Assertions.assertEquals(BigInteger.valueOf(3L), toReactivate.get(1).getId());
    }

    @Test
    void getTriggersBriefInfoBySystem_WhenOnlyActiveTriggers_ShouldReturnEmptyToDeactivate() {
        // given
        List<Object[]> dbResult = new ArrayList<>();
        dbResult.add(new Object[]{BigInteger.valueOf(1L), "ACTIVE"});
        dbResult.add(new Object[]{BigInteger.valueOf(2L), "ACTIVE"});

        when(repository.getTriggersBriefInfoBySystemId(SYSTEM_ID)).thenReturn(dbResult);

        // when
        Map<String, List<EventTriggerBriefInfo>> result = manager.getTriggersBriefInfoBySystem(SYSTEM_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.get("ToDeactivate").isEmpty());
        Assertions.assertEquals(2, result.get("ToReactivate").size());
    }

    @Test
    void getTriggersBriefInfoBySystem_WhenOnlyInactiveTriggers_ShouldReturnEmptyToReactivate() {
        // given
        List<Object[]> dbResult = new ArrayList<>();
        dbResult.add(new Object[]{BigInteger.valueOf(1L), "INACTIVE"});
        dbResult.add(new Object[]{BigInteger.valueOf(2L), "INACTIVE"});

        when(repository.getTriggersBriefInfoBySystemId(SYSTEM_ID)).thenReturn(dbResult);

        // when
        Map<String, List<EventTriggerBriefInfo>> result = manager.getTriggersBriefInfoBySystem(SYSTEM_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(2, result.get("ToDeactivate").size());
        Assertions.assertTrue(result.get("ToReactivate").isEmpty());
    }

    @Test
    void getTriggersBriefInfoBySystem_WhenEmptyResult_ShouldReturnEmptyMaps() {
        // given
        when(repository.getTriggersBriefInfoBySystemId(SYSTEM_ID)).thenReturn(new ArrayList<>());

        // when
        Map<String, List<EventTriggerBriefInfo>> result = manager.getTriggersBriefInfoBySystem(SYSTEM_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.get("ToDeactivate").isEmpty());
        Assertions.assertTrue(result.get("ToReactivate").isEmpty());
    }

    @Test
    void getTriggersBriefInfoBySystem_WhenNullResult_ShouldReturnEmptyMaps() {
        // given
        when(repository.getTriggersBriefInfoBySystemId(SYSTEM_ID)).thenReturn(null);

        // when
        Map<String, List<EventTriggerBriefInfo>> result = manager.getTriggersBriefInfoBySystem(SYSTEM_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.get("ToDeactivate").isEmpty());
        Assertions.assertTrue(result.get("ToReactivate").isEmpty());
    }

    @Test
    void getTriggersBriefInfoBySystem_WhenEntriesHaveInsufficientLength_ShouldSkipThem() {
        // given
        List<Object[]> dbResult = new ArrayList<>();
        dbResult.add(new Object[]{BigInteger.valueOf(1L), "ACTIVE"});  // ok
        dbResult.add(new Object[]{BigInteger.valueOf(2L)});             // insufficient
        dbResult.add(new Object[]{BigInteger.valueOf(3L), "INACTIVE"}); // ok

        when(repository.getTriggersBriefInfoBySystemId(SYSTEM_ID)).thenReturn(dbResult);

        // when
        Map<String, List<EventTriggerBriefInfo>> result = manager.getTriggersBriefInfoBySystem(SYSTEM_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(1, result.get("ToReactivate").size());
        Assertions.assertEquals(1, result.get("ToDeactivate").size());
    }

    @Test
    void getTriggersBriefInfoBySystem_ShouldSetCorrectTriggerType() {
        // given
        List<Object[]> dbResult = new ArrayList<>();
        dbResult.add(new Object[]{BigInteger.valueOf(1L), "ACTIVE"});

        when(repository.getTriggersBriefInfoBySystemId(SYSTEM_ID)).thenReturn(dbResult);

        // when
        Map<String, List<EventTriggerBriefInfo>> result = manager.getTriggersBriefInfoBySystem(SYSTEM_ID);

        // then
        EventTriggerBriefInfo briefInfo = result.get("ToReactivate").getFirst();
        Assertions.assertEquals(SituationEventTrigger.TYPE, briefInfo.getType());
    }

    // ==================== getAllActive TESTS ====================

    @Test
    void getAllActive_ShouldReturnEmptyList() {
        // when
        List<SituationEventTrigger> result = manager.getAllActive(operation);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }
}