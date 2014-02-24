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
 * LoanOfferType.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis2 version: SNAPSHOT  Built on : Feb 05, 2010 (04:24:26 UTC)
 */


package org.wso2.bps.samples.creditrating.types;


/**
 * LoanOfferType bean class
 */

public class LoanOfferType
        implements org.apache.axis2.databinding.ADBBean {
    /* This type was generated from the piece of schema that had
    name = LoanOfferType
    Namespace URI = http://wso2.org/bps/samples/loan_process/schema
    Namespace Prefix = ns1
    */


    /**
     * field for ProviderName
     */


    protected java.lang.String localProviderName;


    /**
     * Auto generated getter method
     *
     * @return java.lang.String
     */
    public java.lang.String getProviderName() {
        return localProviderName;
    }


    /**
     * Auto generated setter method
     *
     * @param param ProviderName
     */
    public void setProviderName(java.lang.String param) {

        this.localProviderName = param;


    }


    /**
     * field for Selected
     */


    protected boolean localSelected;


    /**
     * Auto generated getter method
     *
     * @return boolean
     */
    public boolean getSelected() {
        return localSelected;
    }


    /**
     * Auto generated setter method
     *
     * @param param Selected
     */
    public void setSelected(boolean param) {

        this.localSelected = param;


    }


    /**
     * field for Approved
     */


    protected boolean localApproved;


    /**
     * Auto generated getter method
     *
     * @return boolean
     */
    public boolean getApproved() {
        return localApproved;
    }


    /**
     * Auto generated setter method
     *
     * @param param Approved
     */
    public void setApproved(boolean param) {

        this.localApproved = param;


    }


    /**
     * field for AnualInterestRate
     */


    protected java.math.BigDecimal localAnualInterestRate;


    /**
     * Auto generated getter method
     *
     * @return java.math.BigDecimal
     */
    public java.math.BigDecimal getAnualInterestRate() {
        return localAnualInterestRate;
    }


    /**
     * Auto generated setter method
     *
     * @param param AnualInterestRate
     */
    public void setAnualInterestRate(java.math.BigDecimal param) {

        this.localAnualInterestRate = param;


    }


    /**
     * @param parentQName
     * @param factory
     * @return org.apache.axiom.om.OMElement
     */
    public org.apache.axiom.om.OMElement getOMElement(
            final javax.xml.namespace.QName parentQName,
            final org.apache.axiom.om.OMFactory factory) throws org.apache.axis2.databinding.ADBException {


        org.apache.axiom.om.OMDataSource dataSource =
                new org.apache.axis2.databinding.ADBDataSource(this, parentQName);
        return new org.apache.axiom.om.impl.llom.OMSourcedElementImpl(
                parentQName, factory, dataSource);

    }

    public void serialize(final javax.xml.namespace.QName parentQName,
                          javax.xml.stream.XMLStreamWriter xmlWriter)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {
        serialize(parentQName, xmlWriter, false);
    }

    public void serialize(final javax.xml.namespace.QName parentQName,
                          javax.xml.stream.XMLStreamWriter xmlWriter,
                          boolean serializeType)
            throws javax.xml.stream.XMLStreamException, org.apache.axis2.databinding.ADBException {


        java.lang.String prefix = null;
        java.lang.String namespace = null;


        prefix = parentQName.getPrefix();
        namespace = parentQName.getNamespaceURI();
        writeStartElement(prefix, namespace, parentQName.getLocalPart(), xmlWriter);

        if (serializeType) {


            java.lang.String namespacePrefix = registerPrefix(xmlWriter, "http://wso2.org/bps/samples/loan_process/schema");
            if ((namespacePrefix != null) && (namespacePrefix.trim().length() > 0)) {
                writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                        namespacePrefix + ":LoanOfferType",
                        xmlWriter);
            } else {
                writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "type",
                        "LoanOfferType",
                        xmlWriter);
            }


        }

        namespace = "http://wso2.org/bps/samples/loan_process/schema";
        writeStartElement(null, namespace, "ProviderName", xmlWriter);


        if (localProviderName == null) {
            // write the nil attribute

            throw new org.apache.axis2.databinding.ADBException("ProviderName cannot be null!!");

        } else {


            xmlWriter.writeCharacters(localProviderName);

        }

        xmlWriter.writeEndElement();

        namespace = "http://wso2.org/bps/samples/loan_process/schema";
        writeStartElement(null, namespace, "selected", xmlWriter);

        if (false) {

            throw new org.apache.axis2.databinding.ADBException("selected cannot be null!!");

        } else {
            xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSelected));
        }

        xmlWriter.writeEndElement();

        namespace = "http://wso2.org/bps/samples/loan_process/schema";
        writeStartElement(null, namespace, "approved", xmlWriter);

        if (false) {

            throw new org.apache.axis2.databinding.ADBException("approved cannot be null!!");

        } else {
            xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localApproved));
        }

        xmlWriter.writeEndElement();

        namespace = "http://wso2.org/bps/samples/loan_process/schema";
        writeStartElement(null, namespace, "AnualInterestRate", xmlWriter);


        if (localAnualInterestRate == null) {
            // write the nil attribute

            throw new org.apache.axis2.databinding.ADBException("AnualInterestRate cannot be null!!");

        } else {


            xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAnualInterestRate));

        }

        xmlWriter.writeEndElement();

        xmlWriter.writeEndElement();


    }

    private static java.lang.String generatePrefix(java.lang.String namespace) {
        if (namespace.equals("http://wso2.org/bps/samples/loan_process/schema")) {
            return "ns1";
        }
        return org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
    }

    /**
     * Utility method to write an element start tag.
     */
    private void writeStartElement(java.lang.String prefix, java.lang.String namespace, java.lang.String localPart,
                                   javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
        java.lang.String writerPrefix = xmlWriter.getPrefix(namespace);
        if (writerPrefix != null) {
            xmlWriter.writeStartElement(namespace, localPart);
        } else {
            if (namespace.length() == 0) {
                prefix = "";
            } else if (prefix == null) {
                prefix = generatePrefix(namespace);
            }

            xmlWriter.writeStartElement(prefix, localPart, namespace);
            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }
    }

    /**
     * Util method to write an attribute with the ns prefix
     */
    private void writeAttribute(java.lang.String prefix, java.lang.String namespace, java.lang.String attName,
                                java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
        if (xmlWriter.getPrefix(namespace) == null) {
            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }
        xmlWriter.writeAttribute(namespace, attName, attValue);
    }

    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeAttribute(java.lang.String namespace, java.lang.String attName,
                                java.lang.String attValue, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
        if (namespace.equals("")) {
            xmlWriter.writeAttribute(attName, attValue);
        } else {
            registerPrefix(xmlWriter, namespace);
            xmlWriter.writeAttribute(namespace, attName, attValue);
        }
    }


    /**
     * Util method to write an attribute without the ns prefix
     */
    private void writeQNameAttribute(java.lang.String namespace, java.lang.String attName,
                                     javax.xml.namespace.QName qname, javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

        java.lang.String attributeNamespace = qname.getNamespaceURI();
        java.lang.String attributePrefix = xmlWriter.getPrefix(attributeNamespace);
        if (attributePrefix == null) {
            attributePrefix = registerPrefix(xmlWriter, attributeNamespace);
        }
        java.lang.String attributeValue;
        if (attributePrefix.trim().length() > 0) {
            attributeValue = attributePrefix + ":" + qname.getLocalPart();
        } else {
            attributeValue = qname.getLocalPart();
        }

        if (namespace.equals("")) {
            xmlWriter.writeAttribute(attName, attributeValue);
        } else {
            registerPrefix(xmlWriter, namespace);
            xmlWriter.writeAttribute(namespace, attName, attributeValue);
        }
    }

    /**
     * method to handle Qnames
     */

    private void writeQName(javax.xml.namespace.QName qname,
                            javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {
        java.lang.String namespaceURI = qname.getNamespaceURI();
        if (namespaceURI != null) {
            java.lang.String prefix = xmlWriter.getPrefix(namespaceURI);
            if (prefix == null) {
                prefix = generatePrefix(namespaceURI);
                xmlWriter.writeNamespace(prefix, namespaceURI);
                xmlWriter.setPrefix(prefix, namespaceURI);
            }

            if (prefix.trim().length() > 0) {
                xmlWriter.writeCharacters(prefix + ":" + org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            } else {
                // i.e this is the default namespace
                xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
            }

        } else {
            xmlWriter.writeCharacters(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qname));
        }
    }

    private void writeQNames(javax.xml.namespace.QName[] qnames,
                             javax.xml.stream.XMLStreamWriter xmlWriter) throws javax.xml.stream.XMLStreamException {

        if (qnames != null) {
            // we have to store this data until last moment since it is not possible to write any
            // namespace data after writing the charactor data
            java.lang.StringBuffer stringToWrite = new java.lang.StringBuffer();
            java.lang.String namespaceURI = null;
            java.lang.String prefix = null;

            for (int i = 0; i < qnames.length; i++) {
                if (i > 0) {
                    stringToWrite.append(" ");
                }
                namespaceURI = qnames[i].getNamespaceURI();
                if (namespaceURI != null) {
                    prefix = xmlWriter.getPrefix(namespaceURI);
                    if ((prefix == null) || (prefix.length() == 0)) {
                        prefix = generatePrefix(namespaceURI);
                        xmlWriter.writeNamespace(prefix, namespaceURI);
                        xmlWriter.setPrefix(prefix, namespaceURI);
                    }

                    if (prefix.trim().length() > 0) {
                        stringToWrite.append(prefix).append(":").append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    } else {
                        stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                    }
                } else {
                    stringToWrite.append(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(qnames[i]));
                }
            }
            xmlWriter.writeCharacters(stringToWrite.toString());
        }

    }


    /**
     * Register a namespace prefix
     */
    private java.lang.String registerPrefix(javax.xml.stream.XMLStreamWriter xmlWriter, java.lang.String namespace) throws javax.xml.stream.XMLStreamException {
        java.lang.String prefix = xmlWriter.getPrefix(namespace);
        if (prefix == null) {
            prefix = generatePrefix(namespace);
            while (xmlWriter.getNamespaceContext().getNamespaceURI(prefix) != null) {
                prefix = org.apache.axis2.databinding.utils.BeanUtil.getUniquePrefix();
            }
            xmlWriter.writeNamespace(prefix, namespace);
            xmlWriter.setPrefix(prefix, namespace);
        }
        return prefix;
    }


    /**
     * databinding method to get an XML representation of this object
     */
    public javax.xml.stream.XMLStreamReader getPullParser(javax.xml.namespace.QName qName)
            throws org.apache.axis2.databinding.ADBException {


        java.util.ArrayList elementList = new java.util.ArrayList();
        java.util.ArrayList attribList = new java.util.ArrayList();


        elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/samples/loan_process/schema",
                "ProviderName"));

        if (localProviderName != null) {
            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localProviderName));
        } else {
            throw new org.apache.axis2.databinding.ADBException("ProviderName cannot be null!!");
        }

        elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/samples/loan_process/schema",
                "selected"));

        elementList.add(
                org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localSelected));

        elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/samples/loan_process/schema",
                "approved"));

        elementList.add(
                org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localApproved));

        elementList.add(new javax.xml.namespace.QName("http://wso2.org/bps/samples/loan_process/schema",
                "AnualInterestRate"));

        if (localAnualInterestRate != null) {
            elementList.add(org.apache.axis2.databinding.utils.ConverterUtil.convertToString(localAnualInterestRate));
        } else {
            throw new org.apache.axis2.databinding.ADBException("AnualInterestRate cannot be null!!");
        }


        return new org.apache.axis2.databinding.utils.reader.ADBXMLStreamReaderImpl(qName, elementList.toArray(), attribList.toArray());


    }


    /**
     * Factory class that keeps the parse method
     */
    public static class Factory {


        /**
         * static method to create the object
         * Precondition:  If this object is an element, the current or next start element starts this object and any intervening reader events are ignorable
         * If this object is not an element, it is a complex type and the reader is at the event just after the outer start element
         * Postcondition: If this object is an element, the reader is positioned at its end element
         * If this object is a complex type, the reader is positioned at the end element of its outer element
         */
        public static LoanOfferType parse(javax.xml.stream.XMLStreamReader reader) throws java.lang.Exception {
            LoanOfferType object =
                    new LoanOfferType();

            int event;
            java.lang.String nillableValue = null;
            java.lang.String prefix = "";
            java.lang.String namespaceuri = "";
            try {

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();


                if (reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance", "type") != null) {
                    java.lang.String fullTypeName = reader.getAttributeValue("http://www.w3.org/2001/XMLSchema-instance",
                            "type");
                    if (fullTypeName != null) {
                        java.lang.String nsPrefix = null;
                        if (fullTypeName.indexOf(":") > -1) {
                            nsPrefix = fullTypeName.substring(0, fullTypeName.indexOf(":"));
                        }
                        nsPrefix = nsPrefix == null ? "" : nsPrefix;

                        java.lang.String type = fullTypeName.substring(fullTypeName.indexOf(":") + 1);

                        if (!"LoanOfferType".equals(type)) {
                            //find namespace for the prefix
                            java.lang.String nsUri = reader.getNamespaceContext().getNamespaceURI(nsPrefix);
                            return (LoanOfferType) org.wso2.bps.samples.creditrating.ExtensionMapper.getTypeObject(
                                    nsUri, type, reader);
                        }


                    }


                }


                // Note all attributes that were handled. Used to differ normal attributes
                // from anyAttributes.
                java.util.Vector handledAttributes = new java.util.Vector();


                reader.next();


                while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/samples/loan_process/schema", "ProviderName").equals(reader.getName())) {

                    java.lang.String content = reader.getElementText();

                    object.setProviderName(
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToString(content));

                    reader.next();

                }  // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                }


                while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/samples/loan_process/schema", "selected").equals(reader.getName())) {

                    java.lang.String content = reader.getElementText();

                    object.setSelected(
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));

                    reader.next();

                }  // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                }


                while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/samples/loan_process/schema", "approved").equals(reader.getName())) {

                    java.lang.String content = reader.getElementText();

                    object.setApproved(
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToBoolean(content));

                    reader.next();

                }  // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                }


                while (!reader.isStartElement() && !reader.isEndElement()) reader.next();

                if (reader.isStartElement() && new javax.xml.namespace.QName("http://wso2.org/bps/samples/loan_process/schema", "AnualInterestRate").equals(reader.getName())) {

                    java.lang.String content = reader.getElementText();

                    object.setAnualInterestRate(
                            org.apache.axis2.databinding.utils.ConverterUtil.convertToDecimal(content));

                    reader.next();

                }  // End of if for expected property start element

                else {
                    // A start element we are not expecting indicates an invalid parameter was passed
                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());
                }

                while (!reader.isStartElement() && !reader.isEndElement())
                    reader.next();

                if (reader.isStartElement())
                    // A start element we are not expecting indicates a trailing invalid property
                    throw new org.apache.axis2.databinding.ADBException("Unexpected subelement " + reader.getLocalName());


            } catch (javax.xml.stream.XMLStreamException e) {
                throw new java.lang.Exception(e);
            }

            return object;
        }

    }//end of factory class


}
           
    