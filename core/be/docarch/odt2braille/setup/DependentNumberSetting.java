package be.docarch.odt2braille.setup;

public abstract class DependentNumberSetting extends NumberSetting
                                             implements Dependent {

    @Override
    public abstract boolean accept(Integer value);

    public boolean refresh() {
        if (accept(get())) { return false; }
        return update(0);
    }
    
    public void propertyUpdated(PropertyEvent event) {
        if (event.ValueChanged) {
            fireEvent(refresh(), true);
        }
    }
}
