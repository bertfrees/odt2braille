package be.docarch.odt2braille.setup.style;

import be.docarch.odt2braille.setup.EnumSetting;
import be.docarch.odt2braille.setup.Setting;
import be.docarch.odt2braille.setup.NumberSetting;
import be.docarch.odt2braille.setup.DependentNumberSetting;

public class FootnoteStyle extends Style {

    /************/
    /* SETTINGS */
    /************/

    public final EnumSetting<Alignment> alignment;
    public final DependentNumberSetting firstLine;
    public final DependentNumberSetting runovers;
    public final DependentNumberSetting marginLeftRight;
    public final Setting<Integer> linesAbove;
    public final Setting<Integer> linesBelow;

    /* GETTERS */

    public Alignment  getAlignment()       { return alignment.get(); }
    public int        getFirstLine()       { return firstLine.get(); }
    public int        getRunovers()        { return runovers.get(); }
    public int        getMarginLeftRight() { return marginLeftRight.get(); }
    public int        getLinesAbove()      { return linesAbove.get(); }
    public int        getLinesBelow()      { return linesBelow.get(); }

    /* SETTERS */

    public void setAlignment        (Alignment value) { alignment.set(value); }
    public void setFirstLine        (int value)       { firstLine.set(value); }
    public void setRunovers         (int value)       { runovers.set(value); }
    public void setMarginLeftRight  (int value)       { marginLeftRight.set(value); }
    public void setLinesAbove       (int value)       { linesAbove.set(value); }
    public void setLinesBelow       (int value)       { linesBelow.set(value); }

    public FootnoteStyle() {

        /* DECLARATION */

        alignment = new EnumSetting<Alignment>(Alignment.class);

        firstLine = new DependentNumberSetting() {
            public boolean accept(Integer value) {return value >= 0; }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.LEFT) { return false; }
                return super.enabled();
            }
        };

        runovers  = new DependentNumberSetting() {
            public boolean accept(Integer value) {return value >= 0; }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.LEFT) { return false; }
                return super.enabled();
            }
        };

        marginLeftRight = new DependentNumberSetting() {
            public boolean accept(Integer value) {return value >= 0; }
            @Override
            public boolean enabled() {
                if (getAlignment() != Alignment.CENTERED) { return false; }
                return super.enabled();
            }
        };

        linesAbove = new NumberSetting();
        linesBelow = new NumberSetting();

        /* LINKING */

        alignment.addListener(firstLine);
        alignment.addListener(runovers);
        alignment.addListener(marginLeftRight);

    }
}
