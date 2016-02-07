This directory can be used to deploy bpel archives files (zip) that contains BPEL services.

A valid bpel archives file should contains followings in its root level.
    * One or More BPEL processes (.bpel) files.
    * WSDLs of BPEL services & partner services.
    * Apache ODE deployment descriptor (deploy.xml)

Eg: HelloWorld2.zip
    |-- HelloWorld.bpel
    |-- HelloWorld.wsdl
    |-- deploy.xml