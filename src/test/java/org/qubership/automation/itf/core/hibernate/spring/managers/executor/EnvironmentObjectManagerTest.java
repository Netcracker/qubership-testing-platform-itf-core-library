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
import java.util.Optional;
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
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.report.LinkCollectorConfiguration;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EnvironmentObjectManagerTest {

    private static final BigInteger ENVIRONMENT_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(300L);
    private static final BigInteger SYSTEM_ID = BigInteger.valueOf(400L);
    private static final BigInteger SERVER_ID = BigInteger.valueOf(500L);
    private static final String EC_ID = "ec-id-123";
    private static final String EC_PROJECT_ID = "ec-project-456";
    private static final String ENVIRONMENT_NAME = "TestEnvironment";
    private static final String ENVIRONMENT_STATE = "ACTIVE";

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private Environment environment;

    @Mock
    private Storable nonEnvironmentStorable;

    @InjectMocks
    private EnvironmentObjectManager manager;

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

    // ==================== updateInitialEnvState TESTS ====================

    @Test
    void updateInitialEnvState_ShouldTurnOffLostTriggers() {
        // when
        manager.updateInitialEnvState();

        // then
        verify(environmentRepository).turnOffLostTriggers();
    }

    // ==================== getByEcId TESTS ====================

    @Test
    void getByEcId_ShouldReturnEnvironmentFromRepository() {
        // given
        when(environmentRepository.findEnvironmentByEcId(EC_ID)).thenReturn(environment);

        // when
        Environment result = manager.getByEcId(EC_ID);

        // then
        Assertions.assertSame(environment, result);
        verify(environmentRepository).findEnvironmentByEcId(EC_ID);
    }

    @Test
    void getByEcId_WhenNotFound_ShouldReturnNull() {
        // given
        when(environmentRepository.findEnvironmentByEcId(EC_ID)).thenReturn(null);

        // when
        Environment result = manager.getByEcId(EC_ID);

        // then
        Assertions.assertNull(result);
    }

    // ==================== getByEcProjectId TESTS ====================

    @Test
    void getByEcProjectId_ShouldReturnEnvironmentsFromRepository() {
        // given
        Collection<Environment> expectedEnvironments = List.of(environment);
        when(environmentRepository.getEnvironmentsByEcProject(EC_PROJECT_ID)).thenReturn(expectedEnvironments);

        // when
        Collection<Environment> result = manager.getByEcProjectId(EC_PROJECT_ID);

        // then
        Assertions.assertSame(expectedEnvironments, result);
        verify(environmentRepository).getEnvironmentsByEcProject(EC_PROJECT_ID);
    }

    // ==================== getEcProjectIds TESTS ====================

    @Test
    void getEcProjectIds_ShouldReturnCollectionFromRepository() {
        // given
        Collection<String> expectedIds = List.of("project1", "project2");
        when(environmentRepository.getEcProjectIds(PROJECT_ID)).thenReturn(expectedIds);

        // when
        Collection<String> result = manager.getEcProjectIds(PROJECT_ID);

        // then
        Assertions.assertSame(expectedIds, result);
        verify(environmentRepository).getEcProjectIds(PROJECT_ID);
    }

    // ==================== unbindByEcProject TESTS ====================

    @Test
    void unbindByEcProject_ShouldDelegateToRepository() {
        // when
        manager.unbindByEcProject(EC_PROJECT_ID);

        // then
        verify(environmentRepository).unbindByEcProject(EC_PROJECT_ID);
    }

    // ==================== findByEcLabel TESTS ====================
    @Test
    void findByEcLabel_ShouldReturnNull() {
        // given
        String ecLabel = "test-label";

        // when
        Environment result = manager.findByEcLabel(ecLabel, PROJECT_ID);

        // then
        Assertions.assertNull(result);
    }

    // ==================== getByNameAndProjectId TESTS ====================

    @Test
    void getByNameAndProjectId_ShouldReturnEnvironmentsFromRepository() {
        // given
        List<Environment> expectedEnvironments = List.of(environment);
        when(environmentRepository.findByNameAndProjectId(ENVIRONMENT_NAME, PROJECT_ID))
                .thenReturn(expectedEnvironments);

        // when
        List<Environment> result = manager.getByNameAndProjectId(ENVIRONMENT_NAME, PROJECT_ID);

        // then
        Assertions.assertSame(expectedEnvironments, result);
        verify(environmentRepository).findByNameAndProjectId(ENVIRONMENT_NAME, PROJECT_ID);
    }

    @Test
    void getByNameAndProjectId_WhenNotFound_ShouldReturnEmptyList() {
        // given
        when(environmentRepository.findByNameAndProjectId(ENVIRONMENT_NAME, PROJECT_ID))
                .thenReturn(new ArrayList<>());

        // when
        List<Environment> result = manager.getByNameAndProjectId(ENVIRONMENT_NAME, PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getByPieceOfNameAndProjectId TESTS ====================

    @Test
    void getByPieceOfNameAndProjectId_ShouldReturnEnvironmentsFromRepository() {
        // given
        String pieceOfName = "test";
        List<Environment> expectedEnvironments = List.of(environment);
        when(environmentRepository.findByPieceOfNameAndProjectId(pieceOfName, PROJECT_ID))
                .thenReturn(expectedEnvironments);

        // when
        List<Environment> result = manager.getByPieceOfNameAndProjectId(pieceOfName, PROJECT_ID);

        // then
        Assertions.assertSame(expectedEnvironments, result);
        verify(environmentRepository).findByPieceOfNameAndProjectId(pieceOfName, PROJECT_ID);
    }

    // ==================== getByProjectId TESTS ====================

    @Test
    void getByProjectId_ShouldReturnEnvironmentsFromRepository() {
        // given
        List<Environment> expectedEnvironments = List.of(environment);
        when(environmentRepository.findByProjectId(PROJECT_ID)).thenReturn(expectedEnvironments);

        // when
        Collection<Environment> result = manager.getByProjectId(PROJECT_ID);

        // then
        Assertions.assertSame(expectedEnvironments, result);
        verify(environmentRepository).findByProjectId(PROJECT_ID);
    }

    // ==================== getByServerAndSystemIdPair TESTS ====================

    @Test
    void getByServerAndSystemIdPair_ShouldReturnResultsFromRepository() {
        // given
        List<Object[]> expectedResults = List.of(
                new Object[]{BigInteger.valueOf(1L), "Env1"},
                new Object[]{BigInteger.valueOf(2L), "Env2"}
        );
        when(environmentRepository.findEnvironmentByServerAndSystemIdPair(SYSTEM_ID, SERVER_ID))
                .thenReturn(expectedResults);

        // when
        List<Object[]> result = manager.getByServerAndSystemIdPair(SYSTEM_ID, SERVER_ID);

        // then
        Assertions.assertSame(expectedResults, result);
        verify(environmentRepository).findEnvironmentByServerAndSystemIdPair(SYSTEM_ID, SERVER_ID);
    }

    // ==================== findEnvironmentEcIdsForSystem TESTS ====================

    @Test
    void findEnvironmentEcIdsForSystem_ShouldReturnCollectionFromRepository() {
        // given
        Collection<String> expectedEcIds = List.of("ec-id-1", "ec-id-2");
        when(environmentRepository.findEnvironmentEcIdsForSystem(SYSTEM_ID)).thenReturn(expectedEcIds);

        // when
        Collection<String> result = manager.findEnvironmentEcIdsForSystem(SYSTEM_ID);

        // then
        Assertions.assertSame(expectedEcIds, result);
        verify(environmentRepository).findEnvironmentEcIdsForSystem(SYSTEM_ID);
    }

    // ==================== findEnvironmentEcIdsForServer TESTS ====================

    @Test
    void findEnvironmentEcIdsForServer_ShouldReturnCollectionFromRepository() {
        // given
        Collection<String> expectedEcIds = List.of("ec-id-1", "ec-id-2");
        when(environmentRepository.findEnvironmentEcIdsForServer(SERVER_ID)).thenReturn(expectedEcIds);

        // when
        Collection<String> result = manager.findEnvironmentEcIdsForServer(SERVER_ID);

        // then
        Assertions.assertSame(expectedEcIds, result);
        verify(environmentRepository).findEnvironmentEcIdsForServer(SERVER_ID);
    }

    // ==================== findByServerAndSystems TESTS ====================

    @Test
    void findByServerAndSystems_WhenEnvFound_ShouldReturnEnvironment() {
        // given
        Collection<BigInteger> systemIds = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        List<BigInteger> envIds = List.of(ENVIRONMENT_ID);
        when(environmentRepository.findByServerAndSystems(SERVER_ID, systemIds)).thenReturn(envIds);
        when(environmentRepository.findById(ENVIRONMENT_ID)).thenReturn(Optional.of(environment));

        // when
        Environment result = manager.findByServerAndSystems(SERVER_ID, systemIds);

        // then
        Assertions.assertSame(environment, result);
        verify(environmentRepository).findByServerAndSystems(SERVER_ID, systemIds);
        verify(environmentRepository).findById(ENVIRONMENT_ID);
    }

    @Test
    void findByServerAndSystems_WhenNoEnvFound_ShouldReturnNull() {
        // given
        Collection<BigInteger> systemIds = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        when(environmentRepository.findByServerAndSystems(SERVER_ID, systemIds))
                .thenReturn(new ArrayList<>());

        // when
        Environment result = manager.findByServerAndSystems(SERVER_ID, systemIds);

        // then
        Assertions.assertNull(result);
        verify(environmentRepository, never()).findById(any());
    }

    @Test
    void findByServerAndSystems_WhenEnvIdsIsNull_ShouldReturnNull() {
        // given
        Collection<BigInteger> systemIds = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        when(environmentRepository.findByServerAndSystems(SERVER_ID, systemIds)).thenReturn(null);

        // when
        Environment result = manager.findByServerAndSystems(SERVER_ID, systemIds);

        // then
        Assertions.assertNull(result);
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
    void getByPieceOfNameAndProject_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getByPieceOfNameAndProject("test", PROJECT_ID));
        Assertions.assertTrue(ex.getMessage().contains("Not implemented method"));
    }

    // ==================== getByParentNameAndProject TESTS ====================

    @Test
    void getByParentNameAndProject_ShouldThrowNotImplementedException() {
        NotImplementedException ex = Assertions.assertThrows(NotImplementedException.class,
                () -> manager.getByParentNameAndProject("test", PROJECT_ID));
        Assertions.assertTrue(ex.getMessage().contains("Not implemented method"));
    }

    // ==================== getEnvironmentStateById TESTS ====================

    @Test
    void getEnvironmentStateById_ShouldReturnStateFromRepository() {
        // given
        String environmentId = "123";
        when(environmentRepository.getEnvironmentState(BigInteger.valueOf(123L)))
                .thenReturn(ENVIRONMENT_STATE);

        // when
        String result = manager.getEnvironmentStateById(environmentId);

        // then
        Assertions.assertEquals(ENVIRONMENT_STATE, result);
        verify(environmentRepository).getEnvironmentState(BigInteger.valueOf(123L));
    }

    // ==================== getLinkCollectorsByEnvId TESTS ====================

    @Test
    void getLinkCollectorsByEnvId_ShouldReturnSetFromRepository() {
        // given
        Set<LinkCollectorConfiguration> expectedCollectors = new HashSet<>();
        when(environmentRepository.getLinkCollectorsByEnvId(ENVIRONMENT_ID))
                .thenReturn(expectedCollectors);

        // when
        Set<LinkCollectorConfiguration> result = manager.getLinkCollectorsByEnvId(ENVIRONMENT_ID);

        // then
        Assertions.assertSame(expectedCollectors, result);
        verify(environmentRepository).getLinkCollectorsByEnvId(ENVIRONMENT_ID);
    }

    // ==================== getInboundInfo TESTS ====================

    @Test
    void getInboundInfo_ShouldReturnListFromRepository() {
        // given
        List<String> expectedInfo = List.of("info1", "info2");
        when(environmentRepository.getInboundInfo(ENVIRONMENT_ID)).thenReturn(expectedInfo);

        // when
        List<String> result = manager.getInboundInfo(ENVIRONMENT_ID);

        // then
        Assertions.assertSame(expectedInfo, result);
        verify(environmentRepository).getInboundInfo(ENVIRONMENT_ID);
    }

    // ==================== findDuplicateConfigurationBySystemServer TESTS ====================

    @Test
    void findDuplicateConfigurationBySystemServer_ShouldReturnResultsFromRepository() {
        // given
        List<Object[]> expectedResults = new ArrayList<>();
        expectedResults.add(new Object[]{BigInteger.valueOf(1L), "System1", "Server1", 2L});

        when(environmentRepository.findDuplicateConfigurationBySystemServer(PROJECT_ID))
                .thenReturn(expectedResults);

        // when
        List<Object[]> result = manager.findDuplicateConfigurationBySystemServer(PROJECT_ID);

        // then
        Assertions.assertSame(expectedResults, result);
        verify(environmentRepository).findDuplicateConfigurationBySystemServer(PROJECT_ID);
    }

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_ShouldNotThrowException() {
        // when & then
        Assertions.assertDoesNotThrow(() -> manager.afterDelete(environment));
    }
}