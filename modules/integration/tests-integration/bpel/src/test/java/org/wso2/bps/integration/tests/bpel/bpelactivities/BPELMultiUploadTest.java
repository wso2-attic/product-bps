/*
 *
 *   Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 * /
 */

package org.wso2.bps.integration.tests.bpel.bpelactivities;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.bps.integration.common.clients.bpel.BpelPackageManagementClient;
import org.wso2.bps.integration.common.utils.BPSMasterTest;
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;
import org.wso2.carbon.automation.engine.frameworkutils.FrameworkPathUtil;
import org.wso2.carbon.bpel.stub.mgt.PackageManagementException;
import org.wso2.carbon.utils.FileManipulator;

import java.io.File;
import java.rmi.RemoteException;

public class BPELMultiUploadTest extends BPSMasterTest {

    private BpelPackageManagementClient bpelPackageManagementClient;
    private File[] samples = null;
    private static final Log log = LogFactory.getLog(BPELMultiUploadTest.class);


    public void setEnvironment() throws Exception {
        init();
        bpelPackageManagementClient = new BpelPackageManagementClient(backEndUrl, sessionCookie);
    }

    @BeforeClass(alwaysRun = true)
    public void initialize() throws Exception {
        setEnvironment();
        samples = FileManipulator.getMatchingFiles(FrameworkPathUtil.getSystemResourceLocation() + "artifacts"
                + File.separator + "bpel", null, "zip");
    }

    @Test(groups = {"wso2.bps"}, description = "Copy all artifacts test case", priority = 0)
    public void copyArtifacts() throws Exception {

        for (File sample : samples) {

            String name = sample.getName();
            name = name.substring(0, name.length() - 4);
            uploadBpelForTest(name);
            log.info("Copying: " + sample.getAbsolutePath());

        }
    }

    @Test(groups = {"wso2.bps"}, description = "Remove all artifacts test case", priority = 1)
    public void removeArtifacts() throws InterruptedException, RemoteException, PackageManagementException {

        for (File sample : samples) {

            String name = sample.getName();
            name = name.substring(0, name.length() - 4);
            bpelPackageManagementClient.undeployBPEL(name);
            log.info("Removing: " + sample.getName());

        }
    }

    @AfterClass(alwaysRun = true)
    public void serverLogout() throws RemoteException, LogoutAuthenticationExceptionException {
        this.loginLogoutClient.logout();
    }

}
