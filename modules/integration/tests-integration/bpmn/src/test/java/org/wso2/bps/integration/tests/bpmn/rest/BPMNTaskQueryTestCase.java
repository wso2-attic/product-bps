/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License, 
 * Version 2.0 (the "License"); you may not use this file except 
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.wso2.bps.integration.tests.bpmn.rest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.WorkflowServiceClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.bps.integration.tests.bpmn.BPMNTestUtils;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.user.mgt.stub.UserAdminUserAdminException;

import java.io.IOException;
import java.rmi.RemoteException;

/**
 * Test case for get tasks with runtime/tasks?candidateOrAssigned=<username> request
 */
public class BPMNTaskQueryTestCase extends BPSMasterTest {
    private static final Log log = LogFactory.getLog(BPMNTaskQueryTestCase.class);

//    private ServerConfigurationManager serverConfigurationManager;
    private UserManagementClient userManagementClient;
    private WorkflowServiceClient workflowServiceClient;

    private static final String USER1 = "testUser1";
    private static final String USER2 = "testUser2";
    private static final String SUBSTTUTER_ROLE = "subRole"; //has substitute permission
    private static final String NON_SUB_ROLE = "nonSubRole"; //has login permission only
    public static final String SUBSTITUTION_PERMISSION_PATH = "/permission/admin/manage/bpmn/substitute";
    public static final String LOGIN_PERMISSION_PATH = "/permission/admin/login";
    public static final String PROCESS_NAME = "UserTaskProcess";
    public static final String PROCESS_KEY = "userTaskProcess";

    @BeforeTest(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        userManagementClient = new UserManagementClient(backEndUrl, sessionCookie);
        workflowServiceClient = new WorkflowServiceClient(backEndUrl, sessionCookie);
        addRoles();
    }

    @Test(groups = {
            "wso2.bps.bpmn.rest" }, description = "get tasks with candidateOrAssigned param", priority = 1,
          singleThreaded = true)
    public void candidateOrAssignendTaskQueryTest() throws Exception {
        addUser(USER1,new String[]{SUBSTTUTER_ROLE});
        uploadBPMNForTest(PROCESS_NAME);
        BPMNTestUtils.waitForProcessDeployment(workflowServiceClient, PROCESS_NAME, 0);
        for (int i=0; i < 5; i++) {
            startProcessInstance(USER2, "admin", "-1234"); // will create a task for user2
        }

        //get tasks with assignee USER2
        int queryTaskCount = findTasksWithGivenAssignee(USER2).getInt("total");

        //get tasks with candidateOrAssigned url parameter for USER2
        String queryParam = "?" + "candidateOrAssigned=" + USER2;
        HttpResponse taskQueryResponse = BPMNTestUtils.getRequestResponse(backEndUrl + "runtime/tasks" + queryParam);
        int taskCount = (new JSONObject(EntityUtils.toString(taskQueryResponse.getEntity()))).getInt("total");
        Assert.assertTrue(taskCount >= queryTaskCount, "GET runtime/tasks with candidateOrAssigned param did not "
                + "return expected "
                + "task count");
    }

    private JSONObject findTasksWithGivenAssignee(String assignee) throws JSONException, IOException {
        String payload = "{" + "\"assignee\":\"" + assignee + "\"" + "}";
        HttpResponse taskQueryResponse = BPMNTestUtils.postRequest(backEndUrl + "query/tasks", new JSONObject(payload));
        return new JSONObject(EntityUtils.toString(taskQueryResponse.getEntity()));
    }

    //add a new user with given roles
    private void addUser(String user, String[] role) {
        try {
            userManagementClient.addUser(user, user, role, "test");
        } catch (RemoteException | UserAdminUserAdminException e) {
            log.error("Error adding new user for testing", e);
        }
    }

    private void addRoles() {
        try {
            userManagementClient.addRole(SUBSTTUTER_ROLE, null, new String[]{SUBSTITUTION_PERMISSION_PATH, LOGIN_PERMISSION_PATH});
            userManagementClient.addRole(NON_SUB_ROLE, null, new String[]{LOGIN_PERMISSION_PATH});
        } catch (RemoteException | UserAdminUserAdminException e) {
            log.error("Error adding a new role.", e);
        }
    }

    /**
     * Starting a UserTask process inatance. Sample request payload structure
     * {
     *  "processDefinitionKey":"userTaskProcess",
     *  "tenantId": "-1234",
     *  "variables": [
     *                  {
     *                      "name":"user",
     *                      "value":"admin"
     *                   },
     *                  {
     *                      "name":"role",
     *                      "value":"admin"
     *                  }
     *              ]
     * }
     * @param user
     * @param role
     */
    private void startProcessInstance(String user, String role, String tenantId) throws JSONException, IOException {
        String arrayString = "[{\"name\":\"user\", \"value\":\"" + user + "\"}, {\"name\":\"role\",\"value\":\"" + role + "\"}]";
        JSONArray varArray = new JSONArray(arrayString);

        JSONObject payload = new JSONObject();
        payload.put("variables", varArray);
        payload.put("tenantId", tenantId);
        payload.put("processDefinitionKey", PROCESS_KEY);

        BPMNTestUtils.postRequest(backEndUrl + "runtime/process-instances", payload);
    }

    @AfterClass(alwaysRun = true)
    public void cleanServer () throws Exception {
        workflowServiceClient.undeploy(PROCESS_NAME);
    }
}
