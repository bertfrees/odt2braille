package be.docarch.odt2braille.setup;

import java.util.EventObject;

public class PropertyEvent extends EventObject {

    public final boolean ValueChanged;
    public final boolean ContextChanged;

    public PropertyEvent(Property property,
                         boolean valueChanged,
                         boolean contextChanged) {
        super(property);
        ValueChanged = valueChanged;
        ContextChanged = contextChanged;
    }
}
