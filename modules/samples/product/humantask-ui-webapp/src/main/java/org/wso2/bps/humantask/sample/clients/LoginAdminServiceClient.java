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
package org.wso2.bps.humantask.sample.clients;

import org.apache.axis2.AxisFault;  
import org.wso2.carbon.authenticator.stub.AuthenticationAdminStub;  
import org.wso2.carbon.authenticator.stub.LoginAuthenticationExceptionException;  
import org.wso2.carbon.authenticator.stub.LogoutAuthenticationExceptionException;  
import org.apache.axis2.context.ServiceContext;  
import java.rmi.RemoteException;  
  
public class LoginAdminServiceClient {  
	  private final String serviceName = "AuthenticationAdmin";  
	    private AuthenticationAdminStub authenticationAdminStub;  
	    private String endPoint;  
	    private ServiceContext serviceContext;
	    public LoginAdminServiceClient(String backEndUrl) throws AxisFault {  
	      this.endPoint = backEndUrl + "/services/" + serviceName;  
	      authenticationAdminStub = new AuthenticationAdminStub(endPoint);  
	    }  
	  
	    public ServiceContext authenticate(String userName, String password) throws RemoteException,  
	                                      LoginAuthenticationExceptionException {  
	  
	  
	      if (authenticationAdminStub.login(userName, password, "localhost")) {  
	        System.out.println("Login Successful");  
	  
	        serviceContext = authenticationAdminStub.  
	            _getServiceClient().getLastOperationContext().getServiceContext();   
	      }  
	  
	      return serviceContext;  
	    }  
	  
	    public void logOut() throws RemoteException, LogoutAuthenticationExceptionException {  
	      authenticationAdminStub.logout();  
	    } 
	    
	    
	} 
