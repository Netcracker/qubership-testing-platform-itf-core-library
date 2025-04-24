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

import org.qubership.automation.itf.core.util.eds.service.FileManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import lombok.Getter;

@Service
@Getter
public class ExternalDataManagementService {

    private ExternalStorageService externalStorageService;
    private FileManagementService fileManagementService;

    @Autowired
    public ExternalDataManagementService(ExternalStorageService externalStorageService,
                                         FileManagementService fileManagementService) {
        this.externalStorageService = externalStorageService;
        this.fileManagementService = fileManagementService;
    }

    public boolean checkFolderByContent(String contentType) {
        return fileManagementService.isContentTypeExist(contentType);
    }
}
