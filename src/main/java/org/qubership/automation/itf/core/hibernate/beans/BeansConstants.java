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

package org.qubership.automation.itf.core.hibernate.beans;

public enum BeansConstants {
    EMF_BEAN_NAME("entityManagerFactory"),
    PRE_INSTALL_SCRIPTS_DIRECTORY("install_scripts/pre_scripts"),
    POST_INSTALL_SCRIPTS_DIRECTORY("install_scripts/post_scripts"),
    PRE_SCRIPT_TYPE("pre-install"),
    POST_SCRIPT_TYPE("post-install"),

    CHECK_HISTORY_TABLE_EXISTENCE_QUERY("select count(*) from information_schema.tables where " + "table_schema"
            + "=current_schema() and table_name='mb_install_history'"),
    CREATE_HISTORY_TABLE_QUERY("CREATE TABLE mb_install_history (" + "release_version varchar(30) NOT NULL,"
            + "script_type " + "varchar(12) NOT NULL," + "execution_date timestamp with time zone NOT NULL,"
            + "filename text NOT NULL," + "CONSTRAINT mb_install_history_pk PRIMARY KEY " + "(release_version, "
            + "script_type, filename)" + ")"),
    CHECK_SCRIPT_EXECUTED_QUERY("select " + "count(*) from mb_install_history where release_version = ? and "
            + "script_type = ? and filename = ?"),
    ADD_DATA_INTO_HISTORY_TABLE_QUERY("insert into mb_install_history as history_table (release_version, script_type,"
            + " execution_date, filename) " + "select ? release_version, ? script_type, now() execution_date, ? "
            + "filename " + "on conflict (release_version, script_type, filename) " + "do update set execution_date ="
            + " EXCLUDED.execution_date " + "where history_table.release_version = EXCLUDED.release_version " + "and "
            + "history_table.script_type = EXCLUDED.script_type " + "and history_table.filename = EXCLUDED.filename");

    private final String stringValue;

    BeansConstants(String value) {
        this.stringValue = value;
    }

    public String getStringValue() {
        return stringValue;
    }
}
