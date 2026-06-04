package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.FolderRepository;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.folder.ChainFolder;
import org.qubership.automation.itf.core.model.jpa.folder.EnvFolder;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.folder.ServerFolder;
import org.qubership.automation.itf.core.model.jpa.folder.SystemFolder;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FolderObjectManagerTest {

    private static final BigInteger FOLDER_ID = BigInteger.valueOf(100L);
    private static final BigInteger PROJECT_ID = BigInteger.valueOf(300L);
    private static final String FOLDER_NAME = "TestFolder";
    private static final String SESSION_ID = "test-session-id";

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private Folder<Storable> parentFolder;

    @Mock
    private Folder<Storable> subFolder;

    @Mock
    private Storable objectInFolder;

    @InjectMocks
    private FolderObjectManager manager;

    @BeforeEach
    void setUp() throws Exception {
        // Initialize subclasses map via reflection
        java.lang.reflect.Method initMethod = FolderObjectManager.class.getDeclaredMethod("init");
        initMethod.setAccessible(true);
        initMethod.invoke(manager);
    }

    // ==================== create TESTS ====================

    @Test
    void create_WithEnvFolderType_ShouldCreateEnvFolder() {
        // given
        String type = EnvFolder.TYPE.getSimpleName();
        List<Folder<Storable>> subFolders = new ArrayList<>();
        when(parentFolder.getSubFolders()).thenReturn(subFolders);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Folder result = manager.create(parentFolder, FOLDER_NAME, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(EnvFolder.class, result);
        Assertions.assertEquals(parentFolder, result.getParent());
        Assertions.assertEquals(FOLDER_NAME, result.getName());
        Assertions.assertEquals(type, result.getTypeName());
        Assertions.assertTrue(subFolders.contains(result));
        verify(folderRepository).save(result);
    }

    @Test
    void create_WithChainFolderType_ShouldCreateChainFolder() {
        // given
        String type = ChainFolder.TYPE.getSimpleName();
        List<Folder<Storable>> subFolders = new ArrayList<>();
        when(parentFolder.getSubFolders()).thenReturn(subFolders);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Folder result = manager.create(parentFolder, FOLDER_NAME, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(ChainFolder.class, result);
        Assertions.assertEquals(parentFolder, result.getParent());
        verify(folderRepository).save(result);
    }

    @Test
    void create_WithSystemFolderType_ShouldCreateSystemFolder() {
        // given
        String type = SystemFolder.TYPE.getSimpleName();
        List<Folder<Storable>> subFolders = new ArrayList<>();
        when(parentFolder.getSubFolders()).thenReturn(subFolders);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Folder result = manager.create(parentFolder, FOLDER_NAME, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(SystemFolder.class, result);
        verify(folderRepository).save(result);
    }

    @Test
    void create_WithServerFolderType_ShouldCreateServerFolder() {
        // given
        String type = ServerFolder.TYPE.getSimpleName();
        List<Folder<Storable>> subFolders = new ArrayList<>();
        when(parentFolder.getSubFolders()).thenReturn(subFolders);
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Folder result = manager.create(parentFolder, FOLDER_NAME, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertInstanceOf(ServerFolder.class, result);
        verify(folderRepository).save(result);
    }

    @Test
    void create_WithNullParent_ShouldCreateFolderWithoutParent() {
        // given
        String type = EnvFolder.TYPE.getSimpleName();
        when(folderRepository.save(any(Folder.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        Folder result = manager.create(null, FOLDER_NAME, type);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertNull(result.getParent());
        verify(folderRepository).save(result);
    }

    @Test
    void create_WithInvalidType_ShouldThrowIllegalArgumentException() {
        // given
        String invalidType = "InvalidFolderType";

        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create(parentFolder, FOLDER_NAME, invalidType));
        Assertions.assertTrue(ex.getMessage().contains("Cannot create folder of type " + invalidType));
        verify(folderRepository, never()).save(any());
    }

    // ==================== create() without parameters TESTS ====================

    @Test
    void create_WithoutParameters_ShouldThrowIllegalArgumentException() {
        // when & then
        IllegalArgumentException ex = Assertions.assertThrows(IllegalArgumentException.class,
                () -> manager.create());
        Assertions.assertTrue(ex.getMessage().contains("Cannot create step of unknown type"));
    }

    // ==================== findFolderByPieceOfName TESTS ====================

    @Test
    void findFolderByPieceOfName_WithChainFolder_ShouldReturnChainFolders() {
        // given
        String classShortName = "ChainFolder";
        String pieceOfName = "test";
        List<Folder> expectedFolders = List.of(mock(ChainFolder.class));
        when(folderRepository.findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, PROJECT_ID, "chains"))
                .thenReturn(expectedFolders);

        // when
        List<Folder> result = manager.findFolderByPieceOfName(classShortName, pieceOfName, PROJECT_ID);

        // then
        Assertions.assertSame(expectedFolders, result);
        verify(folderRepository).findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, PROJECT_ID, "chains");
    }

    @Test
    void findFolderByPieceOfName_WithEnvFolder_ShouldReturnEnvFolders() {
        // given
        String classShortName = "EnvFolder";
        String pieceOfName = "test";
        List<Folder> expectedFolders = List.of(mock(EnvFolder.class));
        when(folderRepository.findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, PROJECT_ID, "envs"))
                .thenReturn(expectedFolders);

        // when
        List<Folder> result = manager.findFolderByPieceOfName(classShortName, pieceOfName, PROJECT_ID);

        // then
        Assertions.assertSame(expectedFolders, result);
        verify(folderRepository).findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, PROJECT_ID, "envs");
    }

    @Test
    void findFolderByPieceOfName_WithSystemFolder_ShouldReturnSystemFolders() {
        // given
        String classShortName = "SystemFolder";
        String pieceOfName = "test";
        List<Folder> expectedFolders = List.of(mock(SystemFolder.class));
        when(folderRepository.findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, PROJECT_ID, "systems"))
                .thenReturn(expectedFolders);

        // when
        List<Folder> result = manager.findFolderByPieceOfName(classShortName, pieceOfName, PROJECT_ID);

        // then
        Assertions.assertSame(expectedFolders, result);
        verify(folderRepository).findFolderByProjectAndNameContainingIgnoreCase(pieceOfName, PROJECT_ID, "systems");
    }

    @Test
    void findFolderByPieceOfName_WithUnknownType_ShouldReturnEmptyList() {
        // given
        String classShortName = "UnknownFolder";
        String pieceOfName = "test";

        // when
        List<Folder> result = manager.findFolderByPieceOfName(classShortName, pieceOfName, PROJECT_ID);

        // then
        Assertions.assertNotNull(result);
        Assertions.assertTrue(result.isEmpty());
        verify(folderRepository, never()).findFolderByProjectAndNameContainingIgnoreCase(any(), any(), any());
    }

    // ==================== afterDelete TESTS ====================

    @Test
    void afterDelete_WhenParentIsFolder_ShouldRemoveFromParentSubFolders() {
        // given
        List<Folder<Storable>> subFolders = new ArrayList<>();
        subFolders.add(subFolder);
        when(subFolder.getParent()).thenReturn(parentFolder);
        when(parentFolder.getSubFolders()).thenReturn(subFolders);

        // when
        manager.afterDelete(subFolder);

        // then
        Assertions.assertFalse(subFolders.contains(subFolder));
    }

    @Test
    void afterDelete_WhenParentIsNotFolder_ShouldNotRemoveAnything() {
        // given
        Storable nonFolderParent = mock(Storable.class);
        when(subFolder.getParent()).thenReturn(nonFolderParent);

        // when
        manager.afterDelete(subFolder);

        // then
        verify(parentFolder, never()).getSubFolders();
    }

    @Test
    void afterDelete_WhenParentIsNull_ShouldDoNothing() {
        // given
        when(subFolder.getParent()).thenReturn(null);

        // when
        manager.afterDelete(subFolder);

        // then
        verify(parentFolder, never()).getSubFolders();
    }
}