package org.wso2.bps.integration.common.clients.bpmn;

import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.logging.Log;

import java.io.File;

public class ActivitiRestClient {

	private final static String username = "admin";
	private final static String password = "admin";
	private final static String userClaim = "paul";
	private final static String userDelegate = "will";
	private final static int port = 9763 ;
	private final static String preURL = "http://localhost:"+port+"/bpmnrest/";
	private static final Log log = LogFactory.getLog(ActivitiRestClient.class);

	public void waitForTaskGeneration(){
		try {
			Thread.sleep(1000);
		} catch (InterruptedException ie) {

		}

	}

	//region DeploymentMethods
	public String[] deployBPMNPackage(String filePath, String fileName) throws Exception{
		String url = preURL+"repository/deployments";

		HttpHost target = new HttpHost("localhost",port,"http");

		CredentialsProvider credProvider = new BasicCredentialsProvider();
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

				try {
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
					return new String[] { status, null, null };

				}
				catch (Exception message) {
					log.error("Response Data", message);
				}

			}
			catch (Exception message) {
				log.error("Data Post Failure", message);
			}
		return new String[] { null, null, null };
	}

	
	public String[] getDeploymentById(String ID) throws Exception{

		String url = preURL+"repository/deployments/"
		             +ID;
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));

			try {
				HttpGet httpget = new HttpGet(url);
				HttpResponse response = httpClient.execute(httpget);
				try {
					String status = response.getStatusLine().toString();
					String responseData = EntityUtils.toString(response.getEntity());
					JSONObject jsonResponseObject = new JSONObject(responseData);
					if (status.contains("201") || status.contains("200")) {
						String deploymentID = jsonResponseObject.getString("id");
						String name = jsonResponseObject.getString("name");
						return new String[] { status, deploymentID, name };
					}
					return new String[] { status, null, null };

				}
				catch (Exception message) {
					log.error("Response Data", message);
				}

			}
			catch (Exception message) {
				log.error("Get Request Failure", message);
			}

		return new String[] { null, null, null };
	}


	public String unDeployPackage(String deploymentID) throws Exception{

		String url = preURL+"repository/deployments/"
		             + deploymentID;

		HttpHost target = new HttpHost("localhost",port,"http");

		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpDelete httpDelete = new HttpDelete(url);
			HttpResponse response = httpClient.execute(httpDelete);
			try {
				String status = response.getStatusLine().toString();
				return status;
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}

		}
		catch (Exception message) {
			log.error("Delete Request Failure", message);
		}
		return null;
	}
	//endregion

	//region Process Definition Methods
	public String[] FindProcessDefinitionsID(String deploymentID)throws Exception{
		String url = preURL+"repository/process-definitions";
		String definitionId ="";

		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute( httpget);
			try {
				String status = response.getStatusLine().toString();
				String responseData = EntityUtils.toString(response.getEntity());
				JSONObject jsonResponseObject = new JSONObject(responseData);
				JSONArray data = jsonResponseObject.getJSONArray("data");

				int responseObjectSize = Integer.parseInt(jsonResponseObject.get("total").toString());

				for(int j=0; j < responseObjectSize; j++){
					if (data.getJSONObject(j).getString("deploymentId").equals(deploymentID)){
						definitionId = data.getJSONObject(j).getString("id");
					}
				}
				return new String[] { status,definitionId };
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}
		}
		catch (Exception message) {
			log.error("Get Request Failure", message);
		}
		return new String[] { null, null, null };
	}
	//endregion

	//region Process-Instance Methods
	public String searchProcessInstanceByDefintionID(String processDefintionID) throws Exception{
		String url = preURL+"query/process-instances";
		
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
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
			try {
				String status = response.getStatusLine().toString();
				String responseData = EntityUtils.toString(response.getEntity());
				return status;
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}
		}
		catch (Exception message) {
			log.error("Data Post Failure", message);
		}
		return null;
	}


	public String[] startProcessInstanceByDefintionID(String processDefintionID) throws Exception{

		String url = preURL+"runtime/process-instances";
		
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
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
			try {
				String status = response.getStatusLine().toString();
				String responseData = EntityUtils.toString(response.getEntity());
				JSONObject jsonResponseObject = new JSONObject(responseData);
				if (status.contains("201") || status.contains("200")) {
					String processInstanceID = jsonResponseObject.getString("id");
					return new String[] { status, processInstanceID};
				}
				return new String[] { status, null};
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}

		}
		catch (Exception message) {
			log.error("Data Post Failure", message);
		}

		return new String[] { null, null, null };
	}

	public void activateProcessInstance(String processDefintionID) throws Exception{

		String url = preURL+"runtime/process-instances/" + processDefintionID;

		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
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
			try {
				String responseData = EntityUtils.toString(response.getEntity());
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}

		}
		catch (Exception message) {
			log.error("Put Request Failure", message);
		}
	}


	public String[] suspendProcessInstance(String processInstanceID) throws Exception{

		String url = preURL+"runtime/process-instances/" + processInstanceID;
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
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

			try {
				String status = response.getStatusLine().toString();
				String responseData = EntityUtils.toString(response.getEntity());
				JSONObject jsonResponseObject = new JSONObject(responseData);
				if (status.contains("201") || status.contains("200")) {
					String state = jsonResponseObject.getString("suspended");
					return new String[] { status, state };
				}
				return new String[] { status, null};
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}
		}
		catch (Exception message) {
			log.error("Put Request Failure", message);
		}
		return new String[] { null, null, null };
	}


	public String[] getValueOfVariableOfProcessInstance(String processInstanceId, String variable) throws Exception{
		String url = preURL+"runtime/process-instances/"
		             + processInstanceId+"/variables/" +variable;
		String responseData = "";
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
				 new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			try {
				responseData = EntityUtils.toString(response.getEntity());
				JSONObject resObj = new JSONObject(responseData);
				String status = response.getStatusLine().toString();
				String name = resObj.getString("name");
				String value = resObj.getString("value");
				return new String[]{status, name, value};
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}

		}
		catch (Exception message) {
			log.error("Get Request Failure", message);
		}
		return null;
	}



	public String deleteProcessInstanceByID(String processInstanceId) throws Exception{
		String url = preURL+"runtime/process-instances/"
		             + processInstanceId;
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			
			HttpDelete httpDelete = new HttpDelete(url);
			HttpResponse response = httpClient.execute(httpDelete);
			try {
				String status = response.getStatusLine().toString();
				return status;
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}

		}
		catch (Exception message) {
			log.error("Delete Request Failure", message);
		}
		return null;
	}
	//endregion

	//region Task Methods
	public String getDelegationsStateByTaskId(String taskID) throws Exception{

		String url = preURL+"runtime/tasks/"+ taskID;
		String responseData = "";
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			try {
				responseData = EntityUtils.toString(response.getEntity());
				JSONObject resObj = new JSONObject(responseData);
				String state = resObj.getString("delegationState");
				return state;
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}

		}
		catch (Exception message) {
			log.error("Get Request Failure", message);
		}
		return null;
	}

	public String resolveTask(String taskID)throws Exception{
		String url = preURL+"runtime/tasks/"+ taskID;
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
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
			try {
				String status = response.getStatusLine().toString();
				return status;
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}
		}
		catch (Exception message) {
			log.error("Data Post Failure", message);
		}
		return null;
	}

	public String getAssigneeByTaskId(String taskID) throws Exception{

		String url = preURL+"runtime/tasks/"+ taskID;
		String responseData = "";
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			try {
				responseData = EntityUtils.toString(response.getEntity());
				JSONObject resObj = new JSONObject(responseData);
				String assignee = resObj.getString("assignee");
				return assignee;
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}

		}
		catch (Exception message) {
			log.error("Get Request Failure", message);
		}
		return null;
	}

	public String[] FindTaskIdByProcessInstanceID(String processInstanceId)throws Exception{

		String url = preURL+"runtime/tasks";
		String taskId ="";

		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpGet httpget = new HttpGet(url);
			HttpResponse response = httpClient.execute(httpget);
			try {
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
			catch (Exception message) {
				log.error("Response Data", message);
			}
		} 
		catch (Exception message) {
			log.error("Get Request Failure", message);
		}
		return new String[] { null, null, null };
	}

	public String[] claimTask(String taskID)throws Exception{
		String url = preURL+"runtime/tasks/"+ taskID;

		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
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

			try {
				String status = response.getStatusLine().toString();
//				String responseData = EntityUtils.toString(response.getEntity());
//				if (status.contains("201") || status.contains("200")) {
//					String user = jsonResponseObject.getString("assignee");
//					return new String[] { status, user };
//				}
				return new String[] { status, null};
			} 
			catch (Exception message) {
				log.error("Response Data", message);
			}

		} 
		catch (Exception message) {
			log.error("Data Post Failure", message);
		}

		return new String[] { null, null, null };
	}

	public String delegateTask(String taskID)throws Exception{
		String url = preURL+"runtime/tasks/"+ taskID;

		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
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
			HttpResponse response = httpClient.execute(httpPost);
			try {
				String status = response.getStatusLine().toString();
				return status;
			}
			catch (Exception message) {
				message.printStackTrace();
			}

		}
		catch (Exception message) {
			message.printStackTrace();
		}
		return null;
	}

	public String[] addNewCommentOnTask(String taskID, String comment)throws Exception{
		String url = preURL+"runtime/tasks/"+ taskID+"/comments";
		
		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
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
			try {
				String status = response.getStatusLine().toString();
				String responseData = EntityUtils.toString(response.getEntity());
				JSONObject jsonResponseObject = new JSONObject(responseData);
				if (status.contains("201") || status.contains("200")) {
					String message = jsonResponseObject.getString("message");
					return new String[] { status, message };
				}
				return new String[] { status, null};
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}

		}
		catch (Exception message) {
			log.error("Data Post Failure", message);
		}
		return new String[] { null, null, null };
	}

	public String deleteTask(String taskId, boolean cascadeHistory, String deleteReason) throws Exception{

		String url = preURL+"runtime/tasks/"
		             +taskId+"?cascadeHistory="+cascadeHistory+"&deleteReason="
		             +deleteReason;

		HttpHost target = new HttpHost("localhost",port,"http");
		CredentialsProvider credProvider = new BasicCredentialsProvider();
		DefaultHttpClient httpClient = new DefaultHttpClient();
		httpClient.getCredentialsProvider().setCredentials
				(new AuthScope(target.getHostName(),target.getPort()),
		         new UsernamePasswordCredentials(username,password));
		try {
			HttpDelete httpDelete = new HttpDelete(url);
			HttpResponse response = httpClient.execute(httpDelete);
			try {
				String status = response.getStatusLine().toString();
				return status;
			}
			catch (Exception message) {
				log.error("Response Data", message);
			}
		}
		catch (Exception message) {
			log.error("Delete Request Failure", message);
		}
		return null;
	}
	//endregion

}
