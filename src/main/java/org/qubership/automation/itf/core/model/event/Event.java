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

package org.qubership.automation.itf.core.model.event;

import java.util.Date;
import java.util.UUID;

import org.qubership.automation.itf.core.util.config.Config;

public abstract class Event {

    private static final String RUNNING_HOSTNAME = Config.getConfig().getRunningHostname();

    private Date date;
    private boolean stop = false;
    private String id;
    private String parentId;
    private String runningHostname;

    /**
     * Default constructor (runningHostname is set to separate natives from aliens in multi replica).
     */
    public Event() {
        this.date = new Date();
        this.id = UUID.randomUUID().toString();
        this.runningHostname = RUNNING_HOSTNAME;
    }

    /**
     * Constructor from parent Event (runningHostname is set to separate natives from aliens in multi replica).
     */
    public Event(Event event) {
        this.date = new Date();
        this.id = event.getID();
        this.parentId = event.getParentId();
        this.runningHostname = RUNNING_HOSTNAME;
    }

    public Date getDate() {
        return date;
    }

    public boolean isStopped() {
        return stop;
    }

    public void stop() {
        this.stop = true;
    }

    public String getID() {
        return id;
    }

    public void setID(String id) {
        this.id = id;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getRunningHostname() {
        return runningHostname;
    }
}
