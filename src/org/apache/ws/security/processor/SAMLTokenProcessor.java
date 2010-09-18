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

package org.apache.ws.security.processor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.ws.security.WSConstants;
import org.apache.ws.security.WSDocInfo;
import org.apache.ws.security.WSSConfig;
import org.apache.ws.security.WSSecurityEngineResult;
import org.apache.ws.security.WSSecurityException;
import org.apache.ws.security.components.crypto.Crypto;
import org.opensaml.SAMLAssertion;
import org.opensaml.SAMLException;
import org.w3c.dom.Element;

import javax.security.auth.callback.CallbackHandler;
import java.util.Vector;

public class SAMLTokenProcessor implements Processor {
    private static Log log = LogFactory.getLog(SAMLTokenProcessor.class.getName());
    
    private String id;
    private Element samlTokenElement;

    public void handleToken(
        Element elem, 
        Crypto crypto,
        Crypto decCrypto, 
        CallbackHandler cb, 
        WSDocInfo wsDocInfo, 
        Vector returnResults, 
        WSSConfig wsc
    ) throws WSSecurityException {
        if (log.isDebugEnabled()) {
            log.debug("Found SAML Assertion element");
        }
        SAMLAssertion assertion = handleSAMLToken((Element) elem);
        this.id = assertion.getId();
        wsDocInfo.setAssertion((Element) elem);
        returnResults.add(
            0,
            new WSSecurityEngineResult(WSConstants.ST_UNSIGNED, assertion)
        );
        this.samlTokenElement = elem;

    }

    public SAMLAssertion handleSAMLToken(Element token) throws WSSecurityException {
        boolean result = false;
        SAMLAssertion assertion = null;
        try {
            assertion = new SAMLAssertion(token);
            result = true;
            if (log.isDebugEnabled()) {
                log.debug("SAML Assertion issuer " + assertion.getIssuer());
            }
        } catch (SAMLException e) {
            throw new WSSecurityException(
                WSSecurityException.FAILURE, "invalidSAMLsecurity", null, e
            );
        }
        if (!result) {
            throw new WSSecurityException(WSSecurityException.FAILED_AUTHENTICATION);
        }
        return assertion;
    }

    /**
     * Return the id of the SAML token
     */
    public String getId() {
        return this.id;
    }

    public Element getSamlTokenElement() {
        return samlTokenElement;
    }

}
