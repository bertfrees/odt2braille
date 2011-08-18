package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XNumericField;

import be.docarch.odt2braille.setup.Property;

public class NumericPropertyField extends PropertyField<Property<Integer>> {

    private final XNumericField numericField;
    
    public NumericPropertyField(XControl control) {

        super(control);
        numericField = (XNumericField)UnoRuntime.queryInterface(XNumericField.class, control);

        numericField.setDecimalDigits((short)0);
        numericField.setMin((double)0);
        numericField.setMax((double)Integer.MAX_VALUE);
    }

    public void update() {
        numericField.setValue((double)((property == null) ? 0 : property.get()));
    }
}
