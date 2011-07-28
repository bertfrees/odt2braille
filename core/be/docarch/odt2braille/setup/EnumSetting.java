package be.docarch.odt2braille.setup;

import java.util.Collection;
import java.util.Arrays;

public class EnumSetting<T extends Enum<T>> extends OptionSetting<T> {

    private T value;
    private final Collection<T> options;

    public EnumSetting(Class<T> type) {
        options = Arrays.asList(type.getEnumConstants());
        value = options.iterator().next();
    }

    public Collection<T> options() {
        return options;
    }

    protected boolean update(T value) {
        if (this.value == value) { return false;}
        this.value = value;
        return true;
    }

    public T get() { return value; }
}