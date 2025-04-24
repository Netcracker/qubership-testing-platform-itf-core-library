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

import org.qubership.automation.itf.core.model.jpa.context.TcContext;

public abstract class TcContextEvent extends Event {

    private TcContext context;

    public TcContextEvent(TcContext context) {
        this.context = context;
    }

    public TcContext getContext() {
        return context;
    }

    public static class Start extends TcContextEvent {
        public Start(TcContext context) {
            super(context);
        }
    }

    public static class UpdateInfo extends TcContextEvent {
        public UpdateInfo(TcContext context) {
            super(context);
        }
    }

    public static class Finish extends TcContextEvent {
        private boolean isFinish;

        public Finish(TcContext context) {
            super(context);
            isFinish = context.isFinished();
        }

        public boolean isFinish() {
            return isFinish;
        }
    }

    public static class Stop extends TcContextEvent {
        public Stop(TcContext context) {
            super(context);
        }
    }

    public static class Fail extends TcContextEvent {

        public Fail(TcContext context) {
            super(context);
        }
    }

    public static class Pause extends TcContextEvent {
        public Pause(TcContext context) {
            super(context);
        }
    }

    public static class Resume extends TcContextEvent {
        public Resume(TcContext context) {
            super(context);
        }
    }

    public static class Continue extends TcContextEvent {
        public Continue(TcContext context) {
            super(context);
        }
    }

}
