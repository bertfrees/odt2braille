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
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XControl;

import be.docarch.odt2braille.setup.Setting;

public class CheckBox extends SettingControl<Setting<Boolean>>
                   implements XItemListener {
    
    private final XCheckBox checkbox;
    
    public CheckBox(XControl control) {
        super(control);    
        checkbox = (XCheckBox)UnoRuntime.queryInterface(XCheckBox.class, control);
    }

    public void save() {
        if (property == null) { return; }
        property.set(checkbox.getState()==(short)1);
    }

    public void update() {
        checkbox.setState((short)((property == null) ? 0 : property.get()?1:0));
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.Source.equals(checkbox)) {
            save();
        }
    }
    
    public void listenControl(boolean onOff) {
        if (onOff) {
            checkbox.addItemListener(this);
        } else {
            checkbox.removeItemListener(this);
        }
    }
}
