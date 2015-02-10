package org.wso2.bps.integration.tests.bpmn;

import junit.framework.Assert;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.ActivitiRestClient;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;

public class ProcessInstanceTestCase {

	@Test(groups = {"wso2.bps.test.processInstance"}, description = "Process Instance Test", priority = 2, singleThreaded = true)
	public void ProcessInstanceTests() throws Exception{

		ActivitiRestClient tester = new ActivitiRestClient();

		//deploying Package
		String filePath = FrameworkPathUtil.getSystemResourceLocation()+ File.separator
		                  +BPMNTestConstants.DIR_ARTIFACTS + File.separator
		                  +BPMNTestConstants.DIR_BPMN + File.separator +"HelloApprove.bar";
		String fileName = "HelloApprove.bar";
		String[] deploymentResponse;
		deploymentResponse = tester.deployBPMNPackage(filePath,fileName);
		Assert.assertTrue("Deployment Successful", deploymentResponse[0].contains("201"));
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

		//Suspending the Process Instance
		String[] suspendResponse = tester.suspendProcessInstance(processInstanceResponse[1]);
		Assert.assertTrue("Process Instance has been suspended", suspendResponse[0].contains("200"));
		Assert.assertTrue("Process Instance has been suspended", suspendResponse[1].contains("true"));

		//Deleting a Process Instance
		String deleteStatus = tester.deleteProcessInstanceByID(processInstanceResponse[1]);
		Assert.assertTrue("Process Instance Removed", deleteStatus.contains("204"));

		//Deleting the Deployment
		String undeployStatus = tester.unDeployPackage(deploymentResponse[1]);
		Assert.assertTrue("Package UnDeployed",undeployStatus.contains("204"));
	}
}

