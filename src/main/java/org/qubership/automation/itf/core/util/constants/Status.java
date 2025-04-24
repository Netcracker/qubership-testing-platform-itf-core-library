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

package org.qubership.automation.itf.core.util.constants;

/** enum Status class.
 * @author Roman Aksenenko
 * @since 18.04.2016
 */
public enum Status {

    NOT_STARTED {
        @Override
        public String toString() {
            return "Not Started";
        }
    }, IN_PROGRESS {
        @Override
        public String toString() {
            return "In Progress";
        }
    }, PASSED {
        @Override
        public String toString() {
            return "Passed";
        }
    }, WARNING {
        @Override
        public String toString() {
            return "Warning";
        }
    }, FAILED {
        @Override
        public String toString() {
            return "Failed";
        }
    }, PAUSED {
        @Override
        public String toString() {
            return "Paused";
        }
    }, STOPPED {
        @Override
        public String toString() {
            return "Stopped";
        }
    }, FAILED_BY_TIMEOUT {
        @Override
        public String toString() {
            return "Failed by timeout";
        }
    };

    @Override
    public abstract String toString();

}
