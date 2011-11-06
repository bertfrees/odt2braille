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
 
 package be.docarch.odt2braille.checker;

import java.io.File;
import java.util.Date;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;

import java.net.MalformedURLException;
import java.io.IOException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.NamespaceContext;
import be.docarch.odt2braille.setup.PEFConfiguration;
import be.docarch.odt2braille.setup.ExportConfiguration;
import be.docarch.odt2braille.Volume;
import be.docarch.odt2braille.XPathUtils;
import be.docarch.accessodf.Checker;
import be.docarch.accessodf.Check;
import be.docarch.accessodf.Issue;

/**
 * @author Bert Frees
 */
public class PostConversionBrailleChecker implements Checker {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private Map<String,Check> checks;

    private Set<Check> detectedIssues;

    private static NamespaceContext namespace = new NamespaceContext();

    private final static int MAX_VOLUME_LENGTH = 100;
    private final static int MIN_VOLUME_LENGTH = 70;
    private final static int MAX_VOLUME_DIFFERENCE = 20;

    public PostConversionBrailleChecker() {

        logger.entering("PostConversionBrailleChecker", "<init>");

        checks = new HashMap<String,Check>();

        checks.put(BrailleCheck.ID.A_VolumesTooLong.name(),               new BrailleCheck(BrailleCheck.ID.A_VolumesTooLong));
        checks.put(BrailleCheck.ID.A_VolumesTooShort.name(),              new BrailleCheck(BrailleCheck.ID.A_VolumesTooShort));
        checks.put(BrailleCheck.ID.A_VolumesDifferTooMuch.name(),         new BrailleCheck(BrailleCheck.ID.A_VolumesDifferTooMuch));
        checks.put(BrailleCheck.ID.A_PreliminaryVolumeRequired.name(),    new BrailleCheck(BrailleCheck.ID.A_PreliminaryVolumeRequired));
        checks.put(BrailleCheck.ID.A_PreliminaryVolumeTooShort.name(),    new BrailleCheck(BrailleCheck.ID.A_PreliminaryVolumeTooShort));
        checks.put(BrailleCheck.ID.A_VolumeDoesntBeginWithHeading.name(), new BrailleCheck(BrailleCheck.ID.A_VolumeDoesntBeginWithHeading));
        checks.put(BrailleCheck.ID.A_OmittedInBraille.name(),             new BrailleCheck(BrailleCheck.ID.A_OmittedInBraille));
        checks.put(BrailleCheck.ID.A_NotInBrailleVolume.name(),           new BrailleCheck(BrailleCheck.ID.A_NotInBrailleVolume));
        checks.put(BrailleCheck.ID.A_TransposedInBraille.name(),          new BrailleCheck(BrailleCheck.ID.A_TransposedInBraille));
        checks.put(BrailleCheck.ID.A_PageWidthTooSmall.name(),            new BrailleCheck(BrailleCheck.ID.A_PageWidthTooSmall));
        checks.put(BrailleCheck.ID.A_EmbosserDoesNotSupport8Dot.name(),   new BrailleCheck(BrailleCheck.ID.A_EmbosserDoesNotSupport8Dot));
        checks.put(BrailleCheck.ID.A_FileFormatDoesNotSupport8Dot.name(), new BrailleCheck(BrailleCheck.ID.A_FileFormatDoesNotSupport8Dot));

        detectedIssues = new HashSet<Check>();

    }

    public void checkDaisyFile(File daisyFile) 
                        throws MalformedURLException,
                               IOException {

        logger.entering("PostConversionBrailleChecker", "checkDaisyFile");

        if (XPathUtils.evaluateBoolean(daisyFile.toURL().openStream(),
                "/dtb:dtbook//dtb:div[@class='omission' and not(ancestor::dtb:div[@class='not-in-volume'])]", namespace)) {
            detectedIssues.add(get(BrailleCheck.ID.A_OmittedInBraille.name()));
        }
        if (XPathUtils.evaluateBoolean(daisyFile.toURL().openStream(),
                "/dtb:dtbook//dtb:div[@class='not-in-volume']//dtb:div[@class='omission']", namespace)) {
            detectedIssues.add(get(BrailleCheck.ID.A_NotInBrailleVolume.name()));
        }
        if (XPathUtils.evaluateBoolean(daisyFile.toURL().openStream(),
                "/dtb:dtbook//dtb:div[@class='transposition']", namespace)) {
            detectedIssues.add(get(BrailleCheck.ID.A_TransposedInBraille.name()));
        }
    }

    public void checkPefFile(File pefFile,
                             PEFConfiguration pefSettings)
                      throws MalformedURLException,
                             IOException {

        logger.entering("PostConversionBrailleChecker","checkPefFile");

        boolean eightDots = pefSettings.getEightDots();
        if (!eightDots) {
            try {
                if (Integer.parseInt(XPathUtils.evaluateString(pefFile.toURL().openStream(),
                        "max(distinct-values(string-to-codepoints(string(/pef:pef/pef:body))))", namespace)) > 0x283F) {
                    detectedIssues.add(get((pefSettings instanceof ExportConfiguration) ? BrailleCheck.ID.A_FileFormatDoesNotSupport8Dot.name()
                                                                                        : BrailleCheck.ID.A_EmbosserDoesNotSupport8Dot.name()));
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
    public void checkVolumes(List<Volume> volumes) {

        logger.entering("PostConversionBrailleChecker", "checkVolumes");

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

    public Collection<Check> list() {
        return checks.values();
    }

    public Check get(String identifier) {
        return checks.get(identifier);
    }
    
    public Collection<Issue> getDetectedIssues() {

        List<Issue> issues = new ArrayList<Issue>();
        Date date = new Date();
        for (Check c : detectedIssues) {
            issues.add(new Issue(null, c, this, date));
        }
        return issues;
    }
}
