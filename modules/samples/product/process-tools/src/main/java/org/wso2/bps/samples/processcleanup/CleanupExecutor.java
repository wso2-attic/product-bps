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

import org.apache.xerces.dom.DeferredElementImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.sql.*;
import java.util.*;

// The main class
public class CleanupExecutor {
    private static HashMap<String, List<String>> map;
    static String databaseURL = null;
    static String bpsHome = null;
    //DB query builder according to DB type
    private static DBQuery query;

    //Get user configurations from processCleanup.properties file
    private static String getProperty(String property) throws Exception {
        Properties prop = new Properties();
        String configPath = bpsHome + "repository" + File.separator + "conf" + File.separator + "process-cleanup.properties";
        prop.load(new FileInputStream(configPath));
        return prop.getProperty(property);
    }

    /**
     * Create DB connection
     * @return Connection
     * @throws ParserConfigurationException
     * @throws IOException
     * @throws SAXException
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    private static Connection initializeDBConnection() throws ParserConfigurationException, IOException, SAXException,
            ClassNotFoundException, SQLException {
        String databaseUsername = null;
        String databasePassword = null;
        String databaseDriver = null;
        boolean dbConfigFound = false;
        String carbon = System.getProperty("carbon.home");
        System.out.println(carbon);
        String path[] = carbon.split(File.separator);
        bpsHome = "";
        for (int i = 0; i < path.length; i++) {
            bpsHome = bpsHome + path[i] + File.separator;
            if(path[i].contains("wso2bps")){
                break;
            }
        }
        String configPath = bpsHome + "repository" + File.separator + "conf" + File.separator + "datasources" +
                File.separator + "bps-datasources.xml";
        System.out.println("Using datasource config file at :" + configPath);
        File elementXmlFile = new File(configPath);
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setIgnoringComments(true);
        dbFactory.setIgnoringElementContentWhitespace(true);
        DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
        Document document = dBuilder.parse(elementXmlFile);
        document.getDocumentElement().normalize();
        NodeList datasourceList = document.getDocumentElement().getElementsByTagName("datasource");
        for (int i = 0; i < datasourceList.getLength(); i++) {
            Node datasource = datasourceList.item(i);
            String dbName = ((DeferredElementImpl) datasource).getElementsByTagName("name").item(0).getTextContent();
            if(dbName.equals("BPS_DS")){
                databaseURL = document.getDocumentElement().getElementsByTagName("url").item(i).getTextContent().split(";")[0];
                databaseDriver = document.getDocumentElement().getElementsByTagName("driverClassName").item(i).getTextContent();
                databaseUsername = document.getDocumentElement().getElementsByTagName("username").item(i).getTextContent();
                databasePassword = document.getDocumentElement().getElementsByTagName("password").item(i).getTextContent();
                dbConfigFound = true;
                break;
            }
        }
        if(!dbConfigFound){
            System.out.println("ERROR: DB configurations not found or invalid!");
            System.exit(0);
        }
        Class.forName(databaseDriver);
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
                filters = " and s.DU not in \n" +
                        "(select s.DU \n" +
                        "from ODE_PROCESS_INSTANCE a, ODE_PROCESS b, STORE_PROCESS s \n" +
                        "where a.PROCESS_ID = b.ID \n" +
                        "and s.PID = b.PROCESS_ID \n" +
                        "and (";
                filters += " a.INSTANCE_STATE = " + status.state.values().toArray()[0] + "))";
                break;
            default:
                filters = " and s.DU not in \n" +
                        "(select s.DU \n" +
                        "from ODE_PROCESS_INSTANCE a, ODE_PROCESS b, STORE_PROCESS s \n" +
                        "where a.PROCESS_ID = b.ID \n" +
                        "and s.PID = b.PROCESS_ID \n" +
                        "and (a.INSTANCE_STATE = " + status.state.values().toArray()[0];
                for (int i = 1; i < status.state.size(); i++) {
                    filters += " or a.INSTANCE_STATE = " + status.state.values().toArray()[i];
                }
                filters += "))";
        }

        return filters;
    }

    //Registry and DB cleanup process
    private static boolean deletePackages(String packageName) throws Exception {
        Connection conn = initializeDBConnection();
        conn.setAutoCommit(false);
        String clientTrustStorePath = getProperty("clientTrustStorePath");
        String trustStorePassword = getProperty("clientTrustStorePassword");
        String trustStoreType = getProperty("clientTrustStoreType");
        System.out.println("Deleting package:" + packageName);
        String regPath = "/_system/config/bpel/packages/";

        //DB cleanup happens if the Registry cleaned successfully
        boolean regCleanSuccess = RegistryCleaner.deleteRegistry(regPath, packageName, clientTrustStorePath, trustStorePassword, trustStoreType);

        if (regCleanSuccess) {
            List<String> processList = map.get(packageName);
            try {
                if(getProperty("delete.instances").toLowerCase().equals("true")){
                    cleanProcessInstances(processList, conn);
                }
                for (String id : processList) {
                    conn.createStatement().execute(query.deleteFromOdeProcess(id));
                }
                conn.createStatement().execute(query.deleteFromStoreDu(packageName));
                conn.createStatement().execute(query.deleteFromStoreProcess(packageName));
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

    private static void cleanProcessInstances(List<String> processIdList, Connection conn) throws Exception {
        int i = 0;
        System.out.print("Instance Clean Count : 000000000");
        try {
            Statement stmt = conn.createStatement();
            for (String process : processIdList) {
                String sql = query.getInstancesSearchQuery(process);
                ResultSet rs = stmt.executeQuery(sql);
                while(rs.next()){
                    String id = rs.getString("ID");
                    conn.createStatement().execute(query.deleteFromOdePartnerLink(id));
                    conn.createStatement().execute(query.deleteFromOdeScope(id));
                    conn.createStatement().execute(query.deleteFromOdeEvent(id));
                    conn.createStatement().execute(query.deleteFromOdeCorsetProp(id));
                    conn.createStatement().execute(query.deleteFromOdeCorrelationSet(id));
                    conn.createStatement().execute(query.deleteFromOdeXmlDataProp(id));
                    conn.createStatement().execute(query.deleteFromOdeXmlData(id));
                    conn.createStatement().execute(query.deleteFromOdeMexProp(id));
                    conn.createStatement().execute(query.deleteFromOdeMessage(id));
                    conn.createStatement().execute(query.deleteFromOdeMessageExchange(id));
                    conn.createStatement().execute(query.deleteFromOdeMessageRoute(id));
                    conn.createStatement().execute(query.deleteFromOdeProcessInstance(id));
                    //printing the status
                    System.out.print("\b\b\b\b\b\b\b\b\b");
                    System.out.print(String.format("%09d", ++i));
                }
                conn.commit();
            }
            System.out.println();
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException(e);
        }
    }

    //Using the search query gets all deletable package list
    private static HashMap<String, List<String>> getDeletablePackageList(String name) throws Exception {
        Statement stmt = initializeDBConnection().createStatement();
        String filters = getFilters();
        String sql;
        if(name != null){
            sql = query.getSearchByNameQuery(filters, name);
        }else{
            sql = query.getSearchQuery(filters);
        }

        ResultSet rs = stmt.executeQuery(sql);
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();

        while(rs.next()){
            if(map.get(rs.getString("DU")) == null){
                List<String> list = new ArrayList<String>();
                list.add(rs.getString("ID"));
                map.put(rs.getString("DU"), list);
            }else{
                map.get(rs.getString("DU")).add(rs.getString("ID"));
            }
        }

        String[] keys = map.keySet().toArray(new String[]{});
        for(int i=0; i<map.size(); i++){
            //Display the options
            String id = String.format("%9d", i+1);
            //Add the list of processes retrieved from DB into a list
            System.out.println(String.format("%-10s | %s" , id, keys[i]));
        }

        return map;
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
        File file = new File("." + File.separator);
        System.setProperty("carbon.home", file.getCanonicalFile().toString());
        initializeDBConnection();
        query = new DBQuery(databaseURL, bpsHome);
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
                    System.out.println(String.format(" %-9s | %s" , "Option #", "Package Name"));
                    System.out.println("==========================================");
                    map = getDeletablePackageList(name);
                    String[] list = map.keySet().toArray(new String[]{});
                    int deleteAllOption = list.length + 1;

                    switch (list.length){
                        case 0:
                            System.out.println("*** No Packages Found ***");
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

                    //Get user input with multiple packages to delete at once
                    int options[] = getValidUserInput(0, deleteAllOption, "Enter Option Numbers to Delete (comma separated):");

                    if (options[0] == 0) {
                        //if entered 0 system exits
                        System.exit(0);
                    } else if (options[0] == deleteAllOption) {
                        //Delete all option
                        for (String packageName : list) {
                            deletePackages(packageName);
                        }
                    } else {
                        //Delete several packages
                        for (int op : options) {
                            deletePackages(list[op - 1]);
                        }
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
        }
    }
}
