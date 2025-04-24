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

package org.qubership.automation.itf.core.util.ei.deserialize;

import java.io.IOException;

import org.qubership.automation.itf.core.model.jpa.folder.Folder;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class FolderDeserializer<T extends Folder> extends JsonDeserializer<T> {

    private Class<T> folderClass;

    public FolderDeserializer(Class<T> folderClass) {
        this.folderClass = folderClass;
    }

    @Override
    public T deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws IOException {
        try {
            T folder = folderClass.newInstance();
            folder.setID(jsonParser.getBigIntegerValue());
            return folder;
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Unknown type of folder: " + folderClass.getName());
            throw new IllegalArgumentException(e);
        }
    }
}
