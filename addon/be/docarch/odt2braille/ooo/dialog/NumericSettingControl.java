package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.TextEvent;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XNumericField;
import com.sun.star.awt.XTextListener;

import be.docarch.odt2braille.setup.Setting;

public class NumericSettingControl extends SettingControl<Setting<Integer>>
                         implements XTextListener {

    protected final XNumericField numericField;
    private final XTextComponent textComponent;
    
    public NumericSettingControl(XControl control) {
        super(control);
        numericField = (XNumericField)UnoRuntime.queryInterface(XNumericField.class, control);
        textComponent = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, control);
        numericField.setDecimalDigits((short)0);
        numericField.setMin((double)0);
        numericField.setMax((double)Integer.MAX_VALUE);
    }
    
    public void save() {
        if (property == null) { return; }
        property.set((int)numericField.getValue());
    }

    public void update() {
        if (property == null) { return; }
        numericField.setValue((double)property.get());
    }

    public void textChanged(TextEvent event) {
        if (event.Source.equals(numericField)) {
            save();
            update();
        }
    }

    public void listenControl(boolean onOff) {
        if (onOff) {
            textComponent.addTextListener(this);
        } else {
            textComponent.removeTextListener(this);
        }
    }
}
