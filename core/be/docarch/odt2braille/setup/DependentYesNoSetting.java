package be.docarch.odt2braille.setup;

public abstract class DependentYesNoSetting extends YesNoSetting
                                            implements Dependent {
    @Override
    public abstract boolean accept(Boolean value);
    
    public boolean refresh() {
        if (accept(get())) { return false; }
        return update(!yesNo);
    }

    public void propertyUpdated(PropertyEvent event) {
        if (event.ValueChanged) {
            fireEvent(refresh(), true);
        }
    }
}
