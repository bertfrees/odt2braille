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

package be.docarch.odt2braille.check;

import be.docarch.accessodf.RemoteRunnableChecker;
import be.docarch.accessodf.Report;
import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.ODT;
import be.docarch.odt2braille.convert.ControlFlowMaker;
import be.docarch.odt2braille.convert.XSLTransformer;
import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.utils.FileCreator;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.Collection;
import java.util.Map;
import java.util.HashMap;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Bert Frees
 */
public class ODTChecker implements RemoteRunnableChecker {

    private final static Logger logger = Constants.getLogger();

    private final XSLTransformer earlXSL;
    
    private File odtFile;
    private File earlReport;
    private String reportName;
    private Date lastChecked;
    private SimpleDateFormat dateFormat;
    private Map<String,be.docarch.accessodf.Check> checks;

    static {
        try {
            String uri = ODTChecker.class.getResource(".").toURI().toString();
            uri = uri.substring(4, uri.lastIndexOf("lib/odt2braille.jar!")) + "liblouis";
            Constants.setLiblouisDirectory(new File(new URI(uri)));
        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }
    }

    public ODTChecker() throws Exception {

        dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        
        earlXSL = new XSLTransformer("earl");
        String ns = be.docarch.accessodf.Constants.A11Y_CHECKS;
        
        earlXSL.setParameter("checkerID",                getIdentifier());
        earlXSL.setParameter("paramNoBrailleToc",        ns + Check.ID.A_NoBrailleToc.name());
        earlXSL.setParameter("paramOmittedCaption",      ns + Check.ID.A_OmittedCaption.name());
        earlXSL.setParameter("paramOmittedInBraille",    ns + Check.ID.A_OmittedInBraille.name());
        earlXSL.setParameter("paramNotInBrailleVolume",  ns + Check.ID.A_NotInBrailleVolume.name());
        earlXSL.setParameter("paramTransposedInBraille", ns + Check.ID.A_TransposedInBraille.name());
        
        earlReport = FileCreator.createTempFile(".earl.rdf.xml");

        checks = new HashMap<String,be.docarch.accessodf.Check>();
        checks.put(Check.ID.A_NoBrailleToc.name(),    new Check(Check.ID.A_NoBrailleToc));
        checks.put(Check.ID.A_OmittedCaption.name(),  new Check(Check.ID.A_OmittedCaption));
    }

    public Collection<be.docarch.accessodf.Check> list() {
        return checks.values();
    }

    public be.docarch.accessodf.Check get(String identifier) {
        return checks.get(identifier);
    }

    public void setOdtFile(File odtFile) {
        this.odtFile = odtFile;
    }

    public Report getAccessibilityReport() {
        return new Report(earlReport, reportName);
    }

    public boolean run() {

        if (odtFile == null) { return false; }

        lastChecked = new Date();
        reportName = "be.docarch.odt2braille.checker.BrailleChecker/"
                        + new SimpleDateFormat("yyyy-MM-dd'T'HH.mm.ss").format(lastChecked) + ".rdf";

        try {

            ODT odt = new ODT(odtFile);
            
            XSLTransformer makeControlFlow = new ControlFlowMaker();
            
            makeControlFlow.setParameter("styles-url", odt.getStylesFile().toURI());
            
            // TODO: load settings
            Configuration configuration = null;
            
            String frontMatter = configuration.getFrontMatterSection();
            String repeatFrontMatter = configuration.getRepeatFrontMatterSection();
            String titlePage = configuration.getTitlePageSection();
            String rearMatter = configuration.getRearMatterSection();

            List<String> manualVolumeSections = new ArrayList<String>();
            for (Configuration.SectionVolume v : configuration.getSectionVolumeList().values()) {
                manualVolumeSections.add(v.getSection());
            }
            
            makeControlFlow.setParameter("frontMatterSection", (frontMatter==null)?"":frontMatter);
            makeControlFlow.setParameter("repeatFrontMatterSection", (repeatFrontMatter==null)?"":repeatFrontMatter);
            makeControlFlow.setParameter("titlePageSection", (titlePage==null)?"":titlePage);
            makeControlFlow.setParameter("manualVolumeSections", manualVolumeSections.toArray(new String[manualVolumeSections.size()]));
            makeControlFlow.setParameter("rearMatterSection", (rearMatter==null)?"":rearMatter);
            
            File odtContentFile = odt.getContentFile();
            File controllerFile = makeControlFlow.convert(odtContentFile);

            earlXSL.setParameter("paramTocEnabled", true);
            earlXSL.setParameter("paramTimestamp", dateFormat.format(lastChecked));
            earlXSL.setParameter("content-url", odtContentFile.toURI());

            earlXSL.convert(controllerFile, earlReport);
            
            // Clean up
            odt.close();

            return true;

        } catch (Exception e) {
            logger.log(Level.SEVERE, null, e);
        }

        return false;
    }

    public String getIdentifier() {
        return "http://docarch.be/odt2braille/checker/BrailleChecker";
    }
}
