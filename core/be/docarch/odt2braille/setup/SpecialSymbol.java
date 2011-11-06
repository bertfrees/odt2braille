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
 
 package be.docarch.odt2braille.setup;

import java.util.Locale;
import java.util.ResourceBundle;
import java.io.Serializable;
import be.docarch.odt2braille.Constants;

public class SpecialSymbol implements Serializable {

    /************/
    /* SETTINGS */
    /************/

    public final SymbolSetting symbol;
    public final TextSetting description;
    public final OptionSetting<Type> type;
    public final ModeSetting mode;

    /* GETTERS */

    public String getSymbol()      { return symbol.get(); }
    public String getDotPattern()  { return symbol.getDotPattern(); }
    public String getDescription() { return description.get(); }
    public Type   getType()        { return type.get(); }
    public Mode   getMode()        { return mode.get(); }

    /* SETTERS */

    public void setSymbol(String value)      { symbol.set(value); }
    public void setDescription(String value) { description.set(value); }
    public void setType(Type value)          { type.set(value); }
    public void setMode(Mode value)          { mode.set(value); }


    /***************************/
    /* PUBLIC STATIC CONSTANTS */
    /***************************/

    public static enum Type { LETTER_INDICATOR,
                              NUMBER_INDICATOR,
                              NOTE_REFERENCE_INDICATOR,
                              TRANSCRIBERS_NOTE_INDICATOR,
                              ITALIC_INDICATOR,
                              BOLDFACE_INDICATOR,
                              ELLIPSIS,
                              DOUBLE_DASH,
                              OTHER };

    public static enum Mode { NEVER,
                              FIRST_VOLUME,
                              IF_PRESENT_IN_VOLUME,
                              ALWAYS };


    public SpecialSymbol() {

        type = new EnumSetting<Type>(Type.class);
        symbol = new SymbolSetting();
        description = new TextSetting();
        mode = new ModeSetting();

        type.set(Type.OTHER);
        symbol.set("\u2800");
        description.set("Special symbol");
        mode.set(Mode.NEVER);

        type.addListener(mode);
    }

    public SpecialSymbol(Type type,
                         Locale locale,
                         String mainTableLocale) {

        this();
        setType(type);

        ResourceBundle bundle = ResourceBundle.getBundle(Constants.L10N_PATH, locale);

        switch (type) {
            case ELLIPSIS:
                symbol.set("\u2810\u2810\u2810");
                description.set(bundle.getString("specialSymbolEllipsisDescription"));
                mode.set(Mode.IF_PRESENT_IN_VOLUME);
                break;
            case DOUBLE_DASH:
                symbol.set("\u2824\u2824\u2824\u2824");
                description.set(bundle.getString("specialSymbolDoubleDashDescription"));
                mode.set(Mode.IF_PRESENT_IN_VOLUME);
                break;
            case TRANSCRIBERS_NOTE_INDICATOR:
                symbol.set("\u2820\u2804");
                description.set(bundle.getString("specialSymbolTNIndicatorDescription"));
                mode.set(Mode.IF_PRESENT_IN_VOLUME);
                break;
            case NOTE_REFERENCE_INDICATOR:
                symbol.set("\u2814\u2814");
                description.set(bundle.getString("specialSymbolNoterefIndicatorDescription"));
                mode.set(Mode.IF_PRESENT_IN_VOLUME);
                break;
            case ITALIC_INDICATOR:
                if (mainTableLocale.equals("en-US")) { symbol.set("\u2828"); }
                description.set(bundle.getString("specialSymbolItalicIndicatorDescription"));
                mode.set(Mode.NEVER);
                break;
            case BOLDFACE_INDICATOR:
                if (mainTableLocale.equals("en-US")) { symbol.set("\u2838"); }
                description.set(bundle.getString("specialSymbolBoldIndicatorDescription"));
                mode.set(Mode.NEVER);
                break;
            case LETTER_INDICATOR:
                if (mainTableLocale.equals("en-US")) { symbol.set("\u2830"); }
                description.set(bundle.getString("specialSymbolLetterIndicatorDescription"));
                mode.set(Mode.NEVER);
                break;
            case NUMBER_INDICATOR:
                if (mainTableLocale.equals("en-US")) { symbol.set("\u283C"); }
                description.set(bundle.getString("specialSymbolNumberIndicatorDescription"));
                mode.set(Mode.NEVER);
                break;
            default:
        }
    }


    /*****************/
    /* INNER CLASSES */
    /*****************/

    public class SymbolSetting extends TextSetting {

        @Override
        public boolean accept(String value) {
            return value.matches("[\\p{InBraille_Patterns}]+");
        }

        private String getDotPattern() {

            String symbol = get();
            StringBuffer dotsBuffer = new StringBuffer();
            String singleDots = null;
            int singleChar;
            boolean first = true;

            for (int i=0;i<symbol.length();i++) {
                singleChar = (int)(symbol.charAt(i)) - 0x2800;
                singleDots = "";
                if (singleChar>0) {
                    for (int j=5;j>=0;j--) {
                        if (singleChar >= (1<<j)) {
                            singleChar -= (1<<j);
                            singleDots = Integer.toString(j+1) + singleDots;
                        }
                    }
                } else {
                    singleDots = "0";
                }
                if (!first) { dotsBuffer.append(", "); }
                first = false;
                dotsBuffer.append(singleDots);
            }

            return dotsBuffer.toString();
        }
    }

    public class ModeSetting extends EnumSetting<Mode>
                          implements Dependent {

        public ModeSetting() { super(Mode.class); }

        @Override
        public boolean accept(Mode value) {
            if (!super.accept(value)) { return false; }
            return getType() != Type.OTHER || value != Mode.IF_PRESENT_IN_VOLUME;
        }

        public boolean refresh() {
            if (accept(get())) { return false; }
            update(Mode.NEVER);
            return true;
        }

        public void propertyUpdated(PropertyEvent event) {            
            if (event.getSource() == type && event.ValueChanged) {
                fireEvent(refresh(), true);
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof SpecialSymbol)) { return false; }
        SpecialSymbol that = (SpecialSymbol)object;
        return this.type.equals(that.type) &&
               this.symbol.equals(that.symbol) &&
               this.description.equals(that.description) &&
               this.mode.equals(that.mode);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + type.hashCode();
        hash = 11 * hash + symbol.hashCode();
        hash = 11 * hash + description.hashCode();
        hash = 11 * hash + mode.hashCode();
        return hash;
    }
}


