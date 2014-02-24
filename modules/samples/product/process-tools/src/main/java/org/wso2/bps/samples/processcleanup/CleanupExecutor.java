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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.TimeZone;

// The main class
public class CleanupExecutor {
    //DB query builder according to DB type
    private static DBQuery query = new DBQuery();

    //Get user configurations from processCleanup.properties file
    private static String getProperty(String property) throws Exception {
        Properties prop = new Properties();
        File file = new File("." + File.separator);
        System.setProperty("carbon.home", file.getCanonicalFile().toString());

        if (System.getProperty("os.name").startsWith("Windows")) {
            prop.load(new FileInputStream(System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "conf" + File.separator + "process-cleanup.properties"));
        } else {
            prop.load(new FileInputStream(System.getProperty("carbon.home") + File.separator + ".." + File.separator + "repository" + File.separator + "conf" + File.separator + "process-cleanup.properties"));
        }
        return prop.getProperty(property);

    }

    //Create DB connection
    private static Connection getDBConnection() throws Exception {
        String databaseURL = getProperty("database.url");
        String databaseUsername = getProperty("database.username");
        String databasePassword = getProperty("database.password");
        Class.forName(getProperty("database.driver"));
        return DriverManager.getConnection(databaseURL, databaseUsername, databasePassword);
    }

    //Creating the search query filter string according to user configurations
    private static String getFilters() throws Exception {
        String filterStates[] = getProperty("process.filterStates").split(",");
        InstanceStatus status = new InstanceStatus();
        for (String s: filterStates){
            status.state.remove(s.trim());
        }
        String filters;

        switch (status.state.size()){
            case 0:
                filters = "";
                break;
            case 1:
                filters = " and s.PID not in \n" +
                        "(select b.PROCESS_ID \n" +
                        "from ODE_PROCESS_INSTANCE a, ODE_PROCESS b \n" +
                        "where a.PROCESS_ID = b.ID and (";
                filters += " a.INSTANCE_STATE = " + status.state.values().toArray()[0] + "))";
                break;
            default:
                filters = " and s.PID not in \n" +
                        "(select b.PROCESS_ID \n" +
                        "from ODE_PROCESS_INSTANCE a, ODE_PROCESS b \n" +
                        "where a.PROCESS_ID = b.ID and (a.INSTANCE_STATE = " + status.state.values().toArray()[0];
                for (int i = 1; i < status.state.size(); i++) {
                    filters += " or a.INSTANCE_STATE = " + status.state.values().toArray()[i];
                }
                filters += "))";
        }

        return filters;
    }

    //Registry and DB cleanup process
    private static boolean deleteProcess(String process) throws Exception {
        Connection conn = getDBConnection();
        conn.setAutoCommit(false);
        String clientTrustStorePath = getProperty("clientTrustStorePath");
        String trustStorePassword = getProperty("clientTrustStorePassword");
        String trustStoreType = getProperty("clientTrustStoreType");
        String id = process.split(" ")[0];
        String name = process.split(" ")[2];
        System.out.println("Deleting process:" + name);
        String regPath = "/_system/config/bpel/packages/" + name.split("-")[0] + "/versions/" + name;

        //DB cleanup happens if the Registry cleaned successfully
        boolean regCleanSuccess = RegistryCleaner.deleteRegistry(regPath, clientTrustStorePath, trustStorePassword, trustStoreType);
        if (regCleanSuccess) {
            String ODE_PROCESS = query.deleteFromOdeProcess(id);
            String ODE_PROCESS_INSTANCE = query.deleteFromOdeProcessInstance(id);
            String STORE_DU = query.deleteFromStoreDu(name);
            String STORE_PROCESS = query.deleteFromStoreProcess(name);

            try {
                conn.createStatement().execute(ODE_PROCESS);
                if(getProperty("delete.instances").equals("true")){
                    conn.createStatement().execute(ODE_PROCESS_INSTANCE);
                }
                conn.createStatement().execute(STORE_DU);
                conn.createStatement().execute(STORE_PROCESS);
                conn.commit();
                System.out.println("Database Cleaning Success!!");
                return true;
            } catch (SQLException e) {
                e.printStackTrace();
                conn.rollback();
                System.out.println("Database Cleaning Unsuccessful!! (DB error)");
            } finally {
                if (conn != null) {
                    conn.close();
                }
            }
        }
        System.out.println("Cleanup Unsuccessful! (Server Error)");
        return false;
    }

    //Using the search query gets all deletable process list
    private static List<String> getDeletableProcessList(String name) throws Exception {
        Statement stmt = getDBConnection().createStatement();
        String filters = getFilters();
        String sql;
        if(name != null){
            sql = query.getSearchByNameQuery(filters, name);
        }else{
            sql = query.getSearchQuery(filters);
        }

        ResultSet rs = stmt.executeQuery(sql);
        List<String> list = new ArrayList<String>();

        //index used to display the option number
        int i = 1;

        while (rs.next()) {
            //Display the options
            String id = String.format("%9d", i);
            System.out.println(String.format("%-10s | %s" , id, rs.getString("PROCESS_ID").split("}")[1]));
            //Add the list of processes retrieved from DB into a list
            list.add(rs.getInt("ID") + " " + rs.getString("PROCESS_ID").split("}")[1] + " " + rs.getString("DU"));
            i++;
        }
        return list;
    }

    //Get user inputs and validate
    private static int[] getValidUserInput(int minOption, int maxOption, String message) throws Exception{
        boolean valid = false;
        int[] userInputs = new int[1];
        //Get user inputs
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        do {
            System.out.println();
            System.out.println(message);

            String input = br.readLine();

            try {
                //Creates the list of options entered by user
                if (input.contains(",") && !input.contains(",,")) {
                    String optionsArray[] = input.split(",");
                    userInputs = new int[optionsArray.length];
                    for (int i = 0; i < optionsArray.length; i++) {
                        userInputs[i] = Integer.parseInt(optionsArray[i]);
                        if (userInputs[i] > minOption && userInputs[i] < maxOption) {
                            valid = true;
                        } else {
                            System.out.println("Invalid Input!");
                            valid = false;
                            break;
                        }
                    }
                } else {
                    userInputs[0] = Integer.parseInt(input);
                    if (userInputs[0] >= minOption && userInputs[0] <= maxOption){
                        valid = true;
                    }else {
                        System.out.println("Invalid Input!");
                    }
                }

            } catch (Exception e) {
                System.out.println("Invalid Input!");
            }
        } while (!valid);
        return userInputs;
    }

    public static void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone(getProperty("user.timezone")));
        System.out.println("\n=============ATTENTION=================\n" +
                "This tool deletes selected process versions and optionally all corresponding process instances.\n" +
                "Hence take backups of DB before executing this tool.\n" +
                "Also read configuration information from the docs before running the tool.\n" +
                "=======================================");

        System.out.println("\nInsert option number to list non-active BPEL packages");
        System.out.println("1. List All");
        System.out.println("2. Search and List by Process Name");
        System.out.println("3. Exit");

        int userInput[] = getValidUserInput(1, 3 , "Please Enter your option:");
        String name = null;
        switch (userInput[0]){
            case 3:
                System.exit(0);
                break;
            case 2:
                System.out.println("Please Enter Process Name:");
                BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
                name = br.readLine().trim();
            default:
                System.out.println("Initialize JDBC Connection\n");
                try {
                    //listing out the deletable process list in the console
                    System.out.println("List of Non-Active BPEL Packages\n");
                    System.out.println(String.format(" %-9s | %s" , "Option #", "Process Name with Version"));
                    System.out.println("==========================================");
                    List<String> list = getDeletableProcessList(name);
                    int deleteAllOption = list.size() + 1;

                    switch (list.size()){
                        case 0:
                            System.out.println("*** No Processes Found ***");
                            System.out.println("==========================================");
                            break;
                        case 1:
                            System.out.println("==========================================");
                            break;
                        default:
                            System.out.println("==========================================");
                            String id = String.format("%9d", deleteAllOption);
                            System.out.println(String.format("%-10s | %s" , id, "Delete All"));
                            break;
                    }
                    String id = String.format("%9d", 0);
                    System.out.println(String.format("%-10s | %s" , id, "Exit"));

                    //Get user input with multiple processes to delete at once
                    int options[] = getValidUserInput(0, deleteAllOption, "Enter Option Numbers to Delete (comma separated):");

                    if (options[0] == 0) {
                        //if entered 0 system exits
                        System.exit(0);
                    } else if (options[0] == deleteAllOption) {
                        //Delete all option
                        for (String process : list) {
                            deleteProcess(process);
                        }
                    } else {
                        //Delete several processes
                        for (int op : options) {
                            deleteProcess(list.get(op - 1));
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
