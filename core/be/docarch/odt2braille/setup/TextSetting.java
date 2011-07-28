package be.docarch.odt2braille.setup;


public class TextSetting extends Setting<String> {

    private String text = "";

    public boolean accept(String value) {
        return value != null;
    }

    protected boolean update(String value) {
        if (text.equals(value)) { return false; }
        text = value;
        return true;
    }

    public String get() {
        return text;
    }
}
