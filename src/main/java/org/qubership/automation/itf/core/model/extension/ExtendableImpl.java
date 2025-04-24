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

package org.qubership.automation.itf.core.model.extension;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ExtendableImpl extends AbstractStorable implements Extendable, Serializable {
    private static final long serialVersionUID = 20240812L;
    private Set<Extension> extensions = Sets.newHashSetWithExpectedSize(5);

    @Override
    public boolean extend(Extension extension) {
        return extensions.add(extension);
    }

    @JsonIgnore
    @Override
    public <T extends Extension> T getExtension(Class<T> extensionClass) {
        for (Extension extension : extensions) {
            if (extensionClass.isAssignableFrom(extension.getClass())) {
                return extensionClass.cast(extension);
            }
        }
        return null;
    }

    @Override
    public String getExtensionsJson() {
        Gson gson = new Gson();
        JsonObject result = new JsonObject();
        for (Extension extension : extensions) {
            result.add(extension.getClass().getName(), gson.toJsonTree(extension));
        }
        return gson.toJson(result);
    }

    @Override
    public void setExtensionsJson(String extensionsJson) {
        if (extensionsJson != null) {
            JsonParser parser = new JsonParser();
            JsonObject jsonObject = parser.parse(extensionsJson).getAsJsonObject();
            Gson gson = new Gson();
            extensions.clear();
            for (Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                int idx = entry.getKey().indexOf("$$");
                String className = idx > 1 ? entry.getKey().substring(0, idx) : entry.getKey();
                try {
                    Class<? extends Extension> clazz = Class.forName(className).asSubclass(Extension.class);
                    Extension extension = gson.fromJson(entry.getValue(), clazz);
                    extensions.add(extension);
                } catch (ClassNotFoundException e) {
                    LOGGER.warn("Cannot read extension - class not found {}", className);
                } catch (Exception e) {
                    LOGGER.error("Cannot read extension - {}; runtime exception: {}", className, e);
                }
            }
        }
    }
}
