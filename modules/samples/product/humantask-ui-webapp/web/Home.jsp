<%@page import="org.wso2.bps.humantask.sample.manager.LoginManager"%>
<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<%@ page import="javax.xml.namespace.*"%>
<%@ page import="org.apache.axis2.context.ConfigurationContext"%>
<%@ page import="org.apache.axis2.context.ServiceContext"%>
<%@ page import="org.apache.axis2.transport.http.HTTPConstants"%>
<%@ page
	import="org.wso2.carbon.humantask.sample.clients.HumanTaskClientAPIServiceClient"%>
<%@ page
	import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TPresentationName"%>
<%@ page
	import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TPresentationSubject"%>
<%@ page
	import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryCategory"%>
<%@ page
	import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TSimpleQueryInput"%>
<%@ page
	import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultRow"%>
<%@ page
	import="org.wso2.carbon.humantask.stub.ui.task.client.api.types.TTaskSimpleQueryResultSet"%>
<%@ page
	import="org.wso2.bps.humantask.sample.util.HumanTaskSampleUtil"%>
<%@ page
	import="javax.xml.namespace.QName"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN""http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link href="css/global.css" rel="stylesheet" type="text/css" media="all">
<link href="css/main.css" rel="stylesheet" type="text/css" media="all">
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Home</title>
</head>
<script type="text/javascript">
	function submitSignOutForm() {
		var taskForm = document.getElementById('signOutForm');
		taskForm.submit();
	}
	function submitSignOutForm() {

		var taskForm = document.getElementById('signOutForm');
		taskForm.submit();

	}
</script>
<body>
	<table id="main-table" border="0" cellspacing="0">
		<tbody>
			<tr>
				<td id="header" colspan="3">
					<div id="header-div">
						<div class="right-logo">
							Signed-in as:
							<%=session.getAttribute("USER_NAME")%></br>
							<form id="signOutForm" action="login" method="POST">
								<input type="hidden" id="logout" name="logout" value="" />
								<h6>
									<a href="#" onclick="submitSignOutForm()">Sign out</a>
								</h6>
							</form>
						</div>
						<div class="left-logo"></div>
						<div class="middle-ad"></div>
					</div>
				</td>
			</tr>
			<tr>
				<td id="middle-content">
					<table id="content-table" border="0" cellspacing="0">
						<tbody>
							<tr>
								<td id="body">
									<div id="middle">
										<table cellspacing="0" width="100%">
											<tbody>
												<tr>
													<td width="20%">
														<div id="features">
															<h3>Welcome to Human Task Web App sample!</h3>
														</div>
													</td>
													<td width="60%">
														<div id="workArea">
															<br />
															<table align="center" id="main-table">
																<tr>
																	<td><a href="Home.jsp?queryType=assignedToMe"
																		class="opbutton">My Tasks</a>
																	</td>
																	<td><a href="Home.jsp?queryType=claimableTasks"
																		class="opbutton">Claimable</a></td>
																	<td><a href="Home.jsp?queryType=adminTasks"
																		class="opbutton">Admin Tasks</a>
																	</td>
																	<td><a href="Home.jsp?queryType=notications"
																		class="opbutton">Notifications</a></td>
																	<td><a href="Home.jsp?queryType=allTasks"
																		class="opbutton">All Tasks</a>
																	</td>
																</tr>
															</table>
															<br>
															<%
																// Pagination related;
																int numberOfPages = 0;
																String pageNumber = (String) (request.getParameter("pageNumber"));
																int pageNumberInt = 0;
																String parameters = null;

																if (pageNumber == null) {
																	pageNumber = "0";
																}
																try {
																	pageNumberInt = Integer.parseInt(pageNumber);
																} catch (NumberFormatException ignored) {

																}

																String queryType = (String) (request.getParameter("queryType"));
																// String cookie = (String) session.getAttribute("SESSION_COOKIE");
																// String backendServerURL =   (String)session.getAttribute("BACK_END_URL");
																//ConfigurationContext configContext =(ConfigurationContext) session.getAttribute("CONFIG_CONTEXT");

																TTaskSimpleQueryResultSet taskResults = null;
																parameters = "queryType=" + queryType;

																try {

																	TSimpleQueryInput queryInput = new TSimpleQueryInput();
																	queryInput.setPageNumber(pageNumberInt);
																	queryInput
																			.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNED_TO_ME);

																	if (queryType != null && !"".equals(queryType)) {
																		if ("allTasks".equals(queryType)) {
																			queryInput
																					.setSimpleQueryCategory(TSimpleQueryCategory.ALL_TASKS);
																		} else if ("assignedToMe".equals(queryType)) {
																			queryInput
																					.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNED_TO_ME);
																		} else if ("adminTasks".equals(queryType)) {
																			queryInput
																					.setSimpleQueryCategory(TSimpleQueryCategory.ASSIGNABLE);
																		} else if ("claimableTasks".equals(queryType)) {
																			queryInput
																					.setSimpleQueryCategory(TSimpleQueryCategory.CLAIMABLE);
																		} else if ("notifications".equals(queryType)) {
																			queryInput
																					.setSimpleQueryCategory(TSimpleQueryCategory.NOTIFICATIONS);
																		}
																	}
																	taskResults = LoginManager.taskAPIClient
																			.taskListQuery(queryInput);
																	numberOfPages = taskResults.getPages();
																} catch (Exception e) {

																	request.getRequestDispatcher("./Login.jsp?logout=true")
																			.forward(request, response);
																}
															%>
															<table class="tableEvenRow" width="100%" align="center"
																id="main-table">
																<tr>
																	<th>Task ID</th>
																	<th>Subject</th>
																	<th>Status</th>
																	<th>Priority</th>
																	<th>Created On</th>
																</tr>
																<%
																	if (taskResults != null && taskResults.getRow() != null
																			&& taskResults.getRow().length > 0) {
																		TTaskSimpleQueryResultRow[] rows = taskResults.getRow();
																		for (TTaskSimpleQueryResultRow row : rows) {
																			String qname=row.getName().getLocalPart();
																%>
																<tr>
																	<td><a
																		href="Task.jsp?queryType=<%=queryType%>&taskId=<%=row.getId().toString()%>"><%=row.getId().toString()%>
																			-<%=qname%> </a>
																	</td>
																	<td>
																	<td>
																		<%
																			String presentationName = HumanTaskSampleUtil
																							.getTaskPresentationHeader(
																									row.getPresentationSubject(),
																									row.getPresentationName());
																		%> <%=presentationName%></td>
																	<td><%=row.getStatus().toString()%></td>
																	<td><%=row.getPriority()%></td>
																	<td><%=row.getCreatedTime().getTime().toString()%></td>
																</tr>
																<%
																	}
																	}
																%>
															</table>
															<br />
													</td>
												</tr>
											</tbody>
										</table>
									</div>
								</td>
							</tr>
						</tbody>
					</table>

					</div></td>
				<td width="20%"></td>
			</tr>
			<tr>
				<td id="footer" colspan="3">
					<div id="footer-div">
						<div class="footer-content">
							<div class="copyright" style="text-align: center">© 2005 -
								2013 WSO2 Inc. All Rights Reserved.</div>
						</div>
					</div>
				</td>
			</tr>
		</tbody>
	</table>
</body>
</html>