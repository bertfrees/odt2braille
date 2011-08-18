package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.awt.ItemEvent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XRadioButton;
import com.sun.star.awt.XItemListener;

import com.sun.star.lang.IllegalArgumentException;
import com.sun.star.lang.WrappedTargetException;
import com.sun.star.beans.UnknownPropertyException;
import com.sun.star.beans.PropertyVetoException;

import be.docarch.odt2braille.setup.Setting;

public class RadioButton<T> extends SettingControl<Setting<T>>
                         implements XItemListener {

    protected final XRadioButton radiobutton;
    private T condition;

    public RadioButton(XControl control) {
        super(control);
        radiobutton = (XRadioButton)UnoRuntime.queryInterface(XRadioButton.class, control);
    }

    public void setCondition(T condition) {
        this.condition = condition;
    }

    public void update() {
        radiobutton.setState((property == null || condition == null) ? false : property.get().equals(condition));
    }

    public void save() {
        if (property == null || condition == null) { return; }
        if (radiobutton.getState()) {
            property.set(condition);
        }
    }

    @Override
    public void updateProperties() {
        try {
            propertySet.setPropertyValue("Enabled",
                    (property == null || condition == null) ? false : property.enabled() && property.accept(condition));
        } catch (UnknownPropertyException e) {
        } catch (PropertyVetoException e) {
        } catch (IllegalArgumentException e) {
        } catch (WrappedTargetException e) {
        }
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.Source.equals(radiobutton)) {
            save();
        }
    }

    public void listenControl(boolean onOff) {
        if (onOff) {
            radiobutton.addItemListener(this);
        } else {
            radiobutton.removeItemListener(this);
        }
    }
}
