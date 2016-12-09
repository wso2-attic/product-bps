/*
 *     Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *     WSO2 Inc. licenses this file to you under the Apache License,
 *     Version 2.0 (the "License"); you may not use this file except
 *     in compliance with the License.
 *     You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing,
 *    software distributed under the License is distributed on an
 *    "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *    KIND, either express or implied.  See the License for the
 *    specific language governing permissions and limitations
 *    under the License.
 */

package org.wso2.bps.integration.tests.bpmn.deployment;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpmn.WorkflowServiceClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.bps.integration.tests.bpmn.BPMNTestConstants;
import org.wso2.bps.integration.tests.bpmn.BPMNTestUtils;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNDeployment;
import org.wso2.carbon.integration.common.utils.exceptions.AutomationUtilException;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;

import java.io.File;
import java.io.IOException;

/**
 * This test to verify that slave nodes won't resolve BPMN deployment inconsistencies
 */
public class BPMNResolvingDeploymentsAtStartupTestCase extends BPSMasterTest{
    private static final Log log = LogFactory.getLog(BPMNResolvingDeploymentsAtStartupTestCase.class);
    public static final String PROCESS_NAME = "UserTaskProcess";

    private ServerConfigurationManager serverConfigurationManager;
    private WorkflowServiceClient workflowServiceClient;
    private String artifactLocation;

    @BeforeClass(alwaysRun = true)
    public void init() throws Exception {
        super.init();
        artifactLocation = FrameworkPathUtil.getSystemResourceLocation() + BPMNTestConstants.DIR_ARTIFACTS +
                File.separator + BPMNTestConstants.DIR_CONFIG + File.separator + BPMNTestConstants.DIR_DEPLOYMENT;
        serverConfigurationManager = new ServerConfigurationManager(bpsServer);
        workflowServiceClient = new WorkflowServiceClient(backEndUrl, sessionCookie);

    }

    @Test(groups = {"wso2.bps.bpmn.deployment"},
          description = "This test to verify that slave nodes won't resolve BPMN deployment inconsistencies",
          priority = 1,
          singleThreaded = true)
    public void testSlaveDeployment() throws Exception {
        //deploy new BPMN process
        uploadBPMNForTest(PROCESS_NAME);
        BPMNTestUtils.waitForProcessDeployment(workflowServiceClient, PROCESS_NAME, 0);

        log.info("Deployed process : " +PROCESS_NAME);

        BPMNDeployment deploymentsBeforeRestart[] = workflowServiceClient.getDeployments();
        //restart server as a slave
        updateConfigsForSlaveNode();

        //delete the bpmn process from the file system
        //it will not get undeployed since server is in slave mode (read only registry)
        String bpmnArtifactPath = FrameworkPathUtil.getCarbonHome() + File.separator + "repository" +
                File.separator + "deployment" + File.separator  + "server" + File.separator  +
                BPMNTestConstants.DIR_BPMN + File.separator + PROCESS_NAME + BPMNTestConstants.BAR_EXTENSION;
        File deployedBPMNArtifact = new File(bpmnArtifactPath);

        if (!deployedBPMNArtifact.delete()) {
            Assert.fail("Unable to delete the BPMN process artifact : " + bpmnArtifactPath);
        }

        //restart the server
        serverConfigurationManager.restartGracefully();

        sessionCookie = loginLogoutClient.login();
        workflowServiceClient = new WorkflowServiceClient(backEndUrl, sessionCookie);

        //check deployed BPMN process still exists
        BPMNDeployment deploymentsAfterRestart[] = workflowServiceClient.getDeployments();

        Assert.assertEquals(deploymentsAfterRestart.length, deploymentsBeforeRestart.length,
                PROCESS_NAME + "Process undeployed at startup");
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        serverConfigurationManager.restoreToLastConfiguration(true);
    }

    private void updateConfigsForSlaveNode() throws IOException, AutomationUtilException {

        String srcRegistryXml = artifactLocation + File.separator + BPMNTestConstants.DIR_SLAVE_CONFIGS +
                File.separator + BPMNTestConstants.REGISTRY_CONFIGURATION_FILE_NAME;
        String targetRegistryXml = FrameworkPathUtil.getCarbonServerConfLocation() + File.separator +
                BPMNTestConstants.REGISTRY_CONFIGURATION_FILE_NAME;

        File srcRegistryFile = new File(srcRegistryXml);
        File targetRegistryFile = new File(targetRegistryXml);

        serverConfigurationManager.applyConfiguration(srcRegistryFile, targetRegistryFile, true, true);
    }
}
