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

package org.qubership.automation.itf.core.util.logger;

import org.qubership.automation.itf.core.util.config.ApplicationConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;

public class ItfLogger implements Logger {
    public static final String LOG_LEVEL = "log.level";

    private Logger logger;

    public static Logger getLogger(Class classLogger) {
        return new ItfLogger(LoggerFactory.getLogger(classLogger));
    }

    private ItfLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public String getName() {
        return logger.getName();
    }

    @Override
    public boolean isDebugEnabled() {
        return logLevelEnabled(Level.DEBUG);
    }

    @Override
    public boolean isDebugEnabled(Marker marker) {
        return isDebugEnabled();
    }

    @Override
    public void debug(String msg) {
        if (isDebugEnabled()) {
            logger.debug(msg);
        }
    }

    @Override
    public void debug(String msg, Object object) {
        if (isDebugEnabled()) {
            logger.debug(msg, object);
        }
    }

    @Override
    public void debug(String msg, Object first, Object second) {
        if (isDebugEnabled()) {
            logger.debug(msg, first, second);
        }
    }

    @Override
    public void debug(String msg, Object... objects) {
        if (isDebugEnabled()) {
            logger.debug(msg, objects);
        }
    }

    @Override
    public void debug(String msg, Throwable throwable) {
        if (isDebugEnabled()) {
            logger.debug(msg, throwable);
        }
    }

    @Override
    public void debug(Marker marker, String msg) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, msg);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Object object) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, msg, object);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Object first, Object second) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, msg, first, second);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Object... objects) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, msg, objects);
        }
    }

    @Override
    public void debug(Marker marker, String msg, Throwable throwable) {
        if (isDebugEnabled(marker)) {
            logger.debug(marker, msg, throwable);
        }
    }

    @Override
    public boolean isTraceEnabled() {
        return logLevelEnabled(Level.TRACE);
    }

    @Override
    public boolean isTraceEnabled(Marker marker) {
        return isTraceEnabled();
    }

    @Override
    public void trace(String msg) {
        if (isTraceEnabled()) {
            logger.trace(msg);
        }
    }

    @Override
    public void trace(String msg, Object object) {
        if (isTraceEnabled()) {
            logger.trace(msg, object);
        }
    }

    @Override
    public void trace(String msg, Object first, Object second) {
        if (isTraceEnabled()) {
            logger.trace(msg, first, second);
        }
    }

    @Override
    public void trace(String msg, Object... objects) {
        if (isTraceEnabled()) {
            logger.trace(msg, objects);
        }
    }

    @Override
    public void trace(String msg, Throwable throwable) {
        if (isTraceEnabled()) {
            logger.trace(msg, throwable);
        }
    }

    @Override
    public void trace(Marker marker, String msg) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, msg);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Object object) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, msg, object);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Object first, Object second) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, msg, first, second);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Object... objects) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, msg, objects);
        }
    }

    @Override
    public void trace(Marker marker, String msg, Throwable throwable) {
        if (isTraceEnabled(marker)) {
            logger.trace(marker, msg, throwable);
        }
    }

    @Override
    public boolean isInfoEnabled() {
        return logLevelEnabled(Level.INFO);
    }

    @Override
    public boolean isInfoEnabled(Marker marker) {
        return isInfoEnabled();
    }

    @Override
    public void info(String msg) {
        if (isInfoEnabled()) {
            logger.info(msg);
        }
    }

    @Override
    public void info(String msg, Object object) {
        if (isInfoEnabled()) {
            logger.info(msg, object);
        }
    }

    @Override
    public void info(String msg, Object first, Object second) {
        if (isInfoEnabled()) {
            logger.info(msg, first, second);
        }
    }

    @Override
    public void info(String msg, Object... objects) {
        if (isInfoEnabled()) {
            logger.info(msg, objects);
        }
    }

    @Override
    public void info(String msg, Throwable throwable) {
        if (isInfoEnabled()) {
            logger.info(msg, throwable);
        }
    }

    @Override
    public void info(Marker marker, String msg) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, msg);
        }
    }

    @Override
    public void info(Marker marker, String msg, Object object) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, msg, object);
        }
    }

    @Override
    public void info(Marker marker, String msg, Object first, Object second) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, msg, first, second);
        }
    }

    @Override
    public void info(Marker marker, String msg, Object... objects) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, msg, objects);
        }
    }

    @Override
    public void info(Marker marker, String msg, Throwable throwable) {
        if (isInfoEnabled(marker)) {
            logger.info(marker, msg, throwable);
        }
    }

    @Override
    public boolean isWarnEnabled() {
        return logLevelEnabled(Level.WARN);
    }

    @Override
    public boolean isWarnEnabled(Marker marker) {
        return isWarnEnabled();
    }

    @Override
    public void warn(String msg) {
        if (isWarnEnabled()) {
            logger.warn(msg);
        }
    }

    @Override
    public void warn(String msg, Object object) {
        if (isWarnEnabled()) {
            logger.warn(msg, object);
        }
    }

    @Override
    public void warn(String msg, Object... objects) {
        if (isWarnEnabled()) {
            logger.warn(msg, objects);
        }
    }

    @Override
    public void warn(String msg, Object first, Object second) {
        if (isWarnEnabled()) {
            logger.warn(msg, first, second);
        }
    }

    @Override
    public void warn(String msg, Throwable throwable) {
        if (isWarnEnabled()) {
            logger.warn(msg, throwable);
        }
    }

    @Override
    public void warn(Marker marker, String msg) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, msg);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Object object) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, msg, object);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Object first, Object second) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, msg, first, second);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Object... objects) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, msg, objects);
        }
    }

    @Override
    public void warn(Marker marker, String msg, Throwable throwable) {
        if (isWarnEnabled(marker)) {
            logger.warn(marker, msg, throwable);
        }
    }

    @Override
    public boolean isErrorEnabled() {
        return logLevelEnabled(Level.ERROR);
    }

    @Override
    public boolean isErrorEnabled(Marker marker) {
        return isErrorEnabled();
    }

    @Override
    public void error(String msg) {
        if (isErrorEnabled()) {
            logger.error(msg);
        }
    }

    @Override
    public void error(String msg, Object object) {
        if (isErrorEnabled()) {
            logger.error(msg, object);
        }
    }

    @Override
    public void error(String msg, Object first, Object second) {
        if (isErrorEnabled()) {
            logger.error(msg, first, second);
        }
    }

    @Override
    public void error(String msg, Object... objects) {
        if (isErrorEnabled()) {
            logger.error(msg, objects);
        }
    }

    @Override
    public void error(String msg, Throwable throwable) {
        if (isErrorEnabled()) {
            logger.error(msg, throwable);
        }
    }

    @Override
    public void error(Marker marker, String msg) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, msg);
        }
    }

    @Override
    public void error(Marker marker, String msg, Object object) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, msg, object);
        }
    }

    @Override
    public void error(Marker marker, String msg, Object first, Object second) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, msg, first, second);
        }
    }

    @Override
    public void error(Marker marker, String msg, Object... objects) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, msg, objects);
        }
    }

    @Override
    public void error(Marker marker, String msg, Throwable throwable) {
        if (isErrorEnabled(marker)) {
            logger.error(marker, msg, throwable);
        }
    }

    private boolean logLevelEnabled(Level level) {
        //TODO: Implement the getting project ID from UI (for ITF configuration platform).
        // We can provide it from outside as method parameter or
        // as configuration common properties for all projects (get it as current project id below)
        return level.toString().equals(ApplicationConfig.env.getProperty(LOG_LEVEL,"INFO").toUpperCase());
    }
}
