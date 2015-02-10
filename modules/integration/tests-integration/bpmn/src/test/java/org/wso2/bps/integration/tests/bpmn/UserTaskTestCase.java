package org.wso2.bps.integration.tests.bpmn;

import junit.framework.Assert;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.ActivitiRestClient;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;


public class UserTaskTestCase {

	@Test(groups = {"wso2.bps.test.usertasks"}, description = "User Task Test", priority = 1, singleThreaded = true)
	public void UserTaskTestCase()throws Exception{



		ActivitiRestClient tester = new ActivitiRestClient();
		//deploying Package
		String filePath = FrameworkPathUtil.getSystemResourceLocation()+ File.separator
		                                    +BPMNTestConstants.DIR_ARTIFACTS + File.separator
		                                    +BPMNTestConstants.DIR_BPMN + File.separator +"AssigneeIsEmpty.bar";

		String fileName = "AssigneeIsEmpty.bar";


		String[] deploymentResponse = tester.deployBPMNPackage(filePath, fileName);
		Assert.assertTrue("Deployment Successful",deploymentResponse[0].contains("201"));
		String[] deploymentCheckResponse = tester.getDeploymentById(deploymentResponse[1]);
		Assert.assertTrue("Deployment Present",deploymentCheckResponse[2].contains(fileName));

		//Acquiring Process Definition ID to start Process Instance
		String[] definitionResponse = tester.FindProcessDefinitionsID(deploymentResponse[1]);
		Assert.assertTrue("Search Success",definitionResponse[0].contains("200"));

		//Starting and Verifying Process Instance
		String[] processInstanceResponse = tester.startProcessInstanceByDefintionID(definitionResponse[1]);
		Assert.assertTrue("Process Instance Started", processInstanceResponse[0].contains("201"));
		String searchResponse = tester.searchProcessInstanceByDefintionID(definitionResponse[1]);
		Assert.assertTrue("Process Instance Present", searchResponse.contains("200"));

		tester.waitForTaskGeneration();

		//Acquiring TaskID to perform Task Related Tests
		String[] taskResponse = tester.FindTaskIdByProcessInstanceID(processInstanceResponse[1]);
		Assert.assertTrue("Task ID Acquired", taskResponse[0].contains("200"));

		//Claiming a User Task
		String[] claimResponse = tester.claimTask(taskResponse[1]);
		Assert.assertTrue("User has claimed Task",claimResponse[0].contains("204"));
		String currentAssignee = tester.getAssigneeByTaskId(taskResponse[1]);
		Assert.assertTrue("User has been assigned", currentAssignee.contains(BPMNTestConstants.userClaim));

		//Delegating a User Task
		String delegateStatus = tester.delegateTask(taskResponse[1]);
		Assert.assertTrue("Task has been delegated",delegateStatus.contains("204"));
		currentAssignee = tester.getAssigneeByTaskId(taskResponse[1]);
		Assert.assertTrue("Testing Delegated User Matches Assignee", currentAssignee.equals(BPMNTestConstants.userDelegate));

		//Commenting on a user task
		String[] commentResponse = tester.addNewCommentOnTask(taskResponse[1],BPMNTestConstants.message);
		Assert.assertTrue("Comment Has been added", commentResponse[0].contains("201"));
		Assert.assertTrue("Comment is visible", commentResponse[1].contains(BPMNTestConstants.message));

		//resolving a User Task
		String status = tester.resolveTask(taskResponse[1]);
		String stateValue = tester.getDelegationsStateByTaskId(taskResponse[1]);
		Assert.assertTrue("Checking Delegation State", stateValue.equals("resolved"));

		//Deleting a Process Instance
		String deleteStatus = tester.deleteProcessInstanceByID(processInstanceResponse[1]);
		Assert.assertTrue("Process Instance Removed", deleteStatus.contains("204"));

		//Deleting the Deployment
		String undeployStatus = tester.unDeployPackage(deploymentResponse[1]);
		Assert.assertTrue("Package UnDeployed",undeployStatus.contains("204"));

	}


}
