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

import be.docarch.odt2braille.setup.PropertyEvent;
import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.Dependent;
import be.docarch.odt2braille.setup.DependentNumberSetting;
import be.docarch.odt2braille.setup.NumberSetting;
import be.docarch.odt2braille.setup.YesNoSetting;

/**
 *
 * @author Bert Frees
 */
public class FrameStyle extends Style {

    /************/
    /* SETTINGS */
    /************/

    public final Setting<Integer> linesAbove;
    public final Setting<Integer> linesBelow;
    public final Setting<Boolean> upperBorderEnabled;
    public final Setting<Boolean> lowerBorderEnabled;
    public final DependentNumberSetting paddingAbove;
    public final DependentNumberSetting paddingBelow;
    public final BorderStyleSetting upperBorderStyle;
    public final BorderStyleSetting lowerBorderStyle;

    /* GETTERS */

    public int     getLinesAbove()         { return linesAbove.get(); }
    public int     getLinesBelow()         { return linesBelow.get(); }
    public boolean getUpperBorderEnabled() { return upperBorderEnabled.get(); }
    public boolean getLowerBorderEnabled() { return lowerBorderEnabled.get(); }
    public int     getPaddingAbove()       { return paddingAbove.get(); }
    public int     getPaddingBelow()       { return paddingBelow.get(); }
    public char    getUpperBorderStyle()   { return upperBorderStyle.get(); }
    public char    getLowerBorderStyle()   { return lowerBorderStyle.get(); }

    /* SETTERS */

    public void setLinesAbove         (int value)     { linesAbove.set(value); }
    public void setLinesBelow         (int value)     { linesBelow.set(value); }
    public void setUpperBorderEnabled (boolean value) { upperBorderEnabled.set(value); }
    public void setLowerBorderEnabled (boolean value) { lowerBorderEnabled.set(value); }
    public void setPaddingAbove       (int value)     { paddingAbove.set(value); }
    public void setPaddingBelow       (int value)     { paddingBelow.set(value); }
    public void setUpperBorderStyle   (char value)    { upperBorderStyle.set(value); }
    public void setLowerBorderStyle   (char value)    { lowerBorderStyle.set(value); }


    public FrameStyle() {

        /* DECLARATION */

        linesAbove = new NumberSetting();
        linesBelow = new NumberSetting();
        upperBorderEnabled = new YesNoSetting();
        lowerBorderEnabled = new YesNoSetting();

        paddingAbove = new DependentNumberSetting() {
            public boolean accept(Integer value) {return value >= 0; }
            @Override
            public boolean enabled() {
                if (!getUpperBorderEnabled()) { return false; }
                return super.enabled();
            }
        };

        paddingBelow = new DependentNumberSetting() {
            public boolean accept(Integer value) {return value >= 0; }
            @Override
            public boolean enabled() {
                if (!getUpperBorderEnabled()) { return false; }
                return super.enabled();
            }
        };

        upperBorderStyle = new BorderStyleSetting() {
            @Override
            public boolean enabled() {
                if (!getUpperBorderEnabled()) { return false; }
                return super.enabled();
            }
        };

        lowerBorderStyle = new BorderStyleSetting() {
            @Override
            public boolean enabled() {
                if (!getUpperBorderEnabled()) { return false; }
                return super.enabled();
            }
        };

        /* INITIALIZATION */

        upperBorderEnabled.set(true);
        lowerBorderEnabled.set(true);
        upperBorderStyle.set('\u2812');
        lowerBorderStyle.set('\u2812');

        /* LINKING */

        upperBorderEnabled.addListener(paddingAbove);
        upperBorderEnabled.addListener(upperBorderStyle);
        lowerBorderEnabled.addListener(paddingBelow);
        lowerBorderEnabled.addListener(lowerBorderStyle);

    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    public class BorderStyleSetting extends Setting<Character>
                                 implements Dependent {

        private Character style = '\u2812';

        public boolean accept(Character value) {
            return value > 0x2800 && value < 0x2840;
        }

        protected boolean update(Character value) {
            if (style == value) { return false; }
            style = value;
            return true;
        }

        public Character get() { return style; }
        public boolean refresh() { return false; }
        public void propertyUpdated(PropertyEvent event) {
            if (event.ValueChanged) {
                fireEvent(refresh(), true);
            }
        }
    }
}
