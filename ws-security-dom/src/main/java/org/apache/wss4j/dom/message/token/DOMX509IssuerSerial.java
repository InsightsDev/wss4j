/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.wss4j.dom.message.token;

import org.apache.wss4j.dom.WSConstants;
import org.apache.wss4j.common.util.DOM2Writer;
import org.apache.wss4j.common.util.XMLUtils;
import org.apache.wss4j.dom.util.WSSecurityUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.math.BigInteger;

import javax.security.auth.x500.X500Principal;

/**
 * An X.509 Issuer Serial token.
 */
public final class DOMX509IssuerSerial {
    private final Element element;
    private final String issuer;
    private final BigInteger serialNumber;
    
    /**
     * Constructor.
     */
    public DOMX509IssuerSerial(Element issuerSerialElement) {
        element = issuerSerialElement;
        
        Element issuerNameElement = 
            WSSecurityUtil.getDirectChildElement(
                element, WSConstants.X509_ISSUER_NAME_LN, WSConstants.SIG_NS
            );
        issuer = XMLUtils.getElementText(issuerNameElement);
        
        Element serialNumberElement = 
            WSSecurityUtil.getDirectChildElement(
                element, WSConstants.X509_SERIAL_NUMBER_LN, WSConstants.SIG_NS
            );
        String serialNumberStr = XMLUtils.getElementText(serialNumberElement);
        if (serialNumberStr != null) {
            serialNumber = new BigInteger(serialNumberStr);
        } else {
            serialNumber = null;
        }
        
    }

    /**
     * Constructor.
     */
    public DOMX509IssuerSerial(Document doc, String issuer, BigInteger serialNumber) {
        if (issuer == null) {
            throw new NullPointerException("The issuerName cannot be null");
        }
        if (serialNumber == null) {
            throw new NullPointerException("The serialNumber cannot be null");
        }
        this.issuer = new X500Principal(issuer).getName();
        this.serialNumber = serialNumber;
        
        element = 
            doc.createElementNS(
                WSConstants.SIG_NS, WSConstants.SIG_PREFIX + ":" + WSConstants.X509_ISSUER_SERIAL_LN
            );
        
        Element issuerNameElement = 
            doc.createElementNS(
                WSConstants.SIG_NS, WSConstants.SIG_PREFIX + ":" + WSConstants.X509_ISSUER_NAME_LN
            );
        issuerNameElement.appendChild(doc.createTextNode(this.issuer));
        element.appendChild(issuerNameElement);
        
        Element serialNumberElement = 
            doc.createElementNS(
                WSConstants.SIG_NS, WSConstants.SIG_PREFIX + ":" + WSConstants.X509_SERIAL_NUMBER_LN
            );
        serialNumberElement.appendChild(doc.createTextNode(serialNumber.toString()));
        element.appendChild(serialNumberElement);
    }
    

    /**
     * return the dom element.
     * 
     * @return the dom element.
     */
    public Element getElement() {
        return element;
    }
    
    /**
     * Return the issuer name.
     */
    public String getIssuer() {
        return issuer;
    }
    
    /**
     * Return the Serial Number.
     */
    public BigInteger getSerialNumber() {
        return serialNumber;
    }

    /**
     * return the string representation of the token.
     * 
     * @return the string representation of the token.
     */
    public String toString() {
        return DOM2Writer.nodeToString(element);
    }
    
}
