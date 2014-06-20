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
package org.wso2.bps.integration.tests.bpel.stucturedactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpel.BpelInstanceManagementClient;
import org.wso2.bps.integration.common.clients.bpel.BpelPackageManagementClient;
import org.wso2.bps.integration.common.clients.bpel.BpelProcessManagementClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.bps.integration.common.utils.RequestSender;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;

import java.net.MalformedURLException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BpelStructActionWhileTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelStructActionWhileTest.class);

    LimitedInstanceInfoType instanceInfo = null;
    BpelPackageManagementClient bpelPackageManagementClient;
    BpelProcessManagementClient bpelProcessManagementClient;
    BpelInstanceManagementClient bpelInstanceManagementClient;

    RequestSender requestSender;


    public void setEnvironment() throws Exception {
        init();
        bpelPackageManagementClient = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        bpelProcessManagementClient = new BpelProcessManagementClient(backEndUrl, sessionCookie);
        bpelInstanceManagementClient = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true)
    public void deployArtifact() throws InterruptedException, RemoteException, MalformedURLException,
            PackageManagementException, LoginAuthenticationExceptionException, Exception {
        setEnvironment();
        uploadBpelForTest("TestWhile");
        requestSender.waitForProcessDeployment(backEndUrl + "TestWhileService");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.structures"}, description = "Deploys TestWhile with activity", priority = 0)
    public void runSuccessCase() throws Exception {
        int instanceCount = 0;

        String processID = bpelProcessManagementClient.getProcessId("TestWhile");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstanceManagementClient.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.whileRequest();
                Thread.sleep(5000);
                if (instanceCount >= bpelInstanceManagementClient.filterPageInstances(processID).getInstance().length) {
                    Assert.fail("Instance is not created for the request");
                }
            } catch (InterruptedException e) {
                log.error("Process management failed" + e);
                Assert.fail("Process management failed" + e);
            }
            bpelInstanceManagementClient.clearInstancesOfProcess(processID);
        }
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException {
        bpelPackageManagementClient.undeployBPEL("TestWhile");
        this.loginLogoutClient.logout();
    }

    public void whileRequest() throws Exception {
        String payload = " <p:TestWhileRequest xmlns:p=\"http://wso2.org/bps/samples/testWhile\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <input xmlns=\"http://wso2.org/bps/samples/testWhile\">22.2</input>\n" +
                "   </p:TestWhileRequest>";
        String operation = "process";
        String serviceName = "TestWhileService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("32.2");

        requestSender.sendRequest(backEndUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }
}

