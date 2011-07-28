package be.docarch.odt2braille.setup;


public abstract class DependentTextSetting extends TextSetting
                                        implements Dependent {

    public void propertyUpdated(PropertyEvent event) {
        if (event.ValueChanged) {
            fireEvent(refresh(), true);
        }
    }
}
