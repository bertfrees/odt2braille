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

import java.util.Collection;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Logger;

import com.sun.star.awt.XWindowPeer;
import com.sun.star.uno.XComponentContext;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.checker.PostConversionBrailleChecker;
import be.docarch.odt2braille.ooo.UnoAwtUtils;
import be.docarch.odt2braille.ooo.UnoUtils;
import be.docarch.accessodf.Issue;

/**
 *
 * @author Bert Frees
 */
public class BrailleCheckerDialog {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private Collection<Issue> detectedIssues;
    private XWindowPeer parentWindowPeer;
    private Locale oooLocale;

    private static String L10N_warning;
    private static String L10N_question;
    private static String L10N_details;
    private static String L10N_Warning_MessageBox_Title;

    public BrailleCheckerDialog(PostConversionBrailleChecker checker,
                                XComponentContext xContext,
                                XWindowPeer parentWindowPeer) {

        logger.entering("BrailleCheckerDialog", "<init>");

        detectedIssues = checker.getDetectedIssues();
        this.parentWindowPeer = parentWindowPeer;

        try { oooLocale = UnoUtils.getUILocale(xContext); } catch (Exception e) {
              oooLocale = Locale.ENGLISH; }
        ResourceBundle bundle = ResourceBundle.getBundle(Constants.OOO_L10N_PATH, oooLocale);

        L10N_warning = bundle.getString("checkerWarning");
        L10N_question = bundle.getString("checkerQuestion");
        L10N_details = bundle.getString("checkerDetails");
        L10N_Warning_MessageBox_Title = bundle.getString("warningMessageBoxTitle");
        
    }

    private String getWarningMessage() {

        String warning = L10N_warning + "\n\n" + L10N_details + ": \n";

        for (Issue issue : detectedIssues) {
            warning += "\n \u2022 " + issue.getCheck().getDescription(oooLocale);
        }

        warning += "\n\n" + L10N_question + "\n\n";

        return warning;
    }

    public boolean execute() {

        logger.entering("BrailleCheckerDialog", "execute");

        if (detectedIssues.size() > 0) {
            if (UnoAwtUtils.showYesNoWarningMessageBox(parentWindowPeer, L10N_Warning_MessageBox_Title, getWarningMessage()) == (short) 3) {
                return false;
            }
        }

        return true;
    }
}
