/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.namespace.NamespaceContext;
import javax.xml.transform.TransformerException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;

import net.sf.saxon.xpath.XPathFactoryImpl;

import org.apache.xpath.XPathAPI;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.traversal.NodeIterator;
import org.xml.sax.InputSource;

/**
 * This class was taken from com.versusoft.packages.xml
 * A few changes were made by Bert Frees.
 *
 * @author Vincent Spiewak
 * @see    @url{http://odt2daisy.sourceforge.net/downloads/} - odt2daisy.jar
 */
public class XPathUtils {

    private static final Logger logger = Constants.getLogger();

    public static Double evaluateNumber(InputStream stream, String expression, NamespaceContext namespace) {
        try {
            return (Double)getXPath(namespace).compile(expression).evaluate(new InputSource(stream), XPathConstants.NUMBER);
        } catch (XPathExpressionException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static Integer evaluateInteger(Node context, String expression) {
        try {
            return Integer.parseInt(XPathAPI.eval(context, expression).str());
        } catch (TransformerException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static Boolean evaluateBoolean(InputStream stream, String expression, NamespaceContext namespace) {
        try {
            return (Boolean)getXPath(namespace).compile(expression).evaluate(new InputSource(stream), XPathConstants.BOOLEAN);
        } catch (XPathExpressionException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static Boolean evaluateBoolean(Node context, String expression) {
        try {
            return XPathAPI.eval(context, expression).bool();
        } catch (TransformerException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static String evaluateString(InputStream stream, String expression, NamespaceContext namespace) {
        try {
            return (String)getXPath(namespace).compile(expression).evaluate(new InputSource(stream), XPathConstants.STRING);
        } catch (XPathExpressionException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static String evaluateString(Node context, String expression) {
        try {
            return XPathAPI.eval(context, expression).str();
        } catch (TransformerException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static Node evaluateNode(Element context, String expression) {
        try {
            return XPathAPI.selectSingleNode(context, expression);
        } catch (TransformerException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static NodeIterator evaluateNodeIterator(Node context, String expression) {
        try {
            return XPathAPI.selectNodeIterator(context, expression);
        } catch (TransformerException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static NodeList evaluateNodeList(Node context, String expression, NamespaceContext namespace) {
        try {
            return (NodeList)getXPath(namespace).compile(expression).evaluate(context, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw new RuntimeException(e);
        } catch (RuntimeException e) {
            logger.log(Level.SEVERE, "Error evaluating xpath: " + expression, e);
            throw e;
        }
    }

    public static Iterable<Node> evaluateNodes(Node context, String expression, NamespaceContext namespace) {
        NodeList nodeList = evaluateNodeList(context, expression, namespace);
        List<Node> list = new ArrayList<Node>();
        for (int i = 0; i < nodeList.getLength(); i++) {
            list.add(nodeList.item(i));
        }
        return list;
    }

    private static XPath getXPath(NamespaceContext namespace) {
        XPath xpath = new XPathFactoryImpl().newXPath();
        if (namespace != null)
            xpath.setNamespaceContext(namespace);
        return xpath;
    }
}
