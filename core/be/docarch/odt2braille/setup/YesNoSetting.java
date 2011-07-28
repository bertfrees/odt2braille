package be.docarch.odt2braille.setup;

public class YesNoSetting extends Setting<Boolean> {

    protected Boolean yesNo = false;

    public boolean accept(Boolean value) {
        return true;
    }

    public Boolean get() {
        return yesNo;
    }

    protected boolean update(Boolean value) {
        if (yesNo==value) { return false; }
        yesNo = value;
        return true;
    }

    @Override
    public boolean enabled() {
        if (!accept(false)) { return false; }
        if (!accept(true)) { return false; }
        return super.enabled();
    }
}