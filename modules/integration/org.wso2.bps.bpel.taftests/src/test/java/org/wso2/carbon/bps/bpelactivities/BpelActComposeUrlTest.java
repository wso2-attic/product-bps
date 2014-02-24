package org.wso2.carbon.bps.bpelactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.Assert;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.wso2.bps.integration.core.BPSMasterTest;
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.api.clients.bpel.BpelInstanceManagementClient;
import org.wso2.carbon.automation.api.clients.bpel.BpelPackageManagementClient;
import org.wso2.carbon.automation.api.clients.bpel.BpelProcessManagementClient;
import org.wso2.carbon.automation.core.RequestSender;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.bpel.stub.mgt.types.LimitedInstanceInfoType;
import org.wso2.carbon.bpel.stub.mgt.types.PaginatedInstanceList;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

public class BpelActComposeUrlTest extends BPSMasterTest {

    private static final Log log = LogFactory.getLog(BpelActComposeUrlTest.class);

    LimitedInstanceInfoType instanceInfo = null;
    BpelPackageManagementClient bpelPackageManagementClient;
    BpelProcessManagementClient processManagementClient;
    BpelInstanceManagementClient instanceManagementClient;

    RequestSender requestSender;


    public void setEnvironment() throws LoginAuthenticationExceptionException, RemoteException {
        init();
        bpelPackageManagementClient = new BpelPackageManagementClient(backEndUrl, sessionCookie);
        processManagementClient = new BpelProcessManagementClient(backEndUrl, sessionCookie);
        instanceManagementClient = new BpelInstanceManagementClient(backEndUrl, sessionCookie);
        requestSender = new RequestSender();
    }

    @BeforeClass(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void deployArtifact()
            throws Exception {
        setEnvironment();
        uploadBpelForTest("TestComposeUrl");
        requestSender.waitForProcessDeployment(backEndUrl + "TestComposeUrlService");
    }

    @Test(groups = {"wso2.bps", "wso2.bps.bpelactivities"}, description = "Invike combine URL Bpel")
    public void testComposeUrl() throws Exception, RemoteException {
        int instanceCount = 0;

        String processID = processManagementClient.getProcessId("TestComposeUrl");
        PaginatedInstanceList instanceList = new PaginatedInstanceList();
        instanceList = instanceManagementClient.filterPageInstances(processID);
        if (instanceList.getInstance() != null) {
            instanceCount = instanceList.getInstance().length;
        }
        if (!processID.isEmpty()) {
            try {
                this.forEachRequest();
                Thread.sleep(5000);
                if (instanceCount >= instanceManagementClient.filterPageInstances(processID).getInstance().length) {
                    Assert.fail("Instance is not created for the request");
                }
            } catch (InterruptedException e) {
                log.error("Process management failed" + e);
                Assert.fail("Process management failed" + e);
            }
            instanceManagementClient.clearInstancesOfProcess(processID);
        }
    }

    @AfterTest(alwaysRun = true, groups = {"wso2.bps", "wso2.bps.bpelactivities"})
    public void removeArtifacts()
            throws PackageManagementException, InterruptedException, RemoteException,
            LogoutAuthenticationExceptionException {
        bpelPackageManagementClient.undeployBPEL("TestComposeUrl");
        this.authenticatorClient.logOut();
    }

    public void forEachRequest() throws Exception {
        String payload = " <p:composeUrl xmlns:p=\"http://ode/bpel/unit-test.wsdl\">\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <template>www.google.com</template>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <name>google</name>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <value>ee</value>\n" +
                "      <!--Exactly 1 occurrence-->\n" +
                "      <pairs>\n" +
                "         <!--Exactly 1 occurrence-->\n" +
                "         <user>er</user>\n" +
                "         <!--Exactly 1 occurrence-->\n" +
                "         <tag>ff</tag>\n" +
                "      </pairs>\n" +
                "   </p:composeUrl>";
        String operation = "composeUrl";
        String serviceName = "TestComposeUrlService";
        List<String> expectedOutput = new ArrayList<String>();
        expectedOutput.add("www.google");

        requestSender.sendRequest(backEndUrl + serviceName, operation, payload,
                1, expectedOutput, true);
    }
}

