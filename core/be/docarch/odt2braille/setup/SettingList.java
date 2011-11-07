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

public abstract class SettingList<V> {

    public abstract V get(int index);

    public abstract List<V> values();

    public abstract V add();

    public abstract V remove(int index);

    public abstract void clear();

    public abstract boolean canAdd();

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof SettingList)) { return false; }
        try {
            SettingList<V> that = (SettingList<V>)object;
            return this.values().equals(that.values());
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() { return values().hashCode(); }

}
