package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;

import be.docarch.odt2braille.setup.Setting;

public abstract class SettingButton<S extends Setting> extends SettingControl<S>
                                                    implements XActionListener {

    protected final XButton button;
    
    public SettingButton(XControl control,
                         String label) {

        super(control);
        button = (XButton)UnoRuntime.queryInterface(XButton.class, control);
        button.setLabel(label);
        button.addActionListener(this);
    }

    public void update() {}
    public void save() {}
    public void listenControl(boolean onOff) {}
}
