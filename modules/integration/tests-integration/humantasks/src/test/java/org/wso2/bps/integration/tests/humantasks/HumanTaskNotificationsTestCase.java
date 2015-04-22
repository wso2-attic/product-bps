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


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.humantasks.HumanTaskPackageManagementClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
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
    private HumanTaskPackageManagementClient humanTaskPackageManagementClient;
    private UserManagementClient userManagementClient;
    private ServerConfigurationManager serverConfigurationManager;
    private RequestSender requestSender;

    //Email notification related variables
    private static GreenMail mailServer;
    private static final String USER_PASSWORD = "testwso2123";
    private static final String USER_NAME = "wso2test1";
    private static final String EMAIL_USER_ADDRESS = "wso2test1@localhost";
    private static final String EMAIL_SUBJECT = "email subject to user";
    private static final String EMAIL_TEXT = "Hi wso2test1";
    private static final int SMTP_TEST_PORT = 3025;
    GreenMail greenMail;

    /**
     * Setting up Server and Apply new Configuration Files.
     */
    @BeforeClass(alwaysRun = true)
    public void setEnvironment() throws Exception {
        init();  //init master class
        requestSender = new RequestSender();

        serverConfigurationManager = new ServerConfigurationManager(bpsServer);
        //Replacing config file content
        updateConfigFiles();
        // Need to re-initialize since we have restarted the server
        init();
        userManagementClient = new UserManagementClient(backEndUrl, sessionCookie);
        deployArtifact();
        addRoles();
        requestSender.waitForProcessDeployment(backEndUrl + HumanTaskTestConstants.REMINDER_SERVICE);
        humanTaskPackageManagementClient = new HumanTaskPackageManagementClient(backEndUrl, sessionCookie);
        serverConfigurationManager = new ServerConfigurationManager(bpsServer);
        log.info("Server setting up completed.");
        //initialize HT Client API for Clerk1 user

        AutomationContext clerk1AutomationContext = new AutomationContext("BPS", "bpsServerInstance0001",
                FrameworkConstants.SUPER_TENANT_KEY, "clerk1");
        LoginLogoutClient clerk1LoginLogoutClient = new LoginLogoutClient(clerk1AutomationContext);
        clerk1LoginLogoutClient.login();


        //initialize HT Client API for Manager1 user
        AutomationContext manager1AutomationContext = new AutomationContext("BPS", "bpsServerInstance0001",
                FrameworkConstants.SUPER_TENANT_KEY, "manager1");
        LoginLogoutClient manager1LoginLogoutClient = new LoginLogoutClient(manager1AutomationContext);
        manager1LoginLogoutClient.login();


        ServerSetup setup = new ServerSetup(SMTP_TEST_PORT, "localhost", "smtp");
        greenMail = new GreenMail(setup);
        greenMail.setUser(EMAIL_USER_ADDRESS, USER_NAME, USER_PASSWORD);
        greenMail.start();

    }

    /**
     * Update content in humantask.xml/axis2_client.xml & restart server
     * @throws Exception
     */
    private void updateConfigFiles() throws Exception {
        final String artifactLocation = FrameworkPathUtil.getSystemResourceLocation()
                + HumanTaskTestConstants.DIR_ARTIFACTS + File.separator + HumanTaskTestConstants.DIR_CONFIG + File.separator
                + HumanTaskTestConstants.DIR_EMAIL + File.separator;

        File humantaskConfigNew = new File(artifactLocation + HumanTaskTestConstants.HUMANTASK_XML);
        File humantaskConfigOriginal = new File(FrameworkPathUtil.getCarbonServerConfLocation() + File.separator
                + HumanTaskTestConstants.HUMANTASK_XML);
        serverConfigurationManager.applyConfiguration(humantaskConfigNew, humantaskConfigOriginal, true, false);

        File humanTaskAxis2ClientConfigNew = new File(artifactLocation + HumanTaskTestConstants.AXIS2_CLIENT);
        File humanTaskAxis2ClientConfigOriginal = new File(FrameworkPathUtil.getCarbonServerConfLocation() + File.separator + HumanTaskTestConstants.DIR_AXIS2
                + File.separator + HumanTaskTestConstants.AXIS2_CLIENT);
        serverConfigurationManager.applyConfiguration(humanTaskAxis2ClientConfigNew, humanTaskAxis2ClientConfigOriginal, true, true);
    }

    public void deployArtifact() throws Exception {
        uploadHumanTaskForTest("taskDeadlineWithNotificationsTest");
    }


    private void addRoles() throws Exception {
        String[] clerkUsers = new String[]{HumanTaskTestConstants.CLERK1_USER};
        String[] managerUsers = new String[]{HumanTaskTestConstants.MANAGER1_USER};
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
                        "<firstname>John</firstname>\n" +
                        "<lastname>Denver</lastname>\n" +
                        "</p:notify>";

        String operation = "notify";
        String serviceName = "ClaimReminderService";
        List<String> expectedOutput = Collections.emptyList();
        log.info("Calling Service: " + backEndUrl + serviceName);
        requestSender.sendRequest(backEndUrl + serviceName, operation, soapBody, 1,
                expectedOutput, false);
       //Wait for email notification to be received
        greenMail.waitForIncomingEmail(5000, 1);
        Message[] messages = greenMail.getReceivedMessages();
        Assert.assertNotNull(messages.length);
        Assert.assertEquals(messages[0].getSubject(), EMAIL_SUBJECT);
        Assert.assertTrue(String.valueOf(messages[0].getContent()).contains(EMAIL_TEXT));

    }


    @Test(groups = {"wso2.bps.task.clean"}, description = "Clean up server notifications", priority = 17, singleThreaded = true)
    public void removeArtifacts() throws Exception {
        greenMail.stop();
        log.info("Undeploy claim reminder service");
        userManagementClient.deleteRole(HumanTaskTestConstants.REGIONAL_CLERKS_ROLE);
        userManagementClient.deleteRole(HumanTaskTestConstants.REGIONAL_MANAGER_ROLE);
        humanTaskPackageManagementClient.unDeployHumanTask("ClaimReminderService", "notify");
        loginLogoutClient.logout();
    }


}






