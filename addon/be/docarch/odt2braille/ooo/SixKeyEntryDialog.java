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
import java.util.logging.Level;
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
import com.sun.star.text.XTextViewCursor;
import com.sun.star.lang.EventObject;
import com.sun.star.deployment.PackageInformationProvider;
import com.sun.star.deployment.XPackageInformationProvider;
import com.sun.star.text.XText;
import com.sun.star.text.XTextCursor;

import be.docarch.odt2braille.Constants;


/**
 *
 * @author freesb
 */
public class SixKeyEntryDialog implements XKeyHandler {

    private final static Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    private XText xText = null;
    private XTextCursor xTextCursor = null;
    private XExtendedToolkit myExtToolkit = null;

    private ArrayList<Short> keyCodes = new ArrayList();
    private ArrayList<Boolean> keysPressed = new ArrayList();
    private boolean noKeysPressed = true;

    private XDialog dialog = null;
    private XControlContainer dialogControlContainer = null;
    private XComponent dialogComponent = null;

    private XTextComponent charField = null;
    private static String _charField = "TextField1";


    public SixKeyEntryDialog(XComponentContext xContext,
                             XFrame xFrame,
                             XTextViewCursor xViewCursor)
                      throws com.sun.star.uno.Exception {

        logger.entering("SixKeyEntryDialog", "<init>");

        xText = xViewCursor.getText();
        xTextCursor = xText.createTextCursorByRange(xViewCursor.getStart());

        XWindow xWindow = xFrame.getComponentWindow();
        XWindowPeer myWindowPeer = (XWindowPeer) UnoRuntime.queryInterface (XWindowPeer.class, xWindow);
        XToolkit myToolkit = myWindowPeer.getToolkit();
        myExtToolkit = (XExtendedToolkit) UnoRuntime.queryInterface (XExtendedToolkit.class, myToolkit);

        keyCodes.add(Key.F);
        keyCodes.add(Key.D);
        keyCodes.add(Key.S);
        keyCodes.add(Key.J);
        keyCodes.add(Key.K);
        keyCodes.add(Key.L);

        for (int i=0;i<keyCodes.size();i++) {
            keysPressed.add(false);
        }

        XPackageInformationProvider xPkgInfo = PackageInformationProvider.get(xContext);
        String dialogUrl = xPkgInfo.getPackageLocation(Constants.OOO_PACKAGE_NAME) + "/dialogs/SixKeyEntryDialog.xdl";
        XDialogProvider2 xDialogProvider = DialogProvider2.create(xContext);
        dialog = xDialogProvider.createDialog(dialogUrl);
        dialogControlContainer = (XControlContainer)UnoRuntime.queryInterface(XControlContainer.class, dialog);
        dialogComponent = (XComponent)UnoRuntime.queryInterface(XComponent.class, dialog);

        charField = (XTextComponent) UnoRuntime.queryInterface(XTextComponent.class, dialogControlContainer.getControl(_charField));
        charField.setMaxTextLen((short)1);
        charField.setText("\u2800");

        logger.exiting("SixKeyEntryDialog", "<init>");

    }

    public void execute() throws com.sun.star.uno.Exception {

        logger.entering("InsertDialog", "execute");

        myExtToolkit.addKeyHandler(this);
        dialog.execute();
        dialogComponent.dispose();
        myExtToolkit.removeKeyHandler(this);

        logger.exiting("InsertDialog", "execute");

    }

    private void insertCharacter() {

        logger.log(Level.INFO, "insertCharacter");
    
        int offset = 0;
        String character = null;
        
        for (int i=0;i<keysPressed.size();i++) {
            if (keysPressed.get(i)) {
                offset += 1<<i;
            }
        }
        if (offset>0) {
            character = Character.toString((char)(0x2800 + offset));
            charField.setText(character);
            xText.insertString(xTextCursor.getEnd(), character, false);
        }
    }

    private void insertSpace() {

        logger.log(Level.INFO, "insertSpace");

        charField.setText("\u2800");
        xText.insertString(xTextCursor.getEnd(), " ", false);

    }

    private void insertSoftReturn() {

        logger.log(Level.INFO, "insertSoftReturn");

        charField.setText("\u2800");
        xText.insertString(xTextCursor.getEnd(), "\n", false);

    }

    private void backSpace() {

        logger.log(Level.INFO, "backSpace");

        charField.setText("\u2800");
        xTextCursor.goLeft((short)1, true);
        xTextCursor.setString("");

    }

    public boolean keyPressed(KeyEvent e) {

        if (keyCodes.contains(e.KeyCode)) {
            keysPressed.set(keyCodes.indexOf(e.KeyCode), true);
            return true;
        } else if (e.KeyCode == Key.SPACE ||
                   e.KeyCode == Key.BACKSPACE ||
                   e.KeyCode == Key.RETURN) {
            return true;
        } else {
            return false;
        }
    }

    public boolean keyReleased(KeyEvent e) {

        if (keyCodes.contains(e.KeyCode)) {
            if (noKeysPressed) {
                noKeysPressed = false;
                insertCharacter();
            }
            keysPressed.set(keyCodes.indexOf(e.KeyCode), false);
            if (!keysPressed.contains(true)) {
                noKeysPressed = true;
            }
            return true;
        } else if (e.KeyCode == Key.SPACE) {
            if (noKeysPressed) {
                insertSpace();
            }
            return true;
        } else if (e.KeyCode == Key.BACKSPACE) {
            if (noKeysPressed) {
                backSpace();
            }
            return true;
        } else if (e.KeyCode == Key.RETURN) {
            if (noKeysPressed) {
                insertSoftReturn();
            }
            return true;
        } else {
            return false;
        }
    }

    public void disposing(EventObject event) {}

}
