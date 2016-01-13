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
import org.apache.http.HttpResponse;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.wso2.bps.integration.common.clients.bpmn.WorkflowServiceClient;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class BPMNTestUtils {

    private static final Log log = LogFactory.getLog(BPMNTestUtils.class);
    private static final String BACKEND_URL_SUFFIX = "services";
    private static final String BPMN_REST_URL_SUFFIX = "bpmn";


    /**
     * BPMN does not expose any service to check deployment is done.
     * So we are using deployment count to check completeness of process deployment
     * @param workflowServiceClient
     * @param bpmnPackageName
     * @param previousDeploymentCount
     * @throws Exception
     */
    public static void waitForProcessDeployment(WorkflowServiceClient workflowServiceClient, String bpmnPackageName,
                                                int previousDeploymentCount) throws Exception {
        int serviceTimeOut = 0;

        while (true) {
            if (workflowServiceClient.getDeployments() != null && workflowServiceClient.getDeployments().length >
                                                                  previousDeploymentCount) {
                return;
            }
            if (serviceTimeOut == 0) {
                log.info("Waiting for BPMN package" + bpmnPackageName + " to deploy.");
            } else if (serviceTimeOut > BPMNTestConstants.PROCESS_DEPLOYMENT_MAX_RETRY_COUNT) {
                String errMsg = bpmnPackageName + " package is not found";
                log.error(errMsg);
                throw new Exception(errMsg);
            }
            try {
                serviceTimeOut++;
                Thread.sleep(BPMNTestConstants.PROCESS_DEPLOYMENT_WAIT_TIME_PER_RETRY);
            } catch (InterruptedException e) {
                //Log the interrupt exception and wait for process deployment until timeout
                log.error("Error while waiting for process deployment " + e);
            }
        }
    }

    /**
     * Returns BPMN rest endpoint from bps backend url
     * @param backEndUrl
     * @return bpmnUrl
     */
    public static String getRestEndPoint(String backEndUrl) {
        return backEndUrl.replaceFirst(BACKEND_URL_SUFFIX,BPMN_REST_URL_SUFFIX);
    }

    /**
     * Returns HTTP GET response entity string from given url using admin credentials
     * @param url request url suffix
     * @return HttpResponse from get request
     */
    public static String getRequest(String url) throws IOException {

        String restUrl = getRestEndPoint(url);
        log.info("Sending HTTP GET request: " + restUrl);
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(restUrl);

        request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("admin", "admin"), "UTF-8", false));
        client.getConnectionManager().closeExpiredConnections();
        HttpResponse response = client.execute(request);
        return EntityUtils.toString(response.getEntity());
    }

    /**
     * Returns HTTP GET response from given url using admin credentials
     * @param url request url suffix
     * @return
     * @throws IOException
     */
    public static HttpResponse getRequestResponse(String url) throws IOException {

        String restUrl = getRestEndPoint(url);
        log.info("Sending HTTP GET request: " + restUrl);
        HttpClient client = new DefaultHttpClient();
        HttpGet request = new HttpGet(restUrl);

        request.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("admin", "admin"), "UTF-8", false));
        client.getConnectionManager().closeExpiredConnections();
        HttpResponse response = client.execute(request);
        return response;
    }

    /**
     * Returns response after the given POST request
     * @param url string url suffix to post the request
     * @param payload request payload
     * @return HttpResponse for the post request
     * @throws IOException
     */
    public static HttpResponse postRequest(String url, JSONObject payload) throws IOException {
        String restUrl = getRestEndPoint(url);
        log.info("Sending HTTP POST request: " + restUrl);
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(restUrl);
        post.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("admin", "admin"), "UTF-8", false));
        post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        client.getConnectionManager().closeExpiredConnections();
        HttpResponse response = client.execute(post);
        return response;
    }

    /**
     * Returns response after the given POST request
     * @param url string url suffix to post the request
     * @param payload request payload
     * @return HttpResponse for the post request
     * @throws IOException
     */
    public static HttpResponse postRequest(String url, JSONArray payload) throws IOException, JSONException {
        String restUrl = getRestEndPoint(url);
        log.info("Sending HTTP POST request: " + restUrl);
        HttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(restUrl);
        post.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("admin", "admin"), "UTF-8", false));
        post.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        client.getConnectionManager().closeExpiredConnections();
        HttpResponse response = client.execute(post);
        return response;
    }

    /**
     * Returns response after the given DELETE request
     * @param url string url suffix to delete the request
     * @return HttpResponse for the post request
     * @throws IOException
     */
    public static HttpResponse deleteRequest(String url) throws IOException {
        String restUrl = getRestEndPoint(url);
        log.info("Sending HTTP DELETE request: " + restUrl);
        HttpClient client = new DefaultHttpClient();
        HttpDelete delete = new HttpDelete(restUrl);
        delete.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("admin", "admin"), "UTF-8", false));
        client.getConnectionManager().closeExpiredConnections();
        HttpResponse response = client.execute(delete);
        return response;
    }

    /**
     *
     * @param url
     * @param payload
     * @return
     * @throws IOException
     */
    public static HttpResponse putRequest(String url, JSONObject payload) throws IOException {
        String restUrl = getRestEndPoint(url);
        HttpClient client = new DefaultHttpClient();
        HttpPut put = new HttpPut(restUrl);
        put.addHeader(BasicScheme.authenticate(new UsernamePasswordCredentials("admin", "admin"), "UTF-8", false));
        put.setEntity(new StringEntity(payload.toString(), ContentType.APPLICATION_JSON));
        client.getConnectionManager().closeExpiredConnections();
        HttpResponse response = client.execute(put);
        return response;
    }

//    public static ProcessEngine getBPMNEngine() throws FileNotFoundException, BPMNException {
//        initializeBPMNConfigBeans();
//        ProcessEngine processEngine = null;
//        String carbonConfigDirPath = CarbonUtils.getCarbonConfigDirPath();
//        String activitiConfigPath = carbonConfigDirPath + File.separator +
//                BPMNTestConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
//        File activitiConfigFile = new File(activitiConfigPath);
//        ProcessEngineConfigurationImpl processEngineConfigurationImpl =
//                (ProcessEngineConfigurationImpl) ProcessEngineConfiguration
//                        .createProcessEngineConfigurationFromInputStream(new FileInputStream(activitiConfigFile));
//        // we have to build the process engine first to initialize session factories.
//        processEngine = processEngineConfigurationImpl.buildProcessEngine();
//        processEngineConfigurationImpl.getSessionFactories().put(UserIdentityManager.class,
//                new BPSUserManagerFactory());
//        processEngineConfigurationImpl.getSessionFactories().put(GroupIdentityManager.class,
//                new BPSGroupManagerFactory());
//
//        String dataSourceJndiName = processEngineConfigurationImpl.getProcessEngineConfiguration()
//                .getDataSourceJndiName();
//
//        return processEngine;
//    }

//    private static void initializeBPMNConfigBeans() throws BPMNException {
//
//
//        String activitiConfigPath = CarbonUtils.getCarbonConfigDirPath() + File.separator + BPMNConstants.ACTIVITI_CONFIGURATION_FILE_NAME;
//        Map<String, BPMNBean> bpmnBeanMap = new HashMap<>();
//        File configFile = new File(activitiConfigPath);
//        try {
//            String configContent = FileUtils.readFileToString(configFile);
//            OMElement configElement = AXIOMUtil.stringToOM(configContent);
//            Iterator beans = configElement.getChildrenWithName(new QName(BPMNConstants.SPRING_NAMESPACE,
//                    BPMNConstants.BEAN));
//            while (beans.hasNext()) {
//                OMElement bean = (OMElement) beans.next();
//                BPMNBean bpmnBean = new BPMNBean(bean);
//                bpmnBeanMap.put(bpmnBean.getBeanId(), bpmnBean);
//            }
//
//            InitialContext ic = new InitialContext();
//
//            ic.createSubcontext("java:");
//            ic.createSubcontext("java:/comp");
//            ic.createSubcontext("java:/comp/env");
//            ic.createSubcontext("java:/comp/env/jdbc");
//
//        } catch (IOException e) {
//            String errMsg = "Error on reading activiti configuration file ";
//            throw new BPMNException(errMsg,e);
//        } catch (XMLStreamException e) {
//            String errMsg = "Malformed XML Error occured while processing activiti configuration file ";
//            throw new BPMNException(errMsg,e);
//        } catch (NamingException e) {
//            e.printStackTrace();
//        }
//    }
}
