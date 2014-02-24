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
package org.wso2.carbon.humantask.sample.manager;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.axis2.context.ConfigurationContext;
import org.apache.axis2.context.ServiceContext;
import org.apache.axis2.transport.http.HTTPConstants;
import org.wso2.carbon.humantask.sample.clients.HumanTaskClientAPIServiceClient;
import org.wso2.carbon.humantask.sample.clients.LoginAdminServiceClient;

public class LoginManager extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 9221378012392594537L;
	public static HumanTaskClientAPIServiceClient taskAPIClient;
	public static LoginAdminServiceClient login ;
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		Properties prop = new Properties();

		try {
			
			String logout= req.getParameter("logout");
			if(logout!=null){
				LoginManager.login.logOut();
				req.getRequestDispatcher("/Login.jsp").forward(req, resp);
				return;
				
			}

			// getting configuration properties
			prop.load(getClass().getClassLoader().getResourceAsStream(
					"org/wso2/carbon/humantask/sample/config.properties"));

			String BACK_END_URL = prop.getProperty("BACK_END_URL");
			String BPS_JKS_PATH = prop.getProperty("BPS_JKS_PATH");
			String userName = req.getParameter("userName").trim();
			String userPassword = req.getParameter("userPassword").trim();

			System.setProperty("javax.net.ssl.trustStore", BPS_JKS_PATH);
			System.setProperty("javax.net.ssl.trustStorePassword", "wso2carbon");
			System.setProperty("javax.net.ssl.trustStoreType", "JKS");

			// login to server with given user name and password
			login = new LoginAdminServiceClient(BACK_END_URL);
			ServiceContext serviceContext = login.authenticate(userName,
					userPassword);
			String sessionCookie = (String) serviceContext
					.getProperty(HTTPConstants.COOKIE_STRING);
			System.out.println(sessionCookie);
			ConfigurationContext configContext = serviceContext
					.getConfigurationContext();
			if (sessionCookie == null) {
				System.out.println("Login failed.");
				req.setAttribute("message",
						"Please enter a valid user name and a password.");
				req.getRequestDispatcher("/Login.jsp").forward(req, resp);
				return;
			}

			HttpSession session = req.getSession();
			taskAPIClient = new HumanTaskClientAPIServiceClient(sessionCookie,
					BACK_END_URL + "/services/", configContext);
			
			session.setAttribute("USER_NAME", userName);
			req.getRequestDispatcher(
					"/Home.jsp?queryType=assignedToMe&pageNumber=0").forward(
					req, resp);
		} catch (Exception e) {

			System.out.println(e);
			req.setAttribute("message", e);
			req.getRequestDispatcher("/Login.jsp").forward(req, resp);
		}

	}
}
