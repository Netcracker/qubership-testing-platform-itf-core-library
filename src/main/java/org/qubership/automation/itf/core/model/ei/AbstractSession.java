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

package org.qubership.automation.itf.core.model.ei;

import java.io.File;
import java.util.Date;

import org.qubership.automation.itf.core.util.annotation.Parameter;
import org.qubership.automation.itf.core.util.constants.EiConstants;
import org.qubership.automation.itf.core.util.provider.PropertyProvider;

public class AbstractSession implements PropertyProvider {
    @Parameter(shortName = EiConstants.SESSION_TYPE_SHORT_NAME,
            longName = EiConstants.SESSION_TYPE_LONG_NAME, description = "", order = 1)
    String sessionType;

    @Parameter(shortName = EiConstants.LINK_TO_THE_FILE_SHORT_NAME,
            longName = EiConstants.LINK_TO_THE_FILE_LONG_NAME, description = "", order = 2)
    File linkToFile;

    @Parameter(shortName = EiConstants.SESSION_STATUS_SHORT_NAME,
            longName = EiConstants.SESSION_STATUS_LONG_NAME, description = "", order = 3)
    String status;

    @Parameter(shortName = EiConstants.SESSION_START_DATE_SHORT_NAME,
            longName = EiConstants.SESSION_START_DATE_LONG_NAME, description = "", order = 4)
    Date startDate;

    @Parameter(shortName = EiConstants.SESSION_FINISH_DATE_SHORT_NAME,
            longName = EiConstants.SESSION_FINISH_DATE_LONG_NAME, description = "", order = 5)
    Date endDate;
}
