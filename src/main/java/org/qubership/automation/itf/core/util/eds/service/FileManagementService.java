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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Collection;
import java.util.UUID;

import org.qubership.automation.itf.core.util.eds.model.FileInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class FileManagementService {

    @Value("${local.storage.directory:data}")
    private String rootFolder;

    /**
     * Save files identified by filesInfo collection.
     */
    public void save(Collection<FileInfo> filesInfo) {
        for (FileInfo fileInfo : filesInfo) {
            save(fileInfo);
        }
    }

    /**
     * Save file identified by fileInfo.
     */
    public File save(FileInfo fileInfo) {
        return save(fileInfo.getContentType(), fileInfo.getProjectUuid(), fileInfo.getFilePath(),
                fileInfo.getFileName(), fileInfo.getInputStream());
    }

    /**
     * Save file identified by separate parameters.
     */
    public File save(String contentType, UUID projectUuid, String filePath, String fileName, InputStream inputStream) {
        String directoryName = getDirectory(contentType, projectUuid, filePath);
        try {
            File targetFile = findOrCreateDirectoryWithFile(directoryName, fileName);
            Files.copy(inputStream, targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return targetFile;
        } catch (Exception exc) {
            log.error("Error while saving file '{}' to local directory: {}", fileName, directoryName, exc);
            return null;
        }
    }

    /**
     * Delete file by fileInfo.
     */
    public void delete(FileInfo fileInfo) {
        Path path;
        try {
            Path directory = getDirectoryPath(fileInfo.getContentType(), fileInfo.getProjectUuid(),
                    fileInfo.getFilePath());
            path = resolveWithinRoot(directory, fileInfo.getFileName());
        } catch (IllegalArgumentException e) {
            log.error("An error occurred while deleting file '{}': {}", fileInfo.getFileName(), e.getMessage());
            return;
        }
        try {
            Files.delete(path);
            log.info("File by path '{}' is deleted from storage successfully.", path);
        } catch (IOException e) {
            log.error("An error occurred while deleting file by path '{}'.", path, e);
        }
    }

    /**
     * Checks if contentType exists in {@link EdsContentType}.
     */
    public boolean isContentTypeExist(String contentType) {
        for (EdsContentType value : EdsContentType.values()) {
            if (contentType.equals(value.getStringValue())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Creates file (and directory, if needed).
     */
    private File findOrCreateDirectoryWithFile(String directoryName, String fileName) {
        Path target = resolveWithinRoot(Path.of(directoryName), fileName);
        File directory = target.getParent().toFile();
        if (!directory.exists() && !directory.mkdirs()) {
            log.info("Directory {} is created", directoryName);
        }
        return target.toFile();
    }

    /**
     * Resolves fileName against directory and returns the result.
     *
     * @throws IllegalArgumentException if fileName contains a path separator, or the resolved
     *                                  path is not under the storage root.
     */
    private Path resolveWithinRoot(Path directory, String fileName) {
        if (fileName == null || fileName.isEmpty() || fileName.contains("/") || fileName.contains("\\")) {
            throw new IllegalArgumentException("File name '" + fileName + "' must not contain path separators.");
        }
        Path root = Path.of(rootFolder).toAbsolutePath().normalize();
        Path target = directory.resolve(fileName).toAbsolutePath().normalize();
        if (!target.startsWith(root)) {
            throw new IllegalArgumentException("File name '" + fileName
                    + "' resolves outside of the storage root '" + root + "'.");
        }
        return target;
    }

    /**
     * Get full path to file.
     */
    public String getDirectory(String contentType, UUID projectUuid, String filePath) {
        return dontUseProjectForCreatingDirectory(contentType)
                ? rootFolder + "/" + contentType + filePath
                : rootFolder + "/" + contentType + "/" + projectUuid + filePath;
    }

    /**
     * Returns the storage directory for contentType, projectUuid and filePath.
     *
     * @throws IllegalArgumentException if filePath resolves outside of the storage root.
     */
    public Path getDirectoryPath(String contentType, UUID projectUuid, String filePath) {
        Path root = Path.of(rootFolder).toAbsolutePath().normalize();
        Path directory = EdsContentType.KEYSTORE.getStringValue().equals(contentType)
                ? Path.of(rootFolder, contentType, filePath)
                : Path.of(rootFolder, contentType, projectUuid.toString(), filePath);
        Path normalized = directory.toAbsolutePath().normalize();
        if (!normalized.startsWith(root)) {
            throw new IllegalArgumentException("File path '" + filePath + "' resolves outside of the storage root.");
        }
        return normalized;
    }

    /**
     * Returns true if files of contentType should be in separate project folders.
     * Otherwise, returns false.
     */
    public boolean dontUseProjectForCreatingDirectory(String contentType) {
        return EdsContentType.KEYSTORE.getStringValue().equals(contentType);
    }

    /**
     * Returns predefined path for contentType or path parameter if there is no predefined path.
     */
    public String calcPredefinedPath(String contentType, String path) {
        return EdsContentType.getPredefinedPathByType(contentType) == null
                ? path
                : EdsContentType.getPredefinedPathByType(contentType);
    }

    /**
     * Returns predefined fileName for contentType or fileName parameter if there is no predefined fileName.
     */
    public String calcPredefinedFileName(String contentType, String fileName) {
        return EdsContentType.getPredefinedFileNameByType(contentType) == null
                ? fileName
                : EdsContentType.getPredefinedFileNameByType(contentType);
    }
}
