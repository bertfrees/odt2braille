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
import be.docarch.odt2braille.setup.Dependent;
import be.docarch.odt2braille.setup.NumberSetting;
import be.docarch.odt2braille.setup.DependentNumberSetting;
import be.docarch.odt2braille.setup.DependentYesNoSetting;
import be.docarch.odt2braille.setup.YesNoSetting;
import be.docarch.odt2braille.setup.PropertyEvent;

/**
 *
 * @author Bert Frees
 */
public class HeadingStyle extends Style {

    /************************/
    /* CONSTANTS & SETTINGS */
    /************************/

    private final int level;

    public final EnumSetting<Alignment> alignment;
    public final DependentNumberSetting firstLine;
    public final DependentNumberSetting runovers;
    public final DependentNumberSetting marginLeftRight;
    public final Setting<Integer> linesAbove;
    public final Setting<Integer> linesBelow;
    public final Setting<Boolean> upperBorderEnabled;
    public final Setting<Boolean> lowerBorderEnabled;
    public final DependentNumberSetting paddingAbove;
    public final DependentNumberSetting paddingBelow;
    public final BorderStyleSetting upperBorderStyle;
    public final BorderStyleSetting lowerBorderStyle;
    public final Setting<Boolean> newBraillePage;
    public final DependentYesNoSetting keepWithNext;
    public final DependentYesNoSetting dontSplit;

    /* GETTERS */

    public int       getLevel()              { return level; }
    public Alignment getAlignment()          { return alignment.get(); }
    public int       getFirstLine()          { return firstLine.get(); }
    public int       getRunovers()           { return runovers.get(); }
    public int       getMarginLeftRight()    { return marginLeftRight.get(); }
    public int       getLinesAbove()         { return linesAbove.get(); }
    public int       getLinesBelow()         { return linesBelow.get(); }
    public boolean   getUpperBorderEnabled() { return upperBorderEnabled.get(); }
    public boolean   getLowerBorderEnabled() { return lowerBorderEnabled.get(); }
    public int       getPaddingAbove()       { return paddingAbove.get(); }
    public int       getPaddingBelow()       { return paddingBelow.get(); }
    public char      getUpperBorderStyle()   { return upperBorderStyle.get(); }
    public char      getLowerBorderStyle()   { return lowerBorderStyle.get(); }
    public boolean   getNewBraillePage()     { return newBraillePage.get(); }
    public boolean   getKeepWithNext()       { return keepWithNext.get(); }
    public boolean   getDontSplit()          { return dontSplit.get(); }

    /* SETTERS */

    public void setAlignment          (Alignment value) { alignment.set(value); }
    public void setFirstLine          (int value)       { firstLine.set(value); }
    public void setRunovers           (int value)       { runovers.set(value); }
    public void setMarginLeftRight    (int value)       { marginLeftRight.set(value); }
    public void setLinesAbove         (int value)       { linesAbove.set(value); }
    public void setLinesBelow         (int value)       { linesBelow.set(value); }
    public void setUpperBorderEnabled (boolean value)   { upperBorderEnabled.set(value); }
    public void setLowerBorderEnabled (boolean value)   { lowerBorderEnabled.set(value); }
    public void setPaddingAbove       (int value)       { paddingAbove.set(value); }
    public void setPaddingBelow       (int value)       { paddingBelow.set(value); }
    public void setUpperBorderStyle   (char value)      { upperBorderStyle.set(value); }
    public void setLowerBorderStyle   (char value)      { lowerBorderStyle.set(value); }
    public void setNewBraillePage     (boolean value)   { newBraillePage.set(value); }
    public void setKeepWithNext       (boolean value)   { keepWithNext.set(value); }
    public void setDontSplit          (boolean value)   { dontSplit.set(value); }

    public HeadingStyle(int level) {

        this.level = level;

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

        newBraillePage = new YesNoSetting();
        
        keepWithNext = new DependentYesNoSetting() {
            public boolean accept(Boolean value) {
                return getNewBraillePage() ? !value : true;
            }
        };

        dontSplit = new DependentYesNoSetting() {
            public boolean accept(Boolean value) {
                return getNewBraillePage() ? !value : getKeepWithNext() ? value : true;
            }
        };

        /* INITIALIZATION */

        upperBorderStyle.set('\u2812');
        lowerBorderStyle.set('\u2812');

        /* LINKING */

        alignment.addListener(firstLine);
        alignment.addListener(runovers);
        alignment.addListener(marginLeftRight);
        upperBorderEnabled.addListener(paddingAbove);
        upperBorderEnabled.addListener(upperBorderStyle);
        lowerBorderEnabled.addListener(paddingBelow);
        lowerBorderEnabled.addListener(lowerBorderStyle);
        newBraillePage.addListener(keepWithNext);
        newBraillePage.addListener(dontSplit);
        keepWithNext.addListener(dontSplit);

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
