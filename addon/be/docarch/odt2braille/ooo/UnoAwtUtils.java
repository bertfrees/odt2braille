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

import com.sun.star.awt.MessageBoxButtons;
import com.sun.star.awt.Rectangle;
import com.sun.star.awt.XMessageBox;
import com.sun.star.awt.XMessageBoxFactory;
import com.sun.star.awt.XWindowPeer;
import com.sun.star.beans.XPropertySet;
import com.sun.star.lang.XComponent;
import com.sun.star.lang.XInitialization;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.ui.dialogs.XExecutableDialog;
import com.sun.star.ui.dialogs.XFilePicker;
import com.sun.star.ui.dialogs.XFilterManager;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.uno.XComponentContext;

/**
 * This class was taken from com.versusoft.packages.ooo
 *
 * @author Vincent Spiewak
 */
public class UnoAwtUtils {

    public static String showSaveAsDialog(String filename, String filterName, String filterPattern, XComponentContext m_xContext) {
        String sStorePath = "";
        XComponent xComponent = null;
        XMultiComponentFactory m_xMCF = m_xContext.getServiceManager();

        try {
            // the filepicker is instantiated with the global Multicomponentfactory...
            Object oFilePicker = m_xMCF.createInstanceWithContext("com.sun.star.ui.dialogs.FilePicker", m_xContext);
            XFilePicker xFilePicker = (XFilePicker) UnoRuntime.queryInterface(XFilePicker.class, oFilePicker);

            // choose the template that defines the capabilities of the filepicker dialog
            XInitialization xInitialize = (XInitialization) UnoRuntime.queryInterface(XInitialization.class, xFilePicker);
            Short[] listAny = new Short[]{new Short(com.sun.star.ui.dialogs.TemplateDescription.FILESAVE_AUTOEXTENSION)};
            xInitialize.initialize(listAny);

            // add a control to the dialog to add the extension automatically to the filename...
            // CRASH ON OOo Beta 3 MACOSX
            //XFilePickerControlAccess xFilePickerControlAccess = (XFilePickerControlAccess) UnoRuntime.queryInterface(XFilePickerControlAccess.class, xFilePicker);
            //xFilePickerControlAccess.setValue(com.sun.star.ui.dialogs.ExtendedFilePickerElementIds.CHECKBOX_AUTOEXTENSION, (short) 0, new Boolean(true));

            xComponent = (XComponent) UnoRuntime.queryInterface(XComponent.class, xFilePicker);

            // execute the dialog...
            XExecutableDialog xExecutable = (XExecutableDialog) UnoRuntime.queryInterface(XExecutableDialog.class, xFilePicker);

            // set the filters of the dialog. The filternames may be retrieved from
            // http://wiki.services.openoffice.org/wiki/Framework/Article/Filter
            XFilterManager xFilterManager = (XFilterManager) UnoRuntime.queryInterface(XFilterManager.class, xFilePicker);
            xFilterManager.appendFilter(filterName, filterPattern);

            // set the initial displaydirectory.
            Object oPathSettings = m_xMCF.createInstanceWithContext("com.sun.star.util.PathSettings", m_xContext);
            XPropertySet xPropertySet = (XPropertySet) com.sun.star.uno.UnoRuntime.queryInterface(XPropertySet.class, oPathSettings);
            String sTemplateUrl = (String) xPropertySet.getPropertyValue("Work_writable");
            xFilePicker.setDisplayDirectory(sTemplateUrl);

            //set the initial filename
                xFilePicker.setDefaultName(filename);

            short nResult = xExecutable.execute();

            // query the resulting path of the dialog...
            if (nResult == com.sun.star.ui.dialogs.ExecutableDialogResults.OK) {
                String[] sPathList = xFilePicker.getFiles();
                if (sPathList.length > 0) {
                    sStorePath = sPathList[0];
                }
            }

        } catch (com.sun.star.uno.Exception exception) {
            exception.printStackTrace();

        } finally {
            //make sure always to dispose the component and free the memory!
            if (xComponent != null) {
                xComponent.dispose();
            }
        }
        return sStorePath;
    }

    public static short showMessageBox(XWindowPeer parentWindowPeer, String messageBoxType, int messageBoxButtons, String messageBoxTitle, String message) {
        if (parentWindowPeer == null || messageBoxType == null || messageBoxTitle == null || message == null) {
            return 0;
        }

        // Initialize the message box factory
        XMessageBoxFactory messageBoxFactory = (XMessageBoxFactory) UnoRuntime.queryInterface(XMessageBoxFactory.class, parentWindowPeer.getToolkit());

        Rectangle messageBoxRectangle = new Rectangle();

        XMessageBox box = messageBoxFactory.createMessageBox(parentWindowPeer, messageBoxRectangle, messageBoxType, messageBoxButtons, messageBoxTitle, message);
        return box.execute();
    }

    public static short showInfoMessageBox(XWindowPeer parentWindowPeer, String messageBoxTitle, String message) {
        if (parentWindowPeer == null || messageBoxTitle == null || message == null) {
            return 0;
        }

        return showMessageBox(parentWindowPeer, "infobox", MessageBoxButtons.BUTTONS_OK, messageBoxTitle, message);
    }

    public static short showErrorMessageBox(XWindowPeer parentWindowPeer, String messageBoxTitle, String message) {
        if (parentWindowPeer == null || messageBoxTitle == null || message == null) {
            return 0;
        }

        return showMessageBox(parentWindowPeer, "errorbox", MessageBoxButtons.BUTTONS_OK, messageBoxTitle, message);
    }

    public static short showYesNoWarningMessageBox(XWindowPeer parentWindowPeer, String messageBoxTitle, String message) {
        if (parentWindowPeer == null || messageBoxTitle == null || message == null) {
            return 0;
        }

        return showMessageBox(parentWindowPeer, "warningbox", MessageBoxButtons.BUTTONS_YES_NO + MessageBoxButtons.DEFAULT_BUTTON_NO, messageBoxTitle, message);
    }

    public static short showOkCancelWarningMessageBox(XWindowPeer parentWindowPeer, String messageBoxTitle, String message) {
        if (parentWindowPeer == null || messageBoxTitle == null || message == null) {
            return 0;
        }

        return showMessageBox(parentWindowPeer, "warningbox", MessageBoxButtons.BUTTONS_OK_CANCEL + MessageBoxButtons.DEFAULT_BUTTON_OK, messageBoxTitle, message);
    }

    public static short showQuestionMessageBox(XWindowPeer parentWindowPeer, String messageBoxTitle, String message) {
        if (parentWindowPeer == null || messageBoxTitle == null || message == null) {
            return 0;
        }

        return showMessageBox(parentWindowPeer, "querybox", MessageBoxButtons.BUTTONS_YES_NO_CANCEL + MessageBoxButtons.DEFAULT_BUTTON_YES, messageBoxTitle, message);
    }

    public static short showAbortRetryIgnoreErrorMessageBox(XWindowPeer parentWindowPeer, String messageBoxTitle, String message) {
        if (parentWindowPeer == null || messageBoxTitle == null || message == null) {
            return 0;
        }

        return showMessageBox(parentWindowPeer, "errorbox", MessageBoxButtons.BUTTONS_ABORT_IGNORE_RETRY + MessageBoxButtons.DEFAULT_BUTTON_RETRY, messageBoxTitle, message);
    }

    public static short showRetryCancelErrorMessageBox(XWindowPeer parentWindowPeer, String messageBoxTitle, String message) {
        if (parentWindowPeer == null || messageBoxTitle == null || message == null) {
            return 0;
        }

        return showMessageBox(parentWindowPeer, "errorbox", MessageBoxButtons.BUTTONS_RETRY_CANCEL + MessageBoxButtons.DEFAULT_BUTTON_CANCEL, messageBoxTitle, message);
    }
}
