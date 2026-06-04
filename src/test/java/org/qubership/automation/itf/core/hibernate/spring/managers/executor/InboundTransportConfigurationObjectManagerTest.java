package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

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
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.InboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InboundTransportConfigurationObjectManagerTest {

    private static final BigInteger CONFIG_ID = BigInteger.valueOf(100L);
    private static final BigInteger TRANSPORT_ID = BigInteger.valueOf(200L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(300L);
    private static final String EC_ID = "ec-id-123";
    private static final String EC_PROJECT_ID = "ec-project-456";

    @Mock
    private InboundTransportConfigurationRepository repository;

    @Mock
    private InboundTransportConfiguration parentConfig;

    @Mock
    private TransportConfiguration transportConfiguration;

    @InjectMocks
    private InboundTransportConfigurationObjectManager manager;

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

    // ==================== getChildByClass TESTS (метод с логикой) ====================

    @Test
    void getChildByClass_WithTransportConfigurationClass_ShouldReturnTransport() {
        // given
        when(parentConfig.getID()).thenReturn(CONFIG_ID);
        when(repository.getIdByTransport(CONFIG_ID)).thenReturn(TRANSPORT_ID);
        when(repository.findFirstTransport(TRANSPORT_ID)).thenReturn(transportConfiguration);

        // when
        Storable result = manager.getChildByClass(parentConfig, TransportConfiguration.class);

        // then
        Assertions.assertSame(transportConfiguration, result);
        verify(repository).getIdByTransport(CONFIG_ID);
        verify(repository).findFirstTransport(TRANSPORT_ID);
    }

    @Test
    void getChildByClass_WithOtherClass_ShouldThrowNotImplementedException() {
        // when & then
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getChildByClass(parentConfig, String.class));
        Assertions.assertTrue(ex.getMessage().contains("Not implemented for classes other than"));
    }

    // ==================== getChildrenByClass TESTS ====================

    @Test
    void getChildrenByClass_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getChildrenByClass(parentConfig, String.class));
        Assertions.assertNotNull(ex);
    }

    // ==================== getConfigurationsByTransportId TESTS ====================

    @Test
    void getConfigurationsByTransportId_ShouldReturnFromRepository() {
        // given
        Collection<InboundTransportConfiguration> expectedConfigs = List.of(parentConfig);
        when(repository.getConfigurationsByTransportId(TRANSPORT_ID)).thenReturn(expectedConfigs);

        // when
        Collection<InboundTransportConfiguration> result = manager.getConfigurationsByTransportId(TRANSPORT_ID);

        // then
        Assertions.assertSame(expectedConfigs, result);
        verify(repository).getConfigurationsByTransportId(TRANSPORT_ID);
    }
}