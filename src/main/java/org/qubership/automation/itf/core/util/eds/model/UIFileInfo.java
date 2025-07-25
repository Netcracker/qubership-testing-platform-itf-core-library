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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UIFileInfo {

    private String id;
    private String fileName;
    private String filePath;
    private Long fileLength;
    private String uploadDate;
    private String contentType;
    private String userName;

    /**
     * Constructor.
     *
     * @param id - file id,
     * @param fileName - file name,
     * @param filePath - file path,
     * @param contentType - Content-type.
     */
    public UIFileInfo(String id, String fileName, String filePath, String contentType) {
        this.id = id;
        this.fileName = fileName;
        this.filePath = filePath;
        this.contentType = contentType;
    }
}
