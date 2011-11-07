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

/**
 *
 * @author Bert Frees
 */
public class PictureStyle extends Style {

    /************/
    /* SETTINGS */
    /************/

    public final Setting<Integer> firstLine;
    public final Setting<Integer> runovers;
    public final Setting<Integer> linesAbove;
    public final Setting<Integer> linesBelow;
    public final Setting<String> openingMark;
    public final Setting<String> closingMark;
    public final Setting<String> descriptionPrefix;

    /* GETTERS */

    public int    getFirstLine()         { return firstLine.get(); }
    public int    getRunovers()          { return runovers.get(); }
    public int    getLinesAbove()        { return linesAbove.get(); }
    public int    getLinesBelow()        { return linesBelow.get(); }
    public String getOpeningMark()       { return openingMark.get(); }
    public String getClosingMark()       { return closingMark.get(); }
    public String getDescriptionPrefix() { return descriptionPrefix.get(); }

    /* SETTERS */

    public void setLinesAbove        (int value)    { linesAbove.set(value); }
    public void setLinesBelow        (int value)    { linesBelow.set(value); }
    public void setFirstLine         (int value)    { firstLine.set(value); }
    public void setRunovers          (int value)    { runovers.set(value); }
    public void setOpeningMark       (String value) { openingMark.set(value); }
    public void setClosingMark       (String value) { closingMark.set(value); }
    public void setDescriptionPrefix (String value) { descriptionPrefix.set(value); }


    public PictureStyle() {

        /* DECLARATION */

        firstLine = new NumberSetting();
        runovers = new NumberSetting();
        linesAbove = new NumberSetting();
        linesBelow = new NumberSetting();
        openingMark = new TextSetting();
        closingMark = new TextSetting();
        descriptionPrefix = new TextSetting();

        /* INITIALIZATION */

        firstLine.set(6);
        runovers.set(4);
        linesAbove.set(1);
        linesBelow.set(1);
        openingMark.set("\u2820\u2804");
        closingMark.set("\u2820\u2804");
        descriptionPrefix.set("Picture description:");
    }
}
