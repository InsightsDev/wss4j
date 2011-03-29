/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.swssf.impl;

import org.swssf.ext.InputProcessorChain;
import org.swssf.ext.SecurityProperties;
import org.swssf.ext.WSSecurityException;
import org.swssf.ext.XMLEventNS;

import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.Location;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.*;
import java.util.Iterator;

/**
 * A custom implementation of a XMLStreamReader to get back from the XMLEventReader world
 * to XMLStreamReader
 *
 * @author $Author: giger $
 * @version $Revision: 281 $ $Date: 2011-01-04 21:15:27 +0100 (Tue, 04 Jan 2011) $
 */
public class XMLSecurityStreamReader implements XMLStreamReader {

    private SecurityProperties securityProperties;
    private InputProcessorChain inputProcessorChain;
    private XMLEvent currentEvent;

    private static final String ERR_STATE_NOT_ELEM = "Current state not START_ELEMENT or END_ELEMENT";
    private static final String ERR_STATE_NOT_STELEM = "Current state not START_ELEMENT";
    private static final String ERR_STATE_NOT_PI = "Current state not PROCESSING_INSTRUCTION";

    public XMLSecurityStreamReader(InputProcessorChain inputProcessorChain, SecurityProperties securityProperties) {
        this.inputProcessorChain = inputProcessorChain;
        this.securityProperties = securityProperties;
    }

    public Object getProperty(String name) throws IllegalArgumentException {
        if (XMLInputFactory.IS_NAMESPACE_AWARE.equals(name)) {
            return true;
        }
        return null;
    }

    public int next() throws XMLStreamException {
        try {
            inputProcessorChain.reset();
            currentEvent = ((XMLEventNS) inputProcessorChain.processEvent()).getCurrentEvent();
            if ((currentEvent.getEventType() == START_DOCUMENT)
                    && securityProperties.isSkipDocumentEvents()) {
                currentEvent = ((XMLEventNS) inputProcessorChain.processEvent()).getCurrentEvent();
            }
        } catch (WSSecurityException e) {
            throw new XMLStreamException(e);
        }
        return getCurrentEvent().getEventType();
    }

    private XMLEvent getCurrentEvent() {
        if (currentEvent == null) {
            throw new IllegalStateException("Illegal state");
        }
        return currentEvent;
    }

    public void require(int type, String namespaceURI, String localName) throws XMLStreamException {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != type) {
            throw new XMLStreamException("Event type mismatch");
        }

        if (localName != null) {
            if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT
                    && xmlEvent.getEventType() != ENTITY_REFERENCE) {
                throw new XMLStreamException("Expected non-null local name, but current token not a START_ELEMENT, END_ELEMENT or ENTITY_REFERENCE (was " + xmlEvent.getEventType() + ")");
            }
            String n = getLocalName();
            if (!n.equals(localName)) {
                throw new XMLStreamException("Expected local name '" + localName + "'; current local name '" + n + "'.");
            }
        }
        if (namespaceURI != null) {
            if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
                throw new XMLStreamException("Expected non-null NS URI, but current token not a START_ELEMENT or END_ELEMENT (was " + xmlEvent.getEventType() + ")");
            }
            String uri = getNamespaceURI();
            // No namespace?
            if (namespaceURI.length() == 0) {
                if (uri != null && uri.length() > 0) {
                    throw new XMLStreamException("Expected empty namespace, instead have '" + uri + "'.");
                }
            } else {
                if (!namespaceURI.equals(uri)) {
                    throw new XMLStreamException("Expected namespace '" + namespaceURI + "'; have '"
                            + uri + "'.");
                }
            }
        }
    }

    final private static int MASK_GET_ELEMENT_TEXT =
            (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE)
                    | (1 << ENTITY_REFERENCE);

    public String getElementText() throws XMLStreamException {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new XMLStreamException("Not positioned on a start element");
        }
        StringBuffer stringBuffer = new StringBuffer();

        /**
         * Need to loop to get rid of PIs, comments
         */
        while (true) {
            int type = next();
            if (type == END_ELEMENT) {
                break;
            }
            if (type == COMMENT || type == PROCESSING_INSTRUCTION) {
                continue;
            }
            if (((1 << type) & MASK_GET_ELEMENT_TEXT) == 0) {
                throw new XMLStreamException("Expected a text token, got " + xmlEvent.getEventType() + ".");
            }
            stringBuffer.append(getText());
        }
        return stringBuffer.toString();
    }

    public int nextTag() throws XMLStreamException {
        while (true) {
            int next = next();

            switch (next) {
                case SPACE:
                case COMMENT:
                case PROCESSING_INSTRUCTION:
                    continue;
                case CDATA:
                case CHARACTERS:
                    if (isWhiteSpace()) {
                        continue;
                    }
                    throw new XMLStreamException("Received non-all-whitespace CHARACTERS or CDATA event in nextTag().");
                case START_ELEMENT:
                case END_ELEMENT:
                    return next;
            }
            throw new XMLStreamException("Received event " + next
                    + ", instead of START_ELEMENT or END_ELEMENT.");
        }
    }

    public boolean hasNext() throws XMLStreamException {
        if (currentEvent != null && currentEvent.getEventType() == END_DOCUMENT) {
            return false;
        }
        return true;
    }

    public void close() throws XMLStreamException {
        try {
            inputProcessorChain.reset();
            inputProcessorChain.doFinal();
        } catch (WSSecurityException e) {
            throw new XMLStreamException(e);
        }
    }

    public String getNamespaceURI(String prefix) {
        XMLEvent xmlEvent = getCurrentEvent();

        if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_ELEM);
        }

        if (xmlEvent.isStartElement()) {
            return xmlEvent.asStartElement().getNamespaceURI(prefix);
        } else {
            //todo somehow...
            return null;
        }
    }

    public boolean isStartElement() {
        XMLEvent xmlEvent = getCurrentEvent();
        return xmlEvent.isStartElement();
    }

    public boolean isEndElement() {
        XMLEvent xmlEvent = getCurrentEvent();
        return xmlEvent.isEndElement();
    }

    public boolean isCharacters() {
        XMLEvent xmlEvent = getCurrentEvent();
        return xmlEvent.isCharacters();
    }

    public boolean isWhiteSpace() {
        XMLEvent xmlEvent = getCurrentEvent();
        return xmlEvent.isCharacters() && xmlEvent.asCharacters().isWhiteSpace();
    }

    public String getAttributeValue(String namespaceURI, String localName) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        Attribute attribute = xmlEvent.asStartElement().getAttributeByName(new QName(namespaceURI, localName));
        if (attribute != null) {
            return attribute.getValue();
        }
        return null;
    }

    public int getAttributeCount() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Attribute> attributeIterator = xmlEvent.asStartElement().getAttributes();
        while (attributeIterator.hasNext()) {
            attributeIterator.next();
            count++;
        }
        return count;
    }

    public QName getAttributeName(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Attribute> attributeIterator = xmlEvent.asStartElement().getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            if (count == index) {
                return attribute.getName();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public String getAttributeNamespace(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Attribute> attributeIterator = xmlEvent.asStartElement().getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            if (count == index) {
                return attribute.getName().getNamespaceURI();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public String getAttributeLocalName(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Attribute> attributeIterator = xmlEvent.asStartElement().getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            if (count == index) {
                return attribute.getName().getLocalPart();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public String getAttributePrefix(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Attribute> attributeIterator = xmlEvent.asStartElement().getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            if (count == index) {
                return attribute.getName().getPrefix();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public String getAttributeType(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Attribute> attributeIterator = xmlEvent.asStartElement().getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            if (count == index) {
                return attribute.getDTDType();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public String getAttributeValue(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Attribute> attributeIterator = xmlEvent.asStartElement().getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            if (count == index) {
                return attribute.getValue();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public boolean isAttributeSpecified(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Attribute> attributeIterator = xmlEvent.asStartElement().getAttributes();
        while (attributeIterator.hasNext()) {
            Attribute attribute = attributeIterator.next();
            if (count == index) {
                return attribute.isSpecified();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public int getNamespaceCount() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_ELEM);
        }
        int count = 0;
        Iterator<Namespace> namespaceIterator;
        if (xmlEvent.getEventType() == START_ELEMENT) {
            namespaceIterator = xmlEvent.asStartElement().getNamespaces();
        } else {
            namespaceIterator = xmlEvent.asEndElement().getNamespaces();
        }
        while (namespaceIterator.hasNext()) {
            namespaceIterator.next();
            count++;
        }
        return count;
    }

    public String getNamespacePrefix(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_ELEM);
        }
        int count = 0;
        Iterator<Namespace> namespaceIterator;
        if (xmlEvent.getEventType() == START_ELEMENT) {
            namespaceIterator = xmlEvent.asStartElement().getNamespaces();
        } else {
            namespaceIterator = xmlEvent.asEndElement().getNamespaces();
        }
        while (namespaceIterator.hasNext()) {
            Namespace namespace = namespaceIterator.next();
            if (count == index) {
                return namespace.getPrefix();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public String getNamespaceURI(int index) {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        int count = 0;
        Iterator<Namespace> namespaceIterator = xmlEvent.asStartElement().getNamespaces();
        while (namespaceIterator.hasNext()) {
            Namespace namespace = namespaceIterator.next();
            if (count == index) {
                return namespace.getNamespaceURI();
            }
            count++;
        }
        throw new ArrayIndexOutOfBoundsException(index);
    }

    public NamespaceContext getNamespaceContext() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_STELEM);
        }
        return xmlEvent.asStartElement().getNamespaceContext();
    }

    public int getEventType() {
        XMLEvent xmlEvent = getCurrentEvent();
        return xmlEvent.getEventType();
    }

    final private static int MASK_GET_TEXT =
            (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE)
                    | (1 << COMMENT) | (1 << DTD) | (1 << ENTITY_REFERENCE);

    public String getText() {
        XMLEvent xmlEvent = getCurrentEvent();

        if (((1 << xmlEvent.getEventType()) & MASK_GET_TEXT) == 0) {
            throw new IllegalStateException("Current state not TEXT");
        }
        if (xmlEvent.getEventType() == ENTITY_REFERENCE) {
            return ((EntityReference) xmlEvent).getDeclaration().getReplacementText();
        }
        if (xmlEvent.getEventType() == DTD) {
            return ((javax.xml.stream.events.DTD) xmlEvent).getDocumentTypeDeclaration();
        }
        if (xmlEvent.getEventType() == COMMENT) {
            return ((Comment) xmlEvent).getText();
        }
        return xmlEvent.asCharacters().getData();
    }

    final private static int MASK_GET_TEXT_XXX =
            (1 << CHARACTERS) | (1 << CDATA) | (1 << SPACE) | (1 << COMMENT);

    public char[] getTextCharacters() {
        XMLEvent xmlEvent = getCurrentEvent();

        if (((1 << xmlEvent.getEventType()) & MASK_GET_TEXT_XXX) == 0) {
            throw new IllegalStateException("Current state not TEXT");
        }
        if (xmlEvent.getEventType() == ENTITY_REFERENCE) {
            return ((EntityReference) xmlEvent).getDeclaration().getReplacementText().toCharArray();
        }
        if (xmlEvent.getEventType() == DTD) {
            return ((javax.xml.stream.events.DTD) xmlEvent).getDocumentTypeDeclaration().toCharArray();
        }
        if (xmlEvent.getEventType() == COMMENT) {
            return ((Comment) xmlEvent).getText().toCharArray();
        }
        return xmlEvent.asCharacters().getData().toCharArray();
    }

    public int getTextCharacters(int sourceStart, char[] target, int targetStart, int length) throws XMLStreamException {
        XMLEvent xmlEvent = getCurrentEvent();

        if (((1 << xmlEvent.getEventType()) & MASK_GET_TEXT_XXX) == 0) {
            throw new IllegalStateException("Current state not TEXT");
        }
        if (xmlEvent.getEventType() == ENTITY_REFERENCE) {
            ((EntityReference) xmlEvent).getDeclaration().getReplacementText().getChars(sourceStart, sourceStart + length, target, targetStart);
            return sourceStart + length;
        }
        if (xmlEvent.getEventType() == DTD) {
            ((javax.xml.stream.events.DTD) xmlEvent).getDocumentTypeDeclaration().getChars(sourceStart, sourceStart + length, target, targetStart);
            return sourceStart + length;
        }
        if (xmlEvent.getEventType() == COMMENT) {
            ((Comment) xmlEvent).getText().getChars(sourceStart, sourceStart + length, target, targetStart);
            return sourceStart + length;
        }
        xmlEvent.asCharacters().getData().getChars(sourceStart, sourceStart + length, target, targetStart);
        return sourceStart + length;
    }

    public int getTextStart() {
        return 0;
    }

    public int getTextLength() {
        XMLEvent xmlEvent = getCurrentEvent();

        if (((1 << xmlEvent.getEventType()) & MASK_GET_TEXT_XXX) == 0) {
            throw new IllegalStateException("Current state not TEXT");
        }
        if (xmlEvent.getEventType() == ENTITY_REFERENCE) {
            return ((EntityReference) xmlEvent).getDeclaration().getReplacementText().length();
        }
        if (xmlEvent.getEventType() == DTD) {
            return ((javax.xml.stream.events.DTD) xmlEvent).getDocumentTypeDeclaration().length();
        }
        if (xmlEvent.getEventType() == COMMENT) {
            return ((Comment) xmlEvent).getText().length();
        }
        return xmlEvent.asCharacters().getData().length();
    }

    public String getEncoding() {
        return null;
    }

    public boolean hasText() {
        XMLEvent xmlEvent = getCurrentEvent();
        return (((1 << xmlEvent.getEventType()) & MASK_GET_TEXT) != 0);
    }

    public Location getLocation() {
        return new Location() {
            public int getLineNumber() {
                return -1;
            }

            public int getColumnNumber() {
                return -1;
            }

            public int getCharacterOffset() {
                return -1;
            }

            public String getPublicId() {
                return null;
            }

            public String getSystemId() {
                return null;
            }
        };
    }

    public QName getName() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_ELEM);
        }
        if (xmlEvent.isStartElement()) {
            return xmlEvent.asStartElement().getName();
        } else {
            return xmlEvent.asEndElement().getName();
        }
    }

    public String getLocalName() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_ELEM);
        }
        if (xmlEvent.isStartElement()) {
            return xmlEvent.asStartElement().getName().getLocalPart();
        } else {
            return xmlEvent.asEndElement().getName().getLocalPart();
        }
    }

    public boolean hasName() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
            return false;
        }
        return true;
    }

    public String getNamespaceURI() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_ELEM);
        }
        if (xmlEvent.isStartElement()) {
            return xmlEvent.asStartElement().getName().getNamespaceURI();
        } else {
            return xmlEvent.asEndElement().getName().getNamespaceURI();
        }
    }

    public String getPrefix() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != START_ELEMENT && xmlEvent.getEventType() != END_ELEMENT) {
            throw new IllegalStateException(ERR_STATE_NOT_ELEM);
        }
        if (xmlEvent.isStartElement()) {
            return xmlEvent.asStartElement().getName().getPrefix();
        } else {
            return xmlEvent.asEndElement().getName().getPrefix();
        }
    }

    public String getVersion() {
        return null;
    }

    public boolean isStandalone() {
        return false;
    }

    public boolean standaloneSet() {
        return false;
    }

    public String getCharacterEncodingScheme() {
        return null;
    }

    public String getPITarget() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != PROCESSING_INSTRUCTION) {
            throw new IllegalStateException(ERR_STATE_NOT_PI);
        }
        return ((ProcessingInstruction) xmlEvent).getTarget();
    }

    public String getPIData() {
        XMLEvent xmlEvent = getCurrentEvent();
        if (xmlEvent.getEventType() != PROCESSING_INSTRUCTION) {
            throw new IllegalStateException(ERR_STATE_NOT_PI);
        }
        return ((ProcessingInstruction) xmlEvent).getData();
    }
}
