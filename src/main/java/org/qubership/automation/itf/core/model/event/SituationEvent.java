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

import org.qubership.automation.itf.core.model.common.Storable;
import org.qubership.automation.itf.core.model.jpa.instance.SituationInstance;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = SituationEvent.Finish.class, name = "situationEventFinish"),
        @JsonSubTypes.Type(value = SituationEvent.Start.class, name = "situationEventStart"),
        @JsonSubTypes.Type(value = SituationEvent.Terminate.class, name = "situationEventTerminate"),
        @JsonSubTypes.Type(value = SituationEvent.TerminateWithoutAtpReport.class,
                name = "situationEventTerminateWithoutAtpReport"),
        @JsonSubTypes.Type(value = SituationEvent.EndExceptionalSituationFinish.class,
                name = "endExceptionalSituationFinish")
})
public abstract class SituationEvent extends Event {

    private SituationInstance situationInstance;
    private Storable source;

    public SituationEvent(SituationInstance situationInstance) {
        this.situationInstance = situationInstance;
    }

    @NoArgsConstructor
    @SuppressWarnings("unused")
    public static class Finish extends SituationEvent {

        public Finish(SituationInstance situation) {
            super(situation);
        }
    }

    @SuppressWarnings("unused")
    public static class Start extends SituationEvent {

        public Start(SituationInstance situation) {
            super(situation);
        }
    }

    @SuppressWarnings("unused")
    public static class Terminate extends SituationEvent {

        public Terminate(SituationInstance situation) {
            super(situation);
        }
    }

    @SuppressWarnings("unused")
    public static class TerminateWithoutAtpReport extends SituationEvent {

        public TerminateWithoutAtpReport(SituationInstance situation) {
            super(situation);
        }
    }

    @NoArgsConstructor
    @SuppressWarnings("unused")
    public static class EndExceptionalSituationFinish extends SituationEvent {

        public EndExceptionalSituationFinish(SituationInstance situationInstance) {
            super(situationInstance);
        }
    }
}
