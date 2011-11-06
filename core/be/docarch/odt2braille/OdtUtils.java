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

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipFile;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author Vincent Spiewak
 * @author Bert Frees
 *
 */
public class OdtUtils {

    public static final String PICTURE_FOLDER = "Pictures/";
    private static final Logger logger = Logger.getLogger("be.docarch.odt2braille");

    public static void removeEmptyHeadings(Node root){

        // for each text:h
        // remove empty headings
        NodeList hNodes = ((Element) root).getElementsByTagName("text:h");
        for (int i = 0; i < hNodes.getLength(); i++) {

            Node node = hNodes.item(i);

            if (node.getChildNodes().getLength() > 0) {

                boolean empty = true;

                for (int j = 0; j < node.getChildNodes().getLength(); j++) {
                    if (!node.getChildNodes().item(j).getTextContent().trim().equals("")) {
                        empty = false;
                    }
                }

                if (empty) {
                    node.getParentNode().removeChild(node);
                    i--;
                }

            } else {
                if (node.getTextContent().trim().equals("")) {
                    node.getParentNode().removeChild(node);
                    i--;
                }
            }
        }
    }

    /**
     * Add empty paragraph to heading x - heading x
     * @param doc
     * @param root
     */
    public static void insertEmptyParaForHeadings(Document doc){

        NodeList hNodes = doc.getDocumentElement().getElementsByTagName("text:h");
        for (int i = 0; i < hNodes.getLength()-1; i++) {

            Element hElem = (Element) hNodes.item(i);
            Element nextElem;
            Node nextNode = hElem.getNextSibling();

            while (nextNode != null && nextNode.getNodeType() != Node.ELEMENT_NODE){

                nextNode = nextNode.getNextSibling();

            }

            nextElem = (Element) nextNode;

            if(nextElem != null
                    && nextElem.getNodeName().equals("text:h")
                    && hElem.hasAttribute("text:outline-level")
                    && nextElem.hasAttribute("text:outline-level")
                    && hElem.getAttribute("text:outline-level").equals(
                    nextElem.getAttribute("text:outline-level"))
                    ){
                Element para = doc.createElement("text:p");
                hElem.getParentNode().insertBefore(para, nextNode);
            }
        }
    }

    public static void normalizeTextS(Document doc){

        Element root = doc.getDocumentElement();

        NodeList sNodes = ((Element) root).getElementsByTagName("text:s");

        for (int i = 0; i < sNodes.getLength(); i++) {

            Element elem = (Element) sNodes.item(i);

            int c = 1;
            String s = "";

            if(elem.hasAttribute("text:c")){
                c = Integer.parseInt(elem.getAttribute("text:c"));
            }

            for(int j=0; j<c; j++){
                s += " ";
            }

            Node textNode = doc.createTextNode(s);
            elem.getParentNode().replaceChild(textNode, elem);
            i--;
        }
    }

    public static void removeEmptyParagraphs(Node root){

        // for each text:p
        NodeList pNodes = ((Element) root).getElementsByTagName("text:p");
        for (int i = 0; i < pNodes.getLength(); i++) {

            Node node = pNodes.item(i);

            // if no text
            if (node.getTextContent().trim().equals("")){

                // if no children
                if(!node.hasChildNodes()){

                   // then remove
                   node.getParentNode().removeChild(node);
                   i--;

                // if children
                } else {

                    boolean empty = true;

                    // don't remove if an element is present (like image...)
                    for(int j=0; j<node.getChildNodes().getLength(); j++){
                       if(node.getChildNodes().item(j).getNodeType() == node.ELEMENT_NODE){
                            empty = false;
                       }
                    }

                    if(empty){
                        node.getParentNode().removeChild(node);
                        i--;
                    }
                }
            }
        }
    }

        /**
     * Insert MathML separated files into Flat ODT XML
     * @param docBuilder
     * @param contentDoc
     * @param zf
     * @param parentPath
     * @throws java.io.IOException
     * @throws org.xml.sax.SAXException
     */
    public static void replaceObjectContent(
            DocumentBuilder docBuilder, Document contentDoc, ZipFile zf) throws IOException, SAXException {

        logger.fine("entering");

        Element root = contentDoc.getDocumentElement();
        NodeList nodelist = root.getElementsByTagName("draw:object");

        for (int i = 0; i < nodelist.getLength(); i++) {

            Node objectNode = nodelist.item(i);
            Node hrefNode = objectNode.getAttributes().getNamedItem("xlink:href");

            String objectPath = hrefNode.getTextContent();
            logger.fine("object path=" + objectPath);

            Document objectDoc = docBuilder.parse(zf.getInputStream(zf.getEntry(objectPath.substring(2) + "/" + "content.xml")));
            Node objectContentNode = objectDoc.getDocumentElement();

            String tagName = objectContentNode.getNodeName();
            logger.fine(tagName);

            if (tagName.equals("math:math") || tagName.equals("math")) {
                
                logger.fine("replacing math");

                Node newObjectNode = contentDoc.createElement("draw:object");
                newObjectNode.appendChild(contentDoc.importNode(objectContentNode, true));
                objectNode.getParentNode().replaceChild(newObjectNode, objectNode);
            }
        }

        logger.fine("done");
    }

    public static boolean saveDOM(Document doc, File file) {
        boolean save = false;
        try {

            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.METHOD, "xml");
            transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

            transformer.setOutputProperty(OutputKeys.INDENT, "no");

            StreamResult result = new StreamResult(file);
            DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

            save = true;
        } catch (TransformerConfigurationException ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
        } finally {

            return save;

        }
    }

    public static void getFileFromZip(ZipFile zip,
                                      String fileName,
                                      File outputFile)
                               throws IOException {

        InputStream in = zip.getInputStream(zip.getEntry(fileName));
        OutputStream out = new FileOutputStream(outputFile);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();

    }

    public static void addFilesToZip(File zipFile,
                                     File[] files,
                                     String[] fileNames)
                              throws IOException {

        File tempFile = new File(zipFile.getAbsoluteFile() + ".temp");
        if (!zipFile.renameTo(tempFile)) {
           throw new RuntimeException("could not rename");
        }
        byte[] buf = new byte[1024];
        ZipInputStream zin = new ZipInputStream(new FileInputStream(tempFile));
        ZipOutputStream out = new ZipOutputStream(new FileOutputStream(zipFile));
        ZipEntry entry = zin.getNextEntry();
        while (entry != null) {
            String name = entry.getName();
            boolean notInFiles = true;
            for (String f : fileNames) {
                if (f.equals(name)) {
                    notInFiles = false;
                    break;
                }
            }
            if (notInFiles) {
                out.putNextEntry(new ZipEntry(name));
                int len;
                while ((len = zin.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            entry = zin.getNextEntry();
        }
        zin.close();
        for (int i = 0; i < files.length; i++) {
            InputStream in = new FileInputStream(files[i]);
            out.putNextEntry(new ZipEntry(fileNames[i]));
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.closeEntry();
            in.close();
        }
        out.close();
        tempFile.delete();
    }
}
