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

package org.wso2.bps.integration.tests.bpmn;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bps.integration.common.clients.bpmn.WorkflowServiceClient;

public class BPMNTestUtils {

    private static final Log log = LogFactory.getLog(BPMNTestUtils.class);

    /*
    BPMN does not expose any service to check deployment is done.
    So we are using deployment count to check completeness of process deployment
    */
    public static void waitForProcessDeployment(WorkflowServiceClient workflowServiceClient, String bpmnPackageName, int previousDeploymentCount) throws Exception {
        int serviceTimeOut = 0;
        while (true) {
            if (workflowServiceClient.getDeployments() != null && workflowServiceClient.getDeployments().length > previousDeploymentCount) {
                return;
            }
            if (serviceTimeOut == 0) {
                log.info("Waiting for BPMN package" + bpmnPackageName + " to deploy.");
            } else if (serviceTimeOut > 200) {
                log.error("Time out");
                throw new Exception(bpmnPackageName + " package is not found");
            }
            try {
                Thread.sleep(500);
                serviceTimeOut++;
            } catch (InterruptedException ignored) {
            }
        }
    }
}
