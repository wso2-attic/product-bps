package org.wso2.bps.integration.tests.bpmn;

import junit.framework.Assert;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.ActivitiRestClient;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;

public class DeployUnDeployBPMNPackageTestCase {

	@Test(groups = {"wso2.bps.test.deploy"}, description = "Deploy/UnDeploy Package Test", priority = 1, singleThreaded = true)
	public void deployUnDeployPackage() throws Exception{
		ActivitiRestClient tester = new ActivitiRestClient();
		String filePath = FrameworkPathUtil.getSystemResourceLocation()+ File.separator
		                  +BPMNTestConstants.DIR_ARTIFACTS + File.separator
		                  +BPMNTestConstants.DIR_BPMN + File.separator +"HelloApprove.bar";
		String fileName = "HelloApprove.bar";
		String[] deploymentResponse;
		deploymentResponse = tester.deployBPMNPackage(filePath,fileName);

		Assert.assertTrue("Deployment Successful",deploymentResponse[0].contains("201"));
		String[] deploymentCheckResponse = tester.getDeploymentById(deploymentResponse[1]);
		Assert.assertTrue("Deployment Present",deploymentCheckResponse[2].contains(fileName));

		String status = tester.unDeployPackage(deploymentResponse[1]);
		Assert.assertTrue("Package UnDeployed",status.contains("204"));
		String[] undeployCheckResponse = tester.getDeploymentById(deploymentResponse[1]);
		Assert.assertTrue("Deployment No Present", undeployCheckResponse[0].contains("404"));
	}
}
