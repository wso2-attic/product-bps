/*
 * Copyright (c) 2012, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.bps.samples.processcleanup;

import java.io.File;
import java.io.FileInputStream;
import java.text.MessageFormat;
import java.util.Properties;

//Class used to trigger the relevant SQL query according to the DB type
public class DBQuery {

    private String SEARCH;
    private String ODE_PROCESS;
    private String ODE_PROCESS_INSTANCE;
    private String STORE_DU;
    private String STORE_PROCESS;

    DBQuery() {
        Properties prop = new Properties();

        try {
            File file = new File("." + File.separator);
            System.setProperty("carbon.home", file.getCanonicalFile().toString());

            if (System.getProperty("os.name").startsWith("Windows")) {
                prop.load(new FileInputStream(System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "conf" + File.separator + "process-cleanup.properties"));
            } else {
                prop.load(new FileInputStream(System.getProperty("carbon.home") + File.separator + ".." + File.separator + "repository" + File.separator + "conf" + File.separator + "process-cleanup.properties"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(0);
        }

        //Get the database type using the database url configured in the properties file
        String databaseType = prop.getProperty("database.url").split(":")[1];

        //for mysql, oracle and sqlserver the query is same
        if (databaseType.equals("mysql") || databaseType.equals("oracle") || databaseType.equals("sqlserver")) {

            SEARCH = "select distinct(s.PID) as PROCESS_ID, o.ID, s.VERSION, s.DU\n" +
                    "from STORE_PROCESS s left join ODE_PROCESS o\n" +
                    "on s.PID = o.PROCESS_ID\n" +
                    "where s.STATE != \"ACTIVE\" {0}\n";

            ODE_PROCESS = "delete from ODE_PROCESS where id = {0}";

            ODE_PROCESS_INSTANCE = "delete from ODE_PROCESS_INSTANCE where PROCESS_ID = {0}";

            STORE_DU = "delete from STORE_DU where NAME=\"{0}\"";

            STORE_PROCESS = "delete from STORE_PROCESS where DU=\"{0}\"";

        } else if (databaseType.equals("h2")) {
            //todo: need to update the queries for H2 DB type
            System.out.println("H2 not yet supported.");
            System.exit(0);

        } else {
            System.out.println("Unsupported DB Type \n" +
                    "or Invalid Driver Name!");
        }
    }

    //formats the String with the process name and returns the delete query
    public String deleteFromStoreProcess(String name) {
        String sql = MessageFormat.format(STORE_PROCESS, name);
        return sql.replaceAll("\"","'");
    }

    //formats the String with the filter and returns the search query
    public String getSearchQuery(String filter) {
        String sql = MessageFormat.format(SEARCH, filter);
        return sql.replaceAll("\"","'");
    }

    //formats the String with the filter and returns the search query
    public String getSearchByNameQuery(String filter, String name) {
        String sql = MessageFormat.format(SEARCH, filter).concat("and s.DU like \"%"+name+"%\"");
        return sql.replaceAll("\"","'");
    }

    //formats the String with the process id and returns the delete query
    public String deleteFromOdeProcess(String id) {
        String sql = MessageFormat.format(ODE_PROCESS, id);
        return sql.replaceAll("\"","'");
    }

    //formats the String with the process id and returns the delete query
    public String deleteFromOdeProcessInstance(String id) {
        String sql = MessageFormat.format(ODE_PROCESS_INSTANCE, id);
        return sql.replaceAll("\"","'");
    }

    //formats the String with the process name and returns the delete query
    public String deleteFromStoreDu(String name) {
        String sql = MessageFormat.format(STORE_DU, name);
        return sql.replaceAll("\"","'");
    }
}
