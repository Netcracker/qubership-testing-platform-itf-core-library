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

import org.qubership.automation.itf.core.model.jpa.instance.chain.CallChainInstance;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

public class NextCallChainEvent extends AbstractEvent<CallChainInstance> {

    public NextCallChainEvent(String id) {
        super(id);
    }

    public NextCallChainEvent(String parentId, CallChainInstance instance) {
        super(parentId, instance);
    }

    public NextCallChainEvent(AbstractEvent event) {
        super(event);
    }

    public static class Pause extends NextCallChainEvent {

        public Pause(String parentId, CallChainInstance instance) {
            super(parentId, instance);
        }
    }

    public static class Resume extends NextCallChainEvent {

        public Resume(String parentId, CallChainInstance instance) {
            super(parentId, instance);
        }
    }

    public static class ResumeWithoutContinue extends NextCallChainEvent {

        public ResumeWithoutContinue(String parentId, CallChainInstance instance) {
            super(parentId, instance);
        }
    }

    public static class ResumeStepWithContinueTc extends NextCallChainEvent {

        public ResumeStepWithContinueTc(String parentId, CallChainInstance instance) {
            super(parentId, instance);
        }
    }

    public static class UpdateContext extends NextCallChainEvent {

        public UpdateContext(String parentId, CallChainInstance instance) {
            super(parentId, instance);
        }
    }

    public static class Exception extends NextCallChainEvent {

        private final String exceptionMessage;

        @SuppressFBWarnings("NM_CLASS_NOT_EXCEPTION")
        public Exception(String parentId, CallChainInstance instance, String exceptionMessage) {
            super(parentId, instance);
            this.exceptionMessage = exceptionMessage;
        }

        public String getExceptionMessage() {
            return exceptionMessage;
        }
    }

    public static class Fail extends NextCallChainEvent {

        private java.lang.Exception exception;

        public Fail(AbstractEvent event, java.lang.Exception exception) {
            super(event);
            this.exception = exception;
        }

        public Fail(String id, java.lang.Exception exception) {
            super(id);
            this.exception = exception;
        }

        public java.lang.Exception getException() {
            return exception;
        }
    }

    public static class FailByTimeout extends NextCallChainEvent {

        private java.lang.Exception exception;

        public FailByTimeout(String id, java.lang.Exception exception) {
            super(id);
            this.exception = exception;
        }

        public java.lang.Exception getException() {
            return exception;
        }
    }
}
