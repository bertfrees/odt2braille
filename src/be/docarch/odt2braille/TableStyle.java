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

    protected boolean stairstep;
    protected boolean dontSplitRows;
    protected Style[] columns;
    protected String columnDelimiter;
    protected int paddingAbove;
    protected int paddingBelow;
    protected boolean upperBorder;
    protected boolean lowerBorder;
    protected char upperBorderStyle;
    protected char lowerBorderStyle;
    protected int linesBetween;

    public TableStyle(TableStyle copyStyle) {
    
        super(copyStyle);
        this.stairstep = copyStyle.stairstep;
        this.columnDelimiter = copyStyle.columnDelimiter;
        this.dontSplitRows = copyStyle.dontSplitRows;
        this.columns = new Style[copyStyle.columns.length];
        for (int i=0; i<columns.length; i++) {
            columns[i] = new Style(copyStyle.columns[i]);
        }
        this.paddingAbove = copyStyle.paddingAbove;
        this.paddingBelow = copyStyle.paddingBelow;
        this.upperBorder = copyStyle.upperBorder;
        this.lowerBorder = copyStyle.lowerBorder;
        this.upperBorderStyle = copyStyle.upperBorderStyle;
        this.lowerBorderStyle = copyStyle.lowerBorderStyle;
        this.linesBetween = copyStyle.linesBetween;
        
    }

    public TableStyle() {

        super("table_top");
        this.columnDelimiter = "\u2830";
        this.dontSplitRows = false;
        this.columns = new Style[10];
        for (int i=0; i<columns.length; i++) {
            columns[i] = new Style("table_" + (i+1));
        }
        this.paddingAbove = 0;
        this.paddingBelow = 0;
        this.upperBorder = false;
        this.lowerBorder = false;
        this.upperBorderStyle = '\u2812';
        this.lowerBorderStyle = '\u2812';
        this.linesBetween = 0;
    }

    public void     setDontSplitRows    (boolean dontSplitRows)  { this.dontSplitRows = dontSplitRows; }
    public void     setStairstepTable   (boolean stairstep)      { this.stairstep = stairstep; }
    public void     setColumnDelimiter  (String delimiter)       { this.columnDelimiter = delimiter; }
    public void     setPaddingAbove     (int paddingAbove)       { if (paddingAbove >= 0) { this.paddingAbove = paddingAbove; }}
    public void     setPaddingBelow     (int paddingBelow)       { if (paddingBelow >= 0) { this.paddingBelow = paddingBelow; }}
    public void     setUpperBorder      (boolean border)         { this.upperBorder = border; }
    public void     setLowerBorder      (boolean border)         { this.lowerBorder = border; }
    public void     setUpperBorderStyle (char border)            { if (border > 0x2800 && border < 0x2840) { this.upperBorderStyle = border; }}
    public void     setLowerBorderStyle (char border)            { if (border > 0x2800 && border < 0x2840) { this.lowerBorderStyle = border; }}
    public void     setLinesBetween     (int linesBetween)       { if (linesBetween >= 0) { this.linesBetween = linesBetween; }}

    public boolean  getDontSplitRows   ()          { return getDontSplit() || dontSplitRows; }
    public boolean  getStairstepTable  ()          { return stairstep; }
    public String   getColumnDelimiter ()          { return columnDelimiter; }
    public Style    getColumn          (int index) { return (index <= columns.length && index > 0)?columns[index-1]:null; }
    public int      getPaddingAbove    ()          { return paddingAbove; }
    public int      getPaddingBelow    ()          { return paddingBelow; }
    public boolean  getUpperBorder     ()          { return upperBorder; }
    public boolean  getLowerBorder     ()          { return lowerBorder; }
    public char     getUpperBorderStyle()          { return upperBorderStyle; }
    public char     getLowerBorderStyle()          { return lowerBorderStyle; }
    public int      getLinesBetween    ()          { return linesBetween; }
    
}
