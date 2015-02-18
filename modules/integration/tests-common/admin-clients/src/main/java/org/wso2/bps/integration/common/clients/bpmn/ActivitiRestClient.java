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

package org.wso2.bps.integration.common.clients.bpmn;

import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.apache.commons.logging.Log;

import java.io.File;
import java.io.IOException;

/**
 * This Class contains methods which uses the BPMN Rest Services to carryout BPMN Task
 *
 * @author WSO2
 * @version 1.0
 */
public class ActivitiRestClient {

	private static final Log log = LogFactory.getLog(ActivitiRestClient.class);
	private final static String username = "admin";
	private final static String password = "admin";
	private final static String userClaim = "paul";
	private final static String userDelegate = "will";
	private int port;
	private String hostname = "";
	private String serviceURL = "";


	public ActivitiRestClient(String portM, String hostnameM){
		port = Integer.parseInt(portM);
		hostname = hostnameM;
		serviceURL = "http://" + hostnameM +":" + portM +"/bpmnrest/";
	}

	/**
	 * Used put the thread in sleep until the tasks are generated
	 */
	public void waitForTaskGeneration() throws InterruptedException{
		try {
			Thread.sleep(1000);
		}
		catch (InterruptedException ie) {
			log.error(" Thread Interuppted", ie);
		}
	}

	//region DeploymentMethods
	/**
	 * This Method is used to deploy BPMN packages to the BPMN Server
	 *
	 * @param fileName The name of the Package to be deployed
	 * @param filePath The location of the BPMN package to be deployed
	 * @returns String array with status, deploymentID and Name
	 * @throws java.io.IOException
	 * @throws org.json.JSONException
	 */
	public String[] deployBPMNPackage(String filePath, String fileName) throws IOException,JSONException{
		String url = serviceURL+"repository/deployments";

		HttpHost target = new HttpHost(hostname,port,"http");

		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials(
				new AuthScope(target.getHostName(),target.getPort()),
		        new UsernamePasswordCredentials(username,password)
				);
		try {

			HttpPost httpPost = new HttpPost(url);

			MultipartEntityBuilder builder = MultipartEntityBuilder.create();
			builder.addBinaryBody("file", new File(filePath),
			                      ContentType.MULTIPART_FORM_DATA,fileName);
			HttpEntity multipart = builder.build();
			httpPost.setEntity(multipart);
			HttpResponse response = httpClient.execute(httpPost);
			String status = response.getStatusLine().toString();
			String responseData = EntityUtils.toString(response.getEntity());
			JSONObject jsonResponseObject = new JSONObject(responseData);
			if (status.contains("201") || status.contains("200")) {
				String deploymentID = jsonResponseObject.getString("id");
				String name = jsonResponseObject.getString("name");
				return new String[] { status, deploymentID, name };
			}
			if(status.contains("500")){
				String errorMessage = jsonResponseObject.getString("errorMessage");
				return new String [] {status,errorMessage};
			}
		}
		catch(IOException ioMessage){
			log.error("Data Post Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return new String[] { null, null, null };
	}


	/**
	 * Method is used to acquire deployment details using the deployment ID
	 *
	 * @param ID Deployment ID of the BPMN Package
	 * @returns String Array with status, deploymentID and Name
	 * @throws java.io.IOException
	 * @throws org.json.JSONException
	 */
	public String[] getDeploymentById(String ID) throws IOException,JSONException {

		String url = serviceURL+"repository/deployments/"
		             +ID;
		HttpHost target = new HttpHost(hostname,port,"http");
		
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));

		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			String status = response.getStatusLine().toString();
			String responseData = EntityUtils.toString(response.getEntity());
			JSONObject jsonResponseObject = new JSONObject(responseData);
			if (status.contains("201") || status.contains("200")) {
				String deploymentID = jsonResponseObject.getString("id");
				String name = jsonResponseObject.getString("name");
				return new String[] { status, deploymentID, name };
			}
		}
		catch (IOException ioMessage){
			log.error("Get Request Failure",ioMessage);
		}
		catch (JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return new String[] { null, null, null };
	}

	/**
	 * Method is used to undeploy/remove a deployment from the server
	 *
	 * @param deploymentID used to identify the deployment to be removed
	 * @return String with the Status
	 * @throws IOException
	 */

	public String unDeployPackage(String deploymentID) throws IOException{

		String url = serviceURL+"repository/deployments/"
		             + deploymentID;
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpDelete httpDelete = new HttpDelete(url);
			HttpResponse response = httpClient.execute(httpDelete);
			return response.getStatusLine().toString();
		}
		catch (IOException ioMessage){
			log.error("Delete Request Failure",ioMessage);
		}
		return null;
	}
	//endregion

		//region Process Definition Methods

	/**
	 * Method to find the definitionID which is necessary to start a process instance
	 *
	 * @param deploymentID the deployment id is used to identify the deployment uniquely
	 * @return String Array containing status and definitionID
	 * @throws IOException
	 * @throws JSONException
	 */
	public String[] findProcessDefinitionsID(String deploymentID) throws IOException,JSONException{
		String url = serviceURL+"repository/process-definitions";
		String definitionId ="";
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);

			String status = response.getStatusLine().toString();
			String responseData = EntityUtils.toString(response.getEntity());
			JSONObject jsonResponseObject = new JSONObject(responseData);
			JSONArray data = jsonResponseObject.getJSONArray("data");

			int responseObjectSize = Integer.parseInt(jsonResponseObject.get("total").toString());

			for (int j = 0; j < responseObjectSize; j++) {
				if (data.getJSONObject(j).getString("deploymentId").equals(deploymentID)) {
					definitionId = data.getJSONObject(j).getString("id");
				}
			}
			return new String[] { status, definitionId };
		}
		catch(IOException ioMessage){
			log.error("Get Request Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}

		return new String[] { null, null, null };
	}
	//endregion

	//region Process-Instance Methods

	/**
	 * Methods used to test/search if the specifiy process instance is present
	 *
	 * @param processDefintionID used to start a processInstance
	 * @return String which contains the status
	 * @throws IOException
	 */
	public String searchProcessInstanceByDefintionID(String processDefintionID) throws IOException{
		String url = serviceURL+"query/process-instances";
		
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpPost httpPost = new HttpPost(url);
			StringEntity params = new StringEntity("{\"processDefinitionId\":\""
			                                       + processDefintionID
			                                       +"\"}",ContentType.APPLICATION_JSON);
			httpPost.setEntity(params);
			HttpResponse response = httpClient.execute(httpPost);
			return response.getStatusLine().toString();
		}
		catch(IOException ioMessage){
			log.error("Post Request Failure",ioMessage);
		}
		return null;
	}


	/**
	 * Method use to instantiate a process instance using the definition ID
	 *
	 * @param processDefintionID used to start a processInstance
	 * @returns String Array which contains status and processInstanceID
	 * @throws IOException
	 * @throws JSONException
	 */
	public String[] startProcessInstanceByDefintionID(String processDefintionID)  throws IOException,JSONException{

		String url = serviceURL+"runtime/process-instances";
		
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
		
			HttpPost httpPost = new HttpPost(url);
			StringEntity params = new StringEntity("{\"processDefinitionId\":\""
			                                       +processDefintionID+"\"}",
			                                       ContentType.APPLICATION_JSON);
			httpPost.setEntity(params);
			HttpResponse response = httpClient.execute(httpPost);

				String status = response.getStatusLine().toString();
				String responseData = EntityUtils.toString(response.getEntity());
				JSONObject jsonResponseObject = new JSONObject(responseData);
				if (status.contains("201") || status.contains("200")) {
					String processInstanceID = jsonResponseObject.getString("id");
					return new String[] { status, processInstanceID};
				}
				return new String[] { status, null};
		}
		catch(IOException ioMessage){
			log.error("Data Post Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return new String[] { null, null, null };
	}

	/**
	 * Method used to activate a process instance
	 *
	 * @param processDefintionID used to identify the process instance to activate
	 * @throws IOException
	 */
	public String activateProcessInstance(String processDefintionID) throws IOException{

		String url = serviceURL+"runtime/process-instances/" + processDefintionID;

		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));

		try {
			HttpPut httpPut = new HttpPut(url);
			StringEntity params = new StringEntity("{\"action\":\"activate\"}",
			                                       ContentType.APPLICATION_JSON);
			httpPut.setEntity(params);
			HttpResponse response = httpClient.execute(httpPut);
			return EntityUtils.toString(response.getEntity());
		}
		catch(IOException ioMessage){
			log.error("Put Request Failure",ioMessage);
		}
		return null;
	}

	/**
	 * Method used to suspend a process instance
	 *
	 * @param processInstanceID used to identify the process instance to suspend
	 * @return String array containing the status and the current state
	 * @throws IOException
	 * @throws JSONException
	 */
	public String[] suspendProcessInstance(String processInstanceID) throws IOException,JSONException{

		String url = serviceURL+"runtime/process-instances/" + processInstanceID;
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpPut httpPut = new HttpPut(url);
			StringEntity params = new StringEntity("{\"action\":\"suspend\"}",
			                                       ContentType.APPLICATION_JSON);
			httpPut.setEntity(params);
			HttpResponse response = httpClient.execute(httpPut);
			String status = response.getStatusLine().toString();
			String responseData = EntityUtils.toString(response.getEntity());
			JSONObject jsonResponseObject = new JSONObject(responseData);
			if (status.contains("201") || status.contains("200")) {
				String state = jsonResponseObject.getString("suspended");
				return new String[] { status, state };
			}
			return new String[] { status, null};
		}
		catch(IOException ioMessage){
			log.error("Put Request Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return new String[] { null, null, null };
	}

	/**
	 * Method to get the value of a variable in the process instance
	 *
	 * @param processInstanceId To identify the process instance
	 * @param variable to identify the variable name
	 * @return String Array containing status, name and the value of the variable
	 * @throws IOException
	 * @throws JSONException
	 */
	public String[] getValueOfVariableOfProcessInstance(String processInstanceId, String variable) throws IOException,JSONException {
		String url = serviceURL+"runtime/process-instances/"
		             + processInstanceId+"/variables/" +variable;
		String responseData = "";
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
				 new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			responseData = EntityUtils.toString(response.getEntity());
			JSONObject resObj = new JSONObject(responseData);
			String status = response.getStatusLine().toString();
			String name = resObj.getString("name");
			String value = resObj.getString("value");
			return new String[]{status, name, value};
		}
		catch(IOException ioMessage){
			log.error("Get Request Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return null;
	}

	/**
	 * Method used to remove/delete a process instance
	 *
	 * @param processInstanceId used to identify a process instance
	 * @return String value containing the status of the request.
	 * @throws IOException
	 */
	public String deleteProcessInstanceByID(String processInstanceId) throws IOException{
		String url = serviceURL+"runtime/process-instances/"
		             + processInstanceId;
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			
			HttpDelete httpDelete = new HttpDelete(url);
			HttpResponse response = httpClient.execute(httpDelete);
			return response.getStatusLine().toString();

		}
		catch(IOException ioMessage){
			log.error("Delete Request Failure",ioMessage);
		}
		return null;
	}
	//endregion

	//region Task Methods

	/**
	 * Method used to get the delegation state of a task
	 *
	 * @param taskID used to identify the task
	 * @return String which contains the state of the task
	 * @throws IOException
	 * @throws JSONException
	 */
	public String getDelegationsStateByTaskId(String taskID) throws IOException,JSONException{

		String url = serviceURL+"runtime/tasks/"+ taskID;
		String responseData = "";
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			responseData = EntityUtils.toString(response.getEntity());
			JSONObject resObj = new JSONObject(responseData);
			return resObj.getString("delegationState");
		}
		catch(IOException ioMessage){
			log.error("Get Request Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return null;
	}

	/**
	 * Method used to resolve a task
	 *
 	 * @param taskID used to identify the task
	 * @return String which contains the status of the request
	 * @throws IOException
	 */
	public String resolveTask(String taskID)throws IOException{
		String url = serviceURL+"runtime/tasks/"+ taskID;
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpPost httpPost = new HttpPost(url);
			StringEntity params = new StringEntity("{\"action\" : \"resolve\"}",
			                                       ContentType.APPLICATION_JSON);
			httpPost.setEntity(params);
			HttpResponse response = httpClient.execute(httpPost);
			return response.getStatusLine().toString();
		}
		catch(IOException ioMessage){
			log.error("Post Request Failure",ioMessage);
		}
		return null;
	}

	/**
	 * Method to get the asignee of a task
	 *
	 * @param taskID used to identify the task
	 * @return String containing the Asignee
	 * @throws IOException
	 * @throws JSONException
	 */
	public String getAssigneeByTaskId(String taskID) throws IOException,JSONException{

		String url = serviceURL+"runtime/tasks/"+ taskID;
		String responseData = "";
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			responseData = EntityUtils.toString(response.getEntity());
			JSONObject resObj = new JSONObject(responseData);
			return resObj.getString("assignee");
		}
		catch(IOException ioMessage){
			log.error("Get Request Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return null;
	}

	/**
	 * Method used to find task by using the process instance ID
	 * @param processInstanceId used to identify task through a process instance
	 * @return String Array containing status and the taskID
	 * @throws IOException
	 * @throws JSONException
	 */
	public String[] findTaskIdByProcessInstanceID(String processInstanceId)throws IOException,JSONException{

		String url = serviceURL+"runtime/tasks";
		String taskId ="";

		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			String status = response.getStatusLine().toString();
			String responseData = EntityUtils.toString(response.getEntity());
			JSONObject jsonResponseObject = new JSONObject(responseData);
			JSONArray data = jsonResponseObject.getJSONArray("data");
			int responseObjectSize = Integer.parseInt(jsonResponseObject.get("total")
			                                                            .toString());
			for(int j=0; j < responseObjectSize; j++){
				if (data.getJSONObject(j).getString("processInstanceId").equals(
																	processInstanceId)){
					taskId = data.getJSONObject(j).getString("id");
				}

			}
			return new String[] { status,taskId };
		}
		catch(IOException ioMessage){
			log.error("Get Request Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return new String[] { null, null };
	}

	/**
	 * Method to claim task by a user
	 * @param taskID used to identify the task to be claimed
	 * @return String Array containing status
	 * @throws IOException
	 */
	public String[] claimTask(String taskID)throws IOException{
		String url = serviceURL+"runtime/tasks/"+ taskID;

		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpPost httpPost = new HttpPost(url);
			StringEntity params = new StringEntity("{\"action\" : \"claim\"," +
			                                       "\"assignee\" :\""+userClaim+"\"}",
			                                       ContentType.APPLICATION_JSON);
			
			httpPost.setEntity(params);
			HttpResponse response = httpClient.execute(httpPost);
			String status = response.getStatusLine().toString();
			return new String[] { status, null};

		}
		catch(IOException ioMessage){
			log.error("Post Request Failure",ioMessage);
		}
		return new String[] { null, null };
	}

	/**
	 * Mehtod to delegate a task to certain user
	 *
	 * @param taskID used to identify the task to be delegated
	 * @return String with the status of the delegation
	 * @throws IOException
	 */
	public String delegateTask(String taskID)throws IOException{
		String url = serviceURL+"runtime/tasks/"+ taskID;

		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			
			HttpPost httpPost = new HttpPost(url);
			StringEntity params = new StringEntity("{\"action\" : \"delegate\"," +
			                                       "\"assignee\" :\""+userDelegate+"\"}",
			                                       ContentType.APPLICATION_JSON);
			httpPost.setEntity(params);
			HttpResponse response;
			response = httpClient.execute(httpPost);
			return response.getStatusLine().toString();

		}
		catch(IOException ioMessage){
			log.error("Post Request Failure",ioMessage);
		}
		return null;
	}

	/**
	 * Method used to add a new comment to a task
	 *
	 * @param taskID used to identify the task
	 * @param comment comment to be added
	 * @return String Array containing status and the message
	 * @throws IOException
	 * @throws JSONException
	 */
	public String[] addNewCommentOnTask(String taskID, String comment)throws IOException, JSONException{
		String url = serviceURL+"runtime/tasks/"+ taskID+"/comments";
		
		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpPost httpPost = new HttpPost(url);
			StringEntity params = new StringEntity("{\"message\" : \""+comment
			                                       +"\",\"saveProcessInstanceId\" : true}",
			                                       ContentType.APPLICATION_JSON);
			httpPost.setEntity(params);
			HttpResponse response = httpClient.execute(httpPost);
			String status = response.getStatusLine().toString();
			String responseData = EntityUtils.toString(response.getEntity());
			JSONObject jsonResponseObject = new JSONObject(responseData);
			if (status.contains("201") || status.contains("200")) {
				String message = jsonResponseObject.getString("message");
				return new String[] { status, message };
			}
			return new String[] { status, null};
		}
		catch(IOException ioMessage){
			log.error("Post Request Failure",ioMessage);
		}
		catch(JSONException jsonMessage){
			log.error("JSON Conversion Failure",jsonMessage);
		}
		return new String[] { null, null };
	}

	/**
	 * Method to delete a task
	 *
	 * @param taskId used to identify a task
	 * @param cascadeHistory boolean to either delete the task history or not
	 * @param deleteReason reason for deleteing the task
	 * @return String containing the status of the request
	 * @throws IOException
	 */
	public String deleteTask(String taskId, boolean cascadeHistory, String deleteReason) throws IOException{

		String url = serviceURL+"runtime/tasks/"
		             +taskId+"?cascadeHistory="+cascadeHistory+"&deleteReason="
		             +deleteReason;

		HttpHost target = new HttpHost(hostname,port,"http");
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpDelete httpDelete = new HttpDelete(url);
			HttpResponse response = httpClient.execute(httpDelete);
			return response.getStatusLine().toString();
		}
		catch(IOException ioMessage){
			log.error("Delete Request Failure",ioMessage);
		}
		return null;
	}
	//endregion

}
