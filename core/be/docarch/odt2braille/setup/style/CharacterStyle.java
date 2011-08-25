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
import be.docarch.odt2braille.setup.DependentYesNoSetting;
import be.docarch.odt2braille.setup.EnumSetting;
import be.docarch.odt2braille.setup.TextSetting;

/**
 *
 * @author Bert Frees
 */
public class CharacterStyle extends Style {

    /************************/
    /* CONSTANTS & SETTINGS */
    /************************/

    private final String id;

    private final Setting<String> displayName;
    private final Setting<CharacterStyle> parentStyle;

    public final DependentYesNoSetting inherit;

    public final FollowPrintSetting italic;
    public final FollowPrintSetting boldface;
    public final FollowPrintSetting underline;
    public final FollowPrintSetting capitals;

    /* GETTERS */

    public String         getID()          { return id; }
    public String         getDisplayName() { return displayName.get(); }
    public CharacterStyle getParentStyle() { return parentStyle.get(); }
    public boolean        getInherit()     { return inherit.get(); }
    public FollowPrint    getItalic()      { return italic.get(); }
    public FollowPrint    getBoldface()    { return boldface.get(); }
    public FollowPrint    getUnderline()   { return underline.get(); }
    public FollowPrint    getCapitals()    { return capitals.get(); }

    /* SETTERS */

    public void setDisplayName (String value)         { displayName.set(value); }
    public void setParentStyle (CharacterStyle value) { parentStyle.set(value); }
    public void setInherit     (boolean value)        { inherit.set(value); }
    public void setItalic      (FollowPrint value)    { italic.set(value); }
    public void setBoldface    (FollowPrint value)    { boldface.set(value); }
    public void setUnderline   (FollowPrint value)    { underline.set(value); }
    public void setCapitals    (FollowPrint value)    { capitals.set(value); }


    public CharacterStyle(String id) {

        this.id = id;

        /* DECLARATION */

        displayName = new TextSetting();
        parentStyle = new ParentStyleSetting();

        inherit = new DependentYesNoSetting() {
            public boolean accept(Boolean value) { return !value || getParentStyle() != null; }
        };

        italic = new FollowPrintSetting() {
            public FollowPrint getInheritedValue() { return getParentStyle().getItalic(); }
        };

        boldface = new FollowPrintSetting() {
            public FollowPrint getInheritedValue() { return getParentStyle().getBoldface(); }
        };

        underline = new FollowPrintSetting() {
            public FollowPrint getInheritedValue() { return getParentStyle().getUnderline(); }
        };

        capitals = new FollowPrintSetting() {
            public FollowPrint getInheritedValue() { return getParentStyle().getCapitals(); }
        };

        /* INITIALIZATION */

        displayName.set(id);
        inherit.set(true);
        italic.set(FollowPrint.FOLLOW_PRINT);
        boldface.set(FollowPrint.FOLLOW_PRINT);
        underline.set(FollowPrint.FOLLOW_PRINT);
        capitals.set(FollowPrint.FOLLOW_PRINT);

        /* LINKING */

        parentStyle.addListener(inherit);
        parentStyle.addListener(italic);
        parentStyle.addListener(boldface);
        parentStyle.addListener(underline);
        parentStyle.addListener(capitals);
        inherit.addListener(italic);
        inherit.addListener(boldface);
        inherit.addListener(underline);
        inherit.addListener(capitals);

    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    private class ParentStyleSetting extends Setting<CharacterStyle> {

        CharacterStyle style = null;

        public boolean accept(CharacterStyle value) { return true; }
        public CharacterStyle get() { return style; }

        protected boolean update(CharacterStyle value) {
            if (style == value) { return false; }
            style = value;
            return true;
        }
    }

    public abstract class FollowPrintSetting extends EnumSetting<FollowPrint>
                                          implements Dependent {

        public FollowPrintSetting() { super(FollowPrint.class); }

        public abstract FollowPrint getInheritedValue();

        @Override
        public FollowPrint get() {
            if (getInherit()) { return getInheritedValue(); }
            return super.get();
        }

        @Override
        public boolean enabled() {
            if (getInherit()) { return false; }
            return super.enabled();
        }

        public boolean refresh() { return true; }

        public void propertyUpdated(PropertyEvent event) {
            if (event.getSource() == inherit) {
                fireEvent(true, true);
            } else if (event.ValueChanged) {
                fireEvent(refresh(), true);
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof CharacterStyle)) { return false; }
        CharacterStyle that = (CharacterStyle)object;
        return this.id.equals(that.id) &&
               this.displayName.equals(that.displayName) &&
               this.parentStyle.equals(that.parentStyle) &&
               this.inherit.equals(that.inherit) &&
               this.italic.equals(that.italic) &&
               this.boldface.equals(that.boldface) &&
               this.underline.equals(that.underline) &&
               this.capitals.equals(that.capitals);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + id.hashCode();
        hash = 11 * hash + displayName.hashCode();
        hash = 11 * hash + parentStyle.hashCode();
        hash = 11 * hash + inherit.hashCode();
        hash = 11 * hash + italic.hashCode();
        hash = 11 * hash + boldface.hashCode();
        hash = 11 * hash + underline.hashCode();
        hash = 11 * hash + capitals.hashCode();
        return hash;
    }
}
