package org.wso2.bps.integration.tests.bpmn;

import junit.framework.Assert;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.ActivitiRestClient;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;

public class DeployUnDeployInvalidBPMNPackageTestCase {

	@Test(groups = {"wso2.bps.test.deploy.invalidPackage"}, description = "Deploy/UnDeploy Invalid Package Test", priority = 1, singleThreaded = true)
	public void deployUnDeployInvalidPackage() throws Exception{
		ActivitiRestClient tester = new ActivitiRestClient();
		String filePath = FrameworkPathUtil.getSystemResourceLocation()+ File.separator
		                  +BPMNTestConstants.DIR_ARTIFACTS + File.separator
		                  +BPMNTestConstants.DIR_BPMN + File.separator +"InvalidHelloApprove.bar";
		String fileName = "InvalidHelloApprove.bar";
		String[] deploymentResponse;
		deploymentResponse = tester.deployBPMNPackage(filePath,fileName);
		Assert.assertTrue("Checking the Status", deploymentResponse[0].contains("500"));
		Assert.assertTrue("Checking for Error Message",deploymentResponse[1].contains("Error parsing XML"));
	}
}
