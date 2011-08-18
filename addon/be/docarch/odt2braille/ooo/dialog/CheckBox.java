package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XCheckBox;
import com.sun.star.awt.XItemListener;
import com.sun.star.awt.ItemEvent;
import com.sun.star.awt.XControl;

import be.docarch.odt2braille.setup.Setting;

public class CheckBox extends SettingControl<Setting<Boolean>>
                   implements XItemListener {
    
    private final XCheckBox checkbox;
    
    public CheckBox(XControl control) {
        super(control);    
        checkbox = (XCheckBox)UnoRuntime.queryInterface(XCheckBox.class, control);
    }

    public void save() {
        if (property == null) { return; }
        property.set(checkbox.getState()==(short)1);
    }

    public void update() {
        checkbox.setState((short)((property == null) ? 0 : property.get()?1:0));
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.Source.equals(checkbox)) {
            save();
        }
    }
    
    public void listenControl(boolean onOff) {
        if (onOff) {
            checkbox.addItemListener(this);
        } else {
            checkbox.removeItemListener(this);
        }
    }
}
