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
public class HeadingStyle extends Style {

    protected int level;
    protected boolean newBraillePage;
    protected boolean keepWithNext;


    public HeadingStyle(HeadingStyle copyStyle) {

        super(copyStyle);

        this.level = copyStyle.level;
        this.newBraillePage = copyStyle.newBraillePage;
        this.keepWithNext = copyStyle.keepWithNext;
        this.dontSplit = copyStyle.dontSplit;

    }

    public HeadingStyle(int level) {

        super("h" + level);
        this.newBraillePage = false;
        this.level = level;
        this.keepWithNext = false;
        this.dontSplit = false;

    }

    public void    setNewBraillePage (boolean newBraillePage) { this.newBraillePage = newBraillePage;
                                                                keepWithNext =  !newBraillePage && keepWithNext;
                                                                dontSplit = !newBraillePage && dontSplit; }
    public void    setKeepWithNext   (boolean keepWithNext)   { this.keepWithNext = keepWithNext;
                                                                dontSplit = keepWithNext || dontSplit; }

    public boolean getNewBraillePage () { return newBraillePage; }
    public int     getLevel          () { return level; }
    public boolean getKeepWithNext   () { return keepWithNext; }
    
}
