/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
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

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.EventObject;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XButton;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;

import be.docarch.odt2braille.Constants;


/**
 * Show an OpenOffice.org dialog window for selecting the embosser device.
 *
 * @author   Bert Frees
 */
public class PrintDialog implements XItemListener {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N = Constants.OOO_L10N_PATH;

    private boolean printToFile = false;
    private int numberOfCopies = 1;
    private int maxNumberOfCopies = 1;

    private XDialog xDialog = null;
    private XControlContainer xControlContainer = null;
    private XComponent xComponent = null;

    private XCheckBox printToFileCheckBox = null;
    private XListBox deviceListBox = null;
    private XNumericField numberOfCopiesField = null;
    private XButton okButton = null;
    private XButton cancelButton = null;

    private XPropertySet deviceListBoxProperties = null;
    private XPropertySet numberOfCopiesFieldProperties = null;

    private static String _deviceListBox = "ListBox1";
    private static String _printToFileCheckBox = "CheckBox1";
    private static String _numberOfCopiesField = "NumericField1";
    private static String _okButton = "CommandButton1";
    private static String _cancelButton = "CommandButton2";

    private static String _deviceLabel = "Label1";
    private static String _printToFileLabel = "Label2";
    private static String _numberOfCopiesLabel = "Label3";

    private String L10N_deviceLabel = null;
    private String L10N_printToFileLabel = null;
    private String L10N_numberOfCopiesLabel = null;
    private String L10N_okButton = null;
    private String L10N_cancelButton = null;
    private String L10N_windowTitle = null;


    /**
     * The dialog is created from an OpenOffice.org .xdl file.
     *
     * @param   xContext
     */
    public PrintDialog(XComponentContext xContext)
                 throws com.sun.star.uno.Exception {

        logger.entering("PrintDialog", "<init>");

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/PrintDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);

        Locale oooLocale = null;

        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }

        L10N_deviceLabel = ResourceBundle.getBundle(L10N, oooLocale).getString("deviceLabel") + ":";
        L10N_printToFileLabel = ResourceBundle.getBundle(L10N, oooLocale).getString("printToFileLabel");
        L10N_numberOfCopiesLabel = "Number of copies:";
        L10N_okButton = ResourceBundle.getBundle(L10N, oooLocale).getString("embossButton");
        L10N_cancelButton = ResourceBundle.getBundle(L10N, oooLocale).getString("cancelButton");
        L10N_windowTitle = ResourceBundle.getBundle(L10N, oooLocale).getString("embossDialogTitle");

        xDialog = xDialogProvider.createDialog(dialogUrl);

        xControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, xDialog);
        xComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, xDialog);

        deviceListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                            xControlContainer.getControl(_deviceListBox));
        printToFileCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                            xControlContainer.getControl(_printToFileCheckBox));
        numberOfCopiesField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                            xControlContainer.getControl(_numberOfCopiesField));
        okButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                            xControlContainer.getControl(_okButton));
        cancelButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                            xControlContainer.getControl(_cancelButton));

        XPropertySet windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, xDialog)).getModel());
        deviceListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, deviceListBox)).getModel());
        numberOfCopiesFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, numberOfCopiesField)).getModel());

        windowProperties.setPropertyValue("Title", L10N_windowTitle);

        printToFileCheckBox.addItemListener(this);

    }

    /**
     * Available devices are looked up, labels are set, and the dialog is executed.
     * It is disposed when the user presses OK or Cancel.
     *
     * @return          The name of the selected embosser device, or
     *                  an empty String if the dialog was canceled.
     */
    public String execute() throws com.sun.star.uno.Exception {

        logger.entering("PrintDialog", "execute");

        // System.setProperty("sun.java2d.print.polling", "false");


        XFixedText xFixedText = null;

        okButton.setLabel(L10N_okButton);
        cancelButton.setLabel(L10N_cancelButton);

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                        xControlContainer.getControl(_deviceLabel));
        xFixedText.setText(L10N_deviceLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                        xControlContainer.getControl(_printToFileLabel));
        xFixedText.setText(L10N_printToFileLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                        xControlContainer.getControl(_numberOfCopiesLabel));
        xFixedText.setText(L10N_numberOfCopiesLabel);

        DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(flavor, null);

        short i = 0;
        for (PrintService p : printers) {
            deviceListBox.addItem(p.getName(), i);
            i++;
        } 
        deviceListBox.selectItemPos((short)0, true);

        numberOfCopiesField.setDecimalDigits((short)0);
        numberOfCopiesField.setMin((double)1);
        numberOfCopiesField.setMax((double)maxNumberOfCopies);
        numberOfCopiesField.setValue((double)numberOfCopies);

        updateProperties();

        short ret = xDialog.execute();

        String deviceName = deviceListBox.getSelectedItem();
        numberOfCopies = (int)numberOfCopiesField.getValue();

        xComponent.dispose();

        logger.exiting("PrintDialog", "execute");

        if (ret == ((short) PushButtonType.OK_value)) {
            return deviceName;
        } else {
            return "";
        }
    }

    public void updateProperties() throws com.sun.star.uno.Exception {

        printToFile = (printToFileCheckBox.getState() == (short) 1);
        deviceListBoxProperties.setPropertyValue("Enabled", !printToFile);
        numberOfCopiesFieldProperties.setPropertyValue("Enabled", maxNumberOfCopies > 1);
    }

    public boolean getPrintToFile() {
        return printToFile;
    }

    public int getNumberOfCopies() {
        return numberOfCopies;
    }

    public void setMaxNumberOfCopies(int maxNumberOfCopies) {

        this.maxNumberOfCopies = (int)Math.max(1, maxNumberOfCopies);
        numberOfCopiesField.setMax((double)this.maxNumberOfCopies);
        numberOfCopies = (int)Math.min(this.maxNumberOfCopies, numberOfCopies);
    }

    public void itemStateChanged(ItemEvent itemEvent) {

        Object source = itemEvent.Source;

        try {

            if (source.equals(printToFileCheckBox)) {
                updateProperties();
            }

        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    /**
     * @param event
     */
    public void disposing(EventObject event) {}
}