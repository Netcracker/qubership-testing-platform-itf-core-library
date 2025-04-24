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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConfig {

    protected static final Logger LOGGER = LoggerFactory.getLogger(Config.class);
    static String configPath;
    protected static final InputStreamSupplier CLASS_PATH_RESOURCE = new InputStreamSupplier() {
        private URL lastResource = null;

        @Override
        public InputStream get() throws IOException {
            lastResource = Thread.currentThread().getContextClassLoader().getResource(configPath);
            if (lastResource == null) {
                throw new IOException("Can not find [" + configPath + "] in classpath");
            }
            return lastResource.openStream();
        }

        @Override
        public String toString() {
            StringBuilder b = new StringBuilder("from classpath lookup of ").append(configPath);
            if (lastResource != null) {
                b.append(" [").append(lastResource).append("]");
            }
            return b.toString();
        }
    };
    protected static final InputStreamSupplier FILE_IS = new InputStreamSupplier() {
        @Override
        public InputStream get() throws IOException {
            return new FileInputStream(configPath);
        }

        @Override
        public String toString() {
            return "from " + new File(configPath).getAbsolutePath();
        }
    };
    protected Properties properties = new Properties();

    /**
     * TODO: Add JavaDoc.
     */
    public AbstractConfig(boolean withLoading, String fileName) {
        if (withLoading) {
            configPath = System.getProperty("config.file", fileName);
            if (!load(FILE_IS, properties)) {
                load(CLASS_PATH_RESOURCE, properties);
            }
            properties.putAll(System.getProperties());
        }
    }

    protected static boolean load(InputStreamSupplier from, Properties to) {
        try (InputStreamReader reader = new InputStreamReader(from.get(), java.nio.charset.StandardCharsets.UTF_8)) {
            to.load(reader);
        } catch (IOException e) {
            LOGGER.warn("Unable to load configuration file '{}'", from);
            return false;
        }
        LOGGER.info("Configuration file successfully loaded {}", from);
        return true;
    }

    public Properties getProperties() {
        return properties;
    }

    protected interface InputStreamSupplier {

        InputStream get() throws IOException;
    }
}
