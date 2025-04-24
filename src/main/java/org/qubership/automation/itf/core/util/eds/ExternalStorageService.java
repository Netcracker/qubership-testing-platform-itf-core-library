/*
 *  Copyright 2024-2025 NetCracker Technology Corporation
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

package org.qubership.automation.itf.core.util.eds;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bson.types.ObjectId;
import org.qubership.automation.itf.core.util.eds.model.FileInfo;
import org.springframework.stereotype.Service;

@Service
public interface ExternalStorageService {
    Set<FileInfo> getFilesInfoByProject(UUID uuid) throws IOException;

    Set<FileInfo> getKeyStoreFileInfo() throws IOException;

    Set<FileInfo> getFilesInfoByMetadataMapParams(Map<String, Object> metadataParams) throws IOException;

    FileInfo getFileInfo(ObjectId objectId) throws IOException;

    ObjectId store(String contentType, UUID projectUuid, String userName, UUID userId,
                   String filePath, String fileName, InputStream fileInputStream);

    void delete(String contentType, UUID projectUuid, String filePath, String fileName);
}
