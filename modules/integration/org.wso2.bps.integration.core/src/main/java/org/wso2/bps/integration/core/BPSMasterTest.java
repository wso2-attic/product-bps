/*
*Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*WSO2 Inc. licenses this file to you under the Apache License,
*Version 2.0 (the "License"); you may not use this file except
*in compliance with the License.
*You may obtain a copy of the License at
*
*http://www.apache.org/licenses/LICENSE-2.0
*
*Unless required by applicable law or agreed to in writing,
*software distributed under the License is distributed on an
*"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
*KIND, either express or implied.  See the License for the
*specific language governing permissions and limitations
*under the License.
*/
package org.wso2.bps.integration.core;

import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.authenticators.AuthenticatorClient;
import org.wso2.carbon.automation.api.clients.bpel.BpelUploaderClient;
import org.wso2.carbon.automation.api.clients.humantask.HumanTaskUploaderClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentBuilder;
import org.wso2.carbon.automation.core.utils.environmentutils.EnvironmentVariables;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;

import java.io.File;
import java.rmi.RemoteException;

public class BPSMasterTest {
    protected EnvironmentVariables bpsServer;
    protected String sessionCookie = null;
    protected String backEndUrl = null;
    protected String serviceUrl = null;
    protected AuthenticatorClient authenticatorClient;

    protected BpelUploaderClient bpelUploaderClient;
    protected HumanTaskUploaderClient humanTaskUploaderClient;

    protected void init() throws RemoteException, LoginAuthenticationExceptionException {
        init(3);
    }

    protected void init(int userID) throws RemoteException, LoginAuthenticationExceptionException {
        EnvironmentBuilder builder = new EnvironmentBuilder().bps(userID);
        bpsServer = builder.build().getBps();

        backEndUrl = bpsServer.getBackEndUrl();
        serviceUrl = bpsServer.getServiceUrl();
        sessionCookie = bpsServer.getSessionCookie();
        authenticatorClient = bpsServer.getAdminServiceAuthentication();
        bpelUploaderClient = new BpelUploaderClient(backEndUrl, sessionCookie);
        humanTaskUploaderClient = new HumanTaskUploaderClient(backEndUrl, sessionCookie);
    }


    protected void uploadBpelForTest(String bpelName) throws RemoteException, InterruptedException, PackageManagementException {
        uploadBpelForTest(bpelName, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + BPSTestConstants.DIR_ARTIFACTS
                + File.separator + BPSTestConstants.DIR_BPEL);
    }

    protected void uploadBpelForTest(String bpelPackageName, String artifactLocation) throws RemoteException, InterruptedException, PackageManagementException {
        bpelUploaderClient.deployBPEL(bpelPackageName, artifactLocation);
    }

    protected void uploadHumanTaskForTest(String humantaskName) throws InterruptedException, RemoteException, org.wso2.carbon.humantask.stub.mgt.PackageManagementException {
        uploadHumanTaskForTest(humantaskName, ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + BPSTestConstants.DIR_ARTIFACTS
                + File.separator + BPSTestConstants.DIR_HUMAN_TASK);
    }

    protected void uploadHumanTaskForTest(String humanTaskArtifacts, String artifactLocation) throws InterruptedException, RemoteException, org.wso2.carbon.humantask.stub.mgt.PackageManagementException {
        humanTaskUploaderClient.deployHumantask(humanTaskArtifacts, artifactLocation);
    }


}
