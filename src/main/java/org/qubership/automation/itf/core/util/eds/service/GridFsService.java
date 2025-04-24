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

package org.qubership.automation.itf.core.util.eds.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.qubership.automation.itf.core.util.eds.ExternalStorageService;
import org.qubership.automation.itf.core.util.eds.configuration.ItfGridFsConfiguration;
import org.qubership.automation.itf.core.util.eds.model.FileInfo;
import org.qubership.automation.itf.core.util.eds.repository.ItfGridFsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.stereotype.Component;

import com.mongodb.client.gridfs.model.GridFSFile;

@Component
@ConditionalOnBean(ItfGridFsConfiguration.class)
public class GridFsService implements ExternalStorageService {

    private final ItfGridFsRepository gridFsRepository;

    @Autowired
    public GridFsService(@Qualifier("ItfGridFsRepository") ItfGridFsRepository gridFsRepository) {
        this.gridFsRepository = gridFsRepository;
    }

    @Override
    public Set<FileInfo> getFilesInfoByProject(UUID uuid) throws IOException {
        return getFilesInfoByMetadata(createMetadataParamsMap(EdsMetaInfo.PROJECT_UUID.getStringValue(), uuid));
    }

    @Override
    public Set<FileInfo> getKeyStoreFileInfo() throws IOException {
        return getFilesInfoByMetadata(createMetadataParamsMap(EdsMetaInfo.CONTENT_TYPE.getStringValue(),
                EdsContentType.KEYSTORE.getStringValue()));
    }

    public Set<FileInfo> getFilesInfoByMetadataMapParams(Map<String, Object> metadataParams) throws IOException {
        return getFilesInfoByMetadata(metadataParams);
    }

    @Override
    public FileInfo getFileInfo(ObjectId objectId) throws IOException {
        return createFileInfo(gridFsRepository.findById(objectId));
    }

    @Override
    public ObjectId store(String contentType, UUID projectUuid, String userName, UUID userId,
                          String filePath, String fileName, InputStream fileInputStream) {
        Map<String, Object> map = createMetadataParamsMap(EdsMetaInfo.FILE_PATH.getStringValue(), filePath);
        map.put(EdsMetaInfo.CONTENT_TYPE.getStringValue(), contentType);
        if (projectUuid != null) {
            map.put(EdsMetaInfo.PROJECT_UUID.getStringValue(), projectUuid);
        }
        gridFsRepository.delete(map, fileName);
        map.put(EdsMetaInfo.USER_NAME.getStringValue(), userName);
        map.put(EdsMetaInfo.USER_ID.getStringValue(), userId);
        return gridFsRepository.store(map, fileName, fileInputStream);
    }

    @Override
    public void delete(String contentType, UUID projectUuid, String filePath, String fileName) {
        Map<String, Object> map = createMetadataParamsMap(EdsMetaInfo.FILE_PATH.getStringValue(), filePath);
        map.put(EdsMetaInfo.CONTENT_TYPE.getStringValue(), contentType);
        if (projectUuid != null) {
            map.put(EdsMetaInfo.PROJECT_UUID.getStringValue(), projectUuid);
        }
        gridFsRepository.delete(map, fileName);
    }

    private Set<FileInfo> getFilesInfoByMetadata(Map<String, Object> metadataParams) throws IOException {
        Set<FileInfo> result = new HashSet<>();
        for (GridFSFile gridFsFile : gridFsRepository.findByMetadata(metadataParams)) {
            FileInfo fileInfo = createFileInfo(gridFsFile);
            if (fileInfo != null) {
                result.add(fileInfo);
            }
        }
        return result;
    }

    private FileInfo createFileInfo(GridFSFile gridFsFile) throws IOException {
        if (gridFsFile != null) {
            Document metadata = gridFsFile.getMetadata();
            if (metadata != null) {
                GridFsResource gridFsResource = gridFsRepository.getResource(gridFsFile);
                return new FileInfo(gridFsFile.getObjectId(),
                        gridFsFile.getFilename(),
                        metadata.get(EdsMetaInfo.FILE_PATH.getStringValue(), String.class),
                        gridFsFile.getLength(),
                        metadata.getString(EdsMetaInfo.CONTENT_TYPE.getStringValue()),
                        metadata.get(EdsMetaInfo.PROJECT_UUID.getStringValue(), UUID.class),
                        metadata.get(EdsMetaInfo.USER_NAME.getStringValue(), String.class),
                        metadata.get(EdsMetaInfo.USER_ID.getStringValue(), UUID.class),
                        gridFsFile.getUploadDate(),
                        gridFsResource.getInputStream()
                );
            }
        }
        return null;
    }

    private Map<String, Object> createMetadataParamsMap(String key, Object value) {
        Map<String, Object> params = new HashMap<>();
        params.put(key, value);
        return params;
    }
}
