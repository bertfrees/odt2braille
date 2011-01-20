/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
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

package be.docarch.odt2braille;


/**
 *
 * @author Bert Frees
 */
public class Style implements Comparable {

    public enum Alignment {LEFT, CENTERED, RIGHT};

    protected String name;
    protected boolean dontSplit;
    protected Alignment alignment;
    protected int firstLine;
    protected int runovers;
    protected int marginLeftRight;
    protected int linesAbove;
    protected int linesBelow;
    protected int linesBetween;


    public Style(Style copyStyle) {
    
        this.name = copyStyle.name;
        this.alignment = copyStyle.alignment;
        this.firstLine = copyStyle.firstLine;
        this.linesAbove = copyStyle.linesAbove;
        this.linesBelow = copyStyle.linesBelow;
        this.linesBetween = copyStyle.linesBetween;
        this.runovers = copyStyle.runovers;
        this.marginLeftRight = copyStyle.marginLeftRight;
        this.dontSplit = copyStyle.dontSplit;

    }

    public Style(String name) {

        this.name = name;
        dontSplit = false;
        alignment = Alignment.LEFT;
        firstLine = 0;
        runovers = 0;
        marginLeftRight = 0;
        linesAbove = 0;
        linesBelow = 0;
        linesBetween = 0;

    }
    
    public void setAlignment  (Alignment alignment)  { this.alignment = alignment; }
    public void setDontSplit  (boolean dontSplit)    { this.dontSplit = dontSplit; }

    public void setFirstLine       (int firstLine)    { if (firstLine >= 0)    { this.firstLine = firstLine; }}
    public void setRunovers        (int runovers)     { if (runovers >= 0)     { this.runovers = runovers; }}
    public void setMarginLeftRight (int margin)       { if (margin >= 0)       { this.marginLeftRight = margin; }}
    public void setLinesAbove      (int linesAbove)   { if (linesAbove >= 0)   { this.linesAbove = linesAbove; }}
    public void setLinesBelow      (int linesBelow)   { if (linesBelow >= 0)   { this.linesBelow = linesBelow; }}
    public void setLinesBetween    (int linesBetween) { if (linesBetween >= 0) { this.linesBetween = linesBetween; }}

    public String    getName()            { return name; }
    public Alignment getAlignment()       { return alignment; }
    public int       getFirstLine()       { return firstLine; }
    public int       getRunovers()        { return runovers; }
    public int       getMarginLeftRight() { return marginLeftRight; }
    public int       getLinesAbove()      { return linesAbove; }
    public int       getLinesBelow()      { return linesBelow; }
    public int       getLinesBetween()    { return linesBetween; }
    public boolean   getDontSplit()       { return dontSplit; }

    @Override
    public int compareTo(Object o) {
        return this.name.compareTo(((Style) o).name);
    }
}
