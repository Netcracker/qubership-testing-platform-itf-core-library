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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.hamcrest.core.StringStartsWith;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import org.qubership.automation.itf.core.model.interceptor.Interceptor;
import org.qubership.automation.itf.core.model.interceptor.TransportInterceptor;
import org.qubership.automation.itf.core.model.jpa.interceptor.InterceptorParams;
import org.qubership.automation.itf.core.model.jpa.interceptor.TemplateInterceptor;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.loader.InterceptorClassLoader;

public class InterceptorClassloaderTest {

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        InterceptorClassLoader.getInstance().cleanClassLoaders();
    }

    @Test
    public void testInterceptorThrowsExceptionThatClassLoaderNotFound() throws Exception {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage(StringStartsWith.startsWith("Classloader not found for class"));
        InterceptorClassLoader.getInstance().getClass("org.qubership.mockingbird.interceptor.TestInterceptor");
    }

    @Test
    public void testInterceptorClassLoaderIsLoadClassesAndExecute() throws Exception {
        //InterceptorClassLoader.getInstance().loadInterceptor("./src/test/resources/interceptors/test-interceptor/");
        InterceptorClassLoader.getInstance().load("C:\\Users\\Kuleshov\\Desktop\\mockingbird-light-interceptors"
                + "\\interceptors\\encrypt-interceptor\\", null);
        Class<? extends TransportInterceptor> interceptorClass = InterceptorClassLoader.getInstance()
                .getClass("org.qubership.mockingbird.interceptor.EncryptInterceptor");
        //TODO SZ: constructor shouldn't accept the interceptor!!!!!!!!
        Interceptor mock = mock(TemplateInterceptor.class);
        when(mock.getParameters()).thenReturn(new InterceptorParams());
        when(mock.getParameters().get("Public Keyring File"))
                .thenReturn("\\\\WSMTL-213\\Users\\Kuleshov\\Desktop\\keyrings\\new_key_1\\public.gpg");
        when(mock.getParameters().get("Certificate Fingerprint"))
                .thenReturn("F45ADB69660D98C6B1373507BFC6A645E4733C21");
        when(mock.getParameters().get("ASCII Armor")).thenReturn("Yes");
        when(mock.getParameters().get("Encryption Algorithm")).thenReturn("AES 128");
        when(mock.getParameters().get("Integrity Check")).thenReturn("Yes");
        TransportInterceptor transportInterceptor = interceptorClass.getConstructor(Interceptor.class)
                .newInstance(mock);
        Message result = transportInterceptor.apply(new Message("someText"));
        assertEquals("someText", result.getText());
    }
}
