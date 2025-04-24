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

package org.qubership.automation.itf.core.util.config;

import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.MutablePair;
import org.qubership.automation.itf.core.util.constants.InstanceSettingsConstants;

public class Config extends AbstractConfig {

    protected static final String RUNNING_URL = "runningUrl";
    protected static final String RUNNING_HOSTNAME = "runningHostname";
    protected static final String RUNNING_PORT = "runningPort";
    protected static final String CONTEXT_POPUP_URL = "contextPopupUrl";
    protected static final List<MutablePair<Integer, Integer>> validCodeRanges = new ArrayList<>();
    private static final String CONFIG_FILENAME = "application.properties";
    private static volatile Config config;

    private Config(boolean withLoading) {
        super(withLoading, CONFIG_FILENAME);
    }

    /**
     * Return Config instance, initialize it if needed.
     *
     * @return Config
     */
    public static Config getConfig() {
        if (config == null) {
            synchronized (Config.class) {
                if (config == null) {
                    config = new Config(true);
                    addDefaultHostProperties();
                    parseResponseCodes();
                }
            }
        }
        return config;
    }

    /**
     * Get config filename constant.
     *
     * @return String
     */
    public static String getConfigFilename() {
        return CONFIG_FILENAME;
    }

    /**
     * Parse Http Response Code Ranges string into list of [left;right] pairs.
     */
    public static List<MutablePair<Integer, Integer>> parseCodeRanges(String strCodes) {
        List<MutablePair<Integer, Integer>> rangesList = new ArrayList<>();
        String[] list = strCodes.split("[,;]");
        for (String item : list) {
            if (StringUtils.isBlank(item)) {
                continue;
            }
            MutablePair<Integer, Integer> boundaries = parseCodeRange(item.split("-"));
            if (boundaries != null) {
                rangesList.add(boundaries);
            }
        }
        return rangesList;
    }

    /**
     * Parse a string in the format like "200-299,333,401-401,405-,-111,107-102" into list of ranges.
     * Silently go away in case of empty value.
     * Only valid ranges are added to the list.
     */
    private static void parseResponseCodes() {
        String strCodes = config.getString(InstanceSettingsConstants.HTTP_RESPONSE_CODE_SUCCESS);
        if (StringUtils.isBlank(strCodes)) {
            return;
        }
        validCodeRanges.addAll(parseCodeRanges(strCodes));
    }

    /*
     * Parse range as [String, String] into [Integer, Integer] and check the consistence of boundaries.
     * Valid variants are:
     * "200" - range is the single value,
     * "200-299" - range is a pair of min-max values (the 2nd value is not less than the 1st),
     *
     * Invalid variants are all except valid ones, for example:
     * "299-200" - range is a pair of min-max values but the 2nd value is less than the 1st,
     * "-200", "200-", "-" - range delimiter is present but one or both limits are missed
     */
    private static MutablePair<Integer, Integer> parseCodeRange(String[] range) {
        MutablePair<Integer, Integer> boundaries = new MutablePair<>();
        if (range == null || range.length < 1 || range.length > 2) {
            return null; // Invalid or empty range; silently go away
        }
        for (int i = 0; i < range.length; i++) {
            try {
                int val = Integer.parseInt(range[i].trim());
                if (val > 0) {
                    if (i == 0) {
                        boundaries.setLeft(val);
                        if (range.length == 1) {
                            boundaries.setRight(val);
                        }
                    } else {
                        boundaries.setRight(val);
                    }
                }
            } catch (NumberFormatException goaway) {
                // Invalid range boundary; skip this range
                LOGGER.error("Response codes range: Invalid range boundary[{}]: {}", i, range[i]);
                return null;
            }
        }
        if (boundaries.getLeft() > boundaries.getRight()) {
            // Invalid range boundary; skip this range
            LOGGER.error("Response codes range: Left boundary is greater than right: {} and {}", boundaries.getLeft(),
                    boundaries.getRight());
            return null;
        }
        return boundaries;
    }

    private static void addDefaultHostProperties() {
        if (StringUtils.isBlank(config.getRunningHostname())) {
            config.addProperty(RUNNING_HOSTNAME, determineHostname());
        }
        if (StringUtils.isBlank(config.getRunningPort())) {
            config.addProperty(RUNNING_PORT, determinePort());
        }
        String url = config.getString(RUNNING_URL);
        if (StringUtils.isBlank(url)) {
            config.addProperty(RUNNING_URL, constructRunningUrl());
        } else {
            url = url.trim();
            if (!url.endsWith("/")) {
                config.addProperty(RUNNING_URL, url + "/");
            }
        }
        config.addProperty(CONTEXT_POPUP_URL, config.getString(RUNNING_URL) + "#/context/");
    }

    private static String constructRunningUrl() {
        return String.format("%s://%s:%s/", "http", config.getRunningHostname(), config.getRunningPort());
    }

    private static String determineHostname() {
        try {
            /* Getting short name of the ITF running machine.
                If we need full domain name - we must change the line below to  Inet4Address.getLocalHost()
                .getCanonicalHostName().
                    (it's not good in the OpenShift - NITP-4894)

                If both variants are not suitable for ITF running in the OpenShift container
                    - the last variant is to set url explicitly in the config.properties
                    - in that case I propose to use the existing property 'taPlatformRollout.url'. It must contain
                    valid url of this ITF
             */
            return Inet4Address.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            // It's better to throw an exception at this starting moment than later
            // while starting of callchain or reporting.
            // Alternative behaviour is to log the error and return null or empty value.
            throw new RuntimeException("Can't determine hostname of ITF running machine!", e);
        }
    }

    private static String determinePort() {
        return System.getProperties().containsKey("port") ? System.getProperties().getProperty("port") : "8080";
    }

    public List<MutablePair<Integer, Integer>> getValidCodeRanges() {
        return validCodeRanges;
    }

    public String getString(String key) {
        String result = getKeyValue(key, String.class, properties);
        return StringUtils.isNotEmpty(result) ? result.trim() : null;
    }

    public String getStringOrDefault(String key, String defaultValue) {
        String result = getKeyValue(key, String.class, properties);
        return StringUtils.isNotEmpty(result) ? result.trim() : defaultValue;
    }

    public Integer getInt(String key) {
        String keyValue = getKeyValue(key, String.class, properties);
        return StringUtils.isNotEmpty(keyValue) ? Integer.parseInt(keyValue) : null;
    }

    public Integer getIntOrDefault(String key, int defaultValue) {
        String keyValue = getKeyValue(key, String.class, properties);
        return StringUtils.isNotEmpty(keyValue) ? Integer.parseInt(keyValue) : defaultValue;
    }

    /**
     * Get config value by key.
     */
    private <T> T getKeyValue(String key, Class<T> type, Map container) {
        Object value = container.get(key);
        if (value == null) {
            LOGGER.debug("Key '{}' not found in properties cache", key);
            return null;
        }
        try {
            return type.cast(value);
        } catch (ClassCastException e) {
            //If user put string instead of int, for example
            LOGGER.error("Type mismatch: actual type '{}', expected '{}'. Key is '{}', value is '{}'",
                    value.getClass().getSimpleName(), type.getSimpleName(), key, value);
            return null;
        }
    }

    public void addProperty(String key, Object value) {
        properties.put(key, value);
    }

    /**
     * Merge this.properties with properties parameter.
     */
    public void merge(Properties properties) {
        this.properties.putAll(properties);
    }

    /**
     * Get config properties with given name prefix.
     * It's not clear, but this method cuts not only the prefix but a trailing dot too.
     * So, for example,
     * - prefix is "context.format",
     * - properties are "context.format.prettyPrint", "context.format.messageType",
     * - returned values are "prettyPrint", "messageType" correspondingly.
     * But if a property name is "context.formatABCD", "BCD" value will be returned.
     */
    public Config getByPrefix(String prefix, boolean truncPrefix) {
        Config config = new Config(false); //Don't load file properties just create new empty instance
        for (Map.Entry<Object, Object> property : properties.entrySet()) {
            String key = property.getKey().toString();
            if (StringUtils.startsWith(key, prefix)) {
                config.addProperty(truncPrefix ? key.substring(prefix.length() + 1) : key, property.getValue());
            }
        }
        return config;
    }

    public Config getByPrefix(String prefix) {
        return getByPrefix(prefix, true);
    }

    public Collection<Object> getValues() {
        return getValues(Object.class);
    }

    /**
     * Get config values of given class.
     */
    public <T> Collection<T> getValues(Class<T> type) {
        Collection<Object> objects = properties.values();
        LinkedList<T> list = new LinkedList<>();
        for (Object object : objects) {
            list.addFirst(type.cast(object));
        }
        return list;
    }

    public String getRunningHostname() {
        return config.getString(RUNNING_HOSTNAME);
    }

    public String getRunningPort() {
        return config.getString(RUNNING_PORT);
    }

    public String getContextPopupUrl() {
        return config.getString(CONTEXT_POPUP_URL);
    }

    /**
     * Get URL of VM where ITF is running (it can be from ITF config or from real VM name).
     */
    public String getRunningUrl() {
        String url = config.getString(RUNNING_URL);
        return StringUtils.isNotBlank(url) ? url : constructRunningUrl();
    }
}
