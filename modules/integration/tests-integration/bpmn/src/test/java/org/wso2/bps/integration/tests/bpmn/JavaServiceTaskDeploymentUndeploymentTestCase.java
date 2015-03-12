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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.ActivitiRestClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JavaServiceTaskDeploymentUndeploymentTestCase extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BPSMasterTest.class);


    @BeforeClass
    public void EnvSetup() throws Exception {
        init();
        final String artifactLocation = FrameworkPathUtil.getSystemResourceLocation() + File.separator
                                        + BPMNTestConstants.DIR_ARTIFACTS + File.separator
                                        + BPMNTestConstants.DIR_BPMN + File.separator
                                        + "testArtifactid-1.0.jar";

        ServerConfigurationManager Loader = new ServerConfigurationManager(bpsServer);
        File javaArtifact = new File(artifactLocation);
        Loader.copyToComponentLib(javaArtifact);
        Loader.restartForcefully();

        //reintialising as session cookies and other configuration which expired during restart is reset
        init();
    }


    @Test(groups = {"wso2.bps.test.deploy.JavaServiceTask"}, description = "Deploy/UnDeploy Package Test", priority = 1, singleThreaded = true)

    public void deployUnDeployJavaServiceTaskBPMNPackage() throws Exception {
        init();
        ActivitiRestClient tester = new ActivitiRestClient(bpsServer.getInstance().getPorts().get("http")
                , bpsServer.getInstance().getHosts().get("default"));

        String filePath = FrameworkPathUtil.getSystemResourceLocation() + File.separator
                          + BPMNTestConstants.DIR_ARTIFACTS + File.separator
                          + BPMNTestConstants.DIR_BPMN + File.separator + "sampleJavaServiceTask.bar";

        String fileName = "sampleJavaServiceTask.bar";
        String[] deploymentResponse = {};
        String[] definitionResponse = {};

        //deploying the BPMN Package
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


        //Searching for the definition ID for process instance work
        try {
            definitionResponse = tester.findProcessDefinitionInfoById(deploymentResponse[1]);
            Assert.assertTrue("Search Success", definitionResponse[0].contains("200"));
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

        //check for varible value if true
        try {
            String[] invocationResponse = tester.getValueOfVariableOfProcessInstanceById(processInstanceResponse[1], "executionState");
            Assert.assertTrue("Variable Present", invocationResponse[0].contains(BPMNTestConstants.OK));
            Assert.assertTrue("Variable Present", invocationResponse[1].contains("executionState"));
            Assert.assertTrue("Variable Value is True", invocationResponse[2].contains("true"));
        } catch (Exception exception) {
            log.error("Execution variable response cannot be found ", exception);
            Assert.fail("The execution variable response cannot be found ");
        }


        //Suspending the Process Instance
        try {
            String[] suspendResponse = tester.suspendProcessInstanceById(processInstanceResponse[1]);
            Assert.assertTrue("Process Instance has been suspended", suspendResponse[0].contains(BPMNTestConstants.OK));
            Assert.assertTrue("Process Instance has been suspended", suspendResponse[1].contains("true"));
        } catch (Exception exception) {
            log.error("Process instance cannot be suspended", exception);
            Assert.fail("The Process instance cannot be suspended");
        }


        try {
            String stateVerfication = tester.getSuspendedStateOfProcessInstanceByID(processInstanceResponse[1]);
            Assert.assertTrue("Verifying Suspended State", stateVerfication.contains("true"));
        } catch (Exception exception) {
            log.error("The process instance is not in suspended state ", exception);
            Assert.fail("The process instance is not in suspended state ");
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
            Assert.fail("Process Instance Not Present");
        } catch (Exception exception) {
            Assert.assertTrue("Process instance was removed successfully", BPMNTestConstants.NOT_AVAILABLE.equals(exception.getMessage()));
            log.error("Process instance is still present", exception);
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
            Assert.fail("Package Not Present");
        } catch (Exception exception) {
            Assert.assertTrue("BPMN Package " + fileName + " Does Not Exist", BPMNTestConstants.NOT_AVAILABLE.equals(exception.getMessage()));
            log.error("BPMN Package " + fileName + " Still Exists", exception);
        }
    }
}