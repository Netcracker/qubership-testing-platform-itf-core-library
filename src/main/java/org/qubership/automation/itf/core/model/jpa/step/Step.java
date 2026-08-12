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
import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import jakarta.persistence.Entity;

import org.qubership.automation.itf.core.model.common.Identified;
import org.qubership.automation.itf.core.model.common.Named;
import org.qubership.automation.itf.core.model.common.OptimisticLockable;
import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.util.constants.Mep;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@Entity
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SituationStep.class, name = "SituationStep"),
        @JsonSubTypes.Type(value = EmbeddedStep.class, name = "EmbeddedStep")
})
public interface Step extends Storable, Named, Identified<BigInteger>, OptimisticLockable<Object> {
    Mep getMep();

    String getUnit();

    void setUnit(String unit);

    boolean isEnabled();

    boolean isManual();

    void setEnabled(boolean enabled);

    void setManual(boolean manual);

    long getDelay();

    void setDelay(long delay);

    String getType();

    int getOrder();

    void setOrder(int order);

    @Transient
    @JsonIgnore
    TimeUnit retrieveUnit();
}
