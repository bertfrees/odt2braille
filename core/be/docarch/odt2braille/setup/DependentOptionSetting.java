package be.docarch.odt2braille.setup;

public abstract class DependentOptionSetting<T> extends OptionSetting<T>
                                             implements Dependent {
    
    public void propertyUpdated(PropertyEvent event) {
        if (event.ValueChanged) {
            fireEvent(refresh(), true);
        }
    }
}
