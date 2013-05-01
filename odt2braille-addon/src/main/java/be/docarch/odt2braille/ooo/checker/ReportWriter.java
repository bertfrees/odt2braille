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

package be.docarch.odt2braille.ooo.checker;

import java.util.Date;
import java.text.SimpleDateFormat;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.rdf.URI;
import com.sun.star.rdf.XURI;
import com.sun.star.rdf.XNamedGraph;
import com.sun.star.rdf.XBlankNode;
import com.sun.star.rdf.XResource;
import com.sun.star.rdf.XNode;
import com.sun.star.rdf.Literal;
import com.sun.star.rdf.XDocumentMetadataAccess;
import com.sun.star.rdf.XRepository;
import com.sun.star.lang.XComponent;
import com.sun.star.frame.XModel;
import com.sun.star.text.XTextDocument;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.container.ElementExistException;
import com.sun.star.container.NoSuchElementException;
import com.sun.star.rdf.RepositoryException;

import be.docarch.odt2braille.checker.PostConversionBrailleChecker;
import be.docarch.accessodf.Issue;
import be.docarch.accessodf.Constants;

/**
 *
 * @author Bert Frees
 */
public class ReportWriter {

    private static XURI RDF_TYPE;
    private static XURI EARL_TESTSUBJECT;
    private static XURI EARL_TESTRESULT;
    private static XURI EARL_TESTCASE;
    private static XURI EARL_OUTCOME;
    private static XURI EARL_FAILED;
    private static XURI EARL_TEST;
    private static XURI EARL_SUBJECT;
    private static XURI EARL_RESULT;
    private static XURI EARL_ASSERTOR;
    private static XURI EARL_ASSERTION;
    private static XURI EARL_ASSERTEDBY;
    private static XURI A11Y_DOCUMENT;
    private static XURI A11Y_CHECKER;
    private static XURI DCT_DATE;

    private XRepository xRepository;
    private XComponentContext context;
    private XDocumentMetadataAccess xDMA;
    private PostConversionBrailleChecker checker;
    private XNamedGraph currentGraph;
    private XResource currentAssertor;
    private SimpleDateFormat dateFormat;
    private boolean modified;

    public ReportWriter(PostConversionBrailleChecker checker,
                        XComponentContext context,
                        XComponent doc) {

        this.context = context;
        this.checker = checker;
        
        try {

            RDF_TYPE =            URI.createKnown(context, com.sun.star.rdf.URIs.RDF_TYPE);
            EARL_TESTRESULT =     URI.create(context, Constants.EARL_TESTRESULT);
            EARL_TESTSUBJECT =    URI.create(context, Constants.EARL_TESTSUBJECT);
            EARL_TESTCASE =       URI.create(context, Constants.EARL_TESTCASE);
            EARL_OUTCOME =        URI.create(context, Constants.EARL_OUTCOME);
            EARL_FAILED =         URI.create(context, Constants.EARL_FAILED);
            EARL_RESULT =         URI.create(context, Constants.EARL_RESULT);
            EARL_SUBJECT =        URI.create(context, Constants.EARL_SUBJECT);
            EARL_TEST =           URI.create(context, Constants.EARL_TEST);
            EARL_ASSERTION =      URI.create(context, Constants.EARL_ASSERTION);
            EARL_ASSERTEDBY =     URI.create(context, Constants.EARL_ASSERTEDBY);
            EARL_ASSERTOR =       URI.create(context, Constants.EARL_ASSERTOR);
            A11Y_DOCUMENT =       URI.create(context, Constants.A11Y_DOCUMENT);
            A11Y_CHECKER =        URI.create(context, Constants.A11Y_CHECKER);
            DCT_DATE =            URI.create(context, Constants.DCT_DATE);

            XTextDocument textDocument = (XTextDocument)UnoRuntime.queryInterface(XTextDocument.class, doc);
            XModel xModel = (XModel) UnoRuntime.queryInterface(XModel.class, textDocument);
            xDMA = (XDocumentMetadataAccess)UnoRuntime.queryInterface(XDocumentMetadataAccess.class, xModel);
            xRepository = xDMA.getRDFRepository();

            dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");

        } catch (com.sun.star.uno.Exception e) {
        }
    }

    public boolean write() throws IllegalArgumentException,
                                  NoSuchElementException,
                                  RepositoryException {

        String metaFolder = "meta/";
        XURI metaFolderURI = URI.create(context, xDMA.getNamespace() + metaFolder);
        String reportName = PostConversionBrailleChecker.class.getCanonicalName()
                                + "/" + new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(new Date()) + ".rdf";

        XURI type = URI.create(context, checker.getIdentifier());
        for (XURI graph : xDMA.getMetadataGraphsWithType(type)) {
            xDMA.removeMetadataFile(graph);
        }

        XURI[] types = new XURI[]{ type };
        XURI graphURI = null;
        try {
            graphURI = xDMA.addMetadataFile(metaFolder + reportName, types);
        } catch (ElementExistException e) {
            graphURI = URI.create(context, metaFolderURI.getStringValue() + reportName);
        }

        currentGraph = xRepository.getGraph(graphURI);

        currentAssertor = URI.create(context, checker.getIdentifier());
        addStatement(currentAssertor, RDF_TYPE, EARL_ASSERTOR);
        addStatement(currentAssertor, RDF_TYPE, A11Y_CHECKER);

        modified = false;

        XBlankNode subject = xRepository.createBlankNode();
        addStatement(subject, RDF_TYPE, EARL_TESTSUBJECT);
        addStatement(subject, RDF_TYPE, A11Y_DOCUMENT);

        for (Issue issue : checker.getDetectedIssues()) {

            XURI testcase = URI.createNS(context, Constants.A11Y_CHECKS, issue.getCheck().getIdentifier());
            addStatement(testcase, RDF_TYPE, EARL_TESTCASE);

            XBlankNode assertion = xRepository.createBlankNode();
            XBlankNode testresult = xRepository.createBlankNode();

            addStatement(testresult, RDF_TYPE, EARL_TESTRESULT);
            addStatement(testresult, EARL_OUTCOME, EARL_FAILED);
            addStatement(testresult, DCT_DATE, Literal.create(context, dateFormat.format(issue.getCheckDate())));
            addStatement(assertion, RDF_TYPE, EARL_ASSERTION);
            addStatement(assertion, EARL_RESULT, testresult);
            addStatement(assertion, EARL_TEST, testcase);
            addStatement(assertion, EARL_SUBJECT, subject);
            addStatement(assertion, EARL_ASSERTEDBY, currentAssertor);

            modified = true;
        }

        return modified;
    }

    private void addStatement(XResource subject,
                              XURI predicate,
                              XNode object)
                       throws IllegalArgumentException,
                              NoSuchElementException,
                              RepositoryException {

        if (currentGraph != null) {
            currentGraph.addStatement(subject, predicate, object);
        }
    }
}
