package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XTextComponent;

import be.docarch.odt2braille.setup.Property;

public class TextPropertyField<T> extends PropertyField<Property<T>> {

    private final XTextComponent textField;
    
    public TextPropertyField(XControl control) {
        super(control);
        textField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, control);
    }

    protected String getDisplayValue(T value) {
        return String.valueOf(value);
    }

    public void update() {
        textField.setText(getDisplayValue(property.get()));
    }
}
