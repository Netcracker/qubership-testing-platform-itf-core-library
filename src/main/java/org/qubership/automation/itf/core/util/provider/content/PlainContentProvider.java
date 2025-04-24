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

package org.qubership.automation.itf.core.util.provider.content;

import org.qubership.automation.itf.core.model.content.Content;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.exception.ContentException;

public class PlainContentProvider implements MessageContentProvider<String> {
    public Content<String> provide(Message message) throws ContentException {
        return new StringContent(message.getText());
    }

    public boolean supports(Message message) {
        return true;
    }

    private static class StringContent implements Content<String> {

        private String string;

        private StringContent(String string) {
            this.string = string;
        }

        public String get() {
            return string;
        }
    }
}
