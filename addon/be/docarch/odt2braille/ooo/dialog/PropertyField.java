package be.docarch.odt2braille.ooo.dialog;

import java.util.Collection;
import java.util.HashSet;
import java.util.EventObject;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.beans.XPropertySet;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.odt2braille.setup.Property;
import be.docarch.odt2braille.setup.PropertyEvent;
import be.docarch.odt2braille.setup.PropertyListener;

public abstract class PropertyField<P extends Property> implements Field,
                                                                   PropertyListener,
                                                                   DialogElementListener {
    protected P property;
    protected final XPropertySet propertySet;
    private final Collection<Integer> hashes = new HashSet<Integer>();

    public PropertyField(XControl control) {

        propertySet = (XPropertySet)UnoRuntime.queryInterface(XPropertySet.class, control.getModel());
        
        try {
            propertySet.setPropertyValue("Enabled", false);
        } catch (UnknownPropertyException e) {
        } catch (PropertyVetoException e) {
        } catch (IllegalArgumentException e) {
        } catch (WrappedTargetException e) {
        }
    }

    public void link(P property) {
        this.property = property;
        listenProperty(property);
        update();
    }

    public void listenProperty(P property) {
        if (property != null) {
            int hash = System.identityHashCode(property);
            if (!hashes.contains(hash)) {
                property.addListener(this);
                hashes.add(hash);
            }
        }
    }
    
    public void propertyUpdated(PropertyEvent event) {
        if (event.getSource() == property) {
            update();
        }
    }

    public void dialogElementUpdated(EventObject event) {}

}
