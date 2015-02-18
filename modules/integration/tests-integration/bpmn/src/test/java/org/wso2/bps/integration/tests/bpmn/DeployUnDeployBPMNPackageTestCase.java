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
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.ActivitiRestClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;

public class DeployUnDeployBPMNPackageTestCase extends BPSMasterTest {

	@Test(groups = {"wso2.bps.test.deploy"}, description = "Deploy/UnDeploy Package Test", priority = 1, singleThreaded = true)
	public void deployUnDeployBPMNPackage() throws Exception{
		init();
		ActivitiRestClient tester = new ActivitiRestClient(bpsServer.getInstance().getPorts().get("http"),bpsServer.getInstance().getHosts().get("default"));
		String filePath = FrameworkPathUtil.getSystemResourceLocation()+ File.separator
		                  +BPMNTestConstants.DIR_ARTIFACTS + File.separator
		                  +BPMNTestConstants.DIR_BPMN + File.separator +"HelloApprove.bar";
		String fileName = "HelloApprove.bar";
		String[] deploymentResponse;
		deploymentResponse = tester.deployBPMNPackage(filePath,fileName);

		Assert.assertTrue("Deployment Successful",deploymentResponse[0].contains(BPMNTestConstants.CREATED));
		String[] deploymentCheckResponse = tester.getDeploymentById(deploymentResponse[1]);
		Assert.assertTrue("Deployment Present",deploymentCheckResponse[2].contains(fileName));

		String status = tester.unDeployPackage(deploymentResponse[1]);
		Assert.assertTrue("Package UnDeployed",status.contains(BPMNTestConstants.NO_CONTENT));
	}
}