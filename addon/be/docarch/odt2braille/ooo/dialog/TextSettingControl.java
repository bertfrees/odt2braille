package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XTextComponent;
import com.sun.star.awt.XWindow;
import com.sun.star.awt.XFocusListener;
import com.sun.star.awt.FocusEvent;

import be.docarch.odt2braille.setup.Setting;

public class TextSettingControl extends SettingControl<Setting<String>>
                             implements XFocusListener {

    private final XTextComponent textField;
    protected final XWindow window;
    
    public TextSettingControl(XControl control) {
        super(control);
        textField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, control);
        window = (XWindow)UnoRuntime.queryInterface(XWindow.class, control);
    }

    public void update() {
        textField.setText((property == null) ? "" : property.get());
    }

    public void save() {
        if (property == null) { return; }
        property.set(textField.getText());
    }

    public void listenControl(boolean onOff) {
        if (onOff) {
            window.addFocusListener(this);
        } else {
            window.removeFocusListener(this);
        }
    }

    public void focusGained(FocusEvent event) {}
    public void focusLost(FocusEvent event) {
        if (event.Source.equals(window)) {
            save();
        }
    }
}
