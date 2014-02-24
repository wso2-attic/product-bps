/*
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
/**
 * ExtensionMapper.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT  Built on : Feb 05, 2010 (04:24:26 UTC)
 */


package org.wso2.bps.samples.creditrating;

/**
 * ExtensionMapper class
 */

public class ExtensionMapper {

    public static java.lang.Object getTypeObject(java.lang.String namespaceURI,
                                                 java.lang.String typeName,
                                                 javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {


        if (
                "http://wso2.org/bps/samples/loan_process/schema".equals(namespaceURI) &&
                        "CustomerSSNType".equals(typeName)) {

            return org.wso2.bps.samples.creditrating.types.CustomerSSNType.Factory.parse(reader);


        }


        if (
                "http://wso2.org/bps/samples/loan_process/schema".equals(namespaceURI) &&
                        "CustomerIDType".equals(typeName)) {

            return org.wso2.bps.samples.creditrating.types.CustomerIDType.Factory.parse(reader);


        }


        if (
                "http://wso2.org/bps/samples/loan_process/schema".equals(namespaceURI) &&
                        "CreditRatingType".equals(typeName)) {

            return org.wso2.bps.samples.creditrating.types.CreditRatingType.Factory.parse(reader);


        }


        if (
                "http://wso2.org/bps/samples/loan_process/schema".equals(namespaceURI) &&
                        "CustomerInfoType".equals(typeName)) {

            return org.wso2.bps.samples.creditrating.types.CustomerInfoType.Factory.parse(reader);


        }


        if (
                "http://wso2.org/bps/samples/loan_process/schema".equals(namespaceURI) &&
                        "LoanOfferType".equals(typeName)) {

            return org.wso2.bps.samples.creditrating.types.LoanOfferType.Factory.parse(reader);


        }


        if (
                "http://wso2.org/bps/samples/loan_process/schema".equals(namespaceURI) &&
                        "LoanRequestType".equals(typeName)) {

            return org.wso2.bps.samples.creditrating.types.LoanRequestType.Factory.parse(reader);


        }


        if (
                "http://wso2.org/bps/samples/loan_process/schema".equals(namespaceURI) &&
                        "LoanInfoType".equals(typeName)) {

            return org.wso2.bps.samples.creditrating.types.LoanInfoType.Factory.parse(reader);


        }


        throw new org.apache.axis2.databinding.ADBException("Unsupported type " + namespaceURI + " " + typeName);
    }

}
    