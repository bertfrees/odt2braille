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
import be.docarch.odt2braille.setup.DependentNumberSetting;
import be.docarch.odt2braille.setup.EnumSetting;

/**
 *
 * @author Bert Frees
 */
public class ParagraphStyle extends Style {

    /************************/
    /* CONSTANTS & SETTINGS */
    /************************/

    private final String id;
    private final String displayName;
    private final ParagraphStyle parentStyle;

    public final DependentYesNoSetting inherit;
    
    public final AlignmentSetting alignment;
    public final InheritableNumberSetting firstLine;
    public final InheritableNumberSetting runovers;
    public final InheritableNumberSetting marginLeftRight;
    public final InheritableNumberSetting linesAbove;
    public final InheritableNumberSetting linesBelow;
    public final FollowPrintSetting emptyParagraphs;
    public final FollowPrintSetting hardPageBreaks;
    public final InheritableYesNoSetting keepWithNext;
    public final InheritableYesNoSetting dontSplit;
    public final InheritableYesNoSetting widowControlEnabled;
    public final InheritableYesNoSetting orphanControlEnabled;
    public final InheritableNumberSetting widowControl;
    public final InheritableNumberSetting orphanControl;

    /* GETTERS */

    public String         getID()                   { return id; }
    public String         getDisplayName()          { return displayName; }
    public ParagraphStyle getParentStyle()          { return parentStyle; }

    public boolean        getInherit()              { return inherit.get(); }
    public Alignment      getAlignment()            { return alignment.get(); }
    public int            getFirstLine()            { return firstLine.get(); }
    public int            getRunovers()             { return runovers.get(); }
    public int            getMarginLeftRight()      { return marginLeftRight.get(); }
    public int            getLinesAbove()           { return linesAbove.get(); }
    public int            getLinesBelow()           { return linesBelow.get(); }
    public FollowPrint    getEmptyParagraphs()      { return emptyParagraphs.get(); }
    public FollowPrint    getHardPageBreaks()       { return hardPageBreaks.get(); }
    public boolean        getKeepWithNext()         { return keepWithNext.get(); }
    public boolean        getDontSplit()            { return dontSplit.get(); }
    public boolean        getWidowControlEnabled()  { return widowControlEnabled.get(); }
    public boolean        getOrphanControlEnabled() { return orphanControlEnabled.get(); }
    public int            getWidowControl()         { return widowControl.get(); }
    public int            getOrphanControl()        { return orphanControl.get(); }

    /* SETTERS */

    public void setInherit              (boolean value)        { inherit.set(value); }
    public void setAlignment            (Alignment value)      { alignment.set(value); }
    public void setFirstLine            (int value)            { firstLine.set(value); }
    public void setRunovers             (int value)            { runovers.set(value); }
    public void setMarginLeftRight      (int value)            { marginLeftRight.set(value); }
    public void setLinesAbove           (int value)            { linesAbove.set(value); }
    public void setLinesBelow           (int value)            { linesBelow.set(value); }
    public void setEmptyParagraphs      (FollowPrint value)    { emptyParagraphs.set(value); }
    public void setHardPageBreaks       (FollowPrint value)    { hardPageBreaks.set(value); }
    public void setKeepWithNext         (boolean value)        { keepWithNext.set(value); }
    public void setDontSplit            (boolean value)        { dontSplit.set(value); }
    public void setWidowControlEnabled  (boolean value)        { widowControlEnabled.set(value); }
    public void setOrphanControlEnabled (boolean value)        { orphanControlEnabled.set(value); }
    public void setWidowControl         (int value)            { widowControl.set(value); }
    public void setOrphanControl        (int value)            { orphanControl.set(value); }


    public ParagraphStyle(String id,
                          String displayName,
                          ParagraphStyle parentStyle) {

        this.id = id;
        this.displayName = displayName;
        this.parentStyle = parentStyle;

        /* DECLARATION */

        inherit = new DependentYesNoSetting() {
            public boolean accept(Boolean value) { return !value || getParentStyle() != null; }
        };

        alignment = new AlignmentSetting(parentStyle==null ? null : parentStyle.alignment);

        firstLine = new InheritableNumberSetting(parentStyle==null ? null : parentStyle.firstLine) {
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.LEFT) { return false; }
                return super.enabled();
            }
        };

        runovers = new InheritableNumberSetting(parentStyle==null ? null : parentStyle.runovers) {
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.LEFT) { return false; }
                return super.enabled();
            }
        };

        marginLeftRight = new InheritableNumberSetting(parentStyle==null ? null : parentStyle.marginLeftRight) {
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.CENTERED) { return false; }
                return super.enabled();
            }
        };

        linesAbove = new InheritableNumberSetting(parentStyle==null ? null : parentStyle.linesAbove);
        linesBelow = new InheritableNumberSetting(parentStyle==null ? null : parentStyle.linesBelow);
        emptyParagraphs = new FollowPrintSetting(parentStyle==null ? null : parentStyle.emptyParagraphs);
        hardPageBreaks = new FollowPrintSetting(parentStyle==null ? null : parentStyle.hardPageBreaks);
        keepWithNext = new InheritableYesNoSetting(parentStyle==null ? null : parentStyle.keepWithNext);

        dontSplit = new InheritableYesNoSetting(parentStyle==null ? null : parentStyle.dontSplit) {
            @Override
            public boolean accept(Boolean value) { return getKeepWithNext() ? value : true; }
        };

        widowControlEnabled = new InheritableYesNoSetting(parentStyle==null ? null : parentStyle.widowControlEnabled) {
            @Override
            public boolean accept(Boolean value) { return !value; }
        };

        orphanControlEnabled = new InheritableYesNoSetting(parentStyle==null ? null : parentStyle.orphanControlEnabled) {
            @Override
            public boolean enabled() {
                if (getDontSplit()) { return false; }
                return super.enabled();
            }
        };

        widowControl = new InheritableNumberSetting(parentStyle==null ? null : parentStyle.widowControl) {
            @Override
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

        orphanControl = new InheritableNumberSetting(parentStyle==null ? null : parentStyle.orphanControl) {
            @Override
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

        inherit.set(true);
        widowControl.refresh();
        orphanControl.refresh();

        /* LINKING */

        inherit.addListener(alignment);
        inherit.addListener(firstLine);
        inherit.addListener(runovers);
        inherit.addListener(marginLeftRight);
        inherit.addListener(linesAbove);
        inherit.addListener(linesBelow);
        inherit.addListener(emptyParagraphs);
        inherit.addListener(hardPageBreaks);
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
        dontSplit.addListener(orphanControlEnabled);
        widowControlEnabled.addListener(widowControl);
        orphanControlEnabled.addListener(orphanControl);

    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    public class InheritableNumberSetting extends DependentNumberSetting {

        private final Setting<Integer> parentSetting;
        public InheritableNumberSetting(Setting<Integer> parentSetting) {
            super();
            this.parentSetting = parentSetting;
            if (parentSetting != null) { parentSetting.addListener(this); }
        }
        @Override
        public Integer get() {
            if (getInherit()) { return parentSetting.get(); }
            return super.get();
        }
        @Override
        public boolean enabled() {
            if (getInherit()) { return false; }
            return super.enabled();
        }
        public boolean accept(Integer value) { return value >= 0; }
        @Override
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

    public class InheritableYesNoSetting extends DependentYesNoSetting {

        private final Setting<Boolean> parentSetting;
        public InheritableYesNoSetting(Setting<Boolean> parentSetting) {
            super();
            this.parentSetting = parentSetting;
            if (parentSetting != null) { parentSetting.addListener(this); }
        }
        @Override
        public Boolean get() {
            if (getInherit()) { return parentSetting.get(); }
            return super.get();
        }
        @Override
        public boolean enabled() {
            if (getInherit()) { return false; }
            return super.enabled();
        }
        public boolean accept(Boolean value) { return true; }
        @Override
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

    public class AlignmentSetting extends EnumSetting<Alignment>
                               implements Dependent {

        private final Setting<Alignment> parentSetting;
        public AlignmentSetting(Setting<Alignment> parentSetting) {
            super(Alignment.class);
            this.parentSetting = parentSetting;
            if (parentSetting != null) { parentSetting.addListener(this); }
        }
        @Override
        public Alignment get() {
            if (getInherit()) { return parentSetting.get(); }
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
            } else if (parentSetting != null &&
                       getInherit() &&
                       event.getSource() == parentSetting) {
                fireEvent(true, true);
            } else if (event.ValueChanged) {
                fireEvent(refresh(), true);
            }
        }
    }

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
        if (!(object instanceof ParagraphStyle)) { return false; }
        ParagraphStyle that = (ParagraphStyle)object;
        return this.id.equals(that.id) &&
               this.displayName.equals(that.displayName) &&
               ((this.parentStyle==null)?(that.parentStyle==null):this.parentStyle.equals(that.parentStyle)) &&
               this.inherit.equals(that.inherit) &&
               this.alignment.equals(that.alignment) &&
               this.firstLine.equals(that.firstLine) &&
               this.runovers.equals(that.runovers) &&
               this.marginLeftRight.equals(that.marginLeftRight) &&
               this.linesAbove.equals(that.linesAbove) &&
               this.linesBelow.equals(that.linesBelow) &&
               this.emptyParagraphs.equals(that.emptyParagraphs) &&
               this.hardPageBreaks.equals(that.hardPageBreaks) &&
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
        if (parentStyle!=null) { hash = 11 * hash + parentStyle.hashCode(); }
        hash = 11 * hash + inherit.hashCode();
        hash = 11 * hash + alignment.hashCode();
        hash = 11 * hash + firstLine.hashCode();
        hash = 11 * hash + runovers.hashCode();
        hash = 11 * hash + marginLeftRight.hashCode();
        hash = 11 * hash + linesAbove.hashCode();
        hash = 11 * hash + linesBelow.hashCode();
        hash = 11 * hash + emptyParagraphs.hashCode();
        hash = 11 * hash + hardPageBreaks.hashCode();
        hash = 11 * hash + keepWithNext.hashCode();
        hash = 11 * hash + dontSplit.hashCode();
        hash = 11 * hash + widowControlEnabled.hashCode();
        hash = 11 * hash + orphanControlEnabled.hashCode();
        hash = 11 * hash + widowControl.hashCode();
        hash = 11 * hash + orphanControl.hashCode();
        return hash;
    }
}
