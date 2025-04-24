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

package org.qubership.automation.itf.core.model.jpa.step;

import java.beans.Transient;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AbstractStep extends AbstractStorable implements Step {
    private boolean enabled = true;
    private boolean manual = false;
    private long delay;
    private String unit = TimeUnit.SECONDS.toString();
    private int order;


    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isManual() {
        return manual;
    }

    public void setManual(boolean manual) {
        this.manual = manual;
    }

    @Override
    public long getDelay() {
        return delay;
    }

    @Override
    public void setDelay(long delay) {
        this.delay = delay;
    }

    @Override
    public String getUnit() {
        return unit;
    }

    @Override
    public void setUnit(String unit) {
        this.unit = checkUnit(unit);
    }

    @Override
    public int getOrder() {
        return order;
    }

    @Override
    public void setOrder(int order) {
        this.order = order;
    }

    @Override
    @Transient
    @JsonIgnore
    public TimeUnit retrieveUnit() {
        return convertToTimeUnit(unit);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static String checkUnit(String unitName) {
        if (StringUtils.isBlank(unitName)) {
            return null;
        } else {
            try {
                TimeUnit test = TimeUnit.valueOf(unitName);
                return test.toString();
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public static TimeUnit convertToTimeUnit(String unitName) {
        if (StringUtils.isBlank(unitName)) {
            return null;
        } else {
            try {
                return TimeUnit.valueOf(unitName);
            } catch (IllegalArgumentException ex) {
                return null;
            }
        }
    }
}
