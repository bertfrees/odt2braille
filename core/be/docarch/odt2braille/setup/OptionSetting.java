package be.docarch.odt2braille.setup;

import java.util.Collection;

public abstract class OptionSetting<T> extends Setting<T> {
    
    public abstract Collection<T> options();
    
    public boolean accept(T value) {
        return options().contains(value);
    }

    @Override
    public boolean enabled() {
        if (options().size() < 1) { return false; }
        return super.enabled();
    }
}
