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
 
 package be.docarch.odt2braille;

/**
 * The numbering properties of either an OpenOffice.org list or the sequence of OpenOffice.org headings.
 * These properties are
 * <ul>
 * <li>{@link #styleName}: the name of the numbering style.</li>
 * <li>{@link #numFormat}: the format of the number for various levels (level 1 - level 10).
 *     Possible values are: "<i>bullet</i>", "" (empty string), "<i>i</i>", "<i>I</i>", "<i>a</i>", "<i>A</i>", etc.
 * <li>{@link #prefix}: the prefix of the number for various levels.</li>
 * <li>{@link #suffix}: the suffix of the number for various levels.</li>
 * <li>{@link #displayLevels}: the number of sublevels shown for various levels.</li>
 * <li>{@link #startValue}: the start value if the number for various levels.</li>
 * </ul>
 *
 * @author freesb
 */

public class ListStyleProperties {

    public String styleName = null;

    private String[] numFormat = null;
    private String[] prefix = null;
    private String[] suffix = null;
    private int[] displayLevels = null;
    private int[] startValue = null;

    public ListStyleProperties(String styleName,
                               String[] numFormat,
                               String[] prefix,
                               String[] suffix,
                               int[] displayLevels,
                               int[] startValue) {

        this.styleName = styleName;

        this.numFormat = numFormat.clone();
        this.prefix = prefix.clone();
        this.suffix = suffix.clone();
        this.displayLevels = displayLevels.clone();
        this.startValue = startValue.clone();

    }
    
    public String getStyleName() {
        return styleName;
    }

    public String[] getNumFormat() {
        return numFormat;
    }

    public String[] getPrefix() {
        return prefix;
    }

    public String[] getSuffix() {
        return suffix;
    }

    public int[] getDisplayLevels() {
        return displayLevels;
    }

    public int[] getStartValue() {
        return startValue;
    }
}
