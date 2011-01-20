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
public class CharacterStyle extends Style {

    public enum TypefaceOption { YES, NO, FOLLOW_PRINT };

    protected String displayName;
    protected CharacterStyle parentStyle;
    protected boolean inherit;
    protected TypefaceOption italic;
    protected TypefaceOption boldface;
    protected TypefaceOption underline;
    protected TypefaceOption capitals;

    public CharacterStyle(CharacterStyle copyStyle) {
    
        super(copyStyle);

        this.displayName = copyStyle.displayName;
        this.parentStyle = copyStyle.parentStyle;
        this.inherit = copyStyle.inherit;
        this.italic = copyStyle.italic;
        this.boldface = copyStyle.boldface;
        this.underline = copyStyle.underline;
        this.capitals = copyStyle.capitals;
    
    }

    public CharacterStyle(String name) {

        super(name);
        this.displayName = name;
        this.parentStyle = null;
        this.inherit = false;
        this.italic = TypefaceOption.FOLLOW_PRINT;
        this.boldface = TypefaceOption.FOLLOW_PRINT;
        this.underline = TypefaceOption.FOLLOW_PRINT;
        this.capitals = TypefaceOption.FOLLOW_PRINT;

    }

    public void setDisplayName (String displayName)         { this.displayName = displayName; }
    public void setParentStyle (CharacterStyle parentStyle) { if (parentStyle != null) { this.parentStyle = parentStyle; }}
    public void setInherit     (boolean inherit)            { this.inherit = inherit && parentStyle != null; }
    public void setItalic      (TypefaceOption italic)      { this.italic = italic; }
    public void setBoldface    (TypefaceOption boldface)    { this.boldface = boldface; }
    public void setUnderline   (TypefaceOption underline)   { this.underline = underline; }
    public void setCapitals    (TypefaceOption capitals)    { this.capitals = capitals; }

    public String         getDisplayName() { return displayName; }
    public CharacterStyle getParentStyle() { return parentStyle; }
    public boolean        getInherit()     { return inherit; }

    public TypefaceOption getItalic()      { return inherit?parentStyle.getItalic():italic; }
    public TypefaceOption getBoldface()    { return inherit?parentStyle.getBoldface():boldface; }
    public TypefaceOption getUnderline()   { return inherit?parentStyle.getUnderline():underline; }
    public TypefaceOption getCapitals()    { return inherit?parentStyle.getCapitals():capitals; }

}
