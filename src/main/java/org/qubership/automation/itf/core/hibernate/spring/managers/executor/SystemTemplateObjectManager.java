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

package org.qubership.automation.itf.core.hibernate.spring.managers.executor;

import static org.qubership.automation.itf.core.util.converter.IdConverter.toBigInt;

import java.math.BigInteger;
import java.util.Collection;
import java.util.List;

import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.OutboundTransportConfigurationRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.StepRepository;
import org.qubership.automation.itf.core.hibernate.spring.repositories.executor.SystemTemplateRepository;
import org.qubership.automation.itf.core.model.IdNamePair;
import org.qubership.automation.itf.core.model.jpa.message.template.SystemTemplate;
import org.qubership.automation.itf.core.model.jpa.system.System;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SystemTemplateObjectManager extends TemplateObjectManager<System, SystemTemplate> {

    private final SystemTemplateRepository systemTemplateRepository;

    @Autowired
    public SystemTemplateObjectManager(SystemTemplateRepository systemTemplateRepository,
                                       StepRepository stepRepository,
                                       OutboundTransportConfigurationRepository otcRepository) {
        super(SystemTemplate.class, systemTemplateRepository, stepRepository, otcRepository);
        this.systemTemplateRepository = systemTemplateRepository;
    }

    @Override
    public Collection<SystemTemplate> getByPieceOfNameAndProject(String name, Object projectId) {
        return systemTemplateRepository.findByPieceOfNameAndProject(name, toBigInt(projectId));
    }

    public List<IdNamePair> getByPieceOfNameAndParentId(String name, BigInteger parentId) {
        return systemTemplateRepository.findByPieceOfNameAndParentId(name, parentId);
    }

    @Override
    public Collection<SystemTemplate> getByParentNameAndProject(String name, Object projectId) {
        return systemTemplateRepository.findByParentNameAndProject(name, toBigInt(projectId));
    }

    public SystemTemplate getByIdOnly(BigInteger id) {
        return ((SystemTemplateRepository)repository).findByIdOnly(id);
    }

    public List<IdNamePair> getSimpleSystemTemplatesByParentId(BigInteger parentId) {
        return systemTemplateRepository.findSimpleSystemTemplatesByParentId(parentId);
    }
}
