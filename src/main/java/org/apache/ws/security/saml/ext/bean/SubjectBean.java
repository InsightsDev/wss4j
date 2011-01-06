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

package org.apache.ws.security.saml.ext.bean;

import java.security.cert.X509Certificate;


/**
 * Class SubjectBean represents a SAML subject (can be used to create
 * both SAML v1.1 and v2.0 statements)
 *
 * Created on May 20, 2009
 */
public class SubjectBean {
    private String subjectName;
    private String subjectNameQualifier;
    private String subjectConfirmationMethod;
    private X509Certificate subjectCert;
    private boolean useSendKeyValue;

    /**
     * Constructor SubjectBean creates a new SubjectBean instance.
     */
    public SubjectBean() {
    }

    /**
     * Constructor SubjectBean creates a new SubjectBean instance.
     *
     * @param subjectName of type String
     * @param subjectNameQualifier of type String
     * @param subjectConfirmationMethod of type String
     */
    public SubjectBean(
        String subjectName, 
        String subjectNameQualifier, 
        String subjectConfirmationMethod
    ) {
        this.subjectName = subjectName;
        this.subjectNameQualifier = subjectNameQualifier;
        this.subjectConfirmationMethod = subjectConfirmationMethod;
    }

    /**
     * Method getSubjectName returns the subjectName of this SubjectBean object.
     *
     * @return the subjectName (type String) of this SubjectBean object.
     */
    public String getSubjectName() {
        return subjectName;
    }

    /**
     * Method setSubjectName sets the subjectName of this SubjectBean object.
     *
     * @param subjectName the subjectName of this SubjectBean object.
     */
    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }
    
    /**
     * Method getSubjectNameQualifier returns the subjectNameQualifier of this SubjectBean object.
     *
     * @return the subjectNameQualifier (type String) of this SubjectBean object.
     */
    public String getSubjectNameQualifier() {
        return subjectNameQualifier;
    }

    /**
     * Method setSubjectNameQualifier sets the subjectNameQualifier of this SubjectBean object.
     *
     * @param subjectNameQualifier the subjectNameQualifier of this SubjectBean object.
     */
    public void setSubjectNameQualifier(String subjectNameQualifier) {
        this.subjectNameQualifier = subjectNameQualifier;
    }
    
    /**
     * Method getSubjectConfirmationMethod returns the subjectConfirmationMethod of
     * this SubjectBean object.
     *
     * @return the subjectConfirmationMethod (type String) of this SubjectBean object.
     */
    public String getSubjectConfirmationMethod() {
        return subjectConfirmationMethod;
    }

    /**
     * Method setSubjectConfirmationMethod sets the subjectConfirmationMethod of
     * this SubjectBean object.
     *
     * @param subjectConfirmationMethod the subjectConfirmationMethod of this 
     *        SubjectBean object.
     */
    public void setSubjectConfirmationMethod(String subjectConfirmationMethod) {
        this.subjectConfirmationMethod = subjectConfirmationMethod;
    }
    
    /**
     * Method getSubjectCert returns the subjectCert of this SubjectBean object.
     *
     * @return the subjectCert (type X509Certificate) of this SubjectBean object.
     */
    public X509Certificate getSubjectCert() {
        return subjectCert;
    }

    /**
     * Method setSubjectCert sets the subjectCert of this SubjectBean object.
     *
     * @param subjectCert the subjectCert of this SubjectBean object.
     */
    public void setSubjectCert(X509Certificate subjectCert) {
        this.subjectCert = subjectCert;
    }
    
    /**
     * Method isUseSendKeyValue returns the useSendKeyValue of this SubjectBean object.
     *
     * @return the useSendKeyValue (type boolean) of this SubjectBean object.
     */
    public boolean isUseSendKeyValue() {
        return useSendKeyValue;
    }

    /**
     * Method setUseSendKeyValue sets the useSendKeyValue of this SubjectBean object.
     *
     * @param useSendKeyValue the useSendKeyValue of this SubjectBean object.
     */
    public void setUseSendKeyValue(boolean useSendKeyValue) {
        this.useSendKeyValue = useSendKeyValue;
    }

    /**
     * Method equals ...
     *
     * @param o of type Object
     * @return boolean
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SubjectBean)) return false;

        SubjectBean that = (SubjectBean) o;

        if (!subjectName.equals(that.subjectName)) return false;
        if (!subjectNameQualifier.equals(that.subjectNameQualifier)) return false;
        if (!subjectConfirmationMethod.equals(that.subjectConfirmationMethod)) return false;
        if (subjectCert != null && !subjectCert.equals(that.subjectCert)) return false;
        if (useSendKeyValue != that.useSendKeyValue) return false;

        return true;
    }

    /**
     * Method hashCode ...
     * @return int
     */
    @Override
    public int hashCode() {
        int result = subjectName.hashCode();
        result = 31 * result + subjectNameQualifier.hashCode();
        result = 31 * result + subjectConfirmationMethod.hashCode();
        result = 31 * result + subjectCert.hashCode();
        result = 31 * result + Boolean.valueOf(useSendKeyValue).hashCode();
        return result;
    }
}