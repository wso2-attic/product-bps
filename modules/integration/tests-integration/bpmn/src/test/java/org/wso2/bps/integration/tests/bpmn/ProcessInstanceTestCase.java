
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

public class ProcessInstanceTestCase extends BPSMasterTest {

	@Test(groups = {"wso2.bps.test.processInstance"}, description = "Process Instance Test", priority = 2, singleThreaded = true)
	public void ProcessInstanceTests() throws Exception{
		init();
		ActivitiRestClient tester = new ActivitiRestClient(bpsServer.getInstance().getPorts().get("http"),bpsServer.getInstance().getHosts().get("default"));

		//deploying Package
		String filePath = FrameworkPathUtil.getSystemResourceLocation()+ File.separator
		                  +BPMNTestConstants.DIR_ARTIFACTS + File.separator
		                  +BPMNTestConstants.DIR_BPMN + File.separator +"HelloApprove.bar";
		String fileName = "HelloApprove.bar";
		String[] deploymentResponse;
		deploymentResponse = tester.deployBPMNPackage(filePath,fileName);
		Assert.assertTrue("Deployment Successful", deploymentResponse[0].contains(BPMNTestConstants.CREATED));
		String[] deploymentCheckResponse = tester.getDeploymentById(deploymentResponse[1]);
		Assert.assertTrue("Deployment Present",deploymentCheckResponse[2].contains(fileName));

		//Acquiring Process Definition ID to start Process Instance
		String[] definitionResponse = tester.FindProcessDefinitionsID(deploymentResponse[1]);
		Assert.assertTrue("Search Success",definitionResponse[0].contains(BPMNTestConstants.OK));

		//Starting and Verifying Process Instance
		String[] processInstanceResponse = tester.startProcessInstanceByDefintionID(definitionResponse[1]);
		Assert.assertTrue("Process Instance Started", processInstanceResponse[0].contains(BPMNTestConstants.CREATED));
		String searchResponse = tester.searchProcessInstanceByDefintionID(definitionResponse[1]);
		Assert.assertTrue("Process Instance Present", searchResponse.contains(BPMNTestConstants.OK));

		//Suspending the Process Instance
		String[] suspendResponse = tester.suspendProcessInstance(processInstanceResponse[1]);
		Assert.assertTrue("Process Instance has been suspended", suspendResponse[0].contains(BPMNTestConstants.OK));
		Assert.assertTrue("Process Instance has been suspended", suspendResponse[1].contains("true"));

		//Deleting a Process Instance
		String deleteStatus = tester.deleteProcessInstanceByID(processInstanceResponse[1]);
		Assert.assertTrue("Process Instance Removed", deleteStatus.contains(BPMNTestConstants.NO_CONTENT));

		//Deleting the Deployment
		String undeployStatus = tester.unDeployPackage(deploymentResponse[1]);
		Assert.assertTrue("Package UnDeployed",undeployStatus.contains(BPMNTestConstants.NO_CONTENT));
	}
}

