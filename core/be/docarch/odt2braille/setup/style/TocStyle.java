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

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.TextSetting;
import be.docarch.odt2braille.setup.NumberSetting;
import be.docarch.odt2braille.setup.DependentYesNoSetting;
import be.docarch.odt2braille.setup.SettingMap;
import java.util.Collection;

/**
 *
 * @author Bert Frees
 */
public class TocStyle extends Style
                      implements Serializable {

    private static final int LEVELS = 10;
    private final Configuration configuration;

    /************/
    /* SETTINGS */
    /************/

    private final SettingMap<Integer,TocLevelStyle> levels;

    public final Setting<String> title;
    public final DependentYesNoSetting printPageNumbers;
    public final DependentYesNoSetting braillePageNumbers;
    public final Setting<Integer> linesBetween;
    public final Setting<Character> lineFillSymbol;
    public final Setting<Integer> evaluateUptoLevel;

    /* GETTTERS */

    public SettingMap<Integer,TocLevelStyle> getLevels() { return levels; }

    public String  getTitle()              { return title.get(); }
    public boolean getPrintPageNumbers()   { return printPageNumbers.get(); }
    public boolean getBraillePageNumbers() { return braillePageNumbers.get(); }
    public int     getLinesBetween()       { return linesBetween.get(); }
    public char    getLineFillSymbol()     { return lineFillSymbol.get(); }
    public int     getEvaluateUptoLevel()  { return evaluateUptoLevel.get(); }

    /* SETTERS */

    public void setTitle              (String value)  { title.set(value); }
    public void setPrintPageNumbers   (boolean value) { printPageNumbers.set(value); }
    public void setBraillePageNumbers (boolean value) { braillePageNumbers.set(value); }
    public void setLinesBetween       (int value)     { linesBetween.set(value); }
    public void setLineFillSymbol     (char value)    { lineFillSymbol.set(value); }
    public void setEvaluateUptoLevel  (int value)     { evaluateUptoLevel.set(value); }
    

    public TocStyle(Configuration owner) {

        configuration = owner;

        /* DECLARATION */

        levels = new TocLevelStyleMap();

        title = new TextSetting();
        printPageNumbers = new PrintPageNumbersSetting();
        braillePageNumbers = new BraillePageNumbersSetting();
        linesBetween = new NumberSetting();
        lineFillSymbol = new LineFillSymbolSetting();

        evaluateUptoLevel = new NumberSetting() {
            @Override
            public boolean accept(Integer value) { return value > 0 && value <= LEVELS; }
        };
        
        /* INITIALIZATION */

        printPageNumbers.set(true);
        braillePageNumbers.set(true);
        evaluateUptoLevel.set(2);
        lineFillSymbol.set('\u2804');

        /* LINKING */

        configuration.printPageNumbers.addListener(printPageNumbers);
        configuration.braillePageNumbers.addListener(braillePageNumbers);

    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    private class PrintPageNumbersSetting extends DependentYesNoSetting {
        public boolean accept(Boolean value) {
            return configuration.printPageNumbers.get() ? true : !value;
        }
    }

    private class BraillePageNumbersSetting extends DependentYesNoSetting {
        public boolean accept(Boolean value) {
            return configuration.braillePageNumbers.get() ? true : !value;
        }
    }

    private class LineFillSymbolSetting extends Setting<Character> {

        private Character symbol = '\u2804';

        public boolean accept(Character value) {
            return value > 0x2800 && value < 0x2840;
        }

        protected boolean update(Character value) {
            if (symbol == value) { return false; }
            symbol = value;
            return true;
        }

        public Character get() { return symbol; }
    }

    public class TocLevelStyle {

        public Setting<Integer> firstLine = new NumberSetting();
        public Setting<Integer> runovers = new NumberSetting();

        public int getFirstLine()  { return firstLine.get(); }
        public int getRunovers()   { return runovers.get(); }

        public void setFirstLine(int value)  { firstLine.set(value); }
        public void setRunovers(int value)   { runovers.set(value); }
    }

    public class TocLevelStyleMap extends SettingMap<Integer,TocLevelStyle> {

        private final Map<Integer,TocLevelStyle> map = new HashMap<Integer,TocLevelStyle>();

        public TocLevelStyleMap() {
            for (int i=1; i<=10; i++) {
                map.put(i, new TocLevelStyle());
            }
        }

        public TocLevelStyle get(Integer key) { return map.get(key); }
        public Collection<TocLevelStyle> values() { return map.values(); }
        public Collection<Integer> keys() { return map.keySet(); }
        protected void add(Integer key) {}
    }
}
