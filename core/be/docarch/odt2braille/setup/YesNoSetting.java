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

public class YesNoSetting extends Setting<Boolean> {

    protected Boolean yesNo = false;

    public boolean accept(Boolean value) {
        return true;
    }

    public Boolean get() {
        return yesNo;
    }

    protected boolean update(Boolean value) {
        if (yesNo==value) { return false; }
        yesNo = value;
        return true;
    }

    @Override
    public boolean enabled() {
        if (!accept(false)) { return false; }
        if (!accept(true)) { return false; }
        return super.enabled();
    }
}