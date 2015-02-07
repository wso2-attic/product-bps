/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.bps.humantask.sample.manager;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamException;

import org.apache.axis2.databinding.types.URI;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalAccessFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalArgumentFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalOperationFault;
import org.wso2.carbon.humantask.stub.ui.task.client.api.IllegalStateFault;

public class TaskManager extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1534302868383827750L;

	/**
	 * 
	 */

	protected void doPost(final HttpServletRequest req,
			final HttpServletResponse resp) throws ServletException,
			IOException {

		String operation = req.getParameter("operation");
		String taskID = req.getParameter("taskID");
		System.out.println(operation);
		System.out.println(taskID);
		if (operation.equals("start")) {
			try {
				LoginManager.taskAPIClient.start(new URI(taskID));
			} catch (IllegalStateFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalOperationFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			req.getRequestDispatcher(
					"Task.jsp?queryType=assignedToMe&taskId=" + taskID)
					.forward(req, resp);
		} else if (operation.equals("complete")) {

			String payload = req.getParameter("payload");
			System.out.println(payload);
			try {
				LoginManager.taskAPIClient.complete(new URI(taskID), payload);
			} catch (IllegalAccessFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalOperationFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XMLStreamException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			req.getRequestDispatcher(
					"/Home.jsp?queryType=assignedToMe&pageNumber=0").forward(
					req, resp);
		} else if (operation.equals("stop")) {
			try {
				LoginManager.taskAPIClient.stop(new URI(taskID));
			} catch (IllegalStateFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalOperationFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalArgumentFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessFault e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			req.getRequestDispatcher(
					"Task.jsp?queryType=assignedToMe&taskId=" + taskID)
					.forward(req, resp);

		}

	}
}
