This directory can be used to deploy WS-HumanTask archives files (zip) that contains WS-HumanTask services.

A valid HumanTask archives file should contains followings in its root level.
  * One humantask definition (.ht) files.
  * WSDLs & XSDs of HumanTask services & callback services.
  * HumanTask deployment descriptor (htconfig.xml)


Eg: ClaimApprovalTask.zip
    |-- ClaimApprovalTask.bpel
    |-- ClaimApprovalTask.wsdl
    |-- ClaimApprovalProcessCallback.wsdl
    |-- htconfig.xml