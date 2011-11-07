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

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XFocusListener;
import com.sun.star.awt.FocusEvent;

import be.docarch.odt2braille.setup.Setting;

public class TextSettingControl extends SettingControl<Setting<String>>
                             implements XFocusListener {

    private final XTextComponent textField;
    protected final XWindow window;
    
    public TextSettingControl(XControl control) {
        super(control);
        textField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, control);
        window = (XWindow)UnoRuntime.queryInterface(XWindow.class, control);
    }

    public void update() {
        textField.setText((property == null) ? "" : property.get());
    }

    public void save() {
        if (property == null) { return; }
        property.set(textField.getText());
    }

    public void listenControl(boolean onOff) {
        if (onOff) {
            window.addFocusListener(this);
        } else {
            window.removeFocusListener(this);
        }
    }

    public void focusGained(FocusEvent event) {}
    public void focusLost(FocusEvent event) {
        if (event.Source.equals(window)) {
            save();
        }
    }
}
