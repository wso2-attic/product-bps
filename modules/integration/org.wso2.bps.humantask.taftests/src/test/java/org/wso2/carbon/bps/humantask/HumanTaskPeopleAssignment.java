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

package org.wso2.carbon.bps.humantask;

import org.apache.axis2.AxisFault;
import org.apache.axis2.databinding.types.URI;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeGroups;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.bps.integration.core.BPSMasterTest;
import org.wso2.bps.integration.core.BPSTestConstants;
import org.wso2.carbon.automation.api.clients.humantask.HumanTaskClientApiClient;
import org.wso2.carbon.automation.api.clients.humantask.HumanTaskPackageManagementClient;
import org.wso2.carbon.automation.api.clients.user.mgt.UserManagementClient;
import org.wso2.carbon.automation.core.ProductConstant;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.humantask.stub.ui.task.client.api.types.*;
import org.wso2.carbon.user.mgt.stub.types.carbon.FlaggedName;

import java.io.File;
import java.math.BigInteger;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertTrue;

public class HumanTaskPeopleAssignment extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(HumanTaskPeopleAssignment.class);

    //Test Automation API Clients
    private HumanTaskPackageManagementClient humanTaskPackageManagementClient;
    private HumanTaskClientApiClient humanTaskClientApiClient;
    private UserManagementClient userManagementClient;

    private RequestSender requestSender;

    private URI taskID = null;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        init();  //init master class
        humanTaskPackageManagementClient = new HumanTaskPackageManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
        initialize();
    }

    @BeforeGroups(groups = {"wso2.bps.task.people.assignment"})
    protected void initialize() throws Exception {
        log.info("Initializing HumanTask task creation Test...");
        userManagementClient = new UserManagementClient(backEndUrl, sessionCookie);
        addUsers();
        addRoles();
        humanTaskPackageManagementClient = new HumanTaskPackageManagementClient(backEndUrl, sessionCookie);
        log.info("Add users success !");
        deployArtifact();
        requestSender.waitForProcessDeployment(backEndUrl + HumanTaskTestConstants.CLAIM_SERVICE);
    }

    /**
     * deployArtifact() test1 sample Generic Human Roles. potentialOwners - htd:getInput("ClaimApprovalRequest")/test10:cust/test10:owners
     * businessAdministrators - htd:union(htd:getInput("ClaimApprovalRequest")/test10:cust/test10:globleAdmins,htd:getInput("ClaimApprovalRequest")/test10:cust/test10:regionalAdmins)
     * excludedOwners - htd:getInput("ClaimApprovalRequest")/test10:cust/test10:excludedOwners
     */
    public void deployArtifact() throws Exception {
        final String artifactLocation = ProductConstant.SYSTEM_TEST_RESOURCE_LOCATION + BPSTestConstants.DIR_ARTIFACTS
                + File.separator + BPSTestConstants.DIR_HUMAN_TASK + File.separator + HumanTaskTestConstants.DIR_PEOPLE_ASSIGNMENT
                + File.separator + "test1";
        uploadHumanTaskForTest(HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_ORG_ENTITY_NAME, artifactLocation);
    }

    private void addUsers()
            throws Exception {
        userManagementClient.addUser(HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK1_PASSWORD, null, null);
        userManagementClient.addUser(HumanTaskTestConstants.CLERK2_USER, HumanTaskTestConstants.CLERK2_PASSWORD, null, null);
        userManagementClient.addUser(HumanTaskTestConstants.CLERK3_USER, HumanTaskTestConstants.CLERK3_PASSWORD, null, null);
        userManagementClient.addUser(HumanTaskTestConstants.CLERK4_USER, HumanTaskTestConstants.CLERK4_PASSWORD, null, null);
        userManagementClient.addUser(HumanTaskTestConstants.CLERK5_USER, HumanTaskTestConstants.CLERK5_PASSWORD, null, null);
        userManagementClient.addUser(HumanTaskTestConstants.CLERK6_USER, HumanTaskTestConstants.CLERK6_PASSWORD, null, null);

        //Managers
        userManagementClient.addUser(HumanTaskTestConstants.MANAGER_USER, HumanTaskTestConstants.MANAGER_PASSWORD, null, null);
        userManagementClient.addUser(HumanTaskTestConstants.MANAGER2_USER, HumanTaskTestConstants.MANAGER2_PASSWORD, null, null);
        userManagementClient.addUser(HumanTaskTestConstants.MANAGER3_USER, HumanTaskTestConstants.MANAGER3_PASSWORD, null, null);

        assertTrue(validateUsers(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE, "clerk*") == 6,
                "There should be exactly 2 clerks users in the system!");
        assertTrue(validateUsers(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE, "manager*") == 3,
                "The manager was not added to the regional manager's role properly");
    }

    private void addRoles() throws Exception {
        String[] rc1 = new String[]{HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK2_USER, HumanTaskTestConstants.CLERK3_USER};
        String[] rc2 = new String[]{HumanTaskTestConstants.CLERK3_USER, HumanTaskTestConstants.CLERK4_USER, HumanTaskTestConstants.CLERK5_USER};
        String[] rc3 = new String[]{HumanTaskTestConstants.CLERK4_USER, HumanTaskTestConstants.CLERK5_USER, HumanTaskTestConstants.CLERK6_USER};
        String[] rm1 = new String[]{HumanTaskTestConstants.MANAGER_USER, HumanTaskTestConstants.MANAGER2_USER};
        String[] rm2 = new String[]{HumanTaskTestConstants.MANAGER2_USER, HumanTaskTestConstants.MANAGER3_USER};

        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE, rc1,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"}, false);
        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE_2, rc2,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"}, false);
        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE_3, rc3,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"}, false);
        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE, rm1,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask"}, false);
        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE_2, rm2,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask"}, false);
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


    @Test(groups = {"wso2.bps.task.createTask"}, description = "Create Task 1", priority = 1, singleThreaded = true)
    public void createTask() throws Exception {
        String soapBody =
                "<sch:ClaimApprovalData xmlns:sch=\"http://www.example.com/claims/schema\" xmlns:ns=\"http://docs.oasis-open.org/ns/bpel4people/ws-humantask/types/200803\">\n" +
                        "         <sch:cust>\n" +
                        "            <sch:id>123</sch:id>\n" +
                        "            <sch:firstname>Hasitha</sch:firstname>\n" +
                        "            <sch:lastname>Aravinda</sch:lastname>\n" +
                        "            <sch:owners>\n" +
                        "               <ns:group>" + HumanTaskTestConstants.REGIONAL_CLERKS_ROLE + "</ns:group>\n" +
                        "            </sch:owners>\n" +
                        "            <sch:excludedOwners>\n" +
                        "               <ns:user>" + HumanTaskTestConstants.CLERK3_USER + "</ns:user>\n" +
                        "            </sch:excludedOwners>\n" +
                        "            <sch:globleAdmins>\n" +
                        "               <ns:group>" + HumanTaskTestConstants.REGIONAL_MANAGER_ROLE + "</ns:group>\n" +
                        "            </sch:globleAdmins>\n" +
                        "            <sch:regionalAdmins>\n" +
                        "               <ns:group>" + HumanTaskTestConstants.REGIONAL_MANAGER_ROLE_2 + "</ns:group>\n" +
                        "            </sch:regionalAdmins>\n" +
                        "         </sch:cust>\n" +
                        "         <sch:amount>2500</sch:amount>\n" +
                        "         <sch:region>lk</sch:region>\n" +
                        "         <sch:priority>7</sch:priority>\n" +
                        "      </sch:ClaimApprovalData>";
        String operation = "approve";
        String serviceName = "ClaimService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("taskid>");
        log.info("Calling Service: " + backEndUrl + serviceName);
        requestSender.sendRequest(backEndUrl + serviceName, operation, soapBody, 1, expectedOutput, true);
    }

    @Test(groups = {"wso2.bps.task.createTask"}, description = "Check created Task", priority = 2, singleThreaded = true)
    public void checkCreatedTask() throws Exception {
        //Clerk1 can claim this task.
        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.CLAIMABLE);

        //Login As Clerk1 user
        authenticatorClient.logOut();
        String clerk1SessionCookie = authenticatorClient.login(HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK1_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, clerk1SessionCookie);
        TTaskSimpleQueryResultSet taskResults = humanTaskClientApiClient.simpleQuery(queryInput);

        TTaskSimpleQueryResultRow[] rows = taskResults.getRow();

        Assert.assertNotNull(rows, "No tasks found. Task creation has failed. ");
        Assert.assertTrue(rows.length == 1, "There should be only one claimable task in the engine, but found " + rows.length + " tasks.");
    }

    @Test(groups = {"wso2.bps.task.claim"}, description = "Clerk1 claim task", priority = 3, singleThreaded = true)
    public void clerk1Claim() throws Exception {
        //Clerk1 can claim this task.
        TSimpleQueryInput queryInput = new TSimpleQueryInput();
        queryInput.setPageNumber(0);
        queryInput.setSimpleQueryCategory(TSimpleQueryCategory.CLAIMABLE);

        //Login As Clerk1 user
        authenticatorClient.logOut();
        String clerk1SessionCookie = authenticatorClient.login(HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK1_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, clerk1SessionCookie);
        TTaskSimpleQueryResultSet taskResults = humanTaskClientApiClient.simpleQuery(queryInput);

        TTaskSimpleQueryResultRow[] rows = taskResults.getRow();
        TTaskSimpleQueryResultRow b4pTask = rows[0];
        this.taskID = b4pTask.getId();
        humanTaskClientApiClient.claim(taskID);

        TTaskAbstract loadedTask = humanTaskClientApiClient.loadTask(taskID);
        Assert.assertEquals(loadedTask.getActualOwner().getTUser(), HumanTaskTestConstants.CLERK1_USER,
                "The assignee should be clerk1 !");
        Assert.assertEquals(loadedTask.getStatus().toString(), "RESERVED",
                "The task status should be RESERVED!");
    }

    @Test(groups = {"wso2.bps.task.claim"}, description = "Clerk2 claim task which is RESERVED", priority = 4, singleThreaded = true, expectedExceptions = AxisFault.class)
    public void clerk2Claim() throws Exception {
        //Clerk2 can't claim this task since clerk1 already claimed it.
        //Login As Clerk2 user
        authenticatorClient.logOut();
        String sessionCookie = authenticatorClient.login(HumanTaskTestConstants.CLERK2_USER, HumanTaskTestConstants.CLERK2_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, sessionCookie);
        humanTaskClientApiClient.claim(this.taskID);
    }

    @Test(groups = {"wso2.bps.task.claim"}, description = "Clerk1 release task", priority = 5, singleThreaded = true)
    public void clerk1Release() throws Exception {
        //Login As Clerk2 user
        authenticatorClient.logOut();
        String sessionCookie = authenticatorClient.login(HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK1_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, sessionCookie);
        humanTaskClientApiClient.release(this.taskID);
        TTaskAbstract loadedTask = humanTaskClientApiClient.loadTask(taskID);
        Assert.assertNull(loadedTask.getActualOwner(), "Task has an actual owner. Task Release failed");
        Assert.assertEquals(loadedTask.getStatus().toString(), "READY", "The task status should be READY!");
    }

    @Test(groups = {"wso2.bps.task.claim"}, description = "Clerk2 re-claim task and release", priority = 6, singleThreaded = true)
    public void clerk2ReClaimAndRelease() throws Exception {
        //Login As Clerk2 user
        authenticatorClient.logOut();
        String sessionCookie = authenticatorClient.login(HumanTaskTestConstants.CLERK2_USER, HumanTaskTestConstants.CLERK2_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, sessionCookie);
        humanTaskClientApiClient.claim(this.taskID);
        TTaskAbstract loadedTask = humanTaskClientApiClient.loadTask(taskID);
        Assert.assertEquals(loadedTask.getActualOwner().getTUser(), HumanTaskTestConstants.CLERK2_USER,
                "The assignee should be clerk2 !");
        Assert.assertEquals(loadedTask.getStatus().toString(), "RESERVED",
                "The task status should be RESERVED!");
        humanTaskClientApiClient.release(this.taskID);
        loadedTask = humanTaskClientApiClient.loadTask(taskID);
        Assert.assertNull(loadedTask.getActualOwner(), "Task has an actual owner. Task Release failed");
        Assert.assertEquals(loadedTask.getStatus().toString(), "READY", "The task status should be READY!");
    }

    @Test(groups = {"wso2.bps.task.claim"}, description = "Clerk3 (an excluded owner) try to claim", priority = 7, singleThreaded = true, expectedExceptions = AxisFault.class)
    public void clerk3Claim() throws Exception {
        //Login As Clerk3 user
        authenticatorClient.logOut();
        String sessionCookie = authenticatorClient.login(HumanTaskTestConstants.CLERK3_USER, HumanTaskTestConstants.CLERK3_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, sessionCookie);
        humanTaskClientApiClient.claim(this.taskID);
    }

    //TODO test for viewing task

    @Test(groups = {"wso2.bps.task.xpath.union"}, description = "Test Xpath operation -Union", priority = 10, singleThreaded = true)
    public void testUnion() throws Exception {
        // All 3 manager users should able to perform administrative task.

        //Login As manager user
        authenticatorClient.logOut();
        String sessionCookie = authenticatorClient.login(HumanTaskTestConstants.MANAGER_USER, HumanTaskTestConstants.MANAGER_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, sessionCookie);
        TPriority tPriority = new TPriority();
        tPriority.setTPriority(BigInteger.valueOf(2));
        humanTaskClientApiClient.setPriority(taskID, tPriority);

        TTaskAbstract taskAfterPriorityChange1 = humanTaskClientApiClient.loadTask(taskID);
        TPriority prio1 = taskAfterPriorityChange1.getPriority();
        int newPriority1Int = prio1.getTPriority().intValue();
        Assert.assertEquals(newPriority1Int, 2, "The new priority should be 2 after the set priority operation");

        //Login As manager3 user
        authenticatorClient.logOut();
        sessionCookie = authenticatorClient.login(HumanTaskTestConstants.MANAGER3_USER, HumanTaskTestConstants.MANAGER3_PASSWORD, "localhost");
        humanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, sessionCookie);
        tPriority = new TPriority();
        tPriority.setTPriority(BigInteger.valueOf(3));
        humanTaskClientApiClient.setPriority(taskID, tPriority);

        taskAfterPriorityChange1 = humanTaskClientApiClient.loadTask(taskID);
        TPriority prio2 = taskAfterPriorityChange1.getPriority();
        int newPriority1Int2 = prio2.getTPriority().intValue();
        Assert.assertEquals(newPriority1Int2, 3, "The new priority should be 3 after the set priority operation");
    }

    @Test(groups = {"wso2.bps.task.clean"}, description = "Clean up server", priority = 100, singleThreaded = true)
    public void cleanTestEnvironment() throws Exception {
        userManagementClient.deleteUser(HumanTaskTestConstants.CLERK1_USER);
        userManagementClient.deleteUser(HumanTaskTestConstants.CLERK2_USER);
        userManagementClient.deleteUser(HumanTaskTestConstants.CLERK3_USER);
        userManagementClient.deleteUser(HumanTaskTestConstants.CLERK4_USER);
        userManagementClient.deleteUser(HumanTaskTestConstants.CLERK5_USER);
        userManagementClient.deleteUser(HumanTaskTestConstants.CLERK6_USER);
        userManagementClient.deleteUser(HumanTaskTestConstants.MANAGER_USER);
        userManagementClient.deleteUser(HumanTaskTestConstants.MANAGER2_USER);
        userManagementClient.deleteUser(HumanTaskTestConstants.MANAGER3_USER);
        userManagementClient.deleteRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE);
        userManagementClient.deleteRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE_2);
        userManagementClient.deleteRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE_3);
        userManagementClient.deleteRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE);
        userManagementClient.deleteRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE_2);
        Assert.assertFalse(userManagementClient.roleNameExists(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE));
        Assert.assertFalse(userManagementClient.roleNameExists(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE_2));
        Assert.assertFalse(userManagementClient.roleNameExists(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE_3));
        Assert.assertFalse(userManagementClient.roleNameExists(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE));
        Assert.assertFalse(userManagementClient.roleNameExists(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE_2));
        humanTaskPackageManagementClient.unDeployHumanTask(HumanTaskTestConstants.CLAIMS_APPROVAL_PACKAGE_ORG_ENTITY_NAME, "ApproveClaim");
        authenticatorClient.logOut();
    }

}
