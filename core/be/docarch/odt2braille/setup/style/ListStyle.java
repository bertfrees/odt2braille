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

import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.TextSetting;
import be.docarch.odt2braille.setup.NumberSetting;
import be.docarch.odt2braille.setup.DependentYesNoSetting;

/**
 *
 * @author Bert Frees
 */
public class ListStyle extends Style {

    /************************/
    /* CONSTANTS & SETTINGS */
    /************************/

    public final int level;
    public final ListStyle parentLevel;

    public final Setting<Integer> firstLine;
    public final Setting<Integer> runovers;
    public final Setting<Integer> linesAbove;
    public final Setting<Integer> linesBelow;
    public final Setting<Integer> linesBetween;
    public final DependentYesNoSetting dontSplit;
    public final DependentYesNoSetting dontSplitItems;
    public final Setting<String> prefix;

    /* GETTERS */

    public int       getFirstLine()      { return firstLine.get(); }
    public int       getRunovers()       { return runovers.get(); }
    public int       getLinesAbove()     { return linesAbove.get(); }
    public int       getLinesBelow()     { return linesBelow.get(); }
    public int       getLinesBetween()   { return linesBetween.get(); }
    public boolean   getDontSplit()      { return dontSplit.get(); }
    public boolean   getDontSplitItems() { return dontSplitItems.get(); }
    public String    getPrefix()         { return prefix.get(); }

    /* SETTERS */

    public void setFirstLine      (int value)       { firstLine.set(value); }
    public void setRunovers       (int value)       { runovers.set(value); }
    public void setLinesAbove     (int value)       { linesAbove.set(value); }
    public void setLinesBelow     (int value)       { linesBelow.set(value); }
    public void setLinesBetween   (int value)       { linesBetween.set(value); }
    public void setDontSplit      (boolean value)   { dontSplit.set(value); }
    public void setDontSplitItems (boolean value)   { dontSplitItems.set(value); }
    public void setPrefix         (String value)    { prefix.set(value); }

    public ListStyle(int level,
                     ListStyle parent) {

        this.level = level;
        this.parentLevel = parent;

        /* DECLARATION */

        firstLine = new NumberSetting();
        runovers = new NumberSetting();
        linesAbove = new NumberSetting();
        linesBelow = new NumberSetting();
        linesBetween = new NumberSetting();

        dontSplit = new DependentYesNoSetting() {
            public boolean accept(Boolean value) {
                if (parentLevel != null) {
                    if (parentLevel.getDontSplitItems()) { return value; }
                }
                return true;
            }
        };

        dontSplitItems = new DependentYesNoSetting() {
            public boolean accept(Boolean value) {
                return getDontSplit() ? value : true;
            }
        };

        prefix = new TextSetting();

        /* LINKING */

        dontSplit.addListener(dontSplitItems);
        if (parent != null) {
            parent.dontSplitItems.addListener(dontSplit);
        }
    }
}
