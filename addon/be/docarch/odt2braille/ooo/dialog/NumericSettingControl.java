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
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XTextListener;

import be.docarch.odt2braille.setup.Setting;

public class NumericSettingControl extends SettingControl<Setting<Integer>>
                                implements XTextListener {

    protected final XNumericField numericField;
    private final XTextComponent textComponent;
    
    public NumericSettingControl(XControl control) {
        super(control);
        numericField = (XNumericField)UnoRuntime.queryInterface(XNumericField.class, control);
        textComponent = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, control);
        numericField.setDecimalDigits((short)0);
        numericField.setMin((double)0);
        numericField.setMax((double)Integer.MAX_VALUE);
    }
    
    public void save() {
        if (property == null) { return; }
        property.set((int)numericField.getValue());
    }

    public void update() {
        numericField.setValue((double)((property == null) ? 0 : property.get()));
    }

    public void textChanged(TextEvent event) {
        if (event.Source.equals(numericField)) {
            save();
            update();
        }
    }

    public void listenControl(boolean onOff) {
        if (onOff) {
            textComponent.addTextListener(this);
        } else {
            textComponent.removeTextListener(this);
        }
    }
}
