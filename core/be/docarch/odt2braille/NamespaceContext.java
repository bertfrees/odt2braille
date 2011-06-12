/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
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

import java.util.Iterator;

/**
 * Implementation of <code>NamespaceContext</code> for bounding prefixes to namespace URI's.
 *
 * @author freesb
 */
public class NamespaceContext implements javax.xml.namespace.NamespaceContext {

    public NamespaceContext() {
        super() ;
    }

    public String getNamespaceURI(String prefix) {
        if ("dc".equals(prefix)) {
            return "http://purl.org/dc/elements/1.1/";

        } else if ("office".equals(prefix)) {
            return "urn:oasis:names:tc:opendocument:xmlns:office:1.0";

        } else if ("meta".equals(prefix)) {
            return "urn:oasis:names:tc:opendocument:xmlns:meta:1.0";

        } else if ("text".equals(prefix)) {
            return "urn:oasis:names:tc:opendocument:xmlns:text:1.0";

        } else if ("draw".equals(prefix)) {
            return "urn:oasis:names:tc:opendocument:xmlns:drawing:1.0";

        } else if ("math".equals(prefix)) {
            return "http://www.w3.org/1998/Math/MathML";

        } else if ("fo".equals(prefix)) {
            return "urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0";

        } else if ("style".equals(prefix)){
            return "urn:oasis:names:tc:opendocument:xmlns:style:1.0";

        } else if ("table".equals(prefix)){
            return "urn:oasis:names:tc:opendocument:xmlns:table:1.0";

        } else if ("svg".equals(prefix)){
            return "urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0";

        } else if ("dtb".equals(prefix)){
            return "http://www.daisy.org/z3986/2005/dtbook/";

        } else if ("pef".equals(prefix)){
            return "http://www.daisy.org/ns/2008/pef";

        } else if ("o2b".equals(prefix)){
            return "http://odt2braille.sf.net";

        } else {
            return null;
        }
    }

    public String getPrefix(String namespaceURI) {
        if ("http://purl.org/dc/elements/1.1/".equals(namespaceURI)) {
            return "dc";

        } else if ("urn:oasis:names:tc:opendocument:xmlns:office:1.0".equals(namespaceURI)) {
            return "office";

        } else if ("urn:oasis:names:tc:opendocument:xmlns:meta:1.0".equals(namespaceURI)) {
            return "meta";

        } else if ("urn:oasis:names:tc:opendocument:xmlns:text:1.0".equals(namespaceURI)) {
            return "text";

        } else if ("urn:oasis:names:tc:opendocument:xmlns:drawing:1.0".equals(namespaceURI)) {
            return "draw";

        } else if ("http://www.w3.org/1998/Math/MathML".equals(namespaceURI)) {
            return "math";

        } else if("urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0".equals(namespaceURI)){
            return "fo";

        } else if("urn:oasis:names:tc:opendocument:xmlns:style:1.0".equals(namespaceURI)){
            return "style";

        } else if("urn:oasis:names:tc:opendocument:xmlns:table:1.0".equals(namespaceURI)){
            return "table";

        } else if("urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0".equals(namespaceURI)){
            return "svg";

        } else if("http://www.daisy.org/z3986/2005/dtbook/".equals(namespaceURI)){
            return "dtb";

        } else if("http://www.daisy.org/ns/2008/pef".equals(namespaceURI)){
            return "pef";

        } else if("http://odt2braille.sf.net".equals(namespaceURI)){
            return "o2b";

        } else {
            return null;
        }
    }

    public Iterator getPrefixes(String namespaceURI) {
        return null;
    }

}
