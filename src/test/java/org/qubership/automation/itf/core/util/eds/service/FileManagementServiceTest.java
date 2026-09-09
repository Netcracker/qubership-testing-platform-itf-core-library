/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.qubership.automation.itf.core.util.eds.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.qubership.automation.itf.core.util.eds.model.FileInfo;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class FileManagementServiceTest {

    private static final String WSDL_XSD = EdsContentType.WSDL_XSD.getStringValue();
    private static final String KEYSTORE = EdsContentType.KEYSTORE.getStringValue();

    @Mock(answer = Answers.CALLS_REAL_METHODS)
    private InputStream faultyInputStream;

    private FileManagementService service;
    private Path root;

    @BeforeEach
    void setUp(@TempDir Path tempDir) {
        service = new FileManagementService();
        root = tempDir.resolve("storage-root");
        ReflectionTestUtils.setField(service, "rootFolder", root.toString());
    }

    // ==================== save(...) - legitimate paths ====================

    @Test
    @DisplayName("Should save a file under the project directory for a non-keystore content type")
    void save_withPlainFileName_writesInsideProjectDirectory() throws IOException {
        UUID project = UUID.randomUUID();

        File written = service.save(WSDL_XSD, project, "/sub", "schema.xsd", contentStream("payload"));

        assertNotNull(written);
        Path expected = root.resolve(WSDL_XSD).resolve(project.toString()).resolve("sub").resolve("schema.xsd");
        assertEquals(expected.toAbsolutePath().normalize(), written.toPath().toAbsolutePath().normalize());
        assertEquals("payload", Files.readString(written.toPath(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Should save a keystore file directly under the content-type directory, without a project segment")
    void save_forKeystoreContentType_omitsProjectSegment() {
        UUID project = UUID.randomUUID();

        File written = service.save(KEYSTORE, project, "", "truststore.jks", contentStream("secret"));

        assertNotNull(written);
        Path expected = root.resolve(KEYSTORE).resolve("truststore.jks");
        assertEquals(expected.toAbsolutePath().normalize(), written.toPath().toAbsolutePath().normalize());
    }

    // ==================== save(...) - path traversal ====================

    @Test
    @DisplayName("Should reject a fileName that escapes the storage root and write nothing outside it")
    void save_rejectsFileNameEscapingRoot() {
        UUID project = UUID.randomUUID();
        String maliciousFileName = "../../../../../../../../tmp/itf-eds-escaped.txt";
        Path wouldBeTarget = naivelyResolvedTarget(WSDL_XSD, project, "", maliciousFileName);
        assertEscapesRoot(wouldBeTarget);

        try {
            File written = service.save(WSDL_XSD, project, "", maliciousFileName, contentStream("pwned"));

            assertNull(written);
            assertFalse(Files.exists(wouldBeTarget), "payload must not land outside the storage root");
        } finally {
            deleteQuietly(wouldBeTarget);
        }
    }

    @Test
    @DisplayName("Should reject a fileName containing a backslash even where '/' is not the platform separator")
    void save_rejectsFileNameWithBackslash() {
        UUID project = UUID.randomUUID();

        File written = service.save(WSDL_XSD, project, "", "..\\..\\..\\escaped.txt", contentStream("pwned"));

        assertNull(written);
    }

    @Test
    @DisplayName("Should reject a filePath that escapes the storage root and write nothing outside it")
    void save_rejectsFilePathEscapingRoot() {
        UUID project = UUID.randomUUID();
        String maliciousFilePath = "/../../../../../../../../tmp";
        String fileName = "itf-eds-escaped.txt";
        Path wouldBeTarget = naivelyResolvedTarget(WSDL_XSD, project, maliciousFilePath, fileName);
        assertEscapesRoot(wouldBeTarget);

        try {
            File written = service.save(WSDL_XSD, project, maliciousFilePath, fileName, contentStream("pwned"));

            assertNull(written);
            assertFalse(Files.exists(wouldBeTarget), "payload must not land outside the storage root");
        } finally {
            deleteQuietly(wouldBeTarget);
        }
    }

    @Test
    @DisplayName("Should reject a traversal through the keystore branch, which does not use a project segment")
    void save_rejectsFilePathEscapingRoot_forKeystoreContentType() {
        UUID project = UUID.randomUUID();
        String maliciousFilePath = "/../../../../../../../../tmp";
        String fileName = "itf-eds-escaped-keystore.txt";
        Path wouldBeTarget = naivelyResolvedTarget(KEYSTORE, project, maliciousFilePath, fileName);
        assertEscapesRoot(wouldBeTarget);

        try {
            File written = service.save(KEYSTORE, project, maliciousFilePath, fileName, contentStream("pwned"));

            assertNull(written);
            assertFalse(Files.exists(wouldBeTarget), "payload must not land outside the storage root");
        } finally {
            deleteQuietly(wouldBeTarget);
        }
    }

    @Test
    @DisplayName("Should reject a fileName composed only of path traversal segments")
    void save_rejectsFileName_whenSaveOnFileInfoCarriesTraversal() {
        FileInfo fileInfo = new FileInfo();
        fileInfo.setContentType(WSDL_XSD);
        fileInfo.setProjectUuid(UUID.randomUUID());
        fileInfo.setFilePath("");
        fileInfo.setFileName("../../../../etc/escaped.txt");
        fileInfo.setInputStream(contentStream("pwned"));

        File written = service.save(fileInfo);

        assertNull(written);
    }

    // ==================== save(...) - I/O failure ====================

    @Test
    @DisplayName("Should return null, not throw, when the input stream fails to read")
    void save_returnsNull_whenInputStreamFails() throws IOException {
        when(faultyInputStream.read()).thenThrow(new IOException("boom"));

        File written = service.save(WSDL_XSD, UUID.randomUUID(), "", "schema.xsd", faultyInputStream);

        assertNull(written);
    }

    // ==================== getDirectoryPath(...) ====================

    @Test
    @DisplayName("Should compute the keystore directory path without a project segment")
    void getDirectoryPath_forKeystore_returnsRootChild() {
        Path path = service.getDirectoryPath(KEYSTORE, UUID.randomUUID(), "");

        assertEquals(root.resolve(KEYSTORE).toAbsolutePath().normalize(), path);
    }

    @Test
    @DisplayName("Should throw when a keystore filePath escapes the storage root")
    void getDirectoryPath_forKeystore_rejectsTraversal() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getDirectoryPath(KEYSTORE, UUID.randomUUID(), "/../../../../../../tmp"));
    }

    @Test
    @DisplayName("Should throw when a non-keystore filePath escapes the storage root")
    void getDirectoryPath_forProjectContentType_rejectsTraversal() {
        assertThrows(IllegalArgumentException.class,
                () -> service.getDirectoryPath(WSDL_XSD, UUID.randomUUID(), "/../../../../../../tmp"));
    }

    // ==================== delete(...) ====================

    @Test
    @DisplayName("Should delete a file that resides inside the storage root")
    void delete_removesFileInsideRoot() throws IOException {
        UUID project = UUID.randomUUID();
        Path directory = root.resolve(WSDL_XSD).resolve(project.toString());
        Files.createDirectories(directory);
        Path file = directory.resolve("to-delete.xsd");
        Files.writeString(file, "content", StandardCharsets.UTF_8);

        FileInfo fileInfo = new FileInfo();
        fileInfo.setContentType(WSDL_XSD);
        fileInfo.setProjectUuid(project);
        fileInfo.setFilePath("");
        fileInfo.setFileName("to-delete.xsd");

        service.delete(fileInfo);

        assertFalse(Files.exists(file));
    }

    @Test
    @DisplayName("Should not touch a file outside the storage root when fileName carries a traversal payload")
    void delete_doesNotDeleteFileOutsideRoot_whenFileNameEscapes() throws IOException {
        UUID project = UUID.randomUUID();
        String maliciousFileName = "../../../../../../../../tmp/itf-eds-outside.txt";
        Path outsideFile = naivelyResolvedTarget(WSDL_XSD, project, "", maliciousFileName);
        assertEscapesRoot(outsideFile);
        Files.createDirectories(outsideFile.getParent());
        Files.writeString(outsideFile, "do-not-delete", StandardCharsets.UTF_8);

        try {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setContentType(WSDL_XSD);
            fileInfo.setProjectUuid(project);
            fileInfo.setFilePath("");
            fileInfo.setFileName(maliciousFileName);

            service.delete(fileInfo);

            assertTrue(Files.exists(outsideFile), "a file outside the storage root must not be touched");
        } finally {
            deleteQuietly(outsideFile);
        }
    }

    @Test
    @DisplayName("Should not touch a file outside the storage root when filePath carries a traversal payload")
    void delete_doesNotDeleteFileOutsideRoot_whenFilePathEscapes() throws IOException {
        UUID project = UUID.randomUUID();
        String maliciousFilePath = "/../../../../../../../../tmp";
        String fileName = "itf-eds-outside-by-path.txt";
        Path outsideFile = naivelyResolvedTarget(WSDL_XSD, project, maliciousFilePath, fileName);
        assertEscapesRoot(outsideFile);
        Files.createDirectories(outsideFile.getParent());
        Files.writeString(outsideFile, "do-not-delete", StandardCharsets.UTF_8);

        try {
            FileInfo fileInfo = new FileInfo();
            fileInfo.setContentType(WSDL_XSD);
            fileInfo.setProjectUuid(project);
            fileInfo.setFilePath(maliciousFilePath);
            fileInfo.setFileName(fileName);

            service.delete(fileInfo);

            assertTrue(Files.exists(outsideFile), "a file outside the storage root must not be touched");
        } finally {
            deleteQuietly(outsideFile);
        }
    }

    private InputStream contentStream(String content) {
        return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Reproduces the pre-fix path composition (root + contentType [+ project] + filePath, then + fileName)
     * so a test can assert that nothing lands at the location the vulnerable code used to write to.
     */
    private Path naivelyResolvedTarget(String contentType, UUID project, String filePath, String fileName) {
        String directoryName = service.getDirectory(contentType, project, filePath);
        return Path.of(directoryName, fileName).toAbsolutePath().normalize();
    }

    /**
     * Fails the test if candidate does not actually resolve outside the storage root, so a traversal test
     * cannot pass merely because its payload failed to escape on the current platform.
     */
    private void assertEscapesRoot(Path candidate) {
        assertFalse(candidate.startsWith(root.toAbsolutePath().normalize()),
                "sanity check: the crafted payload must actually resolve outside the storage root");
    }

    private void deleteQuietly(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // best-effort cleanup of a path a failing test may have created outside the storage root
        }
    }
}
