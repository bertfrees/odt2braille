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
import be.docarch.accessibility.Check;

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
    private static XURI EARL_SOFTWARE;
    private static XURI CHECKER_LASTCHECKED;
    private static XURI CHECKER_DOCUMENT;

    public final String CHECKER_CHECKS = "http://www.docarch.be/accessibility/checks#";

    private XRepository xRepository;
    private XComponentContext context;
    private XDocumentMetadataAccess xDMA;
    private PostConversionBrailleChecker checker;
    private XNamedGraph currentGraph;
    private XResource currentAssertor;
    private Date lastChecked;
    private SimpleDateFormat dateFormat;
    private boolean modified;

    public ReportWriter(PostConversionBrailleChecker checker,
                        XComponentContext context,
                        XComponent doc) {

        this.context = context;
        this.checker = checker;
        
        try {

            RDF_TYPE =            URI.create(context, "http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
            EARL_TESTRESULT =     URI.create(context, "http://www.w3.org/ns/earl#TestResult");
            EARL_TESTSUBJECT =    URI.create(context, "http://www.w3.org/ns/earl#TestSubject");
            EARL_TESTCASE =       URI.create(context, "http://www.w3.org/ns/earl#TestCase");
            EARL_OUTCOME =        URI.create(context, "http://www.w3.org/ns/earl#outcome");
            EARL_FAILED =         URI.create(context, "http://www.w3.org/ns/earl#failed");
            EARL_RESULT =         URI.create(context, "http://www.w3.org/ns/earl#result");
            EARL_SUBJECT =        URI.create(context, "http://www.w3.org/ns/earl#subject");
            EARL_TEST =           URI.create(context, "http://www.w3.org/ns/earl#test");
            EARL_ASSERTION =      URI.create(context, "http://www.w3.org/ns/earl#Assertion");
            EARL_ASSERTEDBY =     URI.create(context, "http://www.w3.org/ns/earl#assertedBy");
            EARL_SOFTWARE =       URI.create(context, "http://www.w3.org/ns/earl#Software");
            EARL_ASSERTOR =       URI.create(context, "http://www.w3.org/ns/earl#Assertor");
            CHECKER_LASTCHECKED = URI.create(context, "http://www.docarch.be/accessibility/lastChecked");
            CHECKER_DOCUMENT =    URI.create(context, "http://www.docarch.be/accessibility/types#document");

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
        lastChecked = new Date();
        String reportName = PostConversionBrailleChecker.class.getCanonicalName()
                                + "/" + new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(lastChecked) + ".rdf";

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
        addStatement(currentAssertor, RDF_TYPE, EARL_SOFTWARE);
        addStatement(currentAssertor, RDF_TYPE, EARL_ASSERTOR);

        modified = false;

        XBlankNode subject = xRepository.createBlankNode();
        addStatement(subject, RDF_TYPE, EARL_TESTSUBJECT);
        addStatement(subject, RDF_TYPE, CHECKER_DOCUMENT);

        for (Check issue : checker.getDetectedIssues()) {
            XURI testcase = URI.createNS(context, CHECKER_CHECKS, issue.getIdentifier());
            addStatement(testcase, RDF_TYPE, EARL_TESTCASE);
            addAssertion(subject, testcase);
        }

        return modified;
    }

    private void addAssertion(XResource subject,
                              XURI check)
                       throws IllegalArgumentException,
                              RepositoryException,
                              NoSuchElementException {

        XBlankNode assertion = xRepository.createBlankNode();
        XBlankNode testresult = xRepository.createBlankNode();

        addStatement(testresult, RDF_TYPE, EARL_TESTRESULT);
        addStatement(testresult, EARL_OUTCOME, EARL_FAILED);
        addStatement(testresult, CHECKER_LASTCHECKED, Literal.create(context, dateFormat.format(lastChecked)));
        addStatement(assertion, RDF_TYPE, EARL_ASSERTION);
        addStatement(assertion, EARL_RESULT, testresult);
        addStatement(assertion, EARL_TEST, check);
        addStatement(assertion, EARL_SUBJECT, subject);
        addStatement(assertion, EARL_ASSERTEDBY, currentAssertor);

        modified = true;
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
