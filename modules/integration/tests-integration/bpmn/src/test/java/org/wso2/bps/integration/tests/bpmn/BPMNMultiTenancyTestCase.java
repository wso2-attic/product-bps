/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.bps.integration.tests.bpmn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.WorkflowServiceClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNDeployment;


import java.util.concurrent.TimeUnit;

public class BPMNMultiTenancyTestCase extends BPSMasterTest {
    private static final Log log = LogFactory.getLog(BPMNTaskCreationTestCase.class);

    private WorkflowServiceClient workflowServiceClient;
    private int deploymentCount;
    boolean accessedSameArtifactInstance = false;
    String domainKey1 = "wso2.com";
    String userKey1 = "user1";
    String domainKey2 = "abc.com";
    String userKey2 = "user2";


    @BeforeClass(alwaysRun = true)
    public void createTenant() throws Exception {

        // initialize for tenant wso2.com
        initialize(domainKey1, userKey1);

    }

    //initialize for each tenant
    public void initialize(String domainKey, String userKey) throws Exception {
        init(domainKey, userKey);
        workflowServiceClient = new WorkflowServiceClient(backEndUrl, sessionCookie);
        BPMNDeployment[] bpmnDeployments = workflowServiceClient.getDeployments();
        if (bpmnDeployments != null) {
            deploymentCount = workflowServiceClient.getDeployments().length;
        }

    }

    @Test(groups = {"wso2.bps.task.BPMNMultiTenancy"}, description = "Confirm BPMN Multi tenancy support test case", priority = 1, singleThreaded = true)
    public void confirmMultiTenancyForBPMNArtifact() throws Exception {

        // log in as tenant wso2.com
        String session = loginLogoutClient.login();
        //deploy BPMN artifact from tenant wso2.com
        deployArtifact();
        BPMNTestUtils.waitForProcessDeployment(workflowServiceClient, "VacationRequest", deploymentCount);
        TimeUnit.SECONDS.sleep(5);
        String processId = workflowServiceClient.getProcesses()[workflowServiceClient.getProcesses().length - 1].getProcessId();
        log.info("BPMN Process:" + processId + " accessed by tenant t1 " + session);
        loginLogoutClient.logout();

        initialize(domainKey2, userKey2);
        //login as tenant abc.com
        loginLogoutClient.login();
        int deployedInstance = workflowServiceClient.getInstanceCount();
        if (deployedInstance == 0) {
            log.info("No processes available for tenant:" + "abc.com");
        } else {
            //if deployment instances exist for abc.com
            if (deploymentCount != 0) {

                String processId2 = workflowServiceClient.getProcesses()[workflowServiceClient.getProcesses().length - 1].getProcessId();
                //if it is the same processId as of VacationRequest
                if (processId2.equals(processId)) {
                    accessedSameArtifactInstance = true;
                }
            }
        }

        loginLogoutClient.logout();
        Assert.assertEquals(accessedSameArtifactInstance, false, "Artifact deployed by tenant wso2.com accessed by tenant abc.com");

    }


    public void deployArtifact() throws Exception {
        uploadBPMNForTest("VacationRequest");
    }

    @AfterClass(alwaysRun = true)
    public void deleteTenant() throws Exception {
        initialize(domainKey1, userKey1);
        workflowServiceClient.undeploy("VacationRequest");
        log.info("Successfully undeployed:" + "VacationRequest");

    }
}
