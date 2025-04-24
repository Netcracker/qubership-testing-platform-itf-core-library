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

package org.qubership.automation.itf.core.model.jpa.context;

import java.math.BigInteger;
import java.util.Date;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.jpa.instance.SituationInstance;
import org.qubership.automation.itf.core.model.jpa.instance.chain.CallChainInstance;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.util.annotation.ProduceNewObject;
import org.qubership.automation.itf.core.util.constants.Status;

import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class TcContextBriefInfo extends AbstractStorable {
    private static final long serialVersionUID = 20240812L;

    Object id;
    private String name;
    private Object environment;
    private String envname;
    private String operationName;
    private BigInteger systemId = null;
    private String systemName;
    private Object initiator;
    private String ininame;
    private Status status = Status.NOT_STARTED;
    private Date startTime;
    private Date endTime;
    private Long duration;
    private String client;
    private String initiatortype;
    private BigInteger situationId = null;
    private BigInteger chainId = null;
    private String jsonstring;
    private String executiondata;
    private BigInteger projectId;
    private Integer partNum;

    public TcContextBriefInfo() {
    }

    /**
     * Constructor from a TcContext object.
     */
    public TcContextBriefInfo(TcContext ctx) {
        this.id = ctx.getID();
        this.name = ctx.getName();
        this.status = ctx.getStatus();
        this.startTime = ctx.getStartTime();
        this.endTime = ctx.getEndTime();

        if (ctx.getEnvironmentId() != null) {
            this.environment = ctx.getEnvironmentId();
            this.envname = ctx.getEnvironmentName();
        } else {
            this.environment = null;
            this.envname = "";
        }

        if (ctx.getInitiator() != null) {
            this.initiator = ctx.getInitiator().getID();
            this.ininame = ctx.getInitiator().getName();
            if (ctx.getInitiator() instanceof SituationInstance) {
                this.initiatortype = "SituationInstance";
                this.situationId = ((SituationInstance) ctx.getInitiator()).getSituationId();
            } else {
                this.initiatortype = "CallChainInstance";
                this.chainId = ((CallChainInstance) ctx.getInitiator()).getTestCaseId();
            }
        } else {
            this.initiator = null;
            this.ininame = "";
        }

        this.projectId = ctx.getProjectId();
    }

    /**
     * Constructor from an array of separate TcContext fields.
     * see InstanceContextRepository#getTCContextInformation() method for exact query text.
     */
    public TcContextBriefInfo(Object[] object) {
        if (object.length != 14) {
            throw new IllegalArgumentException("Object isn't correct. Object has " + object.length
                    + " elements (required: 14).");
        }
        this.setID(object[0]);
        if (object[1] != null) {
            this.setName(object[1].toString());
        }
        this.setInitiator(object[2]);
        this.setEnvironment(object[3]);
        if (object[4] != null) {
            this.setStatus(Status.valueOf(object[4].toString()));
        }
        if (object[5] != null) {
            this.setStartTime((Date) object[5]);
        }
        if (object[6] != null) {
            this.setEndTime((Date) object[6]);
        }
        if (object[7] != null) {
            this.setJsonstring(object[7].toString());
        }
        if (object[8] != null) {
            this.setProjectId((BigInteger) object[8]);
        }
        if (object[9] != null) {
            this.setIniname(object[9].toString());
        }
        if (object[10] != null) {
            this.setInitiatortype(object[10].toString());
        }
        this.setSituationId((BigInteger) object[11]);
        this.setChainId((BigInteger) object[12]);
        if (object[13] != null) {
            this.setExecutiondata(object[13].toString());
        }
    }

    @ProduceNewObject
    @Override
    public Object getNaturalId() {
        return super.getNaturalId();
    }
}
