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
 * @author freesb
 */
public class SpecialSymbol {

    public enum Type { LETTER_INDICATOR,
                       NUMBER_INDICATOR,
                       NOTE_REFERENCE_INDICATOR,
                       TRANSCRIBERS_NOTE_INDICATOR,
                       ITALIC_INDICATOR,
                       BOLDFACE_INDICATOR,
                       ELLIPSIS,
                       DOUBLE_DASH,
                       OTHER };

    public enum Mode { NEVER,
                       FIRST_VOLUME,
                       IF_PRESENT_IN_VOLUME,
                       ALWAYS };

    private String symbol;
    private String description;
    private Type type;
    private Mode mode;

    public SpecialSymbol() {
        this("\u2800", "", Type.OTHER, Mode.NEVER);
    }

    public SpecialSymbol(String symbol,
                         String description,
                         Type type,
                         Mode mode) {

        if (!setSymbol(symbol)) { this.symbol = "\u2800"; }
        setDescription(description);
        this.type = type;
        if (!setMode(mode)) { this.mode = Mode.NEVER; }
    }

    public SpecialSymbol(SpecialSymbol copySpecialSymbol) {

        this.symbol = new String(copySpecialSymbol.symbol);
        this.description = new String(copySpecialSymbol.description);
        this.type = copySpecialSymbol.type;
        this.mode = copySpecialSymbol.mode;

    }

    public boolean setSymbol(String symbol) {
        if (symbolValid(symbol) && !symbol.equals("")) {
            this.symbol = symbol;
            return true;
        } else {
            return false;
        }
    }

    public void setDescription(String description)     { this.description = description; }
    public void setType       (Type type) { this.type = type; }

    public boolean setMode(Mode mode) {
        if (this.type != Type.OTHER || mode != Mode.IF_PRESENT_IN_VOLUME) {
            this.mode = mode;
            return true;
        } else {
            return false;
        }
    }

    public String            getSymbol()      { return this.symbol;}
    public String            getDescription() { return this.description;}
    public Type getType()        { return this.type; }
    public Mode getMode()        { return this.mode; }

    public String getDots() {

        StringBuffer symbolBuffer = new StringBuffer(symbol);
        StringBuffer dotsBuffer = new StringBuffer();
        String singleDots = null;
        int singleSymbol;
        boolean first = true;

        if (!symbol.equals("")) {

            dotsBuffer.append('(');

            for (int i=0;i<symbolBuffer.length();i++) {

                singleSymbol = (int)(symbolBuffer.charAt(i)) - 0x2800;
                singleDots = "";

                if (singleSymbol>0) {
                    for (int j=5;j>=0;j--) {
                        if (singleSymbol >= (1<<j)) {
                            singleSymbol -= (1<<j);
                            singleDots = Integer.toString(j+1) + singleDots;
                        }
                    }
                } else {
                    singleDots = "0";
                }

                if (!first) {
                    dotsBuffer.append(',');
                    dotsBuffer.append(' ');
                } else {
                    first = false;
                }

                dotsBuffer.append(singleDots);
            }

            dotsBuffer.append(')');

        }

        return dotsBuffer.toString();

    }

    private boolean symbolValid(String symbol) {
        return symbol.matches("[\\p{InBraille_Patterns}]*");
    }
}
