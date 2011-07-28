package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.beans.XPropertySet;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

public class FixedLine implements DialogElement {

    public FixedLine(XControl control,
                     String label) {

        XPropertySet propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());
        
        try {
            propertySet.setPropertyValue("Label", label);
        } catch (UnknownPropertyException e) {
        } catch (PropertyVetoException e) {
        } catch (IllegalArgumentException e) {
        } catch (WrappedTargetException e) {
        }
    }
}
