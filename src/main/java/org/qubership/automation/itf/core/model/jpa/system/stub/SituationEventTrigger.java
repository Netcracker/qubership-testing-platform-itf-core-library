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

package org.qubership.automation.itf.core.model.jpa.system.stub;

import java.math.BigInteger;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.util.annotation.RefCopy;
import org.qubership.automation.itf.core.util.annotation.UserName;
import org.qubership.automation.itf.core.util.ei.deserialize.SituationDeserializer;
import org.qubership.automation.itf.core.util.ei.serialize.IdSerializer;
import org.qubership.automation.itf.core.util.provider.TriggerProvider;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Strings;

@Entity
@UserName("Situation start/finish event trigger")
@JsonIgnoreProperties(ignoreUnknown = true, value = {"hibernateLazyInitializer", "handler"})
@JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class,
        property = "id", scope = SituationEventTrigger.class)
public class SituationEventTrigger extends EventTriggerImpl {
    private static final long serialVersionUID = 20240812L;
    public static final String TYPE = "Situation Event Trigger";
    private Situation situation;
    private On on;

    /**
     * Constructor.
     */
    public SituationEventTrigger(Situation situationTrigger, On on) {
        super(TYPE);
        this.situation = situationTrigger;
        setOn(on);
    }

    /**
     * Constructor.
     */
    public SituationEventTrigger() {
        super(TYPE);
    }

    /**
     * Constructor.
     */
    public SituationEventTrigger(Situation parent) {
        super(TYPE);
        setParent(parent);
        ((TriggerProvider) parent).getSituationEventTriggers().add(this);
    }

    @RefCopy
    @Override
    @JsonSerialize(using = IdSerializer.class)
    public Situation getParent() {
        return (Situation) super.getParent();
    }

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "id", scope = Situation.class)
    public void setParent(Situation parent) {
        super.setParent(parent);
    }

    @RefCopy
    @JsonSerialize(using = IdSerializer.class)
    public Situation getSituation() {
        return situation;
    }

    @JsonDeserialize(using = SituationDeserializer.class)
    public void setSituation(Situation situation) {
        this.situation = situation;
    }

    public On getOn() {
        return on;
    }

    public void setOn(On on) {
        this.on = on;
    }

    public void setOn(String string) {
        setOn(On.fromString(Strings.nullToEmpty(string).toUpperCase()));
    }

    @JsonIgnore
    @Override
    public String getType() {
        return TYPE;
    }

    public enum On {
        FINISH("Finish"),
        START("Start");

        private final String name;

        On(String name) {
            this.name = name;
        }

        /**
         * Get this enum value by the string parameter.
         *
         * @return enum value. Default is FINISH enum value
         */
        public static On fromString(String string) {
            string = Strings.nullToEmpty(string);
            if (string.isEmpty()) {
                return FINISH;
            } else {
                return valueOf(string.toUpperCase());
            }
        }

        @Override
        public String toString() {
            return name;
        }
    }

    /**
     * Perform Post-Import Actions with the object and linked situation.
     *
     */
    public void performPostImportActions(BigInteger projectId, BigInteger sessionId) {
        super.performPostImportActions(projectId, sessionId);
        if (getSituation() != null) {
            getSituation().performPostImportActions(projectId, sessionId);
        }
    }
}
