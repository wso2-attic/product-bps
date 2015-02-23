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

public class DeployUnDeployBPMNPackageTestCase extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(DeployUnDeployBPMNPackageTestCase.class);

    @Test(groups = {"wso2.bps.test.deploy"}, description = "Deploy/UnDeploy Package Test", priority = 1, singleThreaded = true)
    public void deployUnDeployBPMNPackage() throws Exception {
        init();

        ActivitiRestClient tester = new ActivitiRestClient(bpsServer.getInstance().getPorts().get("http"), bpsServer.getInstance().getHosts().get("default"));
        String filePath = FrameworkPathUtil.getSystemResourceLocation() + File.separator
                          + BPMNTestConstants.DIR_ARTIFACTS + File.separator
                          + BPMNTestConstants.DIR_BPMN + File.separator + "HelloApprove.bar";
        String fileName = "HelloApprove.bar";
        String[] deploymentResponse = {};
        String[] deploymentCheckResponse = {};
        String deploymentStatus = "";


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
            deploymentCheckResponse = tester.getDeploymentInfoById(deploymentResponse[1]);
            Assert.assertTrue("Deployment Present", deploymentCheckResponse[2].contains(fileName));
        } catch (IOException ioException) {
            log.info(ioException.getMessage());
            Assert.fail();
        } catch (JSONException jsonException) {
            log.info(jsonException.getMessage());
            Assert.fail();
        }

        try {
            deploymentStatus = tester.unDeployBPMNPackage(deploymentResponse[1]);
            Assert.assertTrue("Package UnDeployed", deploymentStatus.contains(BPMNTestConstants.NO_CONTENT));
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