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

import java.util.Collection;
import java.util.Arrays;

public class EnumSetting<T extends Enum<T>> extends OptionSetting<T> {

    private T value;
    private final Collection<T> options;

    public EnumSetting(Class<T> type) {
        options = Arrays.asList(type.getEnumConstants());
        value = options.iterator().next();
    }

    public Collection<T> options() {
        return options;
    }

    protected boolean update(T value) {
        if (this.value == value) { return false;}
        this.value = value;
        return true;
    }

    public T get() { return value; }
}