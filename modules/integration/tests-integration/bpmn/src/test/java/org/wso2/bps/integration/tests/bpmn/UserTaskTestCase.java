/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import junit.framework.Assert;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.ActivitiRestClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;
import java.io.IOException;


public class UserTaskTestCase extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(UserTaskTestCase.class);

    @Test(groups = {"wso2.bps.test.usertasks"}, description = "User Task Test", priority = 1, singleThreaded = true)
    public void UserTaskTestCase() throws Exception {
        init();
        ActivitiRestClient tester = new ActivitiRestClient(bpsServer.getInstance().getPorts().get("http"), bpsServer.getInstance().getHosts().get("default"));
        //deploying Package
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + File.separator
                          + BPMNTestConstants.DIR_ARTIFACTS + File.separator
                          + BPMNTestConstants.DIR_BPMN + File.separator + "AssigneeIsEmpty.bar";

        String fileName = "AssigneeIsEmpty.bar";
        String[] deploymentResponse = {};


        try {
            deploymentResponse = tester.deployBPMNPackage(filePath, fileName);
            Assert.assertTrue("Deployment Successful", deploymentResponse[0].contains(BPMNTestConstants.CREATED));
        } catch (Exception exception) {
            log.error("Failed to Deploy BPMN Package " + fileName, exception);
            Assert.fail("Failed to Deploy BPMN Package " + fileName);
        }

        try {
            String[] deploymentCheckResponse = tester.getDeploymentInfoById(deploymentResponse[1]);
            Assert.assertTrue("Deployment Present", deploymentCheckResponse[2].contains(fileName));
        } catch (Exception exception) {
            log.error("Deployed BPMN Package " + fileName + " was not found ", exception);
            Assert.fail("Deployed BPMN Package " + fileName + " was not found ");
        }


        //Acquiring Process Definition ID to start Process Instance
        String[] definitionResponse = new String[0];
        try {
            definitionResponse = tester.findProcessDefinitionInfoById(deploymentResponse[1]);
            Assert.assertTrue("Search Success", definitionResponse[0].contains(BPMNTestConstants.OK));
        } catch (Exception exception) {
            log.error("Could not find Defintion ID for BPMN Package " + fileName, exception);
            Assert.fail("Could not find Defintion ID for BPMN Package " + fileName);
        }


        //Starting and Verifying Process Instance
        String[] processInstanceResponse = new String[0];
        try {
            processInstanceResponse = tester.startProcessInstanceByDefintionID(definitionResponse[1]);
            Assert.assertTrue("Process Instance Started", processInstanceResponse[0].contains(BPMNTestConstants.CREATED));
        } catch (Exception exception) {
            log.error("Process instance failed to start ", exception);
            Assert.fail("Process instance failed to start ");
        }


        try {
            String searchResponse = tester.searchProcessInstanceByDefintionID(definitionResponse[1]);
            Assert.assertTrue("Process Instance Present", searchResponse.contains(BPMNTestConstants.OK));
        } catch (Exception exception) {
            log.error("Process instance cannot be found", exception);
            Assert.fail("Process instance cannot be found");
        }

        tester.waitForTaskGeneration();

        //Acquiring TaskID to perform Task Related Tests
        String[] taskResponse = new String[0];
        try {
            taskResponse = tester.findTaskIdByProcessInstanceID(processInstanceResponse[1]);
            Assert.assertTrue("Task ID Acquired", taskResponse[0].contains(BPMNTestConstants.OK));
        } catch (Exception exception) {
            log.error("Could not identify the task ID", exception);
            Assert.fail("Could not identify the task ID");
        }


        //Claiming a User Task
        try {
            String claimResponse = tester.claimTaskByTaskId(taskResponse[1]);
            Assert.assertTrue("User has claimed Task", claimResponse.contains(BPMNTestConstants.NO_CONTENT));
        } catch (Exception exception) {
            log.error("The Task was not claimable", exception);
            Assert.fail("The Task was not claimable");
        }

        String currentAssignee = null;
        try {
            currentAssignee = tester.getAssigneeByTaskId(taskResponse[1]);
            Assert.assertTrue("User has been assigned", currentAssignee.contains(BPMNTestConstants.USER_CLAIM));
        } catch (Exception exception) {
            log.error("The task not assigned to user", exception);
            Assert.fail("The task not assigned to user");
        }

        //Delegating a User Task
        try {
            String delegateStatus = tester.delegateTaskByTaskId(taskResponse[1]);
            Assert.assertTrue("Task has been delegated", delegateStatus.contains(BPMNTestConstants.NO_CONTENT));
        } catch (Exception exception) {
            log.error("Failed to Delegate Task", exception);
            Assert.fail("Failed to Delegate Task");
        }

        try {
            currentAssignee = tester.getAssigneeByTaskId(taskResponse[1]);
            Assert.assertTrue("Testing Delegated User Matches Assignee", currentAssignee.equals(BPMNTestConstants.USER_DELEGATE));
        } catch (Exception exception) {
            log.error("Delegated user does not match assignee", exception);
            Assert.fail("Delegated user does not match assignee");
        }


        //Commenting on a user task
        String[] commentResponse = new String[0];
        try {
            commentResponse = tester.addNewCommentOnTaskByTaskId(taskResponse[1], BPMNTestConstants.COMMENT_MESSAGE);
            Assert.assertTrue("Comment Has been added", commentResponse[0].contains(BPMNTestConstants.CREATED));
            Assert.assertTrue("Comment is visible", commentResponse[1].contains(BPMNTestConstants.COMMENT_MESSAGE));
        } catch (Exception exception) {
            log.error("Comment was not added", exception);
            Assert.fail("Comment was not added");
        }


        try {
            String validateComment = tester.getCommentByTaskIdAndCommentId(taskResponse[1], commentResponse[2]);
            Assert.assertTrue("Validating Comment Existence", validateComment.contains(BPMNTestConstants.COMMENT_MESSAGE));
        } catch (Exception exception) {
            log.error("Comment does not exist", exception);
            Assert.fail("Comment does not exist");
        }


        //resolving a User Task
        try {
            String status = tester.resolveTaskByTaskId(taskResponse[1]);
            String stateValue = tester.getDelegationsStateByTaskId(taskResponse[1]);
            Assert.assertTrue("Checking Delegation State", stateValue.equals("resolved"));
        } catch (Exception exception) {
            log.error("Failed to set task state to resolved", exception);
            Assert.fail("Failed to set task state to resolved");
        }


        //Deleting a Process Instance
        try {
            String deleteStatus = tester.deleteProcessInstanceByID(processInstanceResponse[1]);
            Assert.assertTrue("Process Instance Removed", deleteStatus.contains(BPMNTestConstants.NO_CONTENT));
        } catch (Exception exception) {
            log.error("Process instance cannot be removed", exception);
            Assert.fail("Process instance cannot be removed");
        }

        try {
            String deleteCheck = tester.validateProcessInstanceById(definitionResponse[1]);
            Assert.fail("Process Instance Is Present");
        } catch (Exception exception) {
            Assert.assertTrue("Process instance was removed successfully", BPMNTestConstants.NOT_AVAILABLE.equals(exception.getMessage()));
            log.error("Process instance does not exist", exception);
        }


        //Deleting the Deployment
        try {
            String undeployStatus = tester.unDeployBPMNPackage(deploymentResponse[1]);
            Assert.assertTrue("Package UnDeployed", undeployStatus.contains(BPMNTestConstants.NO_CONTENT));
        } catch (Exception exception) {
            log.error("Failed to remove BPMN Package " + fileName, exception);
            Assert.fail("Failed to remove BPMN Package " + fileName);
        }

        try {
            String[] unDeployCheck = tester.getDeploymentInfoById(deploymentResponse[1]);
            Assert.fail("Package Still Exists After Undeployment");
        } catch (Exception exception) {
            Assert.assertTrue("BPMN Package " + fileName + " Does Not Exist", BPMNTestConstants.NOT_AVAILABLE.equals(exception.getMessage()));
            log.error("BPMN Package " + fileName + " does not exist", exception);
        }
    }


}
