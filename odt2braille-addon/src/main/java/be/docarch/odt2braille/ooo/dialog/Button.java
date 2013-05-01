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
import com.sun.star.lang.EventObject;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.beans.XPropertySet;

public abstract class Button implements DialogElement,
                                        XActionListener {
    
    protected final XPropertySet propertySet;
    protected final XButton button;
    
    public Button(XControl control,
                  String label) {
    
        button = (XButton)UnoRuntime.queryInterface(XButton.class, control);
        propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());
        button.setLabel(label);
        button.addActionListener(this);
    }

    public void updateProperties() {}
    
    public void disposing(EventObject e) {}
}
