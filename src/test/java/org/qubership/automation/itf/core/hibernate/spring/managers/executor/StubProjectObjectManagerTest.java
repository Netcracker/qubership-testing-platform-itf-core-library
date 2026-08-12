package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

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
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StubProjectRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.folder.ChainFolder;
import org.qubership.automation.itf.core.model.jpa.folder.EnvFolder;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.folder.ServerFolder;
import org.qubership.automation.itf.core.model.jpa.folder.SystemFolder;
import org.qubership.automation.itf.core.model.jpa.project.StubProject;
import org.qubership.automation.itf.core.util.db.TxExecutor;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.qubership.automation.itf.core.util.manager.CoreObjectManagerService;
import org.springframework.transaction.TransactionDefinition;

import com.google.common.base.Optional;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class StubProjectObjectManagerTest {

    private static final BigInteger PROJECT_ID = BigInteger.valueOf(100L);
    private static final BigInteger SYSTEM_ID = BigInteger.valueOf(200L);
    private static final UUID PROJECT_UUID = UUID.randomUUID();
    private static final String EC_PROJECT_ID = "ec-project-123";
    private static final String EC_LABEL = "ec-label";
    private static final String USER_DATA_KEY = "test-key";
    private static final String USER_DATA_VALUE = "test-value";

    @Mock
    private StubProjectRepository stubProjectRepository;

    @Mock
    private StubProject project;

    @Mock
    private EnvFolder environmentsFolder;

    @Mock
    private ChainFolder callchainsFolder;

    @Mock
    private ServerFolder serversFolder;

    @Mock
    private SystemFolder systemsFolder;

    @Mock
    private FolderObjectManager folderManager;

    @Mock
    private CoreObjectManagerService coreObjectManagerService;

    @InjectMocks
    private StubProjectObjectManager manager;

    private MockedStatic<TxExecutor> txExecutorMock;
    private MockedStatic<CoreObjectManager> coreObjectManagerMock;
    private TransactionDefinition mockTxDef;

    @BeforeEach
    void setUp() throws Exception {
        txExecutorMock = mockStatic(TxExecutor.class);
        coreObjectManagerMock = mockStatic(CoreObjectManager.class);

        // Mock TransactionDefinition
        mockTxDef = mock(TransactionDefinition.class);
        txExecutorMock.when(TxExecutor::nestedWritableTransaction).thenReturn(mockTxDef);
        txExecutorMock.when(TxExecutor::defaultWritableTransaction).thenReturn(mockTxDef);
        txExecutorMock.when(TxExecutor::readOnlyTransaction).thenReturn(mockTxDef);

        // Mock executeUnchecked to execute the callable
        txExecutorMock.when(() -> TxExecutor.executeUnchecked(any(Callable.class), any(TransactionDefinition.class)))
                .thenAnswer(invocation -> {
                    Callable<?> callable = invocation.getArgument(0);
                    return callable.call();
                });

        txExecutorMock.when(() -> TxExecutor.execute(any(Callable.class), any(TransactionDefinition.class)))
                .thenAnswer(invocation -> {
                    Callable<?> callable = invocation.getArgument(0);
                    return callable.call();
                });

        // Mock CoreObjectManager
        coreObjectManagerMock.when(CoreObjectManager::getInstance).thenReturn(coreObjectManagerService);
        when(coreObjectManagerService.getManager(Folder.class)).thenReturn(folderManager);

        // Mock folder creation - simpler approach that actually works
        environmentsFolder = mock(EnvFolder.class);
        callchainsFolder = mock(ChainFolder.class);
        serversFolder = mock(ServerFolder.class);
        systemsFolder = mock(SystemFolder.class);

        when(folderManager.create(any(), eq("ROOT"), eq(EnvFolder.TYPE.getSimpleName()))).thenReturn(environmentsFolder);
        when(folderManager.create(any(), eq("ROOT"), eq(ChainFolder.TYPE.getSimpleName()))).thenReturn(callchainsFolder);
        when(folderManager.create(any(), eq("ROOT"), eq(ServerFolder.TYPE.getSimpleName()))).thenReturn(serversFolder);
        when(folderManager.create(any(), eq("ROOT"), eq(SystemFolder.TYPE.getSimpleName()))).thenReturn(systemsFolder);

        // Mock .of(Class).get() chain for each folder
        Optional<EnvFolder> envOptional = mock(Optional.class);
        when(environmentsFolder.of(any(Class.class))).thenReturn(envOptional);
        when(envOptional.get()).thenReturn(environmentsFolder);

        Optional<ChainFolder> chainOptional = mock(Optional.class);
        when(callchainsFolder.of(any(Class.class))).thenReturn(chainOptional);
        when(chainOptional.get()).thenReturn(callchainsFolder);

        Optional<ServerFolder> serverOptional = mock(Optional.class);
        when(serversFolder.of(any(Class.class))).thenReturn(serverOptional);
        when(serverOptional.get()).thenReturn(serversFolder);

        Optional<SystemFolder> systemOptional = mock(Optional.class);
        when(systemsFolder.of(any(Class.class))).thenReturn(systemOptional);
        when(systemOptional.get()).thenReturn(systemsFolder);
    }

    @AfterEach
    void tearDown() {
        txExecutorMock.close();
        coreObjectManagerMock.close();
    }

    // ==================== create TESTS ====================

    @Test
    void create_WithParentAndTypeAndParameters_ShouldCreateProjectWithProperties() {
        // given
        Storable parent = mock(Storable.class);
        String type = "test-type";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("key1", "value1");
        parameters.put("key2", "value2");

        StubProject createdProject = mock(StubProject.class);
        when(stubProjectRepository.save(any(StubProject.class))).thenReturn(createdProject);

        // when
        StubProject result = manager.create(parent, type, parameters);

        // then
        verify(createdProject).setStorableProp(parameters);
        verify(stubProjectRepository).save(createdProject);
        Assertions.assertSame(createdProject, result);
    }

    @Test
    void create_WithoutParameters_ShouldCreateProjectWithRootFolders() {
        // given
        StubProject newProject = mock(StubProject.class);
        when(stubProjectRepository.save(any(StubProject.class))).thenAnswer(inv -> inv.getArgument(0));
        when(newProject.getEnvironments()).thenReturn(null);
        when(newProject.getCallchains()).thenReturn(null);
        when(newProject.getServers()).thenReturn(null);
        when(newProject.getSystems()).thenReturn(null);
        when(newProject.getID()).thenReturn(PROJECT_ID);

        // when
        StubProject result = manager.create();

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(environmentsFolder, result.getEnvironments());
        Assertions.assertEquals(callchainsFolder, result.getCallchains());
        Assertions.assertEquals(serversFolder, result.getServers());
        Assertions.assertEquals(systemsFolder, result.getSystems());
        verify(stubProjectRepository, times(2)).save(result);
    }

    // ==================== setReplicationRole TESTS ====================

    @Test
    void setReplicationRole_WithReplicaRole_ShouldCallSetReplicationRoleReplica() {
        // when
        manager.setReplicationRole("replica");

        // then
        verify(stubProjectRepository).setReplicationRoleReplica();
        verify(stubProjectRepository, never()).setReplicationRoleOrigin();
    }

    @Test
    void setReplicationRole_WithNonReplicaRole_ShouldCallSetReplicationRoleOrigin() {
        // when
        manager.setReplicationRole("origin");
        manager.setReplicationRole("master");
        manager.setReplicationRole("any-other-value");

        // then
        verify(stubProjectRepository, times(3)).setReplicationRoleOrigin();
        verify(stubProjectRepository, never()).setReplicationRoleReplica();
    }

    // ==================== setUserData TESTS ====================

    @Test
    void setUserData_WithSelectAction_ShouldReturnExistingData() {
        // given
        List<String> existingData = List.of("existing-value");
        when(stubProjectRepository.getData(USER_DATA_KEY, PROJECT_ID)).thenReturn(existingData);

        // when
        String result = manager.setUserData("SELECT", USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);

        // then
        Assertions.assertEquals("existing-value", result);
        verify(stubProjectRepository).getData(USER_DATA_KEY, PROJECT_ID);
    }

    @Test
    void setUserData_WithSelectActionAndNoData_ShouldReturnEmptyString() {
        // given
        when(stubProjectRepository.getData(USER_DATA_KEY, PROJECT_ID)).thenReturn(new ArrayList<>());

        // when
        String result = manager.setUserData("SELECT", USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);

        // then
        Assertions.assertEquals("", result);
    }

    @Test
    void setUserData_WithInsertAction_ShouldCallSetData() {
        // when
        String result = manager.setUserData("INSERT", USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);

        // then
        Assertions.assertEquals("", result);
        verify(stubProjectRepository).setData(USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);
    }

    @Test
    void setUserData_WithUpdateAction_ShouldCallUpdateData() {
        // when
        String result = manager.setUserData("UPDATE", USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);

        // then
        Assertions.assertEquals("", result);
        verify(stubProjectRepository).updateData(USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);
    }

    @Test
    void setUserData_WithUpsertAction_ShouldCallUpsertData() {
        // when
        String result = manager.setUserData("UPSERT", USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);

        // then
        Assertions.assertEquals("", result);
        verify(stubProjectRepository).upsertData(USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);
    }

    @Test
    void setUserData_WithDeleteAction_ShouldCallDeleteData() {
        // when
        String result = manager.setUserData("DELETE", USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);

        // then
        Assertions.assertEquals("", result);
        verify(stubProjectRepository).deleteData(USER_DATA_KEY, PROJECT_ID);
    }

    @Test
    void setUserData_WithUnknownAction_ShouldReturnErrorMessage() {
        // when
        String result = manager.setUserData("UNKNOWN", USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);

        // then
        Assertions.assertTrue(result.contains("Unknown 'Action': UNKNOWN"));
        verify(stubProjectRepository, never()).getData(any(), any());
        verify(stubProjectRepository, never()).setData(any(), any(), any());
    }

    @Test
    void setUserData_WithNullAction_ShouldReturnErrorMessage() {
        // when
        String result = manager.setUserData(null, USER_DATA_KEY, USER_DATA_VALUE, PROJECT_ID);

        // then
        Assertions.assertTrue(result.contains("'Action' parameter is null or empty"));
    }

    // ==================== clearUserData TESTS ====================

    @Test
    void clearUserData_ShouldDelegateToRepository() {
        // given
        Integer leaveDays = 30;
        String expectedResult = "Cleared 100 records";
        when(stubProjectRepository.clearUserData(leaveDays)).thenReturn(expectedResult);

        // when
        String result = manager.clearUserData(leaveDays);

        // then
        verify(stubProjectRepository).clearUserData(leaveDays);
        Assertions.assertEquals(expectedResult, result);
    }

    // ==================== getEntityInternalIdByUuid TESTS ====================

    @Test
    void getEntityInternalIdByUuid_ShouldReturnIdFromRepository() {
        // given
        UUID uuid = UUID.randomUUID();
        BigInteger expectedId = BigInteger.valueOf(500L);
        when(stubProjectRepository.getEntityInternalIdByUuid(uuid)).thenReturn(expectedId);

        // when
        BigInteger result = manager.getEntityInternalIdByUuid(uuid);

        // then
        verify(stubProjectRepository).getEntityInternalIdByUuid(uuid);
        Assertions.assertEquals(expectedId, result);
    }

    // ==================== getByUuid TESTS ====================

    @Test
    void getByUuid_ShouldReturnProjectFromRepository() {
        // given
        UUID uuid = UUID.randomUUID();
        when(stubProjectRepository.getByUuid(uuid)).thenReturn(project);

        // when
        StubProject result = manager.getByUuid(uuid);

        // then
        verify(stubProjectRepository).getByUuid(uuid);
        Assertions.assertSame(project, result);
    }

    // ==================== getProjectIdsBySystem TESTS ====================

    @Test
    void getProjectIdsBySystem_WhenFound_ShouldReturnMapWithIdAndUuid() {
        // given
        String[] resultArray = {PROJECT_ID.toString(), PROJECT_UUID.toString()};
        List<String[]> results = new ArrayList<>();
        results.add(resultArray);
        when(stubProjectRepository.determineProjectIdsBySystemId(SYSTEM_ID)).thenReturn(results);

        // when
        Map<String, Object> result = manager.getProjectIdsBySystem(SYSTEM_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertEquals(PROJECT_ID, result.get("projectId"));
        Assertions.assertEquals(PROJECT_UUID, result.get("projectUuid"));
    }

    @Test
    void getProjectIdsBySystem_WhenNotFound_ShouldReturnNull() {
        // given
        when(stubProjectRepository.determineProjectIdsBySystemId(SYSTEM_ID)).thenReturn(new ArrayList<>());

        // when
        Map<String, Object> result = manager.getProjectIdsBySystem(SYSTEM_ID);

        // then
        Assertions.assertNull(result);
    }

    // ==================== getProjectSettingByShortName TESTS ====================

    @Test
    void getProjectSettingByShortName_WhenSettingExists_ShouldReturnValue() {
        // given
        String propertyShortName = "timeout";
        String expectedValue = "30000";
        when(stubProjectRepository.getProjectSetting(PROJECT_ID, propertyShortName))
                .thenReturn(expectedValue);

        // when
        String result = manager.getProjectSettingByShortName(PROJECT_ID, propertyShortName);

        // then
        Assertions.assertEquals(expectedValue, result);
    }

    @Test
    void getProjectSettingByShortName_WhenExceptionOccurs_ShouldReturnEmptyString() {
        // given
        String propertyShortName = "nonexistent";
        when(stubProjectRepository.getProjectSetting(PROJECT_ID, propertyShortName))
                .thenThrow(new RuntimeException("Database error"));

        // when
        String result = manager.getProjectSettingByShortName(PROJECT_ID, propertyShortName);

        // then
        Assertions.assertEquals("", result);
    }

    // ==================== getAllProjectSettingsByProjectId TESTS ====================

    @Test
    void getAllProjectSettingsByProjectId_WhenSettingsExist_ShouldReturnMap() {
        // given
        List<Object[]> settings = List.of(
                new Object[]{"timeout", "30000"},
                new Object[]{"retryCount", "5"}
        );
        when(stubProjectRepository.getAllProjectSettingsByProjectId(PROJECT_ID)).thenReturn(settings);

        // when
        Map<String, String> result = manager.getAllProjectSettingsByProjectId(PROJECT_ID);

        // then
        Assertions.assertEquals(2, result.size());
        Assertions.assertEquals("30000", result.get("timeout"));
        Assertions.assertEquals("5", result.get("retryCount"));
    }

    @Test
    void getAllProjectSettingsByProjectId_WhenNoSettings_ShouldReturnEmptyMap() {
        // given
        when(stubProjectRepository.getAllProjectSettingsByProjectId(PROJECT_ID))
                .thenReturn(new ArrayList<>());

        // when
        Map<String, String> result = manager.getAllProjectSettingsByProjectId(PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getAllProjectSettingsByProjectId_WhenExceptionOccurs_ShouldReturnEmptyMap() {
        // given
        when(stubProjectRepository.getAllProjectSettingsByProjectId(PROJECT_ID))
                .thenThrow(new RuntimeException("Database error"));

        // when
        Map<String, String> result = manager.getAllProjectSettingsByProjectId(PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
    }

    @Test
    void getAllProjectSettingsByProjectId_WithNullValue_ShouldConvertToEmptyString() {
        // given
        List<Object[]> settings = List.of(
                new Object[]{"timeout", null},
                new Object[]{"retryCount", "5"}
        );
        when(stubProjectRepository.getAllProjectSettingsByProjectId(PROJECT_ID)).thenReturn(settings);

        // when
        Map<String, String> result = manager.getAllProjectSettingsByProjectId(PROJECT_ID);

        // then
        Assertions.assertEquals("", result.get("timeout"));
        Assertions.assertEquals("5", result.get("retryCount"));
    }

    // ==================== updateProjectSetting TESTS ====================

    @Test
    void updateProjectSetting_ShouldDelegateToRepository() {
        // given
        String propShortName = "timeout";
        String propValue = "60000";

        // when
        manager.updateProjectSetting(PROJECT_ID, propShortName, propValue);

        // then
        verify(stubProjectRepository).updateProjectSetting(PROJECT_ID, propShortName, propValue);
    }

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_ShouldNotThrowException() {
        // when & then
        Assertions.assertDoesNotThrow(() -> manager.afterDelete(project));
    }
}