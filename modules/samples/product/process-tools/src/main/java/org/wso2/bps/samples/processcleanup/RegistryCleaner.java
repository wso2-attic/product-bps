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

import org.apache.axis2.client.Options;
import org.apache.axis2.client.ServiceClient;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;
import org.wso2.carbon.registry.resource.stub.ResourceAdminServiceStub;
import org.wso2.carbon.utils.NetworkUtils;

import java.io.File;
import java.io.FileInputStream;
import java.util.Properties;

//Class to delete relevant versions from Registry
public class RegistryCleaner {
    private static Properties prop = new Properties();

    //Cleans the registry at the regPath location
    public static boolean deleteRegistry(String regPath, String packageName, String clientTrustStorePath, String trustStorePassword, String trustStoreType) {
        ResourceAdminServiceStub resourceAdminServiceStub;
        setKeyStore(clientTrustStorePath, trustStorePassword, trustStoreType);

        try {
            File file = new File("." + File.separator);
            System.setProperty("carbon.home", file.getCanonicalFile().toString());

            if (System.getProperty("os.name").startsWith("Windows")) {
                prop.load(new FileInputStream(System.getProperty("carbon.home") + File.separator + "repository" + File.separator + "conf" + File.separator + "process-cleanup.properties"));
            } else {
                prop.load(new FileInputStream(System.getProperty("carbon.home") + File.separator + ".." + File.separator + "repository" + File.separator + "conf" + File.separator + "process-cleanup.properties"));
            }

            String resourceAdminServiceURL = prop.getProperty("tenant.context") + "/services/ResourceAdminService";

            resourceAdminServiceStub = new ResourceAdminServiceStub(resourceAdminServiceURL);
            ServiceClient client = resourceAdminServiceStub._getServiceClient();
            Options option = client.getOptions();
            option.setManageSession(true);
            option.setProperty(org.apache.axis2.transport.http.HTTPConstants.COOKIE_STRING, login());
            resourceAdminServiceStub._getServiceClient().getOptions().setTimeOutInMilliSeconds(
		            600000);

            String regPathAppend = regPath + packageName.split("-\\d*$")[0];
            String regPathVersionsAppend = regPathAppend + "/versions/";
            int count = resourceAdminServiceStub.getCollectionContent(regPathVersionsAppend).getChildCount();
            System.out.println( "number of deployment units of " + packageName.split("-\\d*$")[0] + " : " + count);

            //if the number of deployment units of the given package exceed one, then removes the relevant deployment unit from the path that it exists.
            if(count > 1){
                System.out.println("Package removed from " + regPathVersionsAppend + packageName);
                resourceAdminServiceStub.delete(regPathVersionsAppend + packageName);
                return true;
            }
            //if the number of deployment units of the given package equals to one, then removes it from /_system/config/bpel/packages/<package>
            else if(count == 1){
                System.out.println("Package removed from " + regPathAppend);
                resourceAdminServiceStub.delete(regPathAppend);
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    //Setup key store according to the processCleanup.properties
    private static void setKeyStore(String clientTrustStorePath, String trustStorePassword, String trustStoreType) {

        System.setProperty("javax.net.ssl.trustStore", clientTrustStorePath);
        System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        System.setProperty("javax.net.ssl.trustStoreType", trustStoreType);
    }

    //Creates the login session BPS login
    public static String login() throws Exception {

        AuthenticationAdminStub authenticationAdminStub;
        String authenticationAdminServiceURL = prop.getProperty("tenant.context") + "/services/AuthenticationAdmin";
        authenticationAdminStub = new AuthenticationAdminStub(authenticationAdminServiceURL);

        ServiceClient client = authenticationAdminStub._getServiceClient();
        Options options = client.getOptions();
        options.setManageSession(true);

        String userName = prop.getProperty("wso2.bps.username");
        String password = prop.getProperty("wso2.bps.password");
        String hostName = NetworkUtils.getLocalHostname();

        authenticationAdminStub.login(userName, password, hostName);

        ServiceContext serviceContext = authenticationAdminStub.
		                                                               _getServiceClient().getLastOperationContext().getServiceContext();

        return (String) serviceContext.getProperty(HTTPConstants.COOKIE_STRING);
    }
}
