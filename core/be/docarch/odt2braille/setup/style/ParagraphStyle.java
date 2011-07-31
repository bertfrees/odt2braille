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
import be.docarch.odt2braille.setup.DependentNumberSetting;
import be.docarch.odt2braille.setup.EnumSetting;
import be.docarch.odt2braille.setup.TextSetting;
import be.docarch.odt2braille.setup.YesNoSetting;

/**
 *
 * @author Bert Frees
 */
public class ParagraphStyle extends Style {

    /************************/
    /* CONSTANTS & SETTINGS */
    /************************/

    private final String id;

    private final Setting<String> displayName;
    private final Setting<ParagraphStyle> parentStyle;
    private final Setting<Boolean> automatic;

    public final DependentYesNoSetting inherit;

    public final AlignmentSetting alignment;
    public final InheritableNumberSetting firstLine;
    public final InheritableNumberSetting runovers;
    public final InheritableNumberSetting marginLeftRight;
    public final InheritableNumberSetting linesAbove;
    public final InheritableNumberSetting linesBelow;
    public final InheritableYesNoSetting keepEmptyParagraphs;
    public final InheritableYesNoSetting keepWithNext;
    public final InheritableYesNoSetting dontSplit;
    public final InheritableYesNoSetting widowControlEnabled;
    public final InheritableYesNoSetting orphanControlEnabled;
    public final InheritableNumberSetting widowControl;
    public final InheritableNumberSetting orphanControl;

    /* GETTERS */

    public String         getID()                   { return id; }
    public String         getDisplayName()          { return displayName.get(); }
    public ParagraphStyle getParentStyle()          { return parentStyle.get(); }
    public boolean        getAutomatic()            { return automatic.get(); }
    public boolean        getInherit()              { return inherit.get(); }
    public Alignment      getAlignment()            { return alignment.get(); }
    public int            getFirstLine()            { return firstLine.get(); }
    public int            getRunovers()             { return runovers.get(); }
    public int            getMarginLeftRight()      { return marginLeftRight.get(); }
    public int            getLinesAbove()           { return linesAbove.get(); }
    public int            getLinesBelow()           { return linesBelow.get(); }
    public boolean        getKeepEmptyParagraphs()  { return keepEmptyParagraphs.get(); }
    public boolean        getKeepWithNext()         { return keepWithNext.get(); }
    public boolean        getDontSplit()            { return dontSplit.get(); }
    public boolean        getWidowControlEnabled()  { return widowControlEnabled.get(); }
    public boolean        getOrphanControlEnabled() { return orphanControlEnabled.get(); }
    public int            getWidowControl()         { return widowControl.get(); }
    public int            getOrphanControl()        { return orphanControl.get(); }

    /* SETTERS */

    public void setDisplayName          (String value)         { displayName.set(value); }
    public void setParentStyle          (ParagraphStyle value) { parentStyle.set(value); }
    public void setAutomatic            (boolean value)        { automatic.set(value); }
    public void setInherit              (boolean value)        { inherit.set(value); }
    public void setAlignment            (Alignment value)      { alignment.set(value); }
    public void setFirstLine            (int value)            { firstLine.set(value); }
    public void setRunovers             (int value)            { runovers.set(value); }
    public void setMarginLeftRight      (int value)            { marginLeftRight.set(value); }
    public void setLinesAbove           (int value)            { linesAbove.set(value); }
    public void setLinesBelow           (int value)            { linesBelow.set(value); }
    public void setKeepEmptyParagraphs  (boolean value)        { keepEmptyParagraphs.set(value); }
    public void setKeepWithNext         (boolean value)        { keepWithNext.set(value); }
    public void setDontSplit            (boolean value)        { dontSplit.set(value); }
    public void setWidowControlEnabled  (boolean value)        { widowControlEnabled.set(value); }
    public void setOrphanControlEnabled (boolean value)        { orphanControlEnabled.set(value); }
    public void setWidowControl         (int value)            { widowControl.set(value); }
    public void setOrphanControl        (int value)            { orphanControl.set(value); }


    public ParagraphStyle(String id) {

        this.id = id;

        /* DECLARATION */

        displayName = new TextSetting();
        parentStyle = new ParentStyleSetting();
        automatic = new YesNoSetting();

        inherit = new DependentYesNoSetting() {
            public boolean accept(Boolean value) { return !value || getParentStyle() != null; }
        };

        alignment = new AlignmentSetting() {
            public Alignment getInheritedValue() { return getParentStyle().getAlignment(); }
        };

        firstLine = new InheritableNumberSetting() {
            public Integer getInheritedValue() { return getParentStyle().getFirstLine(); }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.LEFT) { return false; }
                return super.enabled();
            }
            public boolean accept(Integer value) { return value >= 0; }
        };

        runovers = new InheritableNumberSetting() {
            public Integer getInheritedValue() { return getParentStyle().getRunovers(); }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.LEFT) { return false; }
                return super.enabled();
            }
            public boolean accept(Integer value) { return value >= 0; }
        };

        marginLeftRight = new InheritableNumberSetting() {
            public Integer getInheritedValue() { return getParentStyle().getMarginLeftRight(); }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.CENTERED) { return false; }
                return super.enabled();
            }
            public boolean accept(Integer value) { return value >= 0; }
        };

        linesAbove = new InheritableNumberSetting() {
            public Integer getInheritedValue() { return getParentStyle().getLinesAbove(); }
            public boolean accept(Integer value) { return value >= 0; }
        };

        linesBelow = new InheritableNumberSetting() {
            public Integer getInheritedValue() { return getParentStyle().getLinesBelow(); }
            public boolean accept(Integer value) { return value >= 0; }
        };

        keepEmptyParagraphs = new InheritableYesNoSetting() {
            public Boolean getInheritedValue() { return getParentStyle().getKeepEmptyParagraphs(); }
            public boolean accept(Boolean value) { return true; }
        };

        keepWithNext = new InheritableYesNoSetting() {
            public Boolean getInheritedValue() { return getParentStyle().getKeepWithNext(); }
            public boolean accept(Boolean value) { return true; }
        };

        dontSplit = new InheritableYesNoSetting() {
            public Boolean getInheritedValue() { return getParentStyle().getDontSplit(); }
            public boolean accept(Boolean value) { return getKeepWithNext() ? value : true; }
        };

        widowControlEnabled = new InheritableYesNoSetting() {
            public Boolean getInheritedValue() { return getParentStyle().getWidowControlEnabled(); }
            public boolean accept(Boolean value) { return !value; }
        };

        orphanControlEnabled = new InheritableYesNoSetting() {
            public Boolean getInheritedValue() { return getParentStyle().getOrphanControlEnabled(); }
            public boolean accept(Boolean value) { return true; }
            @Override
            public boolean enabled() {
                if (getDontSplit()) { return false; }
                return super.enabled();
            }
        };

        widowControl = new InheritableNumberSetting() {
            public Integer getInheritedValue() { return getParentStyle().getWidowControl(); }
            public boolean accept(Integer value) { return value >= 2; }
            @Override
            public boolean refresh() {
                if (accept(get())) { return false; }
                return update(2);
            }
            @Override
            public boolean enabled() {
                if (!getWidowControlEnabled()) { return false; }
                return super.enabled();
            }
        };

        orphanControl = new InheritableNumberSetting() {
            public Integer getInheritedValue() { return getParentStyle().getOrphanControl(); }
            public boolean accept(Integer value) { return value >= 2; }
            @Override
            public boolean refresh() {
                if (accept(get())) { return false; }
                return update(2);
            }
            @Override
            public boolean enabled() {
                if (!getOrphanControlEnabled()) { return false; }
                return super.enabled();
            }
        };

        /* INITIALIZATION */

        displayName.set(id);
        inherit.set(true);
        widowControl.refresh();
        orphanControl.refresh();

        /* LINKING */

        parentStyle.addListener(inherit);
        parentStyle.addListener(alignment);
        parentStyle.addListener(firstLine);
        parentStyle.addListener(runovers);
        parentStyle.addListener(marginLeftRight);
        parentStyle.addListener(linesAbove);
        parentStyle.addListener(linesBelow);
        parentStyle.addListener(keepEmptyParagraphs);
        parentStyle.addListener(keepWithNext);
        parentStyle.addListener(dontSplit);
        parentStyle.addListener(widowControlEnabled);
        parentStyle.addListener(orphanControlEnabled);
        parentStyle.addListener(widowControl);
        parentStyle.addListener(orphanControl);
        inherit.addListener(alignment);
        inherit.addListener(firstLine);
        inherit.addListener(runovers);
        inherit.addListener(marginLeftRight);
        inherit.addListener(linesAbove);
        inherit.addListener(linesBelow);
        inherit.addListener(keepEmptyParagraphs);
        inherit.addListener(keepWithNext);
        inherit.addListener(dontSplit);
        inherit.addListener(widowControlEnabled);
        inherit.addListener(orphanControlEnabled);
        inherit.addListener(widowControl);
        inherit.addListener(orphanControl);
        alignment.addListener(firstLine);
        alignment.addListener(runovers);
        alignment.addListener(marginLeftRight);
        keepWithNext.addListener(dontSplit);
        dontSplit.addListener(widowControlEnabled);
        widowControlEnabled.addListener(widowControl);
        orphanControlEnabled.addListener(orphanControl);

    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    private class ParentStyleSetting extends Setting<ParagraphStyle> {

        ParagraphStyle style = null;

        public boolean accept(ParagraphStyle value) { return true; }
        public ParagraphStyle get() { return style; }

        protected boolean update(ParagraphStyle value) {
            if (style == value) { return false; }
            style = value;
            return true;
        }
    }

    public abstract class InheritableNumberSetting extends DependentNumberSetting {

        public abstract Integer getInheritedValue();

        @Override
        public Integer get() {
            if (getInherit()) { return getInheritedValue(); }
            return super.get();
        }

        @Override
        public boolean enabled() {
            if (getInherit()) { return false; }
            return super.enabled();
        }

        @Override
        public void propertyUpdated(PropertyEvent event) {
            if (event.getSource() == inherit) {
                fireEvent(true, true);
            } else {
                super.propertyUpdated(event);
            }
        }
    }

    public abstract class InheritableYesNoSetting extends DependentYesNoSetting {

        public abstract Boolean getInheritedValue();

        @Override
        public Boolean get() {
            if (getInherit()) { return getInheritedValue(); }
            return super.get();
        }

        @Override
        public boolean enabled() {
            if (getInherit()) { return false; }
            return super.enabled();
        }

        @Override
        public void propertyUpdated(PropertyEvent event) {
            if (event.getSource() == inherit) {
                fireEvent(true, true);
            } else {
                super.propertyUpdated(event);
            }
        }
    }

    public abstract class AlignmentSetting extends EnumSetting<Alignment>
                                        implements Dependent {

        public AlignmentSetting() { super(Alignment.class); }

        public abstract Alignment getInheritedValue();

        @Override
        public Alignment get() {
            if (getInherit()) { return getInheritedValue(); }
            return super.get();
        }

        @Override
        public boolean enabled() {
            if (getInherit()) { return false; }
            return super.enabled();
        }

        public boolean refresh() { return false; }
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
        if (!(object instanceof ParagraphStyle)) { return false; }
        ParagraphStyle that = (ParagraphStyle)object;
        return this.id.equals(that.id) &&
               this.displayName.equals(that.displayName) &&
               this.parentStyle.equals(that.parentStyle) &&
               this.automatic.equals(that.automatic) &&
               this.inherit.equals(that.inherit) &&
               this.alignment.equals(that.alignment) &&
               this.firstLine.equals(that.firstLine) &&
               this.runovers.equals(that.runovers) &&
               this.marginLeftRight.equals(that.marginLeftRight) &&
               this.linesAbove.equals(that.linesAbove) &&
               this.linesBelow.equals(that.linesBelow) &&
               this.keepEmptyParagraphs.equals(that.keepEmptyParagraphs) &&
               this.keepWithNext.equals(that.keepWithNext) &&
               this.dontSplit.equals(that.dontSplit) &&
               this.widowControlEnabled.equals(that.widowControlEnabled) &&
               this.orphanControlEnabled.equals(that.orphanControlEnabled) &&
               this.widowControl.equals(that.widowControl) &&
               this.orphanControl.equals(that.orphanControl);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + id.hashCode();
        hash = 11 * hash + displayName.hashCode();
        hash = 11 * hash + parentStyle.hashCode();
        hash = 11 * hash + automatic.hashCode();
        hash = 11 * hash + inherit.hashCode();
        hash = 11 * hash + alignment.hashCode();
        hash = 11 * hash + firstLine.hashCode();
        hash = 11 * hash + runovers.hashCode();
        hash = 11 * hash + marginLeftRight.hashCode();
        hash = 11 * hash + linesAbove.hashCode();
        hash = 11 * hash + linesBelow.hashCode();
        hash = 11 * hash + keepEmptyParagraphs.hashCode();
        hash = 11 * hash + keepWithNext.hashCode();
        hash = 11 * hash + dontSplit.hashCode();
        hash = 11 * hash + widowControlEnabled.hashCode();
        hash = 11 * hash + orphanControlEnabled.hashCode();
        hash = 11 * hash + widowControl.hashCode();
        hash = 11 * hash + orphanControl.hashCode();
        return hash;
    }
}
