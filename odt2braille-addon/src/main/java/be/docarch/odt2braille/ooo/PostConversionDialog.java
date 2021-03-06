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

package be.docarch.odt2braille.ooo;

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
import com.sun.star.lang.XComponent;

import com.sun.star.lang.EventObject;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;

import be.docarch.odt2braille.Constants;


/**
 *
 * @author freesb
 */
public class PostConversionDialog implements XActionListener {

    private final static Logger logger = Constants.getLogger();
    private final static String L10N = Constants.OOO_L10N_PATH;

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


    public PostConversionDialog(XComponentContext xContext,
                                 PreviewDialog preview)
                          throws com.sun.star.uno.Exception {

        logger.entering("PostTranslationDialog", "<init>");

        this.preview = preview;

        // L10JN

        Locale oooLocale;
        try { oooLocale = UnoUtils.getUILocale(xContext); } catch (Exception e) {
              oooLocale = Locale.ENGLISH; }
        ResourceBundle bundle = ResourceBundle.getBundle(L10N, oooLocale);

        L10N_windowTitle = "Succes"; // successMessageBoxTitle
        L10N_message = "The document was succesfully translated into Braille."; // successMessage
        L10N_previewButton = bundle.getString("previewButton");
        L10N_okButton = bundle.getString("continueButton");

        // Make dialog

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/PostTranslationDialog.xdl";
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
        preview.dispose();
        XComponent dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        dialogComponent.dispose();

        logger.exiting("PostTranslationDialog", "execute");

        if (ret == ((short) PushButtonType.OK_value)) {
            return true;
        } else {
            return false;
        }
    }

    public void actionPerformed(ActionEvent actionEvent) {

        Object source = actionEvent.Source;

        try {
            if (source.equals(previewButton)) {
                preview.execute();
            }
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        } catch (javax.xml.transform.TransformerException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void disposing(EventObject event) {}

}
