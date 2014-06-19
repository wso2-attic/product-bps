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
package org.wso2.bps.integration.tests.bpel.bpelactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.wso2.bps.integration.common.clients.bpel.BpelInstanceManagementClient;
import org.wso2.bps.integration.common.clients.bpel.BpelPackageManagementClient;
import org.wso2.bps.integration.common.clients.bpel.BpelProcessManagementClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.bps.integration.common.utils.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;

public class BpelActCombineUrlTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelActCombineUrlTest.class);

    LimitedInstanceInfoType instanceInfo = null;
    private BpelPackageManagementClient bpelPackageManagementClient;
    BpelProcessManagementClient bpelProcessManagementClient;
    BpelInstanceManagementClient bpelInstanceManagementClient;

   RequestSender requestSender;


    public void setEnvironment() throws Exception, RemoteException {
        init();
        bpelPackageManagementClient = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        bpelProcessManagementClient = new BpelProcessManagementClient(backEndUrl, sessionCookie);
        bpelInstanceManagementClient = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    //@BeforeClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void deployArtifact() throws Exception {
        setEnvironment();
        uploadBpelForTest("TestCombineUrl");

        requestSender.waitForProcessDeployment(backEndUrl + "TestCombineUrlService");
    }

    //@Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Invike combine URL Bpel")
    public void testCombineUrl() throws Exception, RemoteException {
        int instanceCount = 0;

        String processID = bpelProcessManagementClient.getProcessId("TestCombineUrl");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = bpelInstanceManagementClient.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.forEachRequest();
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

  //  @AfterClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts()  throws Exception {
        bpelPackageManagementClient.undeployBPEL("TestCombineUrl");
        this.loginLogoutClient.logout();
    }

    public void forEachRequest() throws Exception {
        String payload = "      <p:combineUrl xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "     <base>http://www.google.lk/</base>\n" +
                "     <!--Exactly 1 occurrence-->\n" +
                "      <relative>search</relative>\n" +
                "   </p:combineUrl>";
        String operation = "combineUrl";
        String serviceName = "TestCombineUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("http://www.google.lk/search");
        requestSender.sendRequest(backEndUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }
}

