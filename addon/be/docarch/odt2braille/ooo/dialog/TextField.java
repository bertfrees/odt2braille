package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XTextComponent;
import com.sun.star.beans.XPropertySet;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

public abstract class TextField implements Field,
                                           DialogElementListener {

    protected final XTextComponent textField;
    private String text;

    public TextField(XControl control) {
        
        textField = (XTextComponent)UnoRuntime.queryInterface(XTextComponent.class, control);
        XPropertySet propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());

        try {
            propertySet.setPropertyValue("Enabled", false);
        } catch (UnknownPropertyException e) {
        } catch (PropertyVetoException e) {
        } catch (IllegalArgumentException e) {
        } catch (WrappedTargetException e) {
        }
    }

    public void update() {
        textField.setText(text != null ? text : "");
    }

    public void setText(String value) {
        text = value;
        update();
    }
}
