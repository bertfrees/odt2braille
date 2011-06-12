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
public class TocStyle extends Style {

    private static final int LEVELS = 10;

    protected boolean printPageNumbers;
    protected boolean braillePageNumbers;
    protected char lineFillSymbol;
    protected Style[] levels;
    protected int uptoLevel;
    protected int linesBetween;

    public TocStyle(TocStyle copyStyle) {
    
        super(copyStyle);
        printPageNumbers = copyStyle.printPageNumbers;
        braillePageNumbers = copyStyle.braillePageNumbers;
        lineFillSymbol = copyStyle.lineFillSymbol;
        levels = new Style[copyStyle.levels.length];
        for (int i=0; i<levels.length; i++) {
            levels[i] = new Style(copyStyle.levels[i]);
        }
        uptoLevel = copyStyle.uptoLevel;
        linesBetween = copyStyle.linesBetween;
    }

    public TocStyle() {

        super("toc_top");
        printPageNumbers = false;
        braillePageNumbers = false;
        lineFillSymbol = '\u2804';
        levels = new Style[LEVELS];
        for (int i=0; i<levels.length; i++) {
            levels[i] = new Style("toc_" + (i+1));
        }
        uptoLevel = 2;
        linesBetween = 0;
    }

    public char    getLineFillSymbol     ()          { return lineFillSymbol; }
    public boolean getBraillePageNumbers ()          { return braillePageNumbers; }
    public boolean getPrintPageNumbers   ()          { return printPageNumbers; }
    public Style   getLevel              (int index) { return (index <= levels.length && index > 0)?levels[index-1]:null; }
    public int     getUptoLevel          ()          { return uptoLevel; }
    public int     getLinesBetween       ()          { return linesBetween; }

    public void    setBraillePageNumbers (boolean numbers)     { this.braillePageNumbers = numbers; }
    public void    setPrintPageNumbers   (boolean numbers)     { this.printPageNumbers = numbers; }
    public void    setUptoLevel          (int level)           { if (level >= 1 && level <=LEVELS) { uptoLevel = level; }}
    public void    setLinesBetween       (int linesBetween)    { if (linesBetween >= 0) { this.linesBetween = linesBetween; }}
    public boolean setLineFillSymbol     (char lineFillSymbol) {
        if (lineFillSymbol > 0x2800 &&
            lineFillSymbol < 0x2840) {
            this.lineFillSymbol = lineFillSymbol;
            return true;
        } else {
            return false;
        }
    }    
}
