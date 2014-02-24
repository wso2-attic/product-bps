/*
*  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/
package org.wso2.bps.samples.client;

import org.wso2.bps.samples.client.types.Login;
import org.wso2.bps.samples.client.types.LoginResponse;
import org.wso2.bps.samples.client.mgt.types.PaginatedProcessInfoList;
import org.wso2.bps.samples.client.mgt.types.LimitedProcessInfoType;
import org.wso2.bps.samples.client.mgt.ProcessManagementServiceStub;
import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.transport.http.HTTPConstants;

import java.rmi.RemoteException;

public class BPSManagementClient {
    final String backendServerURL = "https://localhost:9443/services/";
    final String AUTHENTICATION_ADMIN_SERVICE = "AuthenticationAdminService";
    final String trustStore = "/home/waruna/Desktop/BPS/new/wso2bps-1.1.0/repository/resources/security";
    String userName = "admin";
    String password = "admin";
    String clientIPAddr = "localhost";
    String cookie = null;

    public static void main (String []args) {
        boolean isLogged = false;
        BPSManagementClient client = new BPSManagementClient();
        try {
            isLogged = client.authenticate();
        } catch (AuthenticationExceptionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (RemoteException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        PaginatedProcessInfoList processList = null;

        if (isLogged) {
            try {
                System.out.println("User: " + client.userName + " loggin successful");
                processList = client.listProcessesPaginated("name}}* namespace=*", "deployed name", 0);
            } catch (RemoteException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            LimitedProcessInfoType[] processes = processList.getProcessInfo();
            if (processes != null) {
                System.out.println("---------------------------PROCESS LIST---------------------------");
                System.out.println("\t\tNAME\t\t|\t\tSTATUS\t\t|\t\tVERSION");
                for (LimitedProcessInfoType process : processes) {
                    System.out.println(process.getPid() + "\t|\t" + process.getStatus().getValue() + "\t|\t" +
                            process.getVersion());
                }
            }  else {
                System.out.println("NULL process list");
            }
        } else {
            System.out.println("User: " + client.userName + " loggin FAILED!");
        }
    }

    public boolean authenticate() throws AuthenticationExceptionException, RemoteException {
        String serviceURL = backendServerURL + AUTHENTICATION_ADMIN_SERVICE;
        AuthenticationAdminServiceStub stub = new AuthenticationAdminServiceStub(null, serviceURL);
        Login loginRequest = new Login();
        loginRequest.setUsername(userName);
        loginRequest.setPassword(password);
        loginRequest.setRemoteAddress(clientIPAddr);

        System.setProperty("javax.net.ssl.trustStore", trustStore + "/client-truststore.jks");
        System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");

        Options option = stub._getServiceClient().getOptions();
        option.setManageSession(true);
        LoginResponse loginResponse = stub.login(loginRequest);

        boolean isLogged = loginResponse.get_return();
        if (isLogged) {
            cookie = (String) stub._getServiceClient().getServiceContext().getProperty(HTTPConstants.COOKIE_STRING);
        }
        
        return isLogged;
    }

    public PaginatedProcessInfoList listProcessesPaginated(String filter, String orderBy, int pageNumber) throws RemoteException {
        String serviceURL = backendServerURL + "ProcessManagementService";
        ProcessManagementServiceStub stub = new ProcessManagementServiceStub(null, serviceURL);
        ServiceClient client = stub._getServiceClient();
        Options option = client.getOptions();
        option.setManageSession(true);
        option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, cookie);

        return stub.getPaginatedProcessList(filter, orderBy, pageNumber);
    }

}
