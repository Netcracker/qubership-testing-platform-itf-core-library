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

package org.qubership.automation.itf.core.util.eds.repository;

import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.qubership.automation.itf.core.util.eds.configuration.ItfGridFsConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsCriteria;
import org.springframework.data.mongodb.gridfs.GridFsResource;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Repository;

import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;

@Repository("ItfGridFsRepository")
@ConditionalOnBean(ItfGridFsConfiguration.class)
public class ItfGridFsRepository {
    private final GridFsTemplate gridFsTemplate;

    @Autowired
    public ItfGridFsRepository(GridFsTemplate gridFsTemplate) {
        this.gridFsTemplate = gridFsTemplate;
    }

    public GridFSFindIterable findByMetadata(Map<String, Object> metadataParams) {
        return gridFsTemplate.find(createQuery(metadataParams));
    }

    public GridFSFile findById(ObjectId objectId) {
        return gridFsTemplate.findOne(new Query(Criteria.where("_id").is(objectId)));
    }

    public ObjectId store(Map<String, Object> metadataParams, String fileName, InputStream fileInputStream) {
        return gridFsTemplate.store(fileInputStream, fileName, createDocument(metadataParams));
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void delete(Map<String, Object> metadataParams, String fileName) {
        GridFSFindIterable gridFsFiles = gridFsTemplate.find(createQuery(metadataParams)
                .addCriteria(GridFsCriteria.whereFilename().is(fileName)));
        for (GridFSFile gridFsFile : gridFsFiles) {
            gridFsTemplate.delete(new Query(Criteria.where("_id").is(gridFsFile.getObjectId())));
        }
    }

    public GridFsResource getResource(GridFSFile gridFsFile) {
        return gridFsTemplate.getResource(gridFsFile);
    }

    private Document createDocument(Map<String, Object> metadata) {
        Document document = new Document();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            document.append(entry.getKey(), entry.getValue());
        }
        return document;
    }

    private Query createQuery(Map<String, Object> metadataParams) {
        Iterator<Map.Entry<String, Object>> entryIterator = metadataParams.entrySet().iterator();
        Map.Entry<String, Object> firstEntry = entryIterator.next();
        Criteria criteria = Criteria.where("metadata." + firstEntry.getKey()).is(firstEntry.getValue());
        while (entryIterator.hasNext()) {
            Map.Entry<String, Object> entry = entryIterator.next();
            criteria.and("metadata." + entry.getKey()).is(entry.getValue());
        }
        return new Query(criteria);
    }
}
