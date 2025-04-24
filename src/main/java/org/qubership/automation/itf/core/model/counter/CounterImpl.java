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

package org.qubership.automation.itf.core.model.counter;

import java.util.Date;
import java.util.Set;

import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.util.exception.CounterLimitIsExhaustedException;

import com.google.common.collect.Sets;

public class CounterImpl extends AbstractStorable implements Counter {
    private static final long serialVersionUID = 20240812L;

    private Set<Object> owners = Sets.newHashSet();

    private Date date;

    private Integer index;

    private String format;

    @Override
    public Integer getNextIndex() throws CounterLimitIsExhaustedException {
        if (index == 99 && format.length() == 2 || index == 999 && format.length() == 3) {
            throw new CounterLimitIsExhaustedException("Counter limit is reached: current index = " + index + ", "
                    + "format counter = " + format + ", current date = " + date);
        }
        return ++index;
    }

    @Override
    public Set<Object> getOwners() {
        return owners;
    }

    @Override
    public void setOwners(Set<Object> owners) {
        this.owners = owners;
    }

    @Override
    public Integer getIndex() {
        return index;
    }

    @Override
    public void setIndex(Integer index) {
        this.index = index;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public String getFormat() {
        return format;
    }

    @Override
    public void setFormat(String format) {
        this.format = format;
    }
}
