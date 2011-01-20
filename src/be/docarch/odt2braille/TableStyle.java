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

    public TableStyle(TableStyle copyStyle) {
    
        super(copyStyle);
        this.stairstep = copyStyle.stairstep;
        this.columnDelimiter = copyStyle.columnDelimiter;
        this.dontSplitRows = copyStyle.dontSplitRows;
        this.columns = new Style[copyStyle.columns.length];
        for (int i=0; i<columns.length; i++) {
            columns[i] = new Style(copyStyle.columns[i]);
        }
    }

    public TableStyle() {

        super("table_top");
        this.columnDelimiter = "\u2830";
        this.dontSplitRows = false;
        this.columns = new Style[10];
        for (int i=0; i<columns.length; i++) {
            columns[i] = new Style("table_" + (i+1));
        }
    }

    public void     setDontSplitRows    (boolean dontSplitRows)  { this.dontSplitRows = dontSplitRows; }
    public void     setStairstepTable   (boolean stairstep)      { this.stairstep = stairstep; }
    public void     setColumnDelimiter  (String delimiter)       { this.columnDelimiter = delimiter; }

    public boolean  getDontSplitRows   ()          { return getDontSplit() || dontSplitRows; }
    public boolean  getStairstepTable  ()          { return stairstep; }
    public String   getColumnDelimiter ()          { return columnDelimiter; }
    public Style    getColumn          (int index) { return (index <= columns.length && index > 0)?columns[index-1]:null; }
    
}
