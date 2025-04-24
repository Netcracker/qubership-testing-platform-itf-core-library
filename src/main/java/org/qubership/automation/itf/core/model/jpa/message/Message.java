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

package org.qubership.automation.itf.core.model.jpa.message;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.persistence.Entity;

import org.qubership.automation.itf.core.model.content.Content;
import org.qubership.automation.itf.core.model.jpa.storage.AbstractStorable;
import org.qubership.automation.itf.core.model.transport.ConnectionProperties;
import org.qubership.automation.itf.core.util.helper.StorableUtils;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.Maps;

@Entity
@JsonFilter("reportWorkerFilter_Message")
public class Message extends AbstractStorable implements Serializable {

    private static final long serialVersionUID = -3078836082686301236L;
    private String text;
    private transient Content<?> content;
    private File file;
    private Map<String, Object> connectionProperties = new ConnectionProperties();
    private Map<String, Object> headers = Maps.newHashMapWithExpectedSize(10);
    private Map<String, String> transportProperties = Maps.newHashMapWithExpectedSize(10);
    private String failedMessage;

    public Message() {
    }

    public Message(String text) {
        this.text = text;
    }

    public Message(File file) {
        this.file = file;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @JsonIgnore
    public Content<?> getContent() {
        return content;
    }

    public void setContent(Content<?> content) {
        this.content = content;
    }

    public void addConnectionProperty(String key, Object object) {
        connectionProperties.put(key, object);
    }

    public Map<String, Object> getConnectionProperties() {
        return connectionProperties;
    }

    public <T> T getConnectionPropertiesParameter(String paramName) {
        return (T) connectionProperties.get(paramName);
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setConnectionProperties(Map<String, Object> connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void fillConnectionProperties(Map<String, Object> connectionProperties) {
        for (Map.Entry<String, Object> entry : connectionProperties.entrySet()) {
            if (getConnectionProperties().containsKey(entry.getKey())
                    && getConnectionProperties().get(entry.getKey()).equals("")) {
                getConnectionProperties().put(entry.getKey(), entry.getValue());
            } else {
                getConnectionProperties().computeIfAbsent(entry.getKey(), s -> entry.getValue());
            }
        }
    }

    public Map<String, Object> getHeaders() {
        return headers;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setHeaders(Map<String, Object> headers) {
        this.headers = headers;
    }

    /**
     * Clear target headers collection, then put all headers from source collection into target.
     */
    public void fillHeaders(Map<String, String> headers) {
        StorableUtils.fillMap(getHeaders(), headers);
    }

    /**
     * Replace header values for key parameter.
     */
    public void fillHeaders(Map<String, Object> sourceMap, String key) {
        if (sourceMap.containsKey(key)) {
            fillHeaders((Map<String, String>) sourceMap.get(key));
        }
    }

    /**
     * Clear target headers collection, then convert headers' values from non-String format to String and put.
     */
    public void convertAndSetHeaders(Map<String, Object> sourceHeaders) {
        headers.clear();
        sourceHeaders.forEach((key, value) -> convertAndAddHeader(key, value));
    }

    /**
     * Clear target headers collection, then convert headers' values from non-String format to String and put.
     * Skip headers if their names are in the excludeHeaders list.
     */
    public void convertAndSetHeaders(Map<String, Object> sourceHeaders, List<String> excludeHeaders) {
        if (excludeHeaders == null || excludeHeaders.isEmpty()) {
            convertAndSetHeaders(sourceHeaders);
        } else {
            headers.clear();
            sourceHeaders.forEach((key, value) -> {
                if (!excludeHeaders.contains(key)) {
                    convertAndAddHeader(key, value);
                }
            });
        }
    }

    private void convertAndAddHeader(String key, Object value) {
        if (value instanceof byte[]) {
            headers.put(key, new String((byte[]) value, StandardCharsets.UTF_8)
                    .replace((char) 0, (char) 32));
        } else if (value instanceof ByteArrayInputStream) {
            ByteArrayInputStream bais = (ByteArrayInputStream) value;
            int n = bais.available();
            if (n > 0) {
                byte[] bytes = new byte[n];
                int cnt = bais.read(bytes, 0, n);
                headers.put(key, new String(bytes, 0, cnt, StandardCharsets.UTF_8));
            }
        } else if (value instanceof List) {
            headers.put(key, value);
        } else {
            headers.put(key, Objects.toString(value, "")
                    .replace((char) 0, (char) 32)/*TODO it's WA for postgres DB*/);
        }
    }

    /**
     * The method is used from HTTP2OutboundTransport only, due to implementation specifics.
     * Please note: currently only the 1st element of the list is used to set value,
     * and it's okay for HTTP2OutboundTransport.
     *  TODO: Rename the method or change it to combine all list elements in the resulting value
     */
    public void convertAndSetMultiHeaders(Map<String, List<String>> headers) {
        this.headers.clear();
        for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
            this.headers.put(entry.getKey(), Objects.toString(entry.getValue().get(0), "")
                    .replace((char) 0, (char) 32)/*TODO it's WA for postgres DB*/);
        }
    }

    /**
     * TODO: Add JavaDoc.
     */
    public Map<String, String> getTransportProperties() {
        return transportProperties;
    }

    /**
     * hibernate set its proxy thru this method.
     * The proxy should not be overridden in runtime.
     */
    protected void setTransportProperties(Map<String, String> transportProperties) {
        this.transportProperties = transportProperties;
    }

    public void fillTransportProperties(Map<String, String> transportProperties) {
        StorableUtils.fillMap(getTransportProperties(), transportProperties);
    }

    /**
     * TODO: Add JavaDoc.
     */
    public void connection2TransportProperties(Map<String, Object> connectionProperties) {
        this.transportProperties.clear();
        for (Map.Entry<String, Object> entry : connectionProperties.entrySet()) {
            this.transportProperties.put(entry.getKey(), Objects.toString(entry.getValue(), ""));
        }
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public String getFailedMessage() {
        return failedMessage;
    }

    public void setFailedMessage(String failedMessage) {
        this.failedMessage = failedMessage;
    }
}
