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
package org.swssf.wss.impl.processor.input;

import org.swssf.binding.wss11.SignatureConfirmationType;
import org.swssf.wss.ext.WSSConstants;
import org.swssf.wss.ext.WSSecurityContext;
import org.swssf.wss.ext.WSSecurityException;
import org.swssf.xmlsec.ext.AbstractInputSecurityHeaderHandler;
import org.swssf.xmlsec.ext.InputProcessorChain;
import org.swssf.xmlsec.ext.XMLSecurityException;
import org.swssf.xmlsec.ext.XMLSecurityProperties;

import javax.xml.bind.JAXBElement;
import javax.xml.stream.events.XMLEvent;
import java.util.Deque;

/**
 * Processor for the SignatureConfirmation XML Structure
 *
 * @author $Author$
 * @version $Revision$ $Date$
 */
public class SignatureConfirmationInputHandler extends AbstractInputSecurityHeaderHandler {

    @Override
    public void handle(final InputProcessorChain inputProcessorChain, final XMLSecurityProperties securityProperties,
                       Deque<XMLEvent> eventQueue, Integer index) throws XMLSecurityException {

        @SuppressWarnings("unchecked")
        final SignatureConfirmationType signatureConfirmationType =
                ((JAXBElement<SignatureConfirmationType>) parseStructure(eventQueue, index)).getValue();

        checkBSPCompliance(inputProcessorChain, signatureConfirmationType);

        inputProcessorChain.getSecurityContext().putAsList(SignatureConfirmationType.class, signatureConfirmationType);
    }

    private void checkBSPCompliance(InputProcessorChain inputProcessorChain, SignatureConfirmationType signatureConfirmationType) throws WSSecurityException {
        if (signatureConfirmationType.getId() == null) {
            ((WSSecurityContext) inputProcessorChain.getSecurityContext()).handleBSPRule(WSSConstants.BSPRule.R5441);
        }
    }
}
