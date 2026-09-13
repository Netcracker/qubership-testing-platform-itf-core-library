/*
 *  Copyright 2024-2026 NetCracker Technology Corporation
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

package org.qubership.automation.itf.core.model.jpa.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;

import org.junit.jupiter.api.Test;

/**
 * Fails if {@link TcContext#getDuration()} reports epoch-sized nonsense for a context that hasn't
 * finished yet, instead of the elapsed time {@link TcContext#getDurationMinutes()} already computes
 * correctly for the same state (CORR-06).
 */
class TcContextDurationTest {

    @Test
    void noStartTimeReturnsZeroDuration() {
        TcContext context = new TcContext();
        context.setStartTime(null);

        assertEquals(0L, context.getDuration().getTime());
    }

    @Test
    void runningContextReturnsElapsedTimeNotCurrentEpochMillis() {
        TcContext context = new TcContext();
        long elapsedMillis = 60_000L;
        context.setStartTime(new Date(System.currentTimeMillis() - elapsedMillis));

        long duration = context.getDuration().getTime();

        assertTrue(duration >= elapsedMillis && duration < elapsedMillis + 5_000L,
                "duration should be roughly the elapsed time since startTime, not System.currentTimeMillis(): "
                        + duration);
    }

    @Test
    void finishedContextReturnsEndMinusStart() {
        TcContext context = new TcContext();
        long startMillis = 1_000_000L;
        context.setStartTime(new Date(startMillis));
        context.setEndTime(new Date(startMillis + 60_000L));

        assertEquals(60_000L, context.getDuration().getTime());
    }
}
