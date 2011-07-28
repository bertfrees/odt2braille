package be.docarch.odt2braille.setup;

public class NumberSetting extends Setting<Integer> {

    protected int number = 0;
    
    public boolean accept(Integer value) {
        return value >= 0;
    }

    protected boolean update(Integer value) {
        if (value == number) { return false; }
        number = value;
        return true;
    }

    public Integer get() {
        return number;
    }

    public void increase() {
        set(get()+1);
    }

    public void descrease() {
        set(get()-1);
    }
}
