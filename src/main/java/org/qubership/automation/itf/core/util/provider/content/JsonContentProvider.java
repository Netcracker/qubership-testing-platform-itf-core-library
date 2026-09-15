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

package org.qubership.automation.itf.core.util.provider.content;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.qubership.automation.itf.core.model.content.Content;
import org.qubership.automation.itf.core.model.jpa.message.Message;
import org.qubership.automation.itf.core.util.exception.ContentException;

public class JsonContentProvider implements MessageContentProvider<JSONObject> {

    /**
     * Parses the message body as JSON. An array is wrapped in an object under the key
     * {@code "array"}, so the result is always a {@link JSONObject}. Call only on a message
     * {@link #supports(Message)} accepts; a top-level JSON value that is not an object or an array
     * makes this throw {@link ClassCastException} instead of {@link ContentException}.
     *
     * @throws ContentException if the message text is not well-formed JSON
     */
    public Content<JSONObject> provide(Message message) throws ContentException {
        try {
            JSONParser parser = new JSONParser();
            return new JsonContent((JSONAware) parser.parse(message.getText()));
        } catch (ParseException e) {
            throw new ContentException("Cannot parse JSON in message", e);
        }
    }

    /**
     * Checks whether {@code message}'s trimmed text looks like a JSON object or array: it is
     * non-blank, and starts with {@code "{"} and ends with {@code "}"}, or starts with
     * {@code "["}, or ends with {@code "]"}.
     *
     * @param message the message to check; {@code null} is treated as unsupported
     * @return whether {@link #provide(Message)} should be called for {@code message}
     */
    public boolean supports(Message message) {
        if (message == null || StringUtils.isBlank(message.getText())) {
            return false;
        }
        String text = message.getText().trim();
        return text.startsWith("{") && text.endsWith("}") || text.startsWith("[") || text.endsWith("]");
    }

    private static class JsonContent implements Content<JSONObject> {

        private final JSONObject object;

        private JsonContent(JSONAware object) throws ContentException {
            if (object instanceof JSONArray) {
                this.object = new JSONObject();
                this.object.put("array", object);
            } else if (object instanceof JSONObject nObject) {
                this.object = nObject;
            } else {
                throw new ContentException("Cannot take JSONObject from parsed object");
            }
        }

        public JSONObject get() {
            return object;
        }
    }
}
