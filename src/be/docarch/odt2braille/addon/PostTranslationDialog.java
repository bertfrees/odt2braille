/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.docarch.odt2braille.addon;

import java.util.logging.Logger;
import java.util.Locale;
import java.util.logging.Level;
import java.util.ResourceBundle;

import com.sun.star.awt.XButton;
import com.sun.star.awt.XDialog;
import com.sun.star.uno.XComponentContext;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.XControl;
import com.sun.star.beans.XPropertySet;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XFixedText;

import com.sun.star.lang.EventObject;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;


/**
 *
 * @author freesb
 */
public class PostTranslationDialog implements XActionListener {

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille.addon");

    private PreviewDialog preview = null;
    private XDialog dialog = null;

    private XButton okButton = null;
    private XButton previewButton = null;
    
    private static String _okButton = "CommandButton1";
    private static String _previewButton = "CommandButton2";
    private static String _message = "Label1";

    private String L10N_windowTitle = null;
    private String L10N_message = null;
    private String L10N_okButton = null;
    private String L10N_previewButton = null;


    public PostTranslationDialog(XComponentContext xContext,
                                 PreviewDialog preview)
                          throws com.sun.star.uno.Exception {

        logger.entering("PostTranslationDialog", "<init>");

        this.preview = preview;

        // L10JN

        Locale oooLocale = null;
        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }

        L10N_windowTitle = "Succes";
        L10N_message = "The document was succesfully translated into Braille.";
        L10N_previewButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("previewButton");
        L10N_okButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("continueButton");

        // Make dialog

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn-windows_x86") + "/dialogs/PostTranslationDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        XControlContainer dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        XControl dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);

        // Dialog items

        okButton = (XButton) UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl(_okButton));
        previewButton = (XButton) UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl(_previewButton));
        okButton.setLabel(L10N_okButton);
        previewButton.setLabel(L10N_previewButton);
        previewButton.addActionListener(this);

        XFixedText xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,dialogControlContainer.getControl(_message));
        xFixedText.setText(L10N_message);

        XPropertySet windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, dialogControl.getModel());
        windowProperties.setPropertyValue("Title", L10N_windowTitle);

        logger.exiting("PostTranslationDialog", "<init>");

    }

    public boolean execute() {

        logger.entering("PostTranslationDialog", "execute");

        short ret = dialog.execute();

        logger.exiting("PostTranslationDialog", "execute");

        if (ret == ((short) PushButtonType.OK_value)) {
            return true;
        } else {
            return false;
        }
    }

    public void actionPerformed(ActionEvent actionEvent) {

        Object source = actionEvent.Source;

        if (source.equals(previewButton)) {
            preview.execute();
        }
    }

    public void disposing(EventObject event) {}

}
