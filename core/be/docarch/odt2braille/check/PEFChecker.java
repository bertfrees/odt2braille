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

import be.docarch.odt2braille.PEF;
import be.docarch.odt2braille.XML;
import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.NamespaceContext;
import be.docarch.odt2braille.setup.PEFConfiguration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.utils.XPathUtils;
import be.docarch.accessodf.Checker;
import be.docarch.accessodf.Issue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * @author Bert Frees
 */
public class PEFChecker implements Checker {

    private final static Logger logger = Constants.getLogger();

    private Map<String,be.docarch.accessodf.Check> checks;

    private Set<be.docarch.accessodf.Check> detectedIssues;

    private static NamespaceContext namespace = new NamespaceContext();

    private final static int MAX_VOLUME_LENGTH = 100;
    private final static int MIN_VOLUME_LENGTH = 70;
    private final static int MAX_VOLUME_DIFFERENCE = 20;

    public PEFChecker() {

        checks = new HashMap<String,be.docarch.accessodf.Check>();

        checks.put(Check.ID.A_VolumesTooLong.name(),               new Check(Check.ID.A_VolumesTooLong));
        checks.put(Check.ID.A_VolumesTooShort.name(),              new Check(Check.ID.A_VolumesTooShort));
        checks.put(Check.ID.A_VolumesDifferTooMuch.name(),         new Check(Check.ID.A_VolumesDifferTooMuch));
        checks.put(Check.ID.A_PreliminaryVolumeRequired.name(),    new Check(Check.ID.A_PreliminaryVolumeRequired));
        checks.put(Check.ID.A_PreliminaryVolumeTooShort.name(),    new Check(Check.ID.A_PreliminaryVolumeTooShort));
        checks.put(Check.ID.A_VolumeDoesntBeginWithHeading.name(), new Check(Check.ID.A_VolumeDoesntBeginWithHeading));
        checks.put(Check.ID.A_OmittedInBraille.name(),             new Check(Check.ID.A_OmittedInBraille));
        checks.put(Check.ID.A_NotInBrailleVolume.name(),           new Check(Check.ID.A_NotInBrailleVolume));
        checks.put(Check.ID.A_TransposedInBraille.name(),          new Check(Check.ID.A_TransposedInBraille));
        checks.put(Check.ID.A_PageWidthTooSmall.name(),            new Check(Check.ID.A_PageWidthTooSmall));
        checks.put(Check.ID.A_EmbosserDoesNotSupport8Dot.name(),   new Check(Check.ID.A_EmbosserDoesNotSupport8Dot));
        checks.put(Check.ID.A_FileFormatDoesNotSupport8Dot.name(), new Check(Check.ID.A_FileFormatDoesNotSupport8Dot));

        detectedIssues = new HashSet<be.docarch.accessodf.Check>();

    }

//    public void checkXMLFile(File xmlFile) throws Exception {
//
//        logger.entering("PostConversionBrailleChecker", "checkDaisyFile");
//
//        if (XPathUtils.evaluateBoolean(xmlFile.toURL().openStream(),
//                "/dtb:dtbook//dtb:div[@class='omission' and not(ancestor::dtb:div[@class='not-in-volume'])]", namespace)) {
//            detectedIssues.add(get(Check.ID.A_OmittedInBraille.name()));
//        }
//        if (XPathUtils.evaluateBoolean(xmlFile.toURL().openStream(),
//                "/dtb:dtbook//dtb:div[@class='not-in-volume']//dtb:div[@class='omission']", namespace)) {
//            detectedIssues.add(get(Check.ID.A_NotInBrailleVolume.name()));
//        }
//        if (XPathUtils.evaluateBoolean(xmlFile.toURL().openStream(),
//                "/dtb:dtbook//dtb:div[@class='transposition']", namespace)) {
//            detectedIssues.add(get(Check.ID.A_TransposedInBraille.name()));
//        }
//    }

    public void check(PEF pef, PEFConfiguration pefConfiguration) {}
    
    private void checkPefFile(File pefFile, PEFConfiguration pefConfiguration) {

        if (!pefConfiguration.getEightDots()) {
            try {
                if (Integer.parseInt(XPathUtils.evaluateString(pefFile.toURL().openStream(),
                        "max(distinct-values(string-to-codepoints(string(/pef:pef/pef:body))))", namespace)) > 0x283F) {
                    detectedIssues.add(get((pefConfiguration instanceof ExportConfiguration) 
                            ? Check.ID.A_FileFormatDoesNotSupport8Dot.name()
                            : Check.ID.A_EmbosserDoesNotSupport8Dot.name()));
                }
            } catch (Exception e) {
            }
        }
    }

    /**
     * Check the volume lengths of the resulting braille document for possible accessibility issues.
     * The volumes should not be too long or too short, and they should not differ to much in lenght.
     *
     * @param   bodyPageCount           The number of pages in the body of each volume. The other parameters should have the same array length.
     * @param   preliminaryPageCount    The number of preliminary pages in each volume.
     * @param   volumeTypes             The type of each volume ({@link VolumeType#NORMAL}, {@link VolumeType#PRELIMINARY} or
     *                                  {@link VolumeType#SUPPLEMENTARY}).
     *
     */
    private void checkVolumes(List<XML.Volume> volumes) {

//        int bodyRectoVersoPageCount = 0;
//        int preliminaryRectoVersoPageCount = 0;
//        int maxLength = 0;
//        int minLength = Integer.MAX_VALUE;
//        int length = 0;
//
//        Volume volume;
//        Volume.Type type;
//        int volumeNr;
//        int numberOfBodyPages;
//        int numberOfPreliminaryPages;
//
//        for (int i=0;i<volumes.size();i++) {
//
//            volume = volumes.get(i);
//            type = volume.getType();
//            volumeNr = volume.getNumber();
//            numberOfBodyPages = volume.getLastBraillePage() - volume.getFirstBraillePage() + 1;
//            numberOfPreliminaryPages = volume.getNumberOfPreliminaryPages();
//
//            bodyRectoVersoPageCount = settings.getDuplex()?(int)Math.ceil(numberOfBodyPages/2):numberOfBodyPages;
//            preliminaryRectoVersoPageCount = settings.getDuplex()?(int)Math.ceil(numberOfPreliminaryPages/2):numberOfPreliminaryPages;
//            length = preliminaryRectoVersoPageCount + bodyRectoVersoPageCount;
//
//            switch (type) {
//                case PRELIMINARY:
//                    if (length < MIN_VOLUME_LENGTH) {
//                        preliminaryVolumeTooShort = true;
//                    }
//                    break;
//                case NORMAL:
//                    if (volumeNr == 1 && numberOfPreliminaryPages > MAX_VOLUME_LENGTH) {
//                        preliminaryVolumeRequired = true;
//                    }
//                    if (length > MAX_VOLUME_LENGTH) {
//                        volumesTooLong = true;
//                    }
//                    maxLength = Math.max(maxLength, length);
//                    if (volumeNr < settings.NUMBER_OF_VOLUMES) {
//                        if (length < MIN_VOLUME_LENGTH) {
//                            volumesTooShort = true;
//                        }
//                        minLength = Math.min(minLength, length);
//                    }
//                    break;
//                case SUPPLEMENTARY:
//                default:
//            }
//        }
//
//        if (settings.NUMBER_OF_VOLUMES > 1 && maxLength > minLength + MAX_VOLUME_DIFFERENCE) {
//            volumesDifferTooMuch = true;
//        }
    }

    public String getIdentifier() {
        return "http://docarch.be/odt2braille/checker/PostConversionBrailleChecker";
    }

    public Collection<be.docarch.accessodf.Check> list() {
        return checks.values();
    }

    public be.docarch.accessodf.Check get(String identifier) {
        return checks.get(identifier);
    }
    
    public Collection<Issue> getDetectedIssues() {

        List<Issue> issues = new ArrayList<Issue>();
        Date date = new Date();
        for (be.docarch.accessodf.Check c : detectedIssues) {
            issues.add(new Issue(null, c, this, date));
        }
        return issues;
    }
}
