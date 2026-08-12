package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
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
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.EnvironmentRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.InboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OutboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.ServerRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.environment.Environment;
import org.qubership.automation.itf.core.model.jpa.environment.InboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.environment.OutboundTransportConfiguration;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.model.jpa.server.Server;
import org.qubership.automation.itf.core.model.jpa.server.ServerHB;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.qubership.automation.itf.core.model.jpa.transport.TransportConfiguration;
import org.qubership.automation.itf.core.model.usage.UsageInfo;
import org.qubership.automation.itf.core.util.converter.IdConverter;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.springframework.transaction.TransactionDefinition;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ServerObjectManagerTest {

    private static final BigInteger SERVER_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(200L);
    private static final BigInteger SYSTEM_ID = BigInteger.valueOf(300L);
    private static final String SERVER_NAME = "TestServer";
    private static final String SERVER_URL = "http://test-server:8080";
    private static final String EC_ID = "ec-id-123";
    private static final String EC_PROJECT_ID = "ec-project-456";

    @Mock
    private ServerRepository serverRepository;

    @Mock
    private InboundTransportConfigurationRepository inboundTransportRepository;

    @Mock
    private OutboundTransportConfigurationRepository outboundTransportRepository;

    @Mock
    private EnvironmentRepository environmentRepository;

    @Mock
    private ServerHB server;

    @Mock
    private Folder<Server> serverFolder;

    @Mock
    private StubProject stubProject;

    @Mock
    private System system;

    @Mock
    private TransportConfiguration transportConfiguration;

    @Mock
    private OutboundTransportConfiguration outboundConfig;

    @Mock
    private InboundTransportConfiguration inboundConfig;

    @Mock
    private Environment environment;

    @InjectMocks
    private ServerObjectManager manager;

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
    void protectedOnRemove_ShouldRemoveServerFromEnvironments() {
        // given
        when(server.getID()).thenReturn(SERVER_ID);

        Map<System, Server> outboundMap = new HashMap<>();
        outboundMap.put(system, server);
        when(environment.getOutbound()).thenReturn(outboundMap);

        Map<System, Server> inboundMap = new HashMap<>();
        inboundMap.put(system, server);
        when(environment.getInbound()).thenReturn(inboundMap);

        when(environmentRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                .thenReturn(List.of(environment))
                .thenReturn(List.of(environment));

        // when
        manager.protectedOnRemove(server);

        // then
        verify(environment, atLeastOnce()).store();
    }

    // ==================== create TESTS ====================

    @Test
    void create_WithStubContainerParent_ShouldCreateServerInContainer() {
        // given
        when(stubProject.getServers()).thenReturn(serverFolder);
        when(serverFolder.getObjects()).thenReturn(new ArrayList<>());
        when(serverFolder.getProjectId()).thenReturn(PROJECT_ID);
        when(serverRepository.save(any(ServerHB.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Server result = manager.create(stubProject);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(ServerHB.class, result);
        Assertions.assertEquals(serverFolder, result.getParent());
        Assertions.assertEquals(PROJECT_ID, result.getProjectId());
        verify(serverRepository).save(any(ServerHB.class));
    }

    @Test
    void create_WithFolderParent_ShouldCreateServerInFolder() {
        // given
        Folder<Storable> folder = mock(Folder.class);
        Folder<Server> typedFolder = mock(Folder.class);
        com.google.common.base.Optional<Folder<Server>> optional =
                com.google.common.base.Optional.of(typedFolder);
        when(folder.of(Server.class)).thenReturn(optional);
        when(typedFolder.getObjects()).thenReturn(new ArrayList<>());
        when(typedFolder.getProjectId()).thenReturn(PROJECT_ID);
        when(serverRepository.save(any(ServerHB.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Server result = manager.create(folder);

        // then
        Assertions.assertNotNull(result);
        verify(serverRepository).save(any(ServerHB.class));
    }

    @Test
    void create_WithInvalidParent_ShouldThrowException() {
        // given
        Storable invalidParent = mock(Storable.class);

        // when & then
        RuntimeException ex = Assertions.assertThrows(RuntimeException.class,
                () -> manager.create(invalidParent));
        Assertions.assertTrue(ex.getMessage().contains("ER: ServerFolder or StubContainer"));
        verify(serverRepository, never()).save(any());
    }

    @Test
    void create_WithoutParent_ShouldCreateServer() {
        // given
        when(serverRepository.save(any(ServerHB.class))).thenAnswer(inv -> inv.getArgument(0));

        // when
        Server result = manager.create();

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(ServerHB.class, result);
        verify(serverRepository).save(any(ServerHB.class));
    }

    // ==================== getOutbound TESTS ====================

    @Test
    void getOutbound_ShouldReturnConfigurationFromRepository() {
        // given
        when(server.getID()).thenReturn(SERVER_ID);
        when(system.getID()).thenReturn(SYSTEM_ID);
        String type = "http-outbound";
        when(outboundTransportRepository.findOne(SYSTEM_ID, SERVER_ID, type))
                .thenReturn(outboundConfig);

        // when
        OutboundTransportConfiguration result = manager.getOutbound(server, system, type);

        // then
        Assertions.assertSame(outboundConfig, result);
        verify(outboundTransportRepository).findOne(SYSTEM_ID, SERVER_ID, type);
    }

    // ==================== getOutbounds TESTS ====================

    @Test
    void getOutbounds_ShouldReturnAllConfigurations() {
        // given
        when(server.getID()).thenReturn(SERVER_ID);
        when(system.getID()).thenReturn(SYSTEM_ID);
        List<OutboundTransportConfiguration> expectedConfigs = List.of(outboundConfig);
        when(outboundTransportRepository.findAll(SYSTEM_ID, SERVER_ID))
                .thenReturn(expectedConfigs);

        // when
        Iterable<OutboundTransportConfiguration> result = manager.getOutbounds(server, system);

        // then
        Assertions.assertSame(expectedConfigs, result);
        verify(outboundTransportRepository).findAll(SYSTEM_ID, SERVER_ID);
    }

    // ==================== getInbound TESTS ====================

    @Test
    void getInbound_ShouldReturnConfigurationFromRepository() {
        // given
        when(server.getID()).thenReturn(SERVER_ID);
        when(transportConfiguration.getID()).thenReturn(SERVER_ID);
        when(inboundTransportRepository.findOne(SERVER_ID, SERVER_ID))
                .thenReturn(inboundConfig);

        // when
        InboundTransportConfiguration result = manager.getInbound(server, transportConfiguration);

        // then
        Assertions.assertSame(inboundConfig, result);
        verify(inboundTransportRepository).findOne(SERVER_ID, SERVER_ID);
    }

    // ==================== getInbounds TESTS ====================

    @Test
    void getInbounds_ShouldReturnAllConfigurations() {
        // given
        when(server.getID()).thenReturn(SERVER_ID);
        when(system.getID()).thenReturn(SYSTEM_ID);
        List<InboundTransportConfiguration> expectedConfigs = List.of(inboundConfig);
        when(inboundTransportRepository.findAll(SERVER_ID, SYSTEM_ID))
                .thenReturn(expectedConfigs);

        // when
        Iterable<InboundTransportConfiguration> result = manager.getInbounds(server, system);

        // then
        Assertions.assertSame(expectedConfigs, result);
        verify(inboundTransportRepository).findAll(SERVER_ID, SYSTEM_ID);
    }

    // ==================== findUsages TESTS ====================

    @Test
    void findUsages_WhenStorableIsServer_ShouldReturnUsages() {
        // given
        when(environmentRepository.findAll(any(com.querydsl.core.types.Predicate.class)))
                .thenReturn(List.of(environment))
                .thenReturn(List.of());

        // when
        Collection<UsageInfo> result = manager.findUsages(server);

        // then
        Assertions.assertNotNull(result);
    }

    @Test
    void findUsages_WhenStorableIsNotServer_ShouldReturnEmptyCollection() {
        // given
        Storable nonServer = mock(Storable.class);

        // when
        Collection<UsageInfo> result = manager.findUsages(nonServer);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== findImportantChildren TESTS ====================

    @Test
    void findImportantChildren_WhenStorableIsServer_ShouldReturnTriggerIds() {
        // given
        List<BigInteger> expectedTriggers = List.of(BigInteger.valueOf(1L), BigInteger.valueOf(2L));
        when(server.getID()).thenReturn(SERVER_ID);
        when(serverRepository.getTransportTriggersByServerId(SERVER_ID)).thenReturn(expectedTriggers);

        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(server);

        // then
        Assertions.assertTrue(result.containsKey("TransportTriggers"));
        Assertions.assertEquals(expectedTriggers, result.get("TransportTriggers"));
    }

    @Test
    void findImportantChildren_WhenStorableIsNotServer_ShouldReturnEmptyMap() {
        // given
        Storable nonServer = mock(Storable.class);

        // when
        Map<String, List<BigInteger>> result = manager.findImportantChildren(nonServer);

        // then
        Assertions.assertTrue(result == null || result.isEmpty());
    }

    // ==================== getByEcId TESTS ====================

    @Test
    void getByEcId_ShouldReturnServerFromRepository() {
        // given
        Object[] objects = {"server-name", SERVER_URL};
        when(serverRepository.findByEcId(EC_ID, "server-name", SERVER_URL)).thenReturn(server);

        // when
        Server result = manager.getByEcId(EC_ID, objects);

        // then
        Assertions.assertSame(server, result);
    }

    // ==================== getByEcProjectId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getEcProjectIds TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== unbindByEcProject TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== findByEcLabel TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByUrlAndProjectId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByUrlSlashedAndProjectId TESTS ====================

    @Test
    void getByUrlSlashedAndProjectId_WhenIdsExist_ShouldReturnServers() {
        // given
        List<BigInteger> ids = List.of(SERVER_ID);
        when(serverRepository.getServersByProjectAndUrlSlashed(SERVER_URL + "/", PROJECT_ID))
                .thenReturn(ids);
        when(serverRepository.getOne(SERVER_ID)).thenReturn(server);

        // when
        List<Server> result = manager.getByUrlSlashedAndProjectId(SERVER_URL, PROJECT_ID);

        // then
        Assertions.assertEquals(1, result.size());
        Assertions.assertTrue(result.contains(server));
    }

    @Test
    void getByUrlSlashedAndProjectId_WhenNoIds_ShouldReturnEmptyList() {
        // given
        when(serverRepository.getServersByProjectAndUrlSlashed(SERVER_URL + "/", PROJECT_ID))
                .thenReturn(new ArrayList<>());

        // when
        List<Server> result = manager.getByUrlSlashedAndProjectId(SERVER_URL, PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    // ==================== getByNameAndProjectId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== getByProjectId TESTS ====================
    // Tests were removed as overtesting: the method simply invokes repository method.

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_ShouldNotThrowException() {
        // when & then
        Assertions.assertDoesNotThrow(() -> manager.afterDelete(server));
    }

    // ==================== deleteUnusedOutboundConfigurations TESTS ====================

    @Test
    void deleteUnusedOutboundConfigurations_ShouldReturnCount() {
        // given
        int expectedCount = 5;
        when(serverRepository.deleteUnusedOutboundConfigurations()).thenReturn(expectedCount);

        // when
        int result = manager.deleteUnusedOutboundConfigurations();

        // then
        Assertions.assertEquals(expectedCount, result);
    }

    @Test
    void deleteUnusedOutboundConfigurationsByProjectId_ShouldReturnCount() {
        // given
        int expectedCount = 3;
        when(serverRepository.deleteUnusedOutboundConfigurationsByProjectId(PROJECT_ID))
                .thenReturn(expectedCount);

        // when
        int result = manager.deleteUnusedOutboundConfigurationsByProjectId(PROJECT_ID);

        // then
        Assertions.assertEquals(expectedCount, result);
    }
}