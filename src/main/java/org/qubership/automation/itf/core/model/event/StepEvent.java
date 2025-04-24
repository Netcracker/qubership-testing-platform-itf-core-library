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

import org.qubership.automation.itf.core.model.jpa.instance.step.StepInstance;

public abstract class StepEvent extends Event {

    private StepInstance stepInstance;

    private StepEvent(StepInstance stepInstance) {
        this.stepInstance = stepInstance;
    }

    public StepInstance getStepInstance() {
        return stepInstance;
    }

    public static class Start extends StepEvent {

        public Start(StepInstance stepInstance) {
            super(stepInstance);
        }
    }

    public static class Finish extends StepEvent {

        public Finish(StepInstance stepInstance) {
            super(stepInstance);
        }
    }

    public static class Terminate extends StepEvent {

        public Terminate(StepInstance stepInstance) {
            super(stepInstance);
        }
    }

    public static class Skip extends StepEvent {

        public Skip(StepInstance stepInstance) {
            super(stepInstance);
        }
    }

}
