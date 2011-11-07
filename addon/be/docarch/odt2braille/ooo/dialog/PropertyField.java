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

import java.util.Collection;
import java.util.HashSet;
import java.util.EventObject;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.beans.XPropertySet;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.odt2braille.setup.Property;
import be.docarch.odt2braille.setup.PropertyEvent;
import be.docarch.odt2braille.setup.PropertyListener;

public abstract class PropertyField<P extends Property> implements Field,
                                                                   PropertyListener,
                                                                   DialogElementListener {
    protected P property;
    protected final XPropertySet propertySet;
    private final Collection<Integer> hashes = new HashSet<Integer>();

    public PropertyField(XControl control) {

        propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());
        
        try {
            propertySet.setPropertyValue("Enabled", false);
        } catch (UnknownPropertyException e) {
        } catch (PropertyVetoException e) {
        } catch (IllegalArgumentException e) {
        } catch (WrappedTargetException e) {
        }
    }

    public void link(P property) {
        this.property = property;
        listenProperty(property);
        update();
    }

    public void listenProperty(P property) {
        if (property != null) {
            int hash = System.identityHashCode(property);
            if (!hashes.contains(hash)) {
                property.addListener(this);
                hashes.add(hash);
            }
        }
    }
    
    public void propertyUpdated(PropertyEvent event) {
        if (event.getSource() == property) {
            update();
        }
    }

    public void dialogElementUpdated(EventObject event) {}

}
