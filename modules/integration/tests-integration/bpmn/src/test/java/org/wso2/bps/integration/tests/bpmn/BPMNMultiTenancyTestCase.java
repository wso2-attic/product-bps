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
import org.wso2.carbon.integration.common.admin.client.ApplicationAdminClient;
import org.wso2.carbon.integration.common.admin.client.TenantManagementServiceClient;
import org.wso2.carbon.integration.common.admin.client.AuthenticatorClient;

import java.util.concurrent.TimeUnit;

public class BPMNMultiTenancyTestCase extends BPSMasterTest {
    private static final Log log = LogFactory.getLog(BPMNTaskCreationTestCase.class);

    private WorkflowServiceClient workflowServiceClient;
    private WorkflowServiceClient workflowServiceClient2;
    private int deploymentCount;
    boolean accessedSameArtifactInstance = false;
    String domainKey1 = "wso2.com";
    String userKey1 = "user1";
    String domainKey2 = "abc.com";
    String userKey2 = "user2";


    @BeforeClass(alwaysRun = true)
    public void createTenant() throws Exception {

        // initialize for tenant wso2.com
        init(domainKey1, userKey1);
        workflowServiceClient = new WorkflowServiceClient(backEndUrl, sessionCookie);

        BPMNDeployment[] bpmnDeployments = workflowServiceClient.getDeployments();
        if (bpmnDeployments != null) {
            deploymentCount = workflowServiceClient.getDeployments().length;
        }


    }

    //initialize for abc.com
    public void initializeForTenant2() throws Exception {
        init(domainKey2, userKey2);
        workflowServiceClient2 = new WorkflowServiceClient(backEndUrl, sessionCookie);
        BPMNDeployment[] bpmnDeployments = workflowServiceClient2.getDeployments();
        if (bpmnDeployments != null) {
            deploymentCount = workflowServiceClient2.getDeployments().length;
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

        initializeForTenant2();
        //login as tenant abc.com
        loginLogoutClient.login();
        int deployedInstance = workflowServiceClient2.getInstanceCount();
        if (deployedInstance == 0) {
            log.info("No processes available for tenant:" + "abc.com");
        } else {
            //if deployment instances exist for abc.com
            if (deploymentCount != 0) {

                String processId2 = workflowServiceClient2.getProcesses()[workflowServiceClient.getProcesses().length - 1].getProcessId();
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

        workflowServiceClient.undeploy("VacationRequest");
        log.info("Successfully undeployed:" + "VacationRequest");

    }
}
