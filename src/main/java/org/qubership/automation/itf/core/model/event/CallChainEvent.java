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

import java.util.Map;

import org.qubership.automation.itf.core.model.jpa.instance.chain.CallChainInstance;

import com.google.common.collect.Maps;

public abstract class CallChainEvent extends Event {

    private CallChainInstance instance;

    public CallChainEvent(CallChainInstance callChainInstance) {
        this.instance = callChainInstance;
    }


    public static class Start extends CallChainEvent {

        private Map<String, String> reportLinks;

        public Start(CallChainInstance callChain) {
            super(callChain);
            reportLinks = Maps.newHashMap(callChain.getContext().tc().getReportLinks());
        }

        public Map<String, String> getReportLinks() {
            return reportLinks;
        }
    }

    public static class Finish extends CallChainEvent {

        private boolean isFinish = false;

        public Finish(CallChainInstance callChain) {
            super(callChain);
            isFinish = true;
        }

        public boolean isFinish() {
            return isFinish;
        }
    }

    public static class Terminate extends CallChainEvent {

        public Terminate(CallChainInstance callChain) {
            super(callChain);
        }
    }

    public CallChainInstance getInstance() {
        return instance;
    }

    public void setInstance(CallChainInstance instance) {
        this.instance = instance;
    }
}

