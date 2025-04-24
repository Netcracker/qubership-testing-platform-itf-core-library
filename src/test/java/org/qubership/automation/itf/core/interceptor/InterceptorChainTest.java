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

package org.qubership.automation.itf.core.interceptor;

import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.interceptor.InterceptorChain;
import org.qubership.automation.itf.core.model.interceptor.TransportInterceptor;
import org.qubership.automation.itf.core.model.jpa.interceptor.InterceptorParams;
import org.qubership.automation.itf.core.model.jpa.interceptor.TemplateInterceptor;
import org.qubership.automation.itf.core.model.jpa.interceptor.TransportConfigurationInterceptor;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InterceptorChainTest {
    @Test
    public void testInterceptorChainCallsInterceptors() throws Exception {
        InterceptorChain chain = new InterceptorChain();
        Interceptor encrypt = mock(TemplateInterceptor.class);
        Interceptor decrypt = mock(TransportConfigurationInterceptor.class);
        prepareEncrypt(encrypt);
        prepareDecrypt(decrypt);
        chain.add(encrypt);
        chain.add(decrypt);
        String text = "Some test message";
        Message message = new Message(text);
        message = chain.apply(message);
        assertEquals(text, message.getText());

        TransportInterceptor encryptTransportInterceptor = (TransportInterceptor) Class.forName(encrypt.getTypeName()).getConstructor(InterceptorParams.class).newInstance(encrypt);
        verify(encryptTransportInterceptor).apply(message);

        TransportInterceptor decryptRransportInterceptor = (TransportInterceptor) Class.forName(decrypt.getTypeName()).getConstructor(InterceptorParams.class).newInstance(decrypt);
        verify(decryptRransportInterceptor).apply(message);
    }

    private void prepareDecrypt(Interceptor decrypt) throws Exception {
        TransportInterceptor transportInterceptor = (TransportInterceptor) Class.forName(decrypt.getTypeName()).getConstructor(InterceptorParams.class).newInstance(decrypt);
        when(transportInterceptor.apply(any(Message.class))).then(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            byte[] bytes = (byte[]) message.getContent().get();
            message.setText(new String(bytes));
            return message;
        });
        doReturn(true).when(decrypt).isActive();
    }

    private void prepareEncrypt(Interceptor encrypt) throws Exception {
        TransportInterceptor transportInterceptor = (TransportInterceptor) Class.forName(encrypt.getTypeName()).getConstructor(InterceptorParams.class).newInstance(encrypt);
        when(transportInterceptor.apply(any(Message.class))).then(invocation -> {
            Message message = (Message) invocation.getArguments()[0];
            byte[] bytes = message.getText().getBytes();
            message.setContent(() -> bytes);
            return message;
        });
        doReturn(true).when(encrypt).isActive();
    }

}
