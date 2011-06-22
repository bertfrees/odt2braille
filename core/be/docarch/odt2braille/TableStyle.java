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
public class TableStyle extends Style {

    private String displayName;
    private boolean stairstep;
    private boolean dontSplitRows;
    private String columnDelimiter;
    private String headingSuffix;
    private int paddingAbove;
    private int paddingBelow;
    private boolean upperBorder;
    private boolean lowerBorder;
    private char upperBorderStyle;
    private char lowerBorderStyle;
    private char headingBorderStyle;
    private int linesBetween;
    private int indentPerColumn;
    private boolean mirrorTable;
    private boolean columnHeadings;
    private boolean rowHeadings;
    private boolean repeatHeading;

    public TableStyle(TableStyle copyStyle) {
    
        super(copyStyle);
        this.displayName = copyStyle.displayName;
        this.stairstep = copyStyle.stairstep;
        this.columnDelimiter = copyStyle.columnDelimiter;
        this.headingSuffix = copyStyle.headingSuffix;
        this.dontSplitRows = copyStyle.dontSplitRows;
        this.paddingAbove = copyStyle.paddingAbove;
        this.paddingBelow = copyStyle.paddingBelow;
        this.upperBorder = copyStyle.upperBorder;
        this.lowerBorder = copyStyle.lowerBorder;
        this.upperBorderStyle = copyStyle.upperBorderStyle;
        this.lowerBorderStyle = copyStyle.lowerBorderStyle;
        this.headingBorderStyle = copyStyle.headingBorderStyle;
        this.linesBetween = copyStyle.linesBetween;
        this.indentPerColumn = copyStyle.indentPerColumn;
        this.mirrorTable = copyStyle.mirrorTable;
        this.columnHeadings = copyStyle.columnHeadings;
        this.rowHeadings = copyStyle.rowHeadings;
        this.repeatHeading = copyStyle.repeatHeading;
    }

    public TableStyle(String name) {

        super(name);
        this.displayName = name;
        this.columnDelimiter = "\u2830";
        this.headingSuffix = "\u2812";
        this.dontSplitRows = false;
        this.paddingAbove = 0;
        this.paddingBelow = 0;
        this.upperBorder = false;
        this.lowerBorder = false;
        this.upperBorderStyle = '\u2836';
        this.lowerBorderStyle = '\u281b';
        this.headingBorderStyle = '\u2812';
        this.linesBetween = 0;
        this.indentPerColumn = 2;
        this.mirrorTable = false;
        this.columnHeadings = false;
        this.rowHeadings = false;
        this.repeatHeading = false;
    }

    public void     setDisplayName       (String displayName)     { this.displayName = displayName; }
    public void     setDontSplitRows     (boolean dontSplitRows)  { this.dontSplitRows = dontSplitRows; }
    public void     setStairstepTable    (boolean stairstep)      { this.stairstep = stairstep; }
    public void     setColumnDelimiter   (String delimiter)       { this.columnDelimiter = delimiter; }
    public void     setHeadingSuffix     (String suffix)          { this.headingSuffix = suffix; }
    public void     setPaddingAbove      (int paddingAbove)       { if (paddingAbove >= 0) { this.paddingAbove = paddingAbove; }}
    public void     setPaddingBelow      (int paddingBelow)       { if (paddingBelow >= 0) { this.paddingBelow = paddingBelow; }}
    public void     setUpperBorder       (boolean border)         { this.upperBorder = border; }
    public void     setLowerBorder       (boolean border)         { this.lowerBorder = border; }
    public void     setUpperBorderStyle  (char border)            { if (border > 0x2800 && border < 0x2840) { this.upperBorderStyle = border; }}
    public void     setLowerBorderStyle  (char border)            { if (border > 0x2800 && border < 0x2840) { this.lowerBorderStyle = border; }}
    public void     setHeadingBorderStyle(char border)            { if (border > 0x2800 && border < 0x2840) { this.headingBorderStyle = border; }}
    public void     setLinesBetween      (int linesBetween)       { if (linesBetween >= 0) { this.linesBetween = linesBetween; }}
    public void     setIndentPerColumn   (int indent)             { if (indent >= 0) { this.indentPerColumn = indent; }}
    public void     setMirrorTable       (boolean mirror)         { this.mirrorTable = mirror; }
    public void     setColumnHeadings    (boolean heading)        { this.columnHeadings = heading; }
    public void     setRowHeadings       (boolean heading)        { this.rowHeadings = heading; }
    public void     setRepeatHeading     (boolean repeat)         { this.repeatHeading = repeat; }

    public String   getDisplayName()        { return displayName; }
    public boolean  getDontSplitRows()      { return getDontSplit() || dontSplitRows; }
    public boolean  getStairstepTable()     { return stairstep; }
    public String   getColumnDelimiter()    { return columnDelimiter; }
    public String   getHeadingSuffix()      { return headingSuffix; }
    public int      getPaddingAbove()       { return paddingAbove; }
    public int      getPaddingBelow()       { return paddingBelow; }
    public boolean  getUpperBorder()        { return upperBorder; }
    public boolean  getLowerBorder()        { return lowerBorder; }
    public char     getUpperBorderStyle()   { return upperBorderStyle; }
    public char     getLowerBorderStyle()   { return lowerBorderStyle; }
    public char     getHeadingBorderStyle() { return headingBorderStyle; }
    public int      getLinesBetween()       { return linesBetween; }
    public int      getIndentPerColumn()    { return indentPerColumn; }
    public boolean  getMirrorTable()        { return mirrorTable; }
    public boolean  getColumnHeadings()     { return columnHeadings; }
    public boolean  getRowHeadings()        { return rowHeadings; }
    public boolean  getRepeatHeading()      { return repeatHeading; }
}
