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

package org.wso2.bps.integration.common.clients.bpmn;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.bps.integration.common.clients.AuthenticateStubUtil;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNDeployment;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNInstance;
import org.wso2.carbon.bpmn.core.mgt.model.xsd.BPMNProcess;
import org.wso2.carbon.bpmn.stub.BPMNDeploymentServiceStub;
import org.wso2.carbon.bpmn.stub.BPMNInstanceServiceStub;
import org.wso2.carbon.utils.xml.XMLPrettyPrinter;
import sun.misc.BASE64Decoder;

import javax.imageio.ImageIO;
import javax.xml.bind.DatatypeConverter;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class WorkflowServiceClient {

    private static final Log log = LogFactory.getLog(BPMNUploaderClient.class);
    BPMNInstanceServiceStub instanceServiceStub = null;
    BPMNDeploymentServiceStub deploymentServiceStub = null;


    public WorkflowServiceClient(String backendServerURL, String sessionCookie) throws AxisFault {

        String deploymentServiceURL = backendServerURL + "BPMNDeploymentService";
        deploymentServiceStub = new BPMNDeploymentServiceStub(deploymentServiceURL);
        AuthenticateStubUtil.authenticateStub(sessionCookie, deploymentServiceStub);

        String instanceServiceURL = backendServerURL + "BPMNInstanceService";
        instanceServiceStub = new BPMNInstanceServiceStub(instanceServiceURL);
        AuthenticateStubUtil.authenticateStub(sessionCookie, instanceServiceStub);
    }

    public void startProcess(String processId) throws Exception {
        instanceServiceStub.startProcess(processId);
    }

    public BPMNDeployment[] getDeployments() throws Exception {
        return deploymentServiceStub.getDeployments();
    }

    public int getInstanceCount() throws Exception {
        return instanceServiceStub.getInstanceCount();
    }

    public BPMNProcess[] getProcesses() throws Exception {
        return deploymentServiceStub.getDeployedProcesses();
    }

    public BPMNProcess getProcessById(String processId) throws Exception {
        BPMNProcess bpmnProcess = null;
        for (BPMNProcess process : deploymentServiceStub.getDeployedProcesses()) {
            if (process.getProcessId().equals(processId)) {
                bpmnProcess = process;
            }
        }
        return bpmnProcess;
    }

    public BPMNInstance[] getProcessInstances() throws Exception {
        return instanceServiceStub.getProcessInstances();
    }

    public BPMNInstance getProcessInstanceById(String instanceId) throws Exception {
        for (BPMNInstance instance : instanceServiceStub.getProcessInstances()) {
            if (instance.getInstanceId().equals(instanceId)) {
                return instance;
            }
        }
        return null;
    }

    public void deleteProcessInstance(String instanceID) throws Exception {
        instanceServiceStub.deleteProcessInstance(instanceID);
    }

    public void suspendProcessInstance(String instanceID) throws Exception {
        instanceServiceStub.suspendProcessInstance(instanceID);
    }

    public void activateProcessInstance(String instanceID) throws Exception {
        instanceServiceStub.activateProcessInstance(instanceID);
    }

    public BPMNProcess[] getProcessListByDeploymentID(String deploymentID) throws Exception {
        List<BPMNProcess> processes = new ArrayList<BPMNProcess>();
        for (BPMNProcess process : getProcesses()) {
            if (process.getDeploymentId().equals(deploymentID)) {
                processes.add(process);
            }

        }
        return processes.toArray(new BPMNProcess[processes.size()]);
    }

    public String getProcessDiagram(String processId) throws Exception {
        String imageString = deploymentServiceStub.getProcessDiagram(processId);
        BufferedImage bufferedImage = decodeToImage(imageString);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", baos);
        baos.flush();
        String dataUri = "data:image/png;base64," +
                DatatypeConverter.printBase64Binary(baos.toByteArray());
        baos.close();
        return dataUri;
    }

    public String getProcessModel(String processId) throws Exception {
        String tRawXML = deploymentServiceStub.getProcessModel(processId);
        tRawXML = tRawXML.replaceAll("\n|\\r|\\f|\\t", "");
        tRawXML = tRawXML.replaceAll("> +<", "><");
        InputStream xmlIn = new ByteArrayInputStream(tRawXML.getBytes());
        XMLPrettyPrinter xmlPrettyPrinter = new XMLPrettyPrinter(xmlIn);
        tRawXML = xmlPrettyPrinter.xmlFormat().replaceAll("<", "&lt").replaceAll(">", "&gt");
        return tRawXML;
    }

    public void undeploy(String deploymentName) throws Exception {
        deploymentServiceStub.undeploy(deploymentName);
    }

    private BufferedImage decodeToImage(String imageString) {

        BufferedImage image = null;
        byte[] imageByte;
        try {
            BASE64Decoder decoder = new BASE64Decoder();
            imageByte = decoder.decodeBuffer(imageString);
            ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
            image = ImageIO.read(bis);
            bis.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return image;
    }
}
