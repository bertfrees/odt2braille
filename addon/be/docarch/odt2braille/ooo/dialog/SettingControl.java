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

import com.sun.star.awt.XControl;
import com.sun.star.lang.XEventListener;
import com.sun.star.lang.EventObject;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.PropertyEvent;

public abstract class SettingControl<S extends Setting> extends PropertyField<S>
                                                     implements Control,
                                                                XEventListener {

    public SettingControl(XControl control) {
        super(control);
    }
    
    public abstract void listenControl(boolean onOff);

    @Override
    public void link(S setting) {
        property = setting;
        listenProperty(property);
        listenControl(false);
        update();
        updateProperties();
        listenControl(true);
    }

    public void updateProperties() {
        try {
            propertySet.setPropertyValue("Enabled", property!=null ? property.enabled() : false);
        } catch (UnknownPropertyException e) {
        } catch (PropertyVetoException e) {
        } catch (IllegalArgumentException e) {
        } catch (WrappedTargetException e) {
        }
    }

    @Override
    public void propertyUpdated(PropertyEvent event) {
        if (event.getSource() == property) {
            listenControl(false);
            if (event.ValueChanged) { update(); }
            if (event.ContextChanged) { updateProperties(); }
            listenControl(true);
        }
    }

    public void disposing(EventObject object) {}
}
