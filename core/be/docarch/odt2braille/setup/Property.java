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

package be.docarch.odt2braille.setup;

import java.util.List;
import java.util.ArrayList;

public abstract class Property<T> {

    private List<PropertyListener> listeners;
    
    public abstract T get();
    
    public void addListener(PropertyListener listener) {
        
        if (listeners == null) { listeners = new ArrayList<PropertyListener>(); }
        listeners.add(listener);
    }
    
    protected void fireEvent(boolean valueChanged, boolean contextChanged) {
        if (listeners == null) { return; }
        if (valueChanged || contextChanged) {
            PropertyEvent event = new PropertyEvent(this, valueChanged, contextChanged);
            for (PropertyListener listener : listeners) {
                listener.propertyUpdated(event);
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof Property)) { return false; }
        try {
            Property<T> that = (Property<T>)object;
            return this.get() == null ? that.get() == null : this.get().equals(that.get());
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }
}
