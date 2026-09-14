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

package org.qubership.automation.itf.core.model.communication.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.descriptor.StorableDescriptor;

/**
 * Fails if {@link TriggerExecutionMessage}, {@link CommonTriggerExecutionMessage}, or {@link
 * DiameterTriggerExecutionMessage} stop round-tripping through Java serialization - the wire format
 * itf-executor and itf-stubs exchange as independently deployed services (TEST-04).
 */
class TriggerExecutionMessageSerializationTest {

    @Test
    void triggerExecutionMessageRoundTrips() throws Exception {
        TriggerExecutionMessage original = new TriggerExecutionMessage(
                new Message("payload"), "session-1", "selector-1");

        TriggerExecutionMessage restored = roundTrip(original);

        assertNotSame(original, restored);
        assertEquals(original.getSessionId(), restored.getSessionId());
        assertEquals(original.getBrokerMessageSelectorValue(), restored.getBrokerMessageSelectorValue());
        assertEquals(original.getMessage().getText(), restored.getMessage().getText());
    }

    @Test
    void commonTriggerExecutionMessageRoundTrips() throws Exception {
        StorableDescriptor descriptor = new StorableDescriptor(BigInteger.ONE, "trigger",
                UUID.randomUUID(), BigInteger.TEN);
        CommonTriggerExecutionMessage original = new CommonTriggerExecutionMessage(
                "SomeTriggerType", new Message("payload"), descriptor, "session-1", "selector-1");

        CommonTriggerExecutionMessage restored = roundTrip(original);

        assertNotSame(original, restored);
        assertEquals(original.getTypeName(), restored.getTypeName());
        assertEquals(original.getSessionId(), restored.getSessionId());
        assertEquals(original.getMessage().getText(), restored.getMessage().getText());
        assertEquals(original.getTriggerConfigurationDescriptor().getName(),
                restored.getTriggerConfigurationDescriptor().getName());
        assertEquals(original.getTriggerConfigurationDescriptor().getProjectId(),
                restored.getTriggerConfigurationDescriptor().getProjectId());
    }

    @Test
    void diameterTriggerExecutionMessageRoundTrips() throws Exception {
        DiameterTriggerExecutionMessage original = new DiameterTriggerExecutionMessage(
                new Message("payload"), BigInteger.valueOf(42), BigInteger.valueOf(7), "session-1");

        DiameterTriggerExecutionMessage restored = roundTrip(original);

        assertNotSame(original, restored);
        assertEquals(original.getTransportId(), restored.getTransportId());
        assertEquals(original.getTcContextId(), restored.getTcContextId());
        assertEquals(original.getSessionId(), restored.getSessionId());
        assertEquals(original.getMessage().getText(), restored.getMessage().getText());
    }

    @SuppressWarnings("unchecked")
    private static <T extends Serializable> T roundTrip(T original) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(original);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (T) in.readObject();
        }
    }
}
