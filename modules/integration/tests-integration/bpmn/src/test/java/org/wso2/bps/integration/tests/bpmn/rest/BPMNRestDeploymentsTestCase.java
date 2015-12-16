/*
* Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import junit.framework.Assert;
import org.activiti.engine.ProcessEngine;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.WorkflowServiceClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.bps.integration.tests.bpmn.BPMNTestUtils;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNDeployment;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNProcess;

public class BPMNRestDeploymentsTestCase extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BPMNRestDeploymentsTestCase.class);
    private WorkflowServiceClient workflowServiceClient;
    String domainKey1 = "wso2.com";
    String userKey1 = "user1";
    private String deploymentUrl = "repository/deployments";

    @BeforeTest
    public void init() throws Exception {
        super.init();
        workflowServiceClient = new WorkflowServiceClient(backEndUrl, sessionCookie);
        loginLogoutClient.login();
    }

    /**
     * Test getting deployments list with GET repository/deployments.
     * Depends on deployments counts, need to be no previous deployments.
     * Required resources : oneTaskProcess.bar,HelloApprove.bar, VacationRequest.bar
     * @throws Exception
     */
    @Test
    public void testGetDeployments() throws Exception {

        try {
            // Deploy artifacts and wait for deployment end
            uploadBPMNForTest("oneTaskProcess");
            BPMNTestUtils.waitForProcessDeployment(workflowServiceClient, "oneTaskProcess", 0);
            uploadBPMNForTest("HelloApprove");
            BPMNTestUtils.waitForProcessDeployment(workflowServiceClient, "HelloApprove", 1);

            //HTTP get request
            String result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl);
            JSONObject jsonObject = new JSONObject(result);

            //check result count
            Assert.assertEquals("repository/deployments:total test", "2", jsonObject.getString("total"));
            Assert.assertEquals("repository/deployments:size test", "2", jsonObject.getString("size"));

            String deploymentName = ((JSONObject) ((JSONArray) jsonObject.get("data")).get(0)).getString("name");
            String deploymentName2 = ((JSONObject) ((JSONArray) jsonObject.get("data")).get(1)).getString("name");

            //check all the deployments returned
            Assert.assertEquals("repository/deployments:data:name test", true,
                    deploymentName.equals("oneTaskProcess") || deploymentName.equals("HelloApprove"));
            Assert.assertEquals("repository/deployments:data:name test", true,
                    deploymentName2.equals("oneTaskProcess") || deploymentName2.equals("HelloApprove"));

            //test name filter
            result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "?name=" + "oneTaskProcess");
            jsonObject = new JSONObject(result);
            deploymentName = ((JSONObject) ((JSONArray) jsonObject.get("data")).get(0)).getString("name");

            Assert.assertEquals("repository/deployments name query test", "1", jsonObject.getString("total"));
            Assert.assertEquals("repository/deployments name query test", "oneTaskProcess", deploymentName);

            //test nameLike filter
            result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "?nameLike=" + "Task");
            jsonObject = new JSONObject(result);
            //deploymentName = ((JSONObject)((JSONArray)jsonObject.get("data")).get(0)).getString("name");

            //Assert.assertEquals("repository/deployments nameLike query test", "1", jsonObject.getString("total"));
            //Assert.assertEquals("repository/deployments nameLike query test", "oneTaskProcess", deploymentName);

            //init new domain
            super.init(domainKey1, userKey1);
            loginLogoutClient.login();
            workflowServiceClient = new WorkflowServiceClient(backEndUrl, sessionCookie);
            uploadBPMNForTest("VacationRequest");
            BPMNTestUtils.waitForProcessDeployment(workflowServiceClient, "VacationRequest", 0);
            //test tenantId
            result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "?tenantId=" + "1");
            jsonObject = new JSONObject(result);
            String tenantId = ((JSONObject) ((JSONArray) jsonObject.get("data")).get(0)).getString("tenantId");
            deploymentName = ((JSONObject) ((JSONArray) jsonObject.get("data")).get(0)).getString("name");

            //new tenant id should be 1
            Assert.assertEquals("repository/deployments tenantId query test", "1", jsonObject.getString("total"));
            Assert.assertEquals("repository/deployments tenantId query test", "1", tenantId);
            Assert.assertEquals("repository/deployments tenantId query test", "VacationRequest", deploymentName);

            init();
            loginLogoutClient.login();

            //test tenantIdLike
            //                result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "?tenantIdLike=34");
            //                jsonObject = new JSONObject(result);
            //                tenantId = ((JSONObject)((JSONArray)jsonObject.get("data")).get(0)).getString("tenantId");
            //
            //                Assert.assertEquals("repository/deployments tenantId query test", "2", jsonObject.getString("total"));
            //                Assert.assertEquals("repository/deployments tenantId query test", "-1234", tenantId);

            //test withoutTenantId
            result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "?withoutTenantId=true");
            jsonObject = new JSONObject(result);

            //should not return any, since we always set tenantId
            Assert.assertEquals("repository/deployments withoutTenantId query test", "0",
                    jsonObject.getString("total"));

            // Check ordering by name asc
            result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "?sort=name&order=asc");
            jsonObject = new JSONObject(result);

            //1.HelloApprove,2.OneTaskProcess,3.VacationRequest
            Assert.assertEquals("repository/deployments sort=name,order=asc query test", "HelloApprove",
                    ((JSONObject) ((JSONArray) jsonObject.get("data")).get(0)).getString("name"));
            Assert.assertEquals("repository/deployments sort=name,order=asc query test", "VacationRequest",
                    ((JSONObject) ((JSONArray) jsonObject.get("data")).get(1)).getString("name"));
            Assert.assertEquals("repository/deployments sort=name,order=asc query test", "oneTaskProcess",
                    ((JSONObject) ((JSONArray) jsonObject.get("data")).get(2)).getString("name"));

            // Check ordering by name dec
            result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "?sort=name&order=desc");
            jsonObject = new JSONObject(result);

            //1.VacationRequest,2.OneTaskProcess,3.HelloApprove
            Assert.assertEquals("repository/deployments sort=name,order=asc query test", "oneTaskProcess",
                    ((JSONObject) ((JSONArray) jsonObject.get("data")).get(0)).getString("name"));
            Assert.assertEquals("repository/deployments sort=name,order=asc query test", "VacationRequest",
                    ((JSONObject) ((JSONArray) jsonObject.get("data")).get(1)).getString("name"));
            Assert.assertEquals("repository/deployments sort=name,order=asc query test", "HelloApprove",
                    ((JSONObject) ((JSONArray) jsonObject.get("data")).get(2)).getString("name"));

            // Check paging
            result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "?sort=name&order=asc&start=1&size=2");
            jsonObject = new JSONObject(result);
            Assert.assertEquals("repository/deployments paging query test", "2", jsonObject.getString("size"));
            Assert.assertEquals("repository/deployments paging query test", "VacationRequest",
                    ((JSONObject) ((JSONArray) jsonObject.get("data")).get(0)).getString("name"));
            Assert.assertEquals("repository/deployments paging query test", "oneTaskProcess",
                    ((JSONObject) ((JSONArray) jsonObject.get("data")).get(1)).getString("name"));

        } finally {
            // Always cleanup any created deployments, even if the test failed
            workflowServiceClient.undeploy("oneTaskProcess");
            workflowServiceClient.undeploy("HelloApprove");
            super.init(domainKey1, userKey1);
            workflowServiceClient = new WorkflowServiceClient(backEndUrl, sessionCookie);
            loginLogoutClient.login();
            workflowServiceClient.undeploy("VacationRequest");
        }
    }

    /**
     * Test getting a single resource, deployed in a deployment.
     * GET repository/deployments/{deploymentId}/resources/{resourceId}
     */
    @Test
    public void testGetSingleDeployment() throws Exception {
        try {
            uploadBPMNForTest("oneTaskProcess");
            BPMNTestUtils.waitForProcessDeployment(workflowServiceClient, "oneTaskProcess", 0);

            //get deployments by id
            BPMNDeployment deployment = workflowServiceClient.getDeployments()[0];
            String result = BPMNTestUtils.getRequest(backEndUrl + deploymentUrl + "/" + deployment.getDeploymentId());
            JSONObject jsonObject = new JSONObject(result);

            Assert.assertEquals("repository/deployments/{deploymentId} test", deployment.getDeploymentId(),
                    jsonObject.get("id"));

            //get deployments resource by id
            result = BPMNTestUtils
                    .getRequest(backEndUrl + deploymentUrl + "/" + deployment.getDeploymentId() + "/resources");
            jsonObject = new JSONObject(result);

            //only the processDefinition and diagram resources should be returned
            Assert.assertEquals("repository/deployments/{deploymentId}/resources test", 2,
                    ((JSONArray) jsonObject.get("deploymentResourceResponseList")).length());
            Assert.assertEquals("repository/deployments/{deploymentId}/resources test", "processDefinition",
                    ((JSONObject) ((JSONArray) jsonObject.get("deploymentResourceResponseList")).get(0))
                            .getString("type"));

            //get resource by id
            result = BPMNTestUtils.getRequest(
                    backEndUrl + deploymentUrl + "/" + deployment.getDeploymentId() + "/resources/"
                            + "oneTaskProcess.bpmn20.xml");
            jsonObject = new JSONObject(result);

            //check the id of the result
            Assert.assertEquals("repository/deployments/{deploymentId}/resources/{resourceId} test",
                    "oneTaskProcess.bpmn20.xml", jsonObject.getString("id"));
        } finally {
            workflowServiceClient.undeploy("oneTaskProcess");
        }

    }

    /**
     * Test getting process definitions.
     * GET repository/process-definitions
     */
    @Test
    public void testGetProcessDefinitions() throws Exception {
        try {
            uploadBPMNForTest("oneTaskProcess");
            BPMNTestUtils.waitForProcessDeployment(workflowServiceClient, "oneTaskProcess", 0);

            BPMNProcess[] bpmnProcesses = workflowServiceClient.getProcesses();

            String result = BPMNTestUtils.getRequest(backEndUrl + "repository/process-definitions");
            JSONObject jsonObject = new JSONObject(result);

            Assert.assertEquals("repository/process-definitions test", bpmnProcesses.length,
                    Integer.parseInt(jsonObject.getString("total")));

            result = BPMNTestUtils
                    .getRequest(backEndUrl + "repository/process-definitions/" + bpmnProcesses[0].getProcessId());
            jsonObject = new JSONObject(result);

            Assert.assertEquals("repository/process-definitions/{definitionId} test", bpmnProcesses[0].getProcessId(),
                    jsonObject.getString("id"));

            result = BPMNTestUtils.getRequest(
                    backEndUrl + "repository/process-definitions/" + bpmnProcesses[0].getProcessId() + "/resourcedata");
            //check whether some content returned, ideally xml content
            Assert.assertNotNull(result);
            //result = BPMNTestUtils.getRequest(backEndUrl + "repository/process-definitions/" + bpmnProcesses[0].getProcessId() + "/model");
            //jsonObject = new JSONObject(result);
            //repository/process-definitions/{processDefinitionId}/model not implemented

            result = BPMNTestUtils.getRequest(
                    backEndUrl + "repository/process-definitions/" + bpmnProcesses[0].getProcessId()
                            + "/identitylinks");
            result = "{resultArray:" + result + "}";
            jsonObject = new JSONObject(result);

            //should return kermit user and admin group
            Assert.assertEquals("repository/process-definitions/{definitionId}/identitylinks test", 2,
                    ((JSONArray) ((jsonObject).get("resultArray"))).length());
            Assert.assertTrue("repository/process-definitions/{definitionId}/identitylinks test",
                    jsonObject.toString().contains("\"user\":\"kermit\""));
            Assert.assertTrue("repository/process-definitions/{definitionId}/identitylinks test",
                    jsonObject.toString().contains("\"group\":\"admin\""));

            result = BPMNTestUtils.getRequest(
                    backEndUrl + "repository/process-definitions/" + bpmnProcesses[0].getProcessId() + "/identitylinks"
                            + "/users/kermit");
            jsonObject = new JSONObject(result);

            Assert.assertEquals(
                    "repository/process-definitions/{definitionId}/identitylinks/{familly}/{identityId} test", "kermit",
                    jsonObject.getString("user"));

            result = BPMNTestUtils.getRequest(
                    backEndUrl + "repository/process-definitions/" + bpmnProcesses[0].getProcessId() + "/identitylinks"
                            + "/groups/admin");
            jsonObject = new JSONObject(result);

            Assert.assertEquals(
                    "repository/process-definitions/{definitionId}/identitylinks/{familly}/{identityId} test", "admin",
                    jsonObject.getString("group"));
        } finally {
            workflowServiceClient.undeploy("oneTaskProcess");
        }

    }

}
