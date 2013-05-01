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

import be.docarch.odt2braille.setup.PropertyEvent;
import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.Dependent;
import be.docarch.odt2braille.setup.DependentYesNoSetting;
import be.docarch.odt2braille.setup.EnumSetting;

/**
 *
 * @author Bert Frees
 */
public class CharacterStyle extends Style {

    /************************/
    /* CONSTANTS & SETTINGS */
    /************************/

    private final String id;
    private final String displayName;
    private final CharacterStyle parentStyle;

    public final DependentYesNoSetting inherit;

    public final FollowPrintSetting italic;
    public final FollowPrintSetting boldface;
    public final FollowPrintSetting underline;
    public final FollowPrintSetting capitals;

    /* GETTERS */

    public String         getID()          { return id; }
    public String         getDisplayName() { return displayName; }
    public CharacterStyle getParentStyle() { return parentStyle; }

    public boolean        getInherit()     { return inherit.get(); }
    public FollowPrint    getItalic()      { return italic.get(); }
    public FollowPrint    getBoldface()    { return boldface.get(); }
    public FollowPrint    getUnderline()   { return underline.get(); }
    public FollowPrint    getCapitals()    { return capitals.get(); }

    /* SETTERS */

    public void setInherit     (boolean value)        { inherit.set(value); }
    public void setItalic      (FollowPrint value)    { italic.set(value); }
    public void setBoldface    (FollowPrint value)    { boldface.set(value); }
    public void setUnderline   (FollowPrint value)    { underline.set(value); }
    public void setCapitals    (FollowPrint value)    { capitals.set(value); }


    public CharacterStyle(String id,
                          String displayName,
                          CharacterStyle parentStyle) {

        this.id = id;
        this.displayName = displayName;
        this.parentStyle = parentStyle;

        /* DECLARATION */

        inherit = new DependentYesNoSetting() {
            public boolean accept(Boolean value) { return !value || getParentStyle() != null; }
        };

        italic = new FollowPrintSetting(parentStyle==null ? null : parentStyle.italic);
        boldface = new FollowPrintSetting(parentStyle==null ? null : parentStyle.boldface);
        underline = new FollowPrintSetting(parentStyle==null ? null : parentStyle.underline);
        capitals = new FollowPrintSetting(parentStyle==null ? null : parentStyle.capitals);

        /* INITIALIZATION */

        inherit.set(true);
        italic.set(FollowPrint.FOLLOW_PRINT);
        boldface.set(FollowPrint.FOLLOW_PRINT);
        underline.set(FollowPrint.FOLLOW_PRINT);
        capitals.set(FollowPrint.FOLLOW_PRINT);

        /* LINKING */

        inherit.addListener(italic);
        inherit.addListener(boldface);
        inherit.addListener(underline);
        inherit.addListener(capitals);

    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    public class FollowPrintSetting extends EnumSetting<FollowPrint>
                                 implements Dependent {

        private final Setting<FollowPrint> parentSetting;
        public FollowPrintSetting(Setting<FollowPrint> parentSetting) {
            super(FollowPrint.class);
            this.parentSetting = parentSetting;
            if (parentSetting != null) { parentSetting.addListener(this); }
        }
        @Override
        public FollowPrint get() {
            if (getInherit()) { return parentSetting.get(); }
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
            } else if (parentSetting != null &&
                       getInherit() &&
                       event.getSource() == parentSetting) {
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
               ((this.parentStyle==null)?(that.parentStyle==null):this.parentStyle.equals(that.parentStyle)) &&
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
        if (parentStyle!=null) { hash = 11 * hash + parentStyle.hashCode(); }
        hash = 11 * hash + inherit.hashCode();
        hash = 11 * hash + italic.hashCode();
        hash = 11 * hash + boldface.hashCode();
        hash = 11 * hash + underline.hashCode();
        hash = 11 * hash + capitals.hashCode();
        return hash;
    }
}
