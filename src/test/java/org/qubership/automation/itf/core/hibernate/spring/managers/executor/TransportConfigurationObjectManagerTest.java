package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.InboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OperationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.TransportConfigurationRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.system.operation.Operation;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TransportConfigurationObjectManagerTest {

    private static final BigInteger TRANSPORT_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(200L);
    private static final BigInteger SYSTEM_ID = BigInteger.valueOf(300L);
    private static final String TRANSPORT_NAME = "TestTransport";
    private static final String TRANSPORT_TYPE = "http-outbound";
    private static final String EC_ID = "ec-id-123";
    private static final String EC_PROJECT_ID = "ec-project-456";

    @Mock
    private TransportConfigurationRepository transportConfigurationRepository;

    @Mock
    private OperationRepository operationRepository;

    @Mock
    private InboundTransportConfigurationRepository inboundTransportConfigurationRepository;

    @Mock
    private System parentSystem;

    @Mock
    private TransportConfiguration transportConfiguration;

    @Mock
    private Operation operation;

    @Mock
    private Storable nonTransportStorable;

    @InjectMocks
    private TransportConfigurationObjectManager manager;

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

    // ==================== protectedOnRemove TESTS ====================

    @Test
    void protectedOnRemove_ShouldCallRepositories() {
        // when
        manager.protectedOnRemove(transportConfiguration);

        // then
        verify(operationRepository).onDeleteTransport(transportConfiguration);
        verify(inboundTransportConfigurationRepository).onDeleteTransport(transportConfiguration);
    }

    // ==================== findUsages TESTS ====================

    @Test
    void findUsages_WhenStorableIsTransportConfiguration_ShouldReturnUsages() {
        // given
        when(operationRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                .thenReturn(List.of(operation));

        // when
        Collection<UsageInfo> result = manager.findUsages(transportConfiguration);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertFalse(result.isEmpty());
        verify(operationRepository).findAll(any(com.querydsl.core.types.Predicate.class));
    }

    @Test
    void findUsages_WhenStorableIsNotTransportConfiguration_ShouldReturnEmptyCollection() {
        // when
        Collection<UsageInfo> result = manager.findUsages(nonTransportStorable);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== findImportantChildren TESTS ====================

    @Test
    void findImportantChildren_WhenStorableIsTransportConfiguration_ShouldReturnTriggerIds() {
        // given
        List<BigInteger> expectedTriggers = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        when(transportConfiguration.getID()).thenReturn(TRANSPORT_ID);
        when(transportConfigurationRepository.getTransportTriggersByTransportConfigurationId(TRANSPORT_ID))
                .thenReturn(expectedTriggers);

        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(transportConfiguration);

        // then
        Assertions.assertTrue(result.containsKey("TransportTriggers"));
        Assertions.assertEquals(expectedTriggers, result.get("TransportTriggers"));
        verify(transportConfigurationRepository)
                .getTransportTriggersByTransportConfigurationId(TRANSPORT_ID);
    }

    @Test
    void findImportantChildren_WhenStorableIsNotTransportConfiguration_ShouldReturnEmptyMap() {
        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(nonTransportStorable);

        // then
        Assertions.assertTrue(result == null || result.isEmpty());
    }

    // ==================== create TESTS ====================

    @Test
    void create_WithSystemParent_ShouldCreateTransportConfiguration() {
        // given
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("url", "http://test:8080");
        when(transportConfigurationRepository.save(any(TransportConfiguration.class)))
                .thenAnswer(inv -> inv.getArgument(0));

        // when
        TransportConfiguration result = manager.create(parentSystem, TRANSPORT_NAME, TRANSPORT_TYPE, parameters);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(parentSystem, result.getParent());
        Assertions.assertEquals(TRANSPORT_NAME, result.getName());
        Assertions.assertEquals(TRANSPORT_TYPE, result.getTypeName());
        verify(transportConfigurationRepository).save(result);
    }

    @Test
    void create_WithNullParent_ShouldThrowIllegalArgumentException() {
        // given
        Map<String, Object> parameters = new HashMap<>();

        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create(null, TRANSPORT_NAME, TRANSPORT_TYPE, parameters));
        Assertions.assertEquals("Parent should not be null", ex.getMessage());
        verify(transportConfigurationRepository, never()).save(any());
    }

    // ==================== getByEcId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByEcProjectId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getEcProjectIds TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== unbindByEcProject TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== findByEcLabel TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByProjectId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== findUsagesTemplateOnTransport TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_WhenParentIsSystem_ShouldRemoveFromSystemTransports() {
        // given
        Set<TransportConfiguration> transports = new HashSet<>();
        transports.add(transportConfiguration);
        when(transportConfiguration.getParent()).thenReturn(parentSystem);
        when(parentSystem.getTransports()).thenReturn(transports);

        // when
        manager.afterDelete(transportConfiguration);

        // then
        Assertions.assertFalse(transports.contains(transportConfiguration));
    }

    @Test
    void afterDelete_WhenParentIsNull_ShouldDoNothing() {
        // given
        when(transportConfiguration.getParent()).thenReturn(null);

        // when
        manager.afterDelete(transportConfiguration);

        // then
        verify(parentSystem, never()).getTransports();
    }
}