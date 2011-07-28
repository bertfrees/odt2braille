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

package be.docarch.odt2braille.setup.style;

import be.docarch.odt2braille.setup.PropertyEvent;
import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.Dependent;
import be.docarch.odt2braille.setup.DependentNumberSetting;
import be.docarch.odt2braille.setup.DependentTextSetting;
import be.docarch.odt2braille.setup.DependentYesNoSetting;
import be.docarch.odt2braille.setup.NumberSetting;
import be.docarch.odt2braille.setup.TextSetting;
import be.docarch.odt2braille.setup.YesNoSetting;

/**
 *
 * @author Bert Frees
 */
public class TableStyle extends Style {

    /************/
    /* SETTINGS */
    /************/
        
    private final Setting<String> displayName;
    
    public final Setting<Integer> firstLine;
    public final Setting<Integer> runovers;
    public final Setting<Integer> linesAbove;
    public final Setting<Integer> linesBelow;
    public final Setting<Integer> linesBetween;

    public final Setting<Boolean> stairstepEnabled;
    public final DependentTextSetting columnDelimiter;
    public final DependentNumberSetting indentPerColumn;

    public final Setting<Boolean> dontSplit;
    public final DependentYesNoSetting dontSplitRows;
    public final Setting<Boolean> mirrorTable;
    public final Setting<Boolean> columnHeadings;
    public final Setting<Boolean> rowHeadings;
    public final DependentYesNoSetting repeatHeading;
    public final DependentTextSetting headingSuffix;

    public final Setting<Boolean> upperBorderEnabled;
    public final Setting<Boolean> lowerBorderEnabled;
    public final DependentNumberSetting paddingAbove;
    public final DependentNumberSetting paddingBelow;
    public final BorderStyleSetting upperBorderStyle;
    public final BorderStyleSetting lowerBorderStyle;
    public final BorderStyleSetting headingBorderStyle;

    /* GETTERS */

    public String  getDisplayName()        { return displayName.get(); }
    public int     getFirstLine()          { return firstLine.get(); }
    public int     getRunovers()           { return runovers.get(); }
    public int     getLinesAbove()         { return linesAbove.get(); }
    public int     getLinesBelow()         { return linesBelow.get(); }
    public int     getLinesBetween()       { return linesBetween.get(); }
    public boolean getStairstepEnabled()   { return stairstepEnabled.get(); }
    public String  getColumnDelimiter()    { return columnDelimiter.get(); }
    public int     getIndentPerColumn()    { return indentPerColumn.get(); }
    public boolean getDontSplit()          { return dontSplit.get(); }
    public boolean getDontSplitRows()      { return dontSplitRows.get(); }
    public boolean getMirrorTable()        { return mirrorTable.get(); }
    public boolean getColumnHeadings()     { return columnHeadings.get(); }
    public boolean getRowHeadings()        { return rowHeadings.get(); }
    public boolean getRepeatHeading()      { return repeatHeading.get(); }
    public String  getHeadingSuffix()      { return headingSuffix.get(); }
    public boolean getUpperBorderEnabled() { return upperBorderEnabled.get(); }
    public boolean getLowerBorderEnabled() { return lowerBorderEnabled.get(); }
    public int     getPaddingAbove()       { return paddingAbove.get(); }
    public int     getPaddingBelow()       { return paddingBelow.get(); }
    public char    getUpperBorderStyle()   { return upperBorderStyle.get(); }
    public char    getLowerBorderStyle()   { return lowerBorderStyle.get(); }
    public char    getHeadingBorderStyle() { return headingBorderStyle.get(); }

    /* SETTERS */

    public void setDisplayName        (String value)  { displayName.set(value); }
    public void setFirstLine          (int value)     { firstLine.set(value); }
    public void setRunovers           (int value)     { runovers.set(value); }
    public void setLinesAbove         (int value)     { linesAbove.set(value); }
    public void setLinesBelow         (int value)     { linesBelow.set(value); }
    public void setLinesBetween       (int value)     { linesBetween.set(value); }
    public void setStairstepEnabled   (boolean value) { stairstepEnabled.set(value); }
    public void setColumnDelimiter    (String value)  { columnDelimiter.set(value); }
    public void setIndentPerColumn    (int value)     { indentPerColumn.set(value); }
    public void setDontSplit          (boolean value) { dontSplit.set(value); }
    public void setDontSplitRows      (boolean value) { dontSplitRows.set(value); }
    public void setMirrorTable        (boolean value) { mirrorTable.set(value); }
    public void setColumnHeadings     (boolean value) { columnHeadings.set(value); }
    public void setRowHeadings        (boolean value) { rowHeadings.set(value); }
    public void setRepeatHeading      (boolean value) { repeatHeading.set(value); }
    public void setHeadingSuffix      (String value)  { headingSuffix.set(value); }
    public void setUpperBorderEnabled (boolean value) { upperBorderEnabled.set(value); }
    public void setLowerBorderEnabled (boolean value) { lowerBorderEnabled.set(value); }
    public void setPaddingAbove       (int value)     { paddingAbove.set(value); }
    public void setPaddingBelow       (int value)     { paddingBelow.set(value); }
    public void setUpperBorderStyle   (char value)    { upperBorderStyle.set(value); }
    public void setLowerBorderStyle   (char value)    { lowerBorderStyle.set(value); }
    public void setHeadingBorderStyle (char value)    { headingBorderStyle.set(value); }

    public TableStyle(String name) {

        /* DECLARATION */

        displayName = new TextSetting();
        firstLine = new NumberSetting();
        runovers = new NumberSetting();
        linesAbove = new NumberSetting();
        linesBelow = new NumberSetting();
        linesBetween = new NumberSetting();
        stairstepEnabled = new YesNoSetting();

        columnDelimiter = new DependentTextSetting() {
            public boolean refresh() { return false; }
            @Override
            public boolean enabled() {
                return !getStairstepEnabled();
            }
        };

        indentPerColumn = new DependentNumberSetting() {
            public boolean accept(Integer value) { return value >= 0; }
            @Override
            public boolean enabled() {
                return getStairstepEnabled();
            }
        };

        dontSplit = new YesNoSetting();

        dontSplitRows = new DependentYesNoSetting() {
            public boolean accept(Boolean value) {
                return getDontSplit() ? value : true;
            }
        };

        mirrorTable = new YesNoSetting();
        columnHeadings = new YesNoSetting();
        rowHeadings = new YesNoSetting();

        repeatHeading = new DependentYesNoSetting() {
            @Override
            public boolean accept(Boolean value) {
                return (getMirrorTable() ? getRowHeadings() : getColumnHeadings()) ? true : !value;
            }
        };

        headingSuffix = new DependentTextSetting() {
            public boolean refresh() { return false; }
            @Override
            public boolean enabled() { return getRepeatHeading(); }
        };

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

        headingBorderStyle = new BorderStyleSetting();

        /* INITIALIZATION */

        displayName.set(name);
        columnDelimiter.set("\u2830");
        indentPerColumn.set(2);
        headingSuffix.set("\u2812");
        upperBorderStyle.set('\u2836');
        lowerBorderStyle.set('\u281b');
        headingBorderStyle.set('\u2812');

        /* LINKING */

        stairstepEnabled.addListener(columnDelimiter);
        stairstepEnabled.addListener(indentPerColumn);
        dontSplit.addListener(dontSplitRows);
        upperBorderEnabled.addListener(paddingAbove);
        upperBorderEnabled.addListener(upperBorderStyle);
        lowerBorderEnabled.addListener(paddingBelow);
        lowerBorderEnabled.addListener(lowerBorderStyle);
        mirrorTable.addListener(repeatHeading);
        columnHeadings.addListener(repeatHeading);
        rowHeadings.addListener(repeatHeading);
        repeatHeading.addListener(headingSuffix);

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
