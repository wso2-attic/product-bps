/*
 * Copyright (c) WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.bps.integration.tests.humantasks;

import com.icegreen.greenmail.util.ServerSetupTest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpel.BpelInstanceManagementClient;
import org.wso2.bps.integration.common.clients.bpel.BpelPackageManagementClient;
import org.wso2.bps.integration.common.clients.humantasks.HumanTaskClientApiClient;
import org.wso2.bps.integration.common.clients.humantasks.HumanTaskPackageManagementClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.bps.integration.common.utils.BPSTestConstants;
import org.wso2.bps.integration.common.utils.RequestSender;
import org.wso2.carbon.automation.engine.FrameworkConstants;
import org.wso2.carbon.automation.engine.context.AutomationContext;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.integration.common.admin.client.UserManagementClient;
import org.wso2.carbon.integration.common.utils.LoginLogoutClient;
import com.icegreen.greenmail.util.GreenMail;
import com.icegreen.greenmail.util.ServerSetup;
import javax.mail.Message;
import org.wso2.carbon.integration.common.utils.mgt.ServerConfigurationManager;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class HumanTaskNotificationsTestCase extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(HumanTaskCreationTestCase.class);
    //Test Automation API Clients
    private BpelPackageManagementClient bpelPackageManagementClient;
    private HumanTaskPackageManagementClient humanTaskPackageManagementClient;
    private BpelInstanceManagementClient instanceManagementClient;
    private UserManagementClient userManagementClient;
    private ServerConfigurationManager serverConfigurationManager;

    private HumanTaskClientApiClient clerk1HumanTaskClientApiClient, manager1HumanTaskClientApiClient;

    private RequestSender requestSender;
    //Email notification related variables
    private static GreenMail mailServer;

    private static final String USER_PASSWORD = "testwso2123";
    private static final String USER_NAME = "wso2test1";
    private static final String EMAIL_USER_ADDRESS = "wso2test1@localhost";
    private static final String EMAIL_SUBJECT = "email subject to user";
    private static final String EMAIL_TEXT = "Hi wso2test1";
    private static final String EMAIL_TO = "wso2test1@localhost.com";
    private static final int SMTP_TEST_PORT = 3025;
    GreenMail greenMail;

    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        init();  //init master class
        requestSender = new RequestSender();
        userManagementClient = new UserManagementClient(backEndUrl, sessionCookie);
        deployArtifact();
        addRoles();
        serverConfigurationManager = new ServerConfigurationManager(bpsServer);
        updateConfigFiles();
        init();
        requestSender.waitForProcessDeployment(backEndUrl + HumanTaskTestConstants.REMINDER_SERVICE);
        humanTaskPackageManagementClient = new HumanTaskPackageManagementClient(backEndUrl, sessionCookie);
        instanceManagementClient = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        serverConfigurationManager = new ServerConfigurationManager(bpsServer);
        //copy original humantask.xml/axis2_client.xml files with new configs
        updateConfigFiles();

        log.info("Server setting up completed.");


        //initialize HT Client API for Clerk1 user
        AutomationContext clerk1AutomationContext = new AutomationContext("BPS", "bpsServerInstance0001",
                FrameworkConstants.SUPER_TENANT_KEY, "clerk1");
        LoginLogoutClient clerk1LoginLogoutClient = new LoginLogoutClient(clerk1AutomationContext);
        String clerk1SessionCookie = clerk1LoginLogoutClient.login();


        clerk1HumanTaskClientApiClient = new HumanTaskClientApiClient(backEndUrl, clerk1SessionCookie);
        ServerSetup setup = new ServerSetup(3025, "localhost", "smtp");
        GreenMail greenMail = new GreenMail(setup);
        greenMail.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
        greenMail.start();

    }

    private void updateConfigFiles() throws Exception {
        final String artifactLocation = FrameworkPathUtil.getSystemResourceLocation()
                + BPSTestConstants.DIR_ARTIFACTS + File.separator + BPSTestConstants.DIR_CONFIG + File.separator
                + BPSTestConstants.DIR_EMAIL + File.separator;

        File humantaskConfigNew = new File(artifactLocation + BPSTestConstants.HUMANTASK_XML);
        File humantaskConfigOriginal = new File(FrameworkPathUtil.getCarbonServerConfLocation() + File.separator
                + BPSTestConstants.HUMANTASK_XML);

        File humanTaskAxis2ClientConfigNew = new File(artifactLocation + BPSTestConstants.AXIS2_CLIENT);
        File humanTaskAxis2ClientConfigOriginal = new File(FrameworkPathUtil.getCarbonServerConfLocation() + File.separator
                + BPSTestConstants.AXIS2_CLIENT);
        serverConfigurationManager.applyConfiguration(humantaskConfigNew, humantaskConfigOriginal, true, true);
        serverConfigurationManager.applyConfiguration(humanTaskAxis2ClientConfigNew, humanTaskAxis2ClientConfigOriginal, true, true);
    }

    protected void initialize() throws Exception {
        log.info("Initializing HumanTask task creation Test...");
        userManagementClient = new UserManagementClient(backEndUrl, sessionCookie);
        addRoles();
        instanceManagementClient = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        humanTaskPackageManagementClient = new HumanTaskPackageManagementClient(backEndUrl, sessionCookie);
        log.info("Add users success !");
        deployArtifact();
        requestSender.waitForProcessDeployment(backEndUrl + HumanTaskTestConstants.REMINDER_SERVICE);
        //requestSender.waitForProcessDeployment(backEndUrl + HumanTaskTestConstants.CLAIM_APPROVAL_PROCESS_SERVICE);
        requestSender.waitForProcessDeployment(backEndUrl + HumanTaskTestConstants.CLAIM_SERVICE);
    }

    public void deployArtifact() throws Exception {

        uploadHumanTaskForTest("taskDeadlineWithNotificationsTest");
    }


    private void addRoles() throws Exception {
        String[] clerkUsers = new String[]{HumanTaskTestConstants.CLERK1_USER, HumanTaskTestConstants.CLERK2_USER,
                HumanTaskTestConstants.CLERK3_USER};
        String[] managerUsers = new String[]{HumanTaskTestConstants.MANAGER1_USER, HumanTaskTestConstants.MANAGER2_USER,
                HumanTaskTestConstants.MANAGER3_USER};
        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE, clerkUsers,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"}, false);
        userManagementClient.addRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE, managerUsers,
                new String[]{"/permission/admin/login",
                        "/permission/admin/manage/humantask/viewtasks"}, false);

    }

    @Test(groups = {"wso2.bps.task.create"}, description = "Claims approval notification support test case", priority = 1, singleThreaded = true)
    public void createTaskWithNotifications() throws Exception {

        String soapBody =
                "<p:notify xmlns:p=\"http://www.example.com/claims/\">\n" +
                        "<firstname>san</firstname>\n" +
                        "<lastname>vith</lastname>\n" +
                        "</p:notify>";

        String operation = "notify";
        String serviceName = "ClaimReminderService";
        List<String> expectedOutput = Collections.emptyList();
        log.info("Calling Service: " + backEndUrl + serviceName);
        requestSender.sendRequest(backEndUrl + serviceName, operation, soapBody, 1,
                expectedOutput, false);

        Thread.sleep(60000);

        greenMail.waitForIncomingEmail(5000, 1);
        Message[] messages = greenMail.getReceivedMessages();

        System.out.println("Message length =>" + messages.length);
        System.out.println("Subject => " + messages[0].getSubject());
        System.out.println("Content => " + messages[0].getContent().toString());
        System.out.println("Done");


    }


    @Test(groups = {"wso2.bps.task.clean"}, description = "Clean up server notifications", priority = 17, singleThreaded = true)
    public void removeArtifacts() throws Exception {
        greenMail.stop();
    }


}






