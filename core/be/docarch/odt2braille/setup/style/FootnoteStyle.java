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

package be.docarch.odt2braille.setup.style;

import be.docarch.odt2braille.setup.EnumSetting;
import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.NumberSetting;
import be.docarch.odt2braille.setup.DependentNumberSetting;

public class FootnoteStyle extends Style {

    /************/
    /* SETTINGS */
    /************/

    public final EnumSetting<Alignment> alignment;
    public final DependentNumberSetting firstLine;
    public final DependentNumberSetting runovers;
    public final DependentNumberSetting marginLeftRight;
    public final Setting<Integer> linesAbove;
    public final Setting<Integer> linesBelow;

    /* GETTERS */

    public Alignment  getAlignment()       { return alignment.get(); }
    public int        getFirstLine()       { return firstLine.get(); }
    public int        getRunovers()        { return runovers.get(); }
    public int        getMarginLeftRight() { return marginLeftRight.get(); }
    public int        getLinesAbove()      { return linesAbove.get(); }
    public int        getLinesBelow()      { return linesBelow.get(); }

    /* SETTERS */

    public void setAlignment        (Alignment value) { alignment.set(value); }
    public void setFirstLine        (int value)       { firstLine.set(value); }
    public void setRunovers         (int value)       { runovers.set(value); }
    public void setMarginLeftRight  (int value)       { marginLeftRight.set(value); }
    public void setLinesAbove       (int value)       { linesAbove.set(value); }
    public void setLinesBelow       (int value)       { linesBelow.set(value); }

    public FootnoteStyle() {

        /* DECLARATION */

        alignment = new EnumSetting<Alignment>(Alignment.class);

        firstLine = new DependentNumberSetting() {
            public boolean accept(Integer value) {return value >= 0; }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.LEFT) { return false; }
                return super.enabled();
            }
        };

        runovers  = new DependentNumberSetting() {
            public boolean accept(Integer value) {return value >= 0; }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.LEFT) { return false; }
                return super.enabled();
            }
        };

        marginLeftRight = new DependentNumberSetting() {
            public boolean accept(Integer value) {return value >= 0; }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.CENTERED) { return false; }
                return super.enabled();
            }
        };

        linesAbove = new NumberSetting();
        linesBelow = new NumberSetting();

        /* LINKING */

        alignment.addListener(firstLine);
        alignment.addListener(runovers);
        alignment.addListener(marginLeftRight);

    }
}
