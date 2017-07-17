NOTE
----
Since WSO2 BPS will be include in WSO2 Integrator(http://wso2.com/integration) as the BPM profile, we are deprecating this product and moving it to attic.

WSO2 Business Process Server 
-----------------------------

---

|  Branch | Build Status |
| :------------ |:-------------
| master      | [![Build Status](https://wso2.org/jenkins/job/product-bps/badge/icon)](https://wso2.org/jenkins/job/product-bps) |


---

Welcome to the WSO2 BPS

WSO2 Business Process Server (BPS) is an easy-to-use open source
business process server that executes business processes written using
BPMN standard as well as WS-BPEL standard. It is powered by Activiti BPMN Engine
and Apache ODE BPEL Engine. It provides a complete Web-based graphical 
console to deploy, manage and view processes in addition to managing 
and viewing process instances.

Key Features
------------

* Deploying Business Processes written in compliance with WS-BPEL 2.0 Standard and BPEL4WS 1.1 standard.
* Deploying Business Processes written in compliance with a frequently used subset of BPMN 2.0 standard.
* Support for Human Interactions in BPEL Processes with WS-Human Task and BPEL4People.
* Managing BPMN / BPEL packages, processes and process instances.
* BPEL Extensions and XPath extensions and BPMN Extensions support.
* Instance recovery (Only supports 'Invoke' activity) support through management console.
* WS-Security support for external services exposed by BPEL / Humantasks.
* Support for HumanTask Coordination.
* Human Task Versioning.
* Clustering support for high availability.
* BPEL / BPMN Package hot update which facilitate Versioning of Packages.
* BPEL deployment descriptor editor.
* E4X based data manipulation support for BPEL assignments.
* Ability to configure external data base system as the BPEL / BPMN engine's persistence storage.
* Transport management.
* Internationalized web based management console.
* System monitoring.
* Comprehensive REST API for managing bpmn processes / tasks.
* Try-it for business processes.
* SOAP Message Tracing.
* End-point configuration mechanism based on WSO2 Unified Endpoints.
* Customizable server - You can customize the BPS to fit into your
  exact requirements, by removing certain features or by adding new
  optional features.
* Process Monitoring support with WSO2 Data Analytics Server.
* JMX Monitoring
* Comprehensive and customizable web application for managing human tasks.
* Comprehensive and customizable web application for managing bpmn instances and user tasks.
* Human Task UI Form Generation support
* Ability to publish statistics to WSO2 Data Analytics server for analysis
* Reporting dashboards for BPMN
* Data manipulation support for BPMN with JSON and XML
* Human Task Editor with Developer Studio
* BPMN Process Instance Search/Monitoring capability
* BPMN Task user substitution feature
* Enhanced rest service invoker extension for BPMN
* SOAP service invoker extension for BPMN


New Features In This Release
----------------------------

* BPMN REST Api improvements.
* Data manipulation support for BPMN with JSON and XML
* Human Task Editor with Developer Studio
* BPMN Process Instance Search/Monitoring capability
* BPMN Task user substitution feature
* Enhanced rest service invoker extension for BPMN
* SOAP service invoker extension for BPMN
* Many other bug fixes.

Issues Fixed In This Release
----------------------------

* WSO2 BPS related components of the WSO2 Carbon Platform -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=12625


Known Issues
-----------

* WS-Human Task implementation does not support sub tasks and lean tasks.
* BPEL4People only supports remote tasks and remote notification creation.

* For a complete list of features to be implemented please refer the list of known issues -
       https://wso2.org/jira/secure/IssueNavigator.jspa?mode=hide&requestId=12626


Installation & Running
----------------------
1. Extract the downloaded zip file
2. Run the wso2server.sh or wso2server.bat file in the bin directory
3. Once the server starts, point your Web browser to
   https://localhost:9443/carbon/
4. Use the following username and password to login
    username : admin
    password : admin

For more details, see the Installation Guide


System Requirements
-------------------

1. Minimum memory - 4GB
2. Processor      - 3GHz Dual­core Xeon/Opteron (or latest)
3. The Management Console requires full Javascript enablement of the Web browser
 

WSO2 BPS @product.version@ distribution directory structure
=============================================

	CARBON_HOME
		|- bin <folder>
		|- dbscripts <folder>
		|- lib <folder>
		|- repository <folder>
			|-- logs <folder>
			|-- database <folder>
			|-- samples <folder>
		|--- conf <folder>
		|- resources <folder>
		|- samples <folder>
		|- webapp-mode <folder>
		|- tmp <folder>
		|- LICENSE.txt <file>
		|- README.txt <file>
		|- INSTALL.txt <file>
		|- release-notes.html <file>

    - bin
	  Contains various scripts .sh & .bat scripts

	- conf
	  Contains configuration files

	- database
      Contains the database

    - dbscripts
      Contains all the database scripts

    - lib
	  Contains the basic set of libraries required to startup BPS
	  in standalone mode

	- repository
	  The repository where services and modules deployed in WSO2 BPS
	  are stored. In addition to this the components directory inside the
	  repository directory contains the carbon runtime and the user added
	  jar files including mediators, third party libraries and so on.

	- logs
	  Contains all log files created during execution

	- resources
	  Contains additional resources that may be required, including sample
	  configuration and sample resources

	- samples
	  Contains sample axis2 server and client files to execute some of the
	  samples shipped with WSO2 BPS

	- tmp
	  Used for storing temporary files, and is pointed to by the
	  java.io.tmpdir System property

	- LICENSE.txt
	  Apache License 2.0 and the relevant other licenses under which
	  WSO2 BPS is distributed.

	- README.txt
	  This document.

    - INSTALL.txt
      This document will contain information on installing WSO2 BPS

	- release-notes.html
	  Release information for WSO2 BPS @product.version@

Support
==================================

WSO2 Inc. offers a variety of development and production support
programs, ranging from Web-based support up through normal business
hours, to premium 24x7 phone support.

For additional support information please refer to http://wso2.com/support/

For more information on WSO2 BPS, visit the WSO2 Oxygen Tank (http://wso2.org)

Issue Tracker
==================================

  https://wso2.org/jira/browse/BPS
  https://wso2.org/jira/browse/CARBON

Crypto Notice
==================================

   This distribution includes cryptographic software.  The country in
   which you currently reside may have restrictions on the import,
   possession, use, and/or re-export to another country, of
   encryption software.  BEFORE using any encryption software, please
   check your country's laws, regulations and policies concerning the
   import, possession, or use, and re-export of encryption software, to
   see if this is permitted.  See <http://www.wassenaar.org/> for more
   information.

   The U.S. Government Department of Commerce, Bureau of Industry and
   Security (BIS), has classified this software as Export Commodity
   Control Number (ECCN) 5D002.C.1, which includes information security
   software using or performing cryptographic functions with asymmetric
   algorithms.  The form and manner of this Apache Software Foundation
   distribution makes it eligible for export under the License Exception
   ENC Technology Software Unrestricted (TSU) exception (see the BIS
   Export Administration Regulations, Section 740.13) for both object
   code and source code.

   The following provides more details on the included cryptographic
   software:

   Apache Rampart   : http://ws.apache.org/rampart/
   Apache WSS4J     : http://ws.apache.org/wss4j/
   Apache Santuario : http://santuario.apache.org/
   Bouncycastle     : http://www.bouncycastle.org/
   Apache ODE       : http://ode.apache.org/

--------------------------------------------------------------------------------
(c) Copyright 2016 WSO2 Inc.
