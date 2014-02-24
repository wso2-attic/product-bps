/*
*Copyright (c) 2005-2013, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.bps.humantask.coordination;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.core.BPSMasterTest;
import org.wso2.bps.integration.core.BPSTestConstants;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.bpel.BpelInstanceManagementClient;
import org.wso2.carbon.automation.api.clients.bpel.BpelPackageManagementClient;
import org.wso2.carbon.automation.api.clients.humantask.HumanTaskClientApiClient;
import org.wso2.carbon.automation.api.clients.humantask.HumanTaskPackageManagementClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.automation.core.utils.serverutils.ServerConfigurationManager;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.InstanceInfoType;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryCategory;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultRow;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import java.io.File;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class HumanTaskCoordinationTest extends BPSMasterTest {

    private static Log log = LogFactory.getLog(HumanTaskCoordinationTest.class);

    //Test Automation API Clients
    private BpelPackageManagementClient bpelPackageManagementClient;
    private HumanTaskPackageManagementClient humanTaskPackageManagementClient;
    private BpelInstanceManagementClient instanceManagementClient;
    private HumanTaskClientApiClient humanTaskClientApiClient;
    private UserManagementClient userManagementClient;

    private RequestSender requestSender;
    private ServerConfigurationManager serverConfigurationManager;

    /**
     * Setting up Server and Apply new Configuration Files.
     */
    @BeforeClass(alwaysRun = true)
    public void setupTest() throws Exception {
        //Init Test case server
        log.info("Initializing Test Case");
        init();
        requestSender = new RequestSender();
        userManagementClient = new UserManagementClient(backEndUrl, sessionCookie);
        //1) Deploy BPEL and HumanTask artifacts
        log.info("Deploying Artifacts");
        deployArtifact();
        // 2) Adding Users and Roles
        log.info("Creating uses and roles");
        setupUsersAndRoles();
        // 3) Enable HT-Coordination
        log.info("Enable HumanTask coordination and restarting server.");
        applyCoordinationConfig();
        init();
        requestSender.waitForProcessDeployment(backEndUrl + HumanTaskTestConstants.CLAIM_APPROVAL_PROCESS_SERVICE);
        requestSender.waitForProcessDeployment(backEndUrl + HumanTaskTestConstants.CLAIM_SERVICE);
        log.info("BPEL and Humantask services are up and running");
        // Need to re-initialize since we have restarted the server
        bpelPackageManagementClient = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        humanTaskPackageManagementClient = new HumanTaskPackageManagementClient(backEndUrl, sessionCookie);
        instanceManagementClient = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        log.info("Server setting up completed.");
    }

    public void deployArtifact() throws Exception {

        final String artifactLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + BPSTestConstants.DIR_ARTIFACTS
                + File.separator + BPSTestConstants.DIR_HUMAN_TASK + File.separator + BPSTestConstants.DIR_SCENARIOS
                + File.separator + BPSTestConstants.DIR_HT_COORDINATION;
        uploadBpelForTest(BPSTestConstants.CLAIMS_APPROVAL_PROCESS, artifactLocation);
        uploadHumanTaskForTest(BPSTestConstants.CLAIMS_APPROVAL_TASK, artifactLocation);
    }


    /**
     * Copy humantask.xml and b4p-coordination-config.xml configuration file and restart server
     */
    private void applyCoordinationConfig() throws Exception {
        serverConfigurationManager = new ServerConfigurationManager(backEndUrl);

        final String artifactLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + BPSTestConstants.DIR_CONFIG + File.separator
                + BPSTestConstants.DIR_HT_COORDINATION + File.separator;

        File humantaskConfig = new File(artifactLocation + BPSTestConstants.HUMANTASK_XML);
        serverConfigurationManager.applyConfigurationWithoutRestart(humantaskConfig);
        File b4pConfig = new File(artifactLocation + BPSTestConstants.B4P_COORDINATION_CONFIG_XML);
        serverConfigurationManager.applyConfiguration(b4pConfig);
    }

    /**
     * Add Users and Roles
     */
    private void setupUsersAndRoles() throws Exception {
        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE, null,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"}, false);
        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE, null,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"}, false);
        userManagementClient.addRole(HumanTaskTestConstants.HT_COORDINATOR_ROLE, null,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"}, false);

        //Adding Users
        userManagementClient.addUser(HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK1_PASSWORD,
                new String[]{HumanTaskTestConstants.REGIONAL_CLERKS_ROLE}, null);

        userManagementClient.addUser(HumanTaskTestConstants.CLERK2_USER, HumanTaskTestConstants.CLERK2_PASSWORD,
                new String[]{HumanTaskTestConstants.REGIONAL_CLERKS_ROLE}, null);

        userManagementClient.addUser(HumanTaskTestConstants.MANAGER_USER, HumanTaskTestConstants.MANAGER_PASSWORD,
                new String[]{HumanTaskTestConstants.REGIONAL_MANAGER_ROLE}, null);

        userManagementClient.addUser(HumanTaskTestConstants.HT_COORDINATOR_USER, HumanTaskTestConstants.HT_COORDINATOR_PASSWORD,
                new String[]{HumanTaskTestConstants.HT_COORDINATOR_ROLE}, null);

        assertTrue(validateUsers(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE, "clerk*") == 2,
                "There should be exactly 2 clerks users in the system!");
        assertTrue(validateUsers(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE, "manager*") == 1,
                "The manager was not added to the regional manager's role properly");
        assertTrue(validateUsers(HumanTaskTestConstants.HT_COORDINATOR_ROLE, "htcoor*") == 1,
                "htcoor user was not added to the " + HumanTaskTestConstants.HT_COORDINATOR_ROLE + "'s role properly");

    }

    private int validateUsers(String roleName, String filter) throws Exception {
        int count = 0;
        FlaggedName[] users = userManagementClient.getUsersOfRole(roleName, filter, -1);
        String filterNew = filter.replace("*", ".*");
        for (FlaggedName user : users) {
            if (user.getItemName() != null && user.getItemName().matches(filterNew)) {
                count++;
            }
        }
        return count;
    }

    @Test(groups = {"wso2.bps.task.create"}, description = "Approve Task", priority = 1, singleThreaded = true)
    public void createTaskB4P() throws Exception {
        String soapBody =
                "<cla:ClaimApprovalProcessInput xmlns:cla=\"http://www.wso2.org/humantask/claimsapprovalprocessservice.wsdl\">\n" +
                        "         <cla:custID>Coor1</cla:custID>\n" +
                        "         <cla:custFName>Hasitha</cla:custFName>\n" +
                        "         <cla:custLName>Aravinda</cla:custLName>\n" +
                        "         <cla:amount>5000</cla:amount>\n" +
                        "         <cla:region>LK</cla:region>\n" +
                        "         <cla:priority>4</cla:priority>\n" +
                        "      </cla:ClaimApprovalProcessInput>";

        String operation = "claimsApprovalProcessOperation";
        String serviceName = "ClaimsApprovalProcessService";
        List<String> expectedOutput = Collections.emptyList();
        log.info("Calling Service: " + backEndUrl  +  serviceName);
        requestSender.sendRequest(backEndUrl  +  serviceName, operation, soapBody, 1, expectedOutput, false);
        Thread.sleep(5000);

        instanceManagementClient.listInstances("{http://www.wso2.org/humantask/claimsapprovalprocess.bpel}ClaimsApprovalProcess", 1);

        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.ALL_TASKS);

        //Login As Clerk1 user
        authenticatorClient.logOut();
        String clerk1SessionCookie = authenticatorClient.login(HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK1_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, clerk1SessionCookie);

        TTaskSimpleQueryResultSet taskResults = humanTaskClientApiClient.simpleQuery(queryInput);

        TTaskSimpleQueryResultRow[] rows = taskResults.getRow();
        TTaskSimpleQueryResultRow b4pTask = null;

        Assert.assertNotNull(rows, "No tasks found. Task creation has failed. ");

        // looking for the latest task
        for (TTaskSimpleQueryResultRow row : rows) {
            if (b4pTask == null) {
                b4pTask = row;
            } else {
                if (Long.parseLong(b4pTask.getId().toString()) < Long.parseLong(row.getId().toString())) {
                    b4pTask = row;
                }
            }
        }

        // Validating Task
        Assert.assertNotNull(b4pTask, "Task creation has failed");

        String claimApprovalRequest = (String) humanTaskClientApiClient.getInput(b4pTask.getId(), null);

        Assert.assertNotNull(claimApprovalRequest, "The input of the Task:" + b4pTask.getId() + " is null.");

        Assert.assertFalse(!claimApprovalRequest.contains("Coor1"), "Unexpected input found for the Task");

        // Perform Task Operation
        // Claim and Start and Complete.
        humanTaskClientApiClient.claim(b4pTask.getId());
        humanTaskClientApiClient.start(b4pTask.getId());
        humanTaskClientApiClient.complete(b4pTask.getId(), "<sch:ClaimApprovalResponse xmlns:sch=\"http://www.example.com/claims/schema\">\n" +
                "         <sch:approved>true</sch:approved>\n" +
                "      </sch:ClaimApprovalResponse>");

        Thread.sleep(5000);
        List<String> instances = instanceManagementClient.listInstances("{http://www.wso2.org/humantask/claimsapprovalprocess.bpel}ClaimsApprovalProcess" , 1);

        Assert.assertTrue(instances.size() == 1, "Number of process instances not equal to one.");
        log.info("Waiting for Process instance to Complete.");
        InstanceInfoType instanceInfo = null;
        boolean isInstanceCompleted = false;
        for (int count = 0; count < 100; count++) {
            Thread.sleep(5000);
            instanceInfo = instanceManagementClient.getInstanceInfo(instances.get(0));
            if (instanceInfo.getStatus().getValue().equals("COMPLETED")) {
                isInstanceCompleted = true;
                log.info("Instance COMPLETED");
                break;
            }
        }
        Assert.assertTrue(isInstanceCompleted, "Status of instance " + instances.get(0) + " is not equal to COMPLETED");
        instanceManagementClient.deleteAllInstances();

    }


    @AfterClass(alwaysRun = true, description = "Unload packages after test.")
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException, org.wso2.carbon.humantask.stub.mgt.PackageManagementException {
        bpelPackageManagementClient.undeployBPEL("ClaimsApprovalProcess");
        humanTaskPackageManagementClient.unDeployHumanTask("ClaimsApprovalTask", "ApproveClaim");
        authenticatorClient.logOut();
    }


}
