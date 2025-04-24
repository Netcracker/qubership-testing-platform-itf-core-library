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

package org.qubership.automation.itf.core.util.iterator;

import java.util.List;

import org.qubership.automation.itf.core.model.jpa.callchain.CallChain;
import org.qubership.automation.itf.core.model.jpa.instance.AbstractContainerInstance;
import org.qubership.automation.itf.core.model.jpa.step.EmbeddedStep;
import org.qubership.automation.itf.core.model.jpa.step.SituationStep;
import org.qubership.automation.itf.core.model.jpa.step.Step;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

public class CallChainStepIterator extends AbstractStepIterator {

    public static final Logger LOGGER = LoggerFactory.getLogger(CallChainStepIterator.class);

    /**
     * TODO: Add JavaDoc.
     */
    public CallChainStepIterator(CallChain callChain, AbstractContainerInstance parent) {
        List<Step> steps = Lists.newArrayList();
        getAllSteps(callChain, steps);
        setIterator(steps.iterator());
        setParent(parent);
    }

    public CallChainStepIterator(CallChain callChain, AbstractContainerInstance parent, List<Step> steps) {
        setIterator(steps.iterator());
        setParent(parent);
    }

    private void getAllSteps(CallChain callChain, List<Step> stepList) {
        for (Step step : callChain.getSteps()) {
            if (step == null) {
                /* null steps can be in the callChain.getSteps() due to Hibernate,
                    who wants to maintain 'order' column without gaps.
                    So, it's very easy to produce gaps:
                      1. Insert 4 steps to callchain,
                      2. Save callchain
                      3. View steps in the database - their 'order' column contains 0, 1, 2, 3
                      4. Delete step #1 - AND do NOT save the callchain - 'order' column will contain 0, 2, 3
                      5. If we save the callchain now - Hibernate renumerates 'order' column. It will contain 0, 1, 2
                      6. If we DON'T save callchain after item#4
                        - all subsequent gets will produce the List of 4 steps but step #1 is null !!!

                    There is no simple way to avoid nulls if we are staying at our current mappings and UI.
                 */
                continue;
            }
            if (!step.isEnabled()) {
                LOGGER.debug("Call chain step '{}' is disabled ==> step is skipped.", step);
                continue;
            } else if (step instanceof SituationStep) {
                if (((SituationStep) step).getSituation() == null) {
                    LOGGER.debug("Call chain step '{}': situation is null ==> step is skipped.", step);
                    continue;
                }
            } else if (step instanceof EmbeddedStep && ((EmbeddedStep) step).getChain() == null) {
                LOGGER.debug("Call chain step '{}': callchain is null ==> step is skipped.", step);
                continue;
            }
            stepList.add(step);
        }
    }
}
