package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.lang.EventObject;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XActionListener;
import com.sun.star.awt.XButton;
import com.sun.star.beans.XPropertySet;

public abstract class Button implements DialogElement,
                                        XActionListener {
    
    protected final XPropertySet propertySet;
    protected final XButton button;
    
    public Button(XControl control,
                  String label) {
    
        button = (XButton)UnoRuntime.queryInterface(XButton.class, control);
        propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());
        button.setLabel(label);
        button.addActionListener(this);
    }

    public void updateProperties() {}
    
    public void disposing(EventObject e) {}
}
