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
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.util.exception.NoSuchManagerException;
import org.qubership.automation.itf.core.util.manager.CoreObjectManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ImportedDataCache {

    public static final String SIMPLE_PARENT_MARKER = "#SimpleParent#";
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportedDataCache.class);
    private final Map<BigInteger, Storable> importedDataCache = new ConcurrentHashMap<>();
    private BigInteger projectId;

    /**
     * Deserialize storable identified by 'id' treeNode.
     *
     * @return deserialized Storable object
     */
    public synchronized Storable deserialize(JsonParser p, BigInteger sessionId) throws IOException {
        JsonNode treeNode = p.getCodec().readTree(p);
        BigInteger id = new BigInteger(treeNode.get("id").asText());
        boolean jsonIsSimpleParent = checkJsonSimpleParent(treeNode.get("description"));
        Storable storable = importedDataCache.get(id);
        if (storable == null || !jsonIsSimpleParent && storable.getDescription() != null && storable.getDescription()
                .equals(SIMPLE_PARENT_MARKER)) {
            storable = getDeserializedEntity(p, treeNode, id);
            if (storable.getParent() == null && "ROOT".equals(storable.getName())) {
                storable = storable.findRootObject(projectId);
                importedDataCache.put((BigInteger) storable.getID(), storable);
            } else {
                storable.performPostImportActions(projectId, sessionId);
            }
        }
        return storable;
    }

    private Storable getDeserializedEntity(JsonParser p, JsonNode treeNode, BigInteger entityId) {
        String clazz = treeNode.get("type").asText();
        try {
            Class<? extends Storable> storableClass = Class.forName(clazz).asSubclass(Storable.class);
            return ((ObjectMapper) p.getCodec()).readValue(treeNode.toString(), storableClass);
        } catch (ClassNotFoundException | JsonProcessingException e) {
            LOGGER.warn(String.format("Class for type=%s of entity with id=%s was not found.", clazz, entityId));
            throw new IllegalArgumentException(e);
        }
    }

    public Storable getById(BigInteger id) {
        return importedDataCache.get(id);
    }

    /**
     * Populate and return array of imported objects from importedDataCache for className parameter.
     *
     * @return ArrayList of Storable objects
     */
    public ArrayList<Storable> getStorablesByClassName(String className) {
        ArrayList<Storable> storables = new ArrayList();
        for (Storable storable : importedDataCache.values()) {
            if (storable.getClass().getName().equals(className)) {
                storables.add(storable);
            }
        }
        return storables;
    }

    public void put(BigInteger storableId, Storable storable) {
        importedDataCache.put(storableId, storable);
    }

    /**
     * Populate and return array of all imported objects from importedDataCache.
     *
     * @return ArrayList of Storable objects
     */
    public ArrayList<Storable> getAll() {
        ArrayList<Storable> storables = new ArrayList();
        Storable next;
        for (Iterator iterator = importedDataCache.values().iterator(); iterator.hasNext(); ) {
            next = (Storable) iterator.next();
            try {
                CoreObjectManager.getInstance().getManager(next.getClass());
                storables.add(next);
            } catch (NoSuchManagerException ex) {
                // Silently do nothing. In real life it's impossible event
            }
        }
        return storables;
    }

    private boolean checkJsonSimpleParent(JsonNode node) {
        if (node == null || node.isNull() || !node.isTextual()) {
            return false;
        } else {
            return node.asText().equals(SIMPLE_PARENT_MARKER);
        }
    }

    public BigInteger getProjectId() {
        return projectId;
    }

    public void setProjectId(BigInteger projectId) {
        this.projectId = projectId;
    }
}
