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
public class ParagraphStyle extends Style {

    private String displayName;
    private ParagraphStyle parentStyle;
    private boolean inherit;
    private boolean automatic;
    private boolean keepEmptyParagraphs;
    private boolean keepWithNext;
    private boolean widowControlEnabled;
    private boolean orphanControlEnabled;
    private int widowControl;
    private int orphanControl;


    public ParagraphStyle(ParagraphStyle copyStyle) {
    
        super(copyStyle);

        this.displayName = copyStyle.displayName;
        this.parentStyle = copyStyle.parentStyle;
        this.inherit = copyStyle.inherit;
        this.automatic = copyStyle.automatic;
        this.keepEmptyParagraphs = copyStyle.keepEmptyParagraphs;
        this.keepWithNext = copyStyle.keepWithNext;
        this.widowControlEnabled = copyStyle.widowControlEnabled;
        this.orphanControlEnabled = copyStyle.orphanControlEnabled;
        this.widowControl = copyStyle.widowControl;
        this.orphanControl = copyStyle.orphanControl;
    
    }

    public ParagraphStyle(String name) {

        super(name);
        this.displayName = name;
        this.parentStyle = null;
        this.inherit = false;
        this.automatic = false;
        this.keepEmptyParagraphs = false;
        this.keepWithNext = false;
        this.widowControlEnabled = false;
        this.orphanControlEnabled = false;
        this.widowControl = 2;
        this.orphanControl = 2;
        
    }

    public void setDisplayName          (String displayName)           { this.displayName = displayName; }
    public void setParentStyle          (ParagraphStyle parentStyle)   { if (parentStyle != null) { this.parentStyle = parentStyle; }}
    public void setInherit              (boolean inherit)              { this.inherit = inherit && parentStyle != null; }
    public void setAutomatic            (boolean automatic)            { this.automatic = automatic; }
    public void setKeepEmptyParagraphs  (boolean keep)                 { this.keepEmptyParagraphs = keep; }
    public void setKeepWithNext         (boolean keepWithNext)         { this.keepWithNext = keepWithNext;
                                                                         setDontSplit(keepWithNext || dontSplit); }
    public void setWidowControlEnabled  (boolean widowControlEnabled)  { this.widowControlEnabled = widowControlEnabled; }
    public void setOrphanControlEnabled (boolean orphanControlEnabled) { this.orphanControlEnabled = orphanControlEnabled; }
    public void setWidowControl         (int widowControl)             { if (widowControlEnabled && widowControl >= 2) { this.widowControl = widowControl; }}
    public void setOrphanControl        (int orphanControl)            { if (orphanControlEnabled && orphanControl >= 2) { this.orphanControl = orphanControl; }}

    public String         getDisplayName()          { return displayName; }
    public ParagraphStyle getParentStyle()          { return parentStyle; }
    public boolean        getInherit()              { return inherit; }
    public boolean        getAutomatic()            { return automatic; }
    public boolean        getKeepEmptyParagraphs()  { return inherit?parentStyle.getKeepEmptyParagraphs():keepEmptyParagraphs; }
    public boolean        getKeepWithNext()         { return inherit?parentStyle.getKeepWithNext():keepWithNext; }
    public boolean        getWidowControlEnabled()  { return widowControlEnabled; }
    public boolean        getOrphanControlEnabled() { return orphanControlEnabled; }
    public int            getWidowControl()         { return inherit?parentStyle.getWidowControl():widowControl; }
    public int            getOrphanControl()        { return inherit?parentStyle.getOrphanControl():orphanControl; }

    @Override public boolean   getDontSplit()       { return inherit?parentStyle.getDontSplit():dontSplit; }
    @Override public Alignment getAlignment()       { return inherit?parentStyle.getAlignment():alignment; }
    @Override public int       getFirstLine()       { return inherit?parentStyle.getFirstLine():firstLine; }
    @Override public int       getRunovers()        { return inherit?parentStyle.getRunovers():runovers; }
    @Override public int       getMarginLeftRight() { return inherit?parentStyle.getMarginLeftRight():marginLeftRight; }
    @Override public int       getLinesAbove()      { return inherit?parentStyle.getLinesAbove():linesAbove; }
    @Override public int       getLinesBelow()      { return inherit?parentStyle.getLinesBelow():linesBelow; }
}
