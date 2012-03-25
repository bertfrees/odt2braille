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

package be.docarch.odt2braille.ooo.dialog;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.ooo.UnoUtils;

import javax.print.DocFlavor;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserFeatures;

import com.sun.star.uno.XComponentContext;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.EventObject;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XDialog;
import com.sun.star.awt.XControlContainer;
import com.sun.star.awt.PushButtonType;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.XTextListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.XButton;
import com.sun.star.awt.XPrinterServer;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.beans.XPropertySet;

/**
 * Show an OpenOffice.org dialog window for selecting the embosser driver.
 *
 * @author   Bert Frees
 */
public class PrintDialog implements XItemListener,
                                    XTextListener {

    private final static Logger logger = Constants.getLogger();
    private final static String L10N = Constants.OOO_L10N_PATH;

    private boolean printToFile = false;
    private Embosser embosser = null;

    private List<String> availableDrivers;
    private String defaultDriver = "";

    private XDialog xDialog = null;
    private XControlContainer xControlContainer = null;
    private XComponent xComponent = null;

    private XCheckBox printToFileCheckBox = null;
    private XListBox driverListBox = null;
    private XNumericField numberOfCopiesField = null;
    private XButton okButton = null;
    private XButton cancelButton = null;
    private XTextComponent numberOfCopiesTextComponent = null;

    private XPropertySet driverListBoxProperties = null;
    private XPropertySet numberOfCopiesFieldProperties = null;

    private static String _driverListBox = "ListBox1";
    private static String _printToFileCheckBox = "CheckBox1";
    private static String _numberOfCopiesField = "NumericField1";
    private static String _okButton = "CommandButton1";
    private static String _cancelButton = "CommandButton2";

    private static String _driverLabel = "Label1";
    private static String _printToFileLabel = "Label2";
    private static String _numberOfCopiesLabel = "Label3";

    private String L10N_driverLabel = null;
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
    public PrintDialog(XComponentContext xContext,
                       Embosser embosser,
                       String driver)
                throws com.sun.star.uno.Exception {

        this.embosser = embosser;
        if (driver != null) { defaultDriver = driver; }
        
        availableDrivers = new ArrayList<String>();

     /* DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
        PrintService[] printers = PrintServiceLookup.lookupPrintServices(flavor, null);
        for (PrintService p : printers) { availableDrivers.add(p.getName()); } */

        XPrinterServer printerServer = (XPrinterServer)UnoRuntime.queryInterface(
                                         XPrinterServer.class, xContext.getServiceManager().createInstanceWithContext(
                                         "com.sun.star.awt.PrinterServer", xContext));
        if (printerServer != null) {
            for (String p : printerServer.getPrinterNames()) {
                availableDrivers.add(p);
            }
        }

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/PrintDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);

        Locale oooLocale;
        try { oooLocale = UnoUtils.getUILocale(xContext); } catch (Exception e) {
              oooLocale = Locale.ENGLISH; }
        ResourceBundle bundle = ResourceBundle.getBundle(L10N, oooLocale);

        L10N_driverLabel = bundle.getString("deviceLabel") + ":";
        L10N_printToFileLabel = bundle.getString("printToFileLabel");
        L10N_numberOfCopiesLabel = "Number of copies:"; // numberOfCopiesLabel
        L10N_okButton = bundle.getString("embossButton");
        L10N_cancelButton = bundle.getString("cancelButton");
        L10N_windowTitle = bundle.getString("embossDialogTitle");

        xDialog = xDialogProvider.createDialog(dialogUrl);

        xControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, xDialog);
        xComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, xDialog);

        driverListBox = (XListBox) UnoRuntime.queryInterface(XListBox.class,
                            xControlContainer.getControl(_driverListBox));
        printToFileCheckBox = (XCheckBox) UnoRuntime.queryInterface(XCheckBox.class,
                            xControlContainer.getControl(_printToFileCheckBox));
        numberOfCopiesField = (XNumericField) UnoRuntime.queryInterface(XNumericField.class,
                            xControlContainer.getControl(_numberOfCopiesField));
        okButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                            xControlContainer.getControl(_okButton));
        cancelButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                            xControlContainer.getControl(_cancelButton));
        numberOfCopiesTextComponent = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                            xControlContainer.getControl(_numberOfCopiesField));

        XPropertySet windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, xDialog)).getModel());
        driverListBoxProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, driverListBox)).getModel());
        numberOfCopiesFieldProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, numberOfCopiesField)).getModel());

        windowProperties.setPropertyValue("Title", L10N_windowTitle);
        
        if (embosser.getFeature(EmbosserFeatures.NUMBER_OF_COPIES) == null) {
            numberOfCopiesFieldProperties.setPropertyValue("Enabled", false);
        } else {
            numberOfCopiesFieldProperties.setPropertyValue("Enabled", true);
        }

        numberOfCopiesTextComponent.addTextListener(this);
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

        // System.setProperty("sun.java2d.print.polling", "false");


        XFixedText xFixedText = null;

        okButton.setLabel(L10N_okButton);
        cancelButton.setLabel(L10N_cancelButton);

        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                        xControlContainer.getControl(_driverLabel));
        xFixedText.setText(L10N_driverLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                        xControlContainer.getControl(_printToFileLabel));
        xFixedText.setText(L10N_printToFileLabel);
        xFixedText = (XFixedText) UnoRuntime.queryInterface(XFixedText.class,
                        xControlContainer.getControl(_numberOfCopiesLabel));
        xFixedText.setText(L10N_numberOfCopiesLabel);

        short i = 0;
        for (String device : availableDrivers) {
            driverListBox.addItem(device, i);
            i++;
        }
        if (availableDrivers.contains(defaultDriver)) {
            driverListBox.selectItemPos((short)availableDrivers.indexOf(defaultDriver), true);
        } else {
            driverListBox.selectItemPos((short)0, true);
        }

        numberOfCopiesField.setDecimalDigits((short)0);
        numberOfCopiesField.setMin((double)1);
        numberOfCopiesField.setMax((double)Integer.MAX_VALUE);
        numberOfCopiesField.setValue((double)1);

        updateProperties();

        short ret = xDialog.execute();

        String selectedDriver = driverListBox.getSelectedItem();

        xComponent.dispose();

        if (ret == ((short) PushButtonType.OK_value)) {
            return selectedDriver;
        } else {
            return "";
        }
    }

    public void updateProperties() throws com.sun.star.uno.Exception {

        printToFile = (printToFileCheckBox.getState() == (short) 1);
        driverListBoxProperties.setPropertyValue("Enabled", !printToFile);
    }

    public boolean getPrintToFile() {
        return printToFile;
    }

    @Override
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
    @Override
    public void disposing(EventObject event) {}

    @Override
    public void textChanged(TextEvent textEvent) {

        Object source = textEvent.Source;

        if (source.equals(numberOfCopiesTextComponent)) {

            try {
                embosser.setFeature(EmbosserFeatures.NUMBER_OF_COPIES, (int)numberOfCopiesField.getValue());
                numberOfCopiesField.setValue((Integer)embosser.getFeature(EmbosserFeatures.NUMBER_OF_COPIES));
            } catch (IllegalArgumentException e) {
            } catch (ClassCastException e) {
            }
        }
    }
}