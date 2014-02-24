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
 * CreditRatingServiceMessageReceiverInOut.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: 1.5-wso2v3  Built on : Dec 07, 2009 (10:26:58 LKT)
 */
package org.wso2.bps.samples.creditrating;

/**
 * CreditRatingServiceMessageReceiverInOut message receiver
 */

public class CreditRatingServiceMessageReceiverInOut extends org.apache.axis2.receivers.AbstractInOutMessageReceiver {


    public void invokeBusinessLogic(org.apache.axis2.context.MessageContext msgContext, org.apache.axis2.context.MessageContext newMsgContext)
            throws org.apache.axis2.AxisFault {

        try {

            // get the implementation class for the Web Service
            Object obj = getTheImplementationObject(msgContext);

            CreditRatingServiceSkeleton skel = (CreditRatingServiceSkeleton) obj;
            //Out Envelop
            org.apache.axiom.soap.SOAPEnvelope envelope = null;
            //Find the axisOperation that has been set by the Dispatch phase.
            org.apache.axis2.description.AxisOperation op = msgContext.getOperationContext().getAxisOperation();
            if (op == null) {
                throw new org.apache.axis2.AxisFault("Operation is not located, if this is doclit style the SOAP-ACTION should specified via the SOAP Action to use the RawXMLProvider");
            }

            java.lang.String methodName;
            if ((op.getName() != null) && ((methodName = org.apache.axis2.util.JavaUtils.xmlNameToJavaIdentifier(op.getName().getLocalPart())) != null)) {


                if ("getCreditRating".equals(methodName)) {

                    org.wso2.bps.samples.creditrating.types.CreditRating creditRating3 = null;
                    org.wso2.bps.samples.creditrating.types.CustomerSSN wrappedParam =
                            (org.wso2.bps.samples.creditrating.types.CustomerSSN) fromOM(
                                    msgContext.getEnvelope().getBody().getFirstElement(),
                                    org.wso2.bps.samples.creditrating.types.CustomerSSN.class,
                                    getEnvelopeNamespaces(msgContext.getEnvelope()));

                    creditRating3 =


                            wrapgetCreditRating(


                                    skel.getCreditRating(

                                            getSSN(wrappedParam)
                                    )

                            )
                            ;

                    envelope = toEnvelope(getSOAPFactory(msgContext), creditRating3, false);

                } else {
                    throw new java.lang.RuntimeException("method not found");
                }


                newMsgContext.setEnvelope(envelope);
            }
        }
        catch (java.lang.Exception e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }

    //
    private org.apache.axiom.om.OMElement toOM(org.wso2.bps.samples.creditrating.types.CustomerSSN param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {


        try {
            return param.getOMElement(org.wso2.bps.samples.creditrating.types.CustomerSSN.MY_QNAME,
                    org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }


    }

    private org.apache.axiom.om.OMElement toOM(org.wso2.bps.samples.creditrating.types.CreditRating param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {


        try {
            return param.getOMElement(org.wso2.bps.samples.creditrating.types.CreditRating.MY_QNAME,
                    org.apache.axiom.om.OMAbstractFactory.getOMFactory());
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }


    }

    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory, org.wso2.bps.samples.creditrating.types.CreditRating param, boolean optimizeContent)
            throws org.apache.axis2.AxisFault {
        try {
            org.apache.axiom.soap.SOAPEnvelope emptyEnvelope = factory.getDefaultEnvelope();

            emptyEnvelope.getBody().addChild(param.getOMElement(org.wso2.bps.samples.creditrating.types.CreditRating.MY_QNAME, factory));


            return emptyEnvelope;
        } catch (org.apache.axis2.databinding.ADBException e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
    }


    private java.lang.String getSSN(
            org.wso2.bps.samples.creditrating.types.CustomerSSN wrappedType) {

        return wrappedType.getCustomerSSN().getSSN();

    }

    private org.wso2.bps.samples.creditrating.types.CustomerSSNType getgetCreditRating(
            org.wso2.bps.samples.creditrating.types.CustomerSSN wrappedType) {
        return wrappedType.getCustomerSSN();
    }


    private org.wso2.bps.samples.creditrating.types.CreditRating wrapCreditRatingSSN(
            java.lang.String param) {
        org.wso2.bps.samples.creditrating.types.CreditRating wrappedElement = new org.wso2.bps.samples.creditrating.types.CreditRating();
        org.wso2.bps.samples.creditrating.types.CreditRatingType innerType = new org.wso2.bps.samples.creditrating.types.CreditRatingType();
        innerType.setSSN(param);
        wrappedElement.setCreditRating(innerType);

        return wrappedElement;
    }


    private org.wso2.bps.samples.creditrating.types.CreditRating wrapCreditRatingRating(
            int param) {
        org.wso2.bps.samples.creditrating.types.CreditRating wrappedElement = new org.wso2.bps.samples.creditrating.types.CreditRating();
        org.wso2.bps.samples.creditrating.types.CreditRatingType innerType = new org.wso2.bps.samples.creditrating.types.CreditRatingType();
        innerType.setRating(param);
        wrappedElement.setCreditRating(innerType);

        return wrappedElement;
    }

    private org.wso2.bps.samples.creditrating.types.CreditRating wrapgetCreditRating(
            org.wso2.bps.samples.creditrating.types.CreditRatingType innerType) {
        org.wso2.bps.samples.creditrating.types.CreditRating wrappedElement = new org.wso2.bps.samples.creditrating.types.CreditRating();
        wrappedElement.setCreditRating(innerType);
        return wrappedElement;
    }


    /**
     * get the default envelope
     */
    private org.apache.axiom.soap.SOAPEnvelope toEnvelope(org.apache.axiom.soap.SOAPFactory factory) {
        return factory.getDefaultEnvelope();
    }


    private java.lang.Object fromOM(
            org.apache.axiom.om.OMElement param,
            java.lang.Class type,
            java.util.Map extraNamespaces) throws org.apache.axis2.AxisFault {

        try {

            if (org.wso2.bps.samples.creditrating.types.CustomerSSN.class.equals(type)) {

                return org.wso2.bps.samples.creditrating.types.CustomerSSN.Factory.parse(param.getXMLStreamReaderWithoutCaching());


            }

            if (org.wso2.bps.samples.creditrating.types.CreditRating.class.equals(type)) {

                return org.wso2.bps.samples.creditrating.types.CreditRating.Factory.parse(param.getXMLStreamReaderWithoutCaching());


            }

        } catch (java.lang.Exception e) {
            throw org.apache.axis2.AxisFault.makeFault(e);
        }
        return null;
    }


    /**
     * A utility method that copies the namepaces from the SOAPEnvelope
     */
    private java.util.Map getEnvelopeNamespaces(org.apache.axiom.soap.SOAPEnvelope env) {
        java.util.Map returnMap = new java.util.HashMap();
        java.util.Iterator namespaceIterator = env.getAllDeclaredNamespaces();
        while (namespaceIterator.hasNext()) {
            org.apache.axiom.om.OMNamespace ns = (org.apache.axiom.om.OMNamespace) namespaceIterator.next();
            returnMap.put(ns.getPrefix(), ns.getNamespaceURI());
        }
        return returnMap;
    }

    private org.apache.axis2.AxisFault createAxisFault(java.lang.Exception e) {
        org.apache.axis2.AxisFault f;
        Throwable cause = e.getCause();
        if (cause != null) {
            f = new org.apache.axis2.AxisFault(e.getMessage(), cause);
        } else {
            f = new org.apache.axis2.AxisFault(e.getMessage());
        }

        return f;
    }

}//end of class
    