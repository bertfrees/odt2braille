package be.docarch.odt2braille.setup;

public abstract class Setting<T> extends Property<T> {

    private boolean locked = false;

    public abstract boolean accept(T value);

    protected abstract boolean update(T value);

    public void set(T value) {
        if (!enabled()) { return; }
        if (!accept(value)) { return; }
        if (!update(value)) { return; }
        fireEvent(true, false);
    }

    public boolean enabled() {
        return !locked;
    }
    
    protected void lock() {
        locked = true;
    }
}
