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

package org.qubership.automation.itf.core.util.eds.model;

import java.io.InputStream;
import java.util.Date;
import java.util.UUID;

import org.bson.types.ObjectId;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sun.xml.messaging.saaj.util.ByteInputStream;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo {
    private ObjectId objectId;
    private String fileName;
    private String filePath;
    private Long fileLength;
    private String contentType;
    private UUID projectUuid;
    private String userName;
    private UUID userId;
    private Date uploadDate;
    private FileEventType eventType;
    @JsonDeserialize(as = ByteInputStream.class)
    private InputStream inputStream;

    /**
     * Constructor from not all fields provided.
     */
    public FileInfo(ObjectId objectId, String fileName, String filePath, String contentType, UUID projectUuid,
                    InputStream inputStream, FileEventType eventType) {
        this.objectId = objectId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.contentType = contentType;
        this.projectUuid = projectUuid;
        this.inputStream = inputStream;
        this.eventType = eventType;
    }

    /**
     * Constructor from not all fields provided.
     */
    public FileInfo(ObjectId objectId, String fileName, String filePath, Long fileLength, String contentType,
                    UUID projectUuid, String userName, UUID userId, Date uploadDate, InputStream inputStream) {
        this.objectId = objectId;
        this.fileName = fileName;
        this.filePath = filePath;
        this.fileLength = fileLength;
        this.contentType = contentType;
        this.projectUuid = projectUuid;
        this.userName = userName;
        this.userId = userId;
        this.uploadDate = uploadDate;
        this.inputStream = inputStream;
    }
}
