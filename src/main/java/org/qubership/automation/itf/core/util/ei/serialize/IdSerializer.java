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
import java.math.BigInteger;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.folder.Folder;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;

public class IdSerializer extends AbstractSerializer<Storable> {

    public IdSerializer() {
    }

    @Override
    public void serialize(Storable storable, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (storable instanceof Folder
                && storable.getParent() == null) {
            gen.writeNull();
        } else {
            gen.writeNumber(((BigInteger) storable.getID()).longValue());
        }
    }
}
