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
import java.util.logging.Level;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.ArrayList;

import com.sun.star.awt.XWindow;
import com.sun.star.frame.XFrame;
import com.sun.star.awt.XExtendedToolkit;
import com.sun.star.awt.XToolkit;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.awt.XKeyHandler;
import com.sun.star.awt.KeyEvent;
import com.sun.star.awt.Key;
import com.sun.star.awt.XDialogProvider2;
import com.sun.star.awt.DialogProvider2;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XDialog;
import com.sun.star.uno.XComponentContext;
import com.sun.star.awt.XControlContainer;
import com.sun.star.lang.XComponent;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XButton;
import com.sun.star.text.XTextViewCursor;
import com.sun.star.lang.EventObject;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;


/**
 *
 * @author freesb
 */
public class SixKeyEntryDialog implements XKeyHandler {

    private final static Logger logger = Logger.getLogger("be.docarch.odt2braille.addon");
    private XText xText = null;
    private XTextCursor xTextCursor = null;
    private XExtendedToolkit MyExtToolkit = null;

    private ArrayList<Short> keyCodes = new ArrayList();
    private ArrayList<Boolean> keysPressed = new ArrayList();
    private boolean ready = true;

    private XDialog dialog = null;
    private XControlContainer dialogControlContainer = null;
    private XComponent dialogComponent = null;

    private XButton closeButton = null;
    private XTextComponent charField = null;

    private static String _charField = "TextField1";
    private static String _closeButton = "CommandButton1";
    private static String L10N_closeButton = null;


    public SixKeyEntryDialog(XComponentContext xContext,
                             XFrame xFrame,
                             XTextViewCursor xViewCursor)
                      throws com.sun.star.uno.Exception {

        logger.entering("SixKeyEntryDialog", "<init>");

        xText = xViewCursor.getText();
        xTextCursor = xText.createTextCursorByRange(xViewCursor.getStart());

        XWindow xWindow = xFrame.getComponentWindow();
        XWindowPeer MyWindowPeer = (XWindowPeer) UnoRuntime.queryInterface (XWindowPeer.class, xWindow);
        XToolkit MyToolkit = MyWindowPeer.getToolkit();
        MyExtToolkit = (XExtendedToolkit) UnoRuntime.queryInterface (XExtendedToolkit.class, MyToolkit);

        keyCodes.add(Key.SPACE);
        keyCodes.add(Key.F);
        keyCodes.add(Key.D);
        keyCodes.add(Key.S);
        keyCodes.add(Key.J);
        keyCodes.add(Key.K);
        keyCodes.add(Key.L);

        for (int i=0;i<7;i++) {
            keysPressed.add(false);
        }

        // L10N

        Locale oooLocale;
        try {
            oooLocale = new Locale(UnoUtils.getUILocale(xContext));
        } catch (com.sun.star.uno.Exception ex) {
            logger.log(Level.SEVERE, null, ex);
            oooLocale = Locale.getDefault();
        }

        L10N_closeButton = ResourceBundle.getBundle("be/docarch/odt2braille/addon/l10n/Bundle", oooLocale).getString("closeButton");

        // Dialog creation

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation("be.docarch.odt2braille.addon.Odt2BrailleAddOn")+ "/dialogs/SixKeyEntryDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);

        // Dialog items

        closeButton = (XButton) UnoRuntime.queryInterface(XButton.class, dialogControlContainer.getControl(_closeButton));
        closeButton.setLabel(L10N_closeButton);
        charField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class, dialogControlContainer.getControl(_charField));
        charField.setMaxTextLen((short)1);
        charField.setText("\u2800");

        logger.exiting("SixKeyEntryDialog", "<init>");

    }

    public void execute() throws com.sun.star.uno.Exception {

        logger.entering("InsertDialog", "execute");

        MyExtToolkit.addKeyHandler(this);
        dialog.execute();
        dialogComponent.dispose();
        MyExtToolkit.removeKeyHandler(this);

        logger.exiting("InsertDialog", "execute");

    }

    private void insertCharacter() {

        logger.log(Level.INFO, "insertCharacter");
    
        int offset = 0;
        String character = null;
        
        for (int i=0;i<6;i++) {
            if (keysPressed.get(i+1)) {
                offset += 1<<i;
            }
        }

        character = Character.toString((char)(0x2800 + offset));
        charField.setText(character);
        xText.insertString(xTextCursor.getEnd(), character, false);
        
    }

    public boolean keyPressed(KeyEvent e) {

        logger.log(Level.INFO, "Pressed: " + Short.toString(e.KeyCode));

        if (keyCodes.contains(e.KeyCode)) {
            keysPressed.set(keyCodes.indexOf(e.KeyCode), true);
            return true;
        } else {
            return false;
        }
    }

    public boolean keyReleased(KeyEvent e) {

        logger.log(Level.INFO, "Released: " + Short.toString(e.KeyCode));

        if (keyCodes.contains(e.KeyCode)) {
            if (ready) {
                ready = false;
                logger.log(Level.INFO, "ready = false");
                insertCharacter();
            }
            keysPressed.set(keyCodes.indexOf(e.KeyCode), false);
            if (!keysPressed.contains(true)) {
                ready = true;
                logger.log(Level.INFO, "ready = true");
            }
            return true;
        } else {
            return false;
        }
    }

    public void disposing(EventObject event) {}

}
