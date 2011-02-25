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

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.Locale;
import java.util.ResourceBundle;

import com.sun.star.awt.XDialog;
import com.sun.star.uno.XComponentContext;
import com.sun.star.awt.XControlContainer;
import com.sun.star.lang.XComponent;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XButton;
import com.sun.star.beans.XPropertySet;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XTextListener;
import com.sun.star.awt.TextEvent;
import com.sun.star.lang.EventObject;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.ActionEvent;

import be.docarch.odt2braille.Constants;


/**
 *
 * @author  Bert Frees
 */
public class InsertDialog implements XTextListener,
                                     XActionListener {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private final static String L10N = Constants.OOO_L10N_PATH;

    private boolean ret = false;

    private String dots = "";
    private String chars = "";

    private XText xText = null;
    private XTextCursor xTextCursor = null;
    private Locale oooLocale = null;

    private XDialog dialog = null;
    private XControlContainer dialogControlContainer = null;
    private XComponent dialogComponent = null;

    private XButton okButton = null;
    private XButton nextButton = null;
    private XButton cancelButton = null;
    private XTextComponent dotsField = null;
    private XTextComponent charsField = null;

    private XPropertySet windowProperties = null;
    private XPropertySet okButtonProperties = null;
    private XPropertySet nextButtonProperties = null;

    private static String _cancelButton = "CommandButton1";
    private static String _okButton = "CommandButton2";
    private static String _nextButton = "CommandButton3";
    private static String _charsField = "TextField1";
    private static String _dotsField = "TextField2";

    private static String L10N_windowTitle = null;
    private static String L10N_okButton = "OK";
    private static String L10N_nextButton = "Next";
    private static String L10N_cancelButton = null;    


    public InsertDialog(XComponentContext xContext,
                        XTextViewCursor xViewCursor)
                 throws com.sun.star.uno.Exception {
        
        this(xContext);

        xText = xViewCursor.getText();
        xTextCursor = xText.createTextCursorByRange(xViewCursor.getStart());

        L10N_cancelButton = ResourceBundle.getBundle(L10N, oooLocale).getString("closeButton");
        L10N_okButton = ResourceBundle.getBundle(L10N, oooLocale).getString("insertButton");
        nextButtonProperties.setPropertyValue("Enabled", true);
        nextButton.addActionListener(this);
    }

    public InsertDialog(XComponentContext xContext)
                 throws com.sun.star.uno.Exception {

        logger.entering("InsertDialog", "<init>");
        
        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }
        
        L10N_windowTitle = ResourceBundle.getBundle(L10N, oooLocale).getString("insertDialogTitle");
        L10N_cancelButton = ResourceBundle.getBundle(L10N, oooLocale).getString("cancelButton");

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/InsertDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);
        XControl dialogControl = (XControl)UnoRuntime.queryInterface(XControl.class, dialog);

        okButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_okButton));
        nextButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_nextButton));
        cancelButton = (XButton) UnoRuntime.queryInterface(XButton.class,
                dialogControlContainer.getControl(_cancelButton));
        dotsField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_dotsField));
        charsField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class,
                dialogControlContainer.getControl(_charsField));

        windowProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,dialogControl.getModel());
        okButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, okButton)).getModel());
        nextButtonProperties = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class,
                ((XControl)UnoRuntime.queryInterface(XControl.class, nextButton)).getModel());
        nextButtonProperties.setPropertyValue("Enabled", false);
        okButton.addActionListener(this);

        logger.exiting("InsertDialog", "<init>");

    }

    public boolean setBrailleCharacters(String characters) {
    
        if (charsValid(characters)) {
            this.chars = characters;
            this.dots = charsToDots(characters);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean setBrailleDots(String dots) {
    
        if (dotsValid(dots)) {
            this.dots = dots;
            this.chars = dotsToChars(dots);
            return true;
        } else {
            return false;
        }
    }
    
    public boolean execute() throws com.sun.star.uno.Exception {

        logger.entering("InsertDialog", "execute");

        setLabels();
        setDialogValues();
        dotsField.addTextListener(this);
        dialog.execute();
        dialogComponent.dispose();

        logger.exiting("InsertDialog", "execute");

        return ret;

    }

    public String getBrailleCharacters() {
        return chars;
    }

    public String getBrailleDots() {
        return dots;
    }

    private void setLabels() throws com.sun.star.uno.Exception {
    
        windowProperties.setPropertyValue("Title", L10N_windowTitle);
        cancelButton.setLabel(L10N_cancelButton);
        okButton.setLabel(L10N_okButton);
        nextButton.setLabel(L10N_nextButton);
        
    }

    private void setDialogValues() throws com.sun.star.uno.Exception {

        charsField.setText(chars);
        dotsField.setText(dots);
        updateButtons();

    }
    
    private void updateButtons() throws com.sun.star.uno.Exception {

        okButtonProperties.setPropertyValue("Enabled", (dotsValid(dots)));
        nextButtonProperties.setPropertyValue("Enabled", (dotsValid(dots)) && xText!=null);

    }

    private String convertTo6dot(String chars) {

        StringBuffer charsBuffer = new StringBuffer(chars);
        StringBuffer chars6dotBuffer = new StringBuffer();

        for (int i=0;i<charsBuffer.length();i++) {
            chars6dotBuffer.append((char)((int)(charsBuffer.charAt(i)) & 0xFF3F));
        }

        return chars6dotBuffer.toString();

    }

    private boolean charsValid(String chars) {
        if (chars.length()>0) {
            return chars.matches("[\\p{InBraille_Patterns}]*");
        } else {
            return true;
        }
    }
    
    private boolean dotsValid(String dots) {
        return dots.matches("^$|(0|12?3?4?5?6?7?8?|23?4?5?6?7?8?|34?5?6?7?8?|45?6?7?8?|56?7?8?|67?8?|78?|8)" +
                             "(-(0|12?3?4?5?6?7?8?|23?4?5?6?7?8?|34?5?6?7?8?|45?6?7?8?|56?7?8?|67?8?|78?|8))*");
    }
    
    private String charsToDots(String chars) {

        StringBuffer charsBuffer = new StringBuffer(chars);
        StringBuffer dotsBuffer = new StringBuffer();
        String singleDots = null;
        int singleChar;
        boolean first = true;

        if (charsValid(chars) && chars.length()>0) {

            for (int i=0;i<charsBuffer.length();i++) {

                singleChar = (int)(charsBuffer.charAt(i)) - 0x2800;
                singleDots = "";

                if (singleChar>0) {
                    for (int j=7;j>=0;j--) {
                        if (singleChar >= (1<<j)) {
                            singleChar -= (1<<j);
                            singleDots = Integer.toString(j+1) + singleDots;
                        }
                    }
                } else {
                    singleDots = "0";
                }

                if (!first) {
                    dotsBuffer.append('-');
                } else {
                    first = false;
                }

                dotsBuffer.append(singleDots);
            }
        }

        return dotsBuffer.toString();
    
    }
    
    private String dotsToChars(String dots) {

        StringBuffer dotsBuffer = new StringBuffer(dots);
        StringBuffer charsBuffer = new StringBuffer();
        String singleDots = null;
        int offset;
        int index;
        boolean last = false;

        if (dotsValid(dots) && dots.length()>0) {

            while(!last) {

                offset = 0;
                index = dotsBuffer.indexOf("-");

                if (index <= 0) {
                    last = true;
                    singleDots = dotsBuffer.toString();
                } else {
                    singleDots = dotsBuffer.substring(0, index);
                    dotsBuffer.delete(0, index+1);
                }

                for (int i=0;i<8;i++) {
                    if (singleDots.indexOf(Integer.toString(i+1)) >= 0) {
                        offset += 1<<i;
                    }
                }

                charsBuffer.append((char)(0x2800 + offset));

            }
        }

        return charsBuffer.toString();
    
    }

    private void reset() {
    
        chars = "";
        dots = "";
        charsField.setText(chars);
        dotsField.setText(dots);

    }

    public void textChanged(TextEvent textEvent) {

        Object source = textEvent.Source;

        try {
            if (source.equals(dotsField)) {
                dots = dotsField.getText();
                if (dotsValid(dots)) {
                    chars = dotsToChars(dots);
                    charsField.setText(chars);
                }
                updateButtons();
            }
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
        }
    }

    public void actionPerformed(ActionEvent actionEvent) {

        Object source = actionEvent.Source;

        if (source.equals(okButton)) {
            if (xText==null) {
                ret = true;
                dialog.endExecute();
            } else {
                xText.insertString(xTextCursor.getEnd(), chars, false);
                reset();
            }
        } else if (source.equals(nextButton)) {
            xText.insertString(xTextCursor.getEnd(), chars + " ", false);
            reset();
        }
    }

    /**
     * @param event
     */
    public void disposing(EventObject event) {}

}
