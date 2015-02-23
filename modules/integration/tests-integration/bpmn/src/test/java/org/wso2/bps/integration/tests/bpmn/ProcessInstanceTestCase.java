
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

public class ProcessInstanceTestCase extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(ProcessInstanceTestCase.class);

    @Test(groups = {"wso2.bps.test.processInstance"}, description = "Process Instance Test", priority = 2, singleThreaded = true)
    public void ProcessInstanceTests() throws Exception {
        init();
        ActivitiRestClient tester = new ActivitiRestClient(bpsServer.getInstance().getPorts().get("http"), bpsServer.getInstance().getHosts().get("default"));

        //deploying Package
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + File.separator
                          + BPMNTestConstants.DIR_ARTIFACTS + File.separator
                          + BPMNTestConstants.DIR_BPMN + File.separator + "HelloApprove.bar";
        String fileName = "HelloApprove.bar";
        String[] deploymentResponse = {};
        try {
            deploymentResponse = tester.deployBPMNPackage(filePath, fileName);
            Assert.assertTrue("Deployment Successful", deploymentResponse[0].contains(BPMNTestConstants.CREATED));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        try {
            String[] deploymentCheckResponse = tester.getDeploymentInfoById(deploymentResponse[1]);
            Assert.assertTrue("Deployment Present", deploymentCheckResponse[2].contains(fileName));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        //Acquiring Process Definition ID to start Process Instance
        String[] definitionResponse = new String[0];
        try {
            definitionResponse = tester.findProcessDefinitionInfoById(deploymentResponse[1]);
            Assert.assertTrue("Search Success", definitionResponse[0].contains(BPMNTestConstants.OK));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        //Starting and Verifying Process Instance
        String[] processInstanceResponse = new String[0];
        try {
            processInstanceResponse = tester.startProcessInstanceByDefintionID(definitionResponse[1]);
            Assert.assertTrue("Process Instance Started", processInstanceResponse[0].contains(BPMNTestConstants.CREATED));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        try {
            String searchResponse = tester.searchProcessInstanceByDefintionID(definitionResponse[1]);
            Assert.assertTrue("Process Instance Present", searchResponse.contains(BPMNTestConstants.OK));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        }

        //Suspending the Process Instance
        try {
            String[] suspendResponse = tester.suspendProcessInstanceById(processInstanceResponse[1]);
            Assert.assertTrue("Process Instance has been suspended", suspendResponse[0].contains(BPMNTestConstants.OK));
            Assert.assertTrue("Process Instance has been suspended", suspendResponse[1].contains("true"));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        try {
            String stateVerfication = tester.getSuspendedStateOfProcessInstanceByID(processInstanceResponse[1]);
            Assert.assertTrue("Verifying Suspended State", stateVerfication.contains("true"));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        //Deleting a Process Instance
        try {
            String deleteStatus = tester.deleteProcessInstanceByID(processInstanceResponse[1]);
            Assert.assertTrue("Process Instance Removed", deleteStatus.contains(BPMNTestConstants.NO_CONTENT));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        }

        try {
            String deleteCheck = tester.validateProcessInstanceById(definitionResponse[1]);
            Assert.assertTrue("Process Instance Removed Check", deleteCheck.contains(BPMNTestConstants.NOT_AVAILABLE));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        //Deleting the Deployment
        try {
            String undeployStatus = tester.unDeployBPMNPackage(deploymentResponse[1]);
            Assert.assertTrue("Package UnDeployed", undeployStatus.contains(BPMNTestConstants.NO_CONTENT));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        }

        try {
            String[] unDeployCheck = tester.getDeploymentInfoById(deploymentResponse[1]);
            Assert.assertTrue("Package UnDeployment", unDeployCheck[0].equals(BPMNTestConstants.NOT_AVAILABLE));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }
    }
}

