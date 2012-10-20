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

package org.apache.ws.security.stax.test.saml;

import org.apache.ws.security.common.crypto.Crypto;
import org.apache.ws.security.common.crypto.CryptoFactory;
import org.apache.ws.security.common.crypto.CryptoType;
import org.apache.ws.security.common.ext.WSSecurityException;
import org.apache.ws.security.common.saml.SAMLCallback;
import org.apache.ws.security.common.saml.bean.AuthenticationStatementBean;
import org.apache.ws.security.common.saml.bean.KeyInfoBean;
import org.apache.ws.security.common.saml.bean.SubjectBean;
import org.apache.ws.security.common.saml.builder.SAML1Constants;
import org.opensaml.common.SAMLVersion;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.UnsupportedCallbackException;
import java.io.IOException;
import java.security.cert.X509Certificate;
import java.util.Collections;

/**
 * A Callback Handler implementation for a SAML 1.1 authentication assertion using
 * Holder of Key.
 */
public class SAML1AuthnHOKHandler implements CallbackHandler {

    private String subjectName = "uid=joe,ou=people,ou=saml-demo,o=example.com";
    private String subjectQualifier = "www.example.com";
    private X509Certificate[] certs;
    private String issuerKeyName;
    private String issuerKeyPassword;
    private Crypto issuerCrypto;

    public SAML1AuthnHOKHandler() throws WSSecurityException {
        Crypto crypto = CryptoFactory.getInstance("saml/saml-signed.properties");
        CryptoType cryptoType = new CryptoType(CryptoType.TYPE.ALIAS);
        cryptoType.setAlias("transmitter");
        certs = crypto.getX509Certificates(cryptoType);
        issuerKeyName = "samlissuer";
        issuerKeyPassword = "default";
        issuerCrypto = CryptoFactory.getInstance("saml/samlissuer.properties");
    }

    @Override
    public void handle(Callback[] callbacks)
            throws IOException, UnsupportedCallbackException {
        for (int i = 0; i < callbacks.length; i++) {
            if (callbacks[i] instanceof SAMLCallback) {
                SAMLCallback callback = (SAMLCallback) callbacks[i];
                callback.setSamlVersion(SAMLVersion.VERSION_11);
                callback.setIssuer("www.example.com");
                callback.setIssuerKeyName(issuerKeyName);
                callback.setIssuerKeyPassword(issuerKeyPassword);
                callback.setIssuerCrypto(issuerCrypto);
                
                SubjectBean subjectBean =
                        new SubjectBean(
                                subjectName, subjectQualifier, SAML1Constants.CONF_HOLDER_KEY
                        );
                KeyInfoBean keyInfo = new KeyInfoBean();
                keyInfo.setCertificate(certs[0]);
                subjectBean.setKeyInfo(keyInfo);
                AuthenticationStatementBean authBean = new AuthenticationStatementBean();
                authBean.setSubject(subjectBean);
                authBean.setAuthenticationMethod("Password");
                callback.setAuthenticationStatementData(Collections.singletonList(authBean));
            } else {
                throw new UnsupportedCallbackException(callbacks[i], "Unrecognized Callback");
            }
        }
    }
}
