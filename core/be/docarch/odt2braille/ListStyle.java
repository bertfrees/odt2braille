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
public class ListStyle extends Style {

    protected int level;
    protected ListStyle parentLevel;
    protected boolean dontSplitItems;
    protected String prefix;
    protected int linesBetween;


    public ListStyle(ListStyle copyStyle) {
    
        super(copyStyle);
        this.level = copyStyle.level;
        this.parentLevel = copyStyle.parentLevel;
        this.dontSplitItems = copyStyle.dontSplitItems;
        this.prefix = copyStyle.prefix;
        this.linesBetween = copyStyle.linesBetween;
    
    }

    public ListStyle(int level) {

        super("list_" + level);
        this.level = level;
        this.parentLevel = null;
        this.dontSplitItems = false;
        this.prefix = "";
        this.linesBetween = 0;
        
    }

    public void setParentLevel    (ListStyle parentLevel)   { if (parentLevel != null) { this.parentLevel = parentLevel; }}
    public void setDontSplitItems (boolean dontSplitItems)  { this.dontSplitItems = dontSplitItems; }
    public void setPrefix         (String prefix)           { this.prefix = prefix; }
    public void setLinesBetween   (int linesBetween)        { if (linesBetween >= 0) { this.linesBetween = linesBetween; }}

    public int       getLevel()           { return level; }
    public ListStyle getParentLevel()     { return parentLevel; }
    public boolean   getDontSplitItems()  { return getDontSplit() || dontSplitItems; }
    public String    getPrefix()          { return prefix; }
    public int       getLinesBetween()    { return linesBetween; }

    @Override public boolean  getDontSplit() { return (parentLevel!=null)?dontSplit || parentLevel.getDontSplitItems():dontSplit; }

}
