/*
 * Copyright (c) 2002-2014 mgm technology partners GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.mgmtp.perfload.core.common.xml;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.validation.SchemaFactory;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Utility class for loading DOM4J documents.
 * 
 * @author rnaegele
 */
public final class Dom4jReader {

	private Dom4jReader() {
		// don't allow instantiation
	}

	/**
	 * Creates a DOM4J documents from the specified {@link InputSource} and validates it against the
	 * given schema.
	 * 
	 * @param xmlSource
	 *            the source for the XML document
	 * @param schemaSource
	 *            the source for the schema
	 * @param xIncludeAware
	 *            specifies whether XIncludes should be supported
	 * @return the DOM4J document
	 */
	public static Document loadDocument(final InputSource xmlSource, final Source schemaSource, final boolean xIncludeAware,
			final String encoding) throws ParserConfigurationException, SAXException, DocumentException {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(true); // turn on validation
		factory.setNamespaceAware(true);

		//		factory.setFeature("http://apache.org/xml/features/validation/schema", true);
		if (xIncludeAware) {
			// allow XIncludes
			factory.setFeature("http://apache.org/xml/features/xinclude", true);
			factory.setFeature("http://apache.org/xml/features/xinclude/fixup-base-uris", false);
		}

		SchemaFactory schemaFactory = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory.newSchema(schemaSource));

		SAXParser parser = factory.newSAXParser();
		SAXReader reader = new SAXReader(parser.getXMLReader());
		reader.setEncoding(encoding);

		return reader.read(xmlSource);
	}

	/**
	 * Creates a DOM4J documents from the specified {@link InputSource} and validates it against the
	 * given schema. XIncludes are supported by this method.
	 * 
	 * @param xmlSource
	 *            the source for the XML document
	 * @param schemaSource
	 *            the source for the schema
	 * @return the DOM4J document
	 */
	public static Document loadDocument(final InputSource xmlSource, final Source schemaSource, final String encoding)
			throws ParserConfigurationException, SAXException, DocumentException {
		return loadDocument(xmlSource, schemaSource, true, encoding);
	}
}
