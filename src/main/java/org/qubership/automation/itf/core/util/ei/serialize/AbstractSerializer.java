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

package org.qubership.automation.itf.core.util.ei.serialize;

import java.io.IOException;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;
import org.qubership.automation.itf.core.model.jpa.system.System;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.jsontype.TypeSerializer;

abstract class AbstractSerializer<T extends Storable> extends JsonSerializer<T> {

    @Override
    public void serializeWithType(T value, JsonGenerator gen, SerializerProvider provider,
                                  TypeSerializer typeSer) throws IOException {
        typeSer.writeTypePrefixForObject(value, gen);
        serialize(value, gen, provider);
        typeSer.writeTypeSuffixForObject(value, gen);
    }

    Folder getHierachy(Storable folderParent) {
        if (folderParent != null) {
            Folder<System> simpleFolder = new Folder<>();
            simpleFolder.setID(folderParent.getID());
            simpleFolder.setName(folderParent.getName());
            simpleFolder.setDescription(folderParent.getDescription());
            simpleFolder.setVersion(folderParent.getVersion());
            simpleFolder.setNaturalId(folderParent.getNaturalId());
            simpleFolder.setProject(((Folder) folderParent).getProject());
            simpleFolder.setParent(getHierachy(folderParent.getParent()));

            return simpleFolder;
        } else {
            return null;
        }
    }
}
