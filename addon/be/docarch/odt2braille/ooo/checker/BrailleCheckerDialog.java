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
import be.docarch.odt2braille.checker.BrailleCheck;
//import be.docarch.accessibility.Check;

/**
 *
 * @author Bert Frees
 */
public class BrailleCheckerDialog {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);

    private Collection<BrailleCheck> detectedIssues;
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

        for (BrailleCheck issue : detectedIssues) {
            warning += "\n \u2022 " + issue.getDescription(oooLocale);
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
