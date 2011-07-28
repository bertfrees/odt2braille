package be.docarch.odt2braille.ooo.dialog;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;

import com.sun.star.awt.ItemEvent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XItemListener;

import be.docarch.odt2braille.setup.OptionSetting;
import be.docarch.odt2braille.setup.PropertyEvent;

public class ListBox<T> extends SettingControl<OptionSetting<T>>
                     implements XItemListener,
                                Comparator<T> {

    protected final XListBox listbox;
    protected final List<T> options = new ArrayList<T>();
    
    public ListBox(XControl control) {        
        super(control);
        listbox = (XListBox)UnoRuntime.queryInterface(XListBox.class, control);
    }

    protected String getDisplayValue(T value) {
        return String.valueOf(value);
    }

    public int compare(T o1, T o2) {
        return getDisplayValue(o1).compareTo(getDisplayValue(o2));
    }
    
    public void save() {
        if (property == null) { return; }
        if (options.size() == 0) { return; }
        if (listbox.getSelectedItemPos() < 0) { return; }
        property.set(options.get(listbox.getSelectedItemPos()));
    }

    public void update() {
        if (property == null) { return; }
        if (property.get() == null) { return; }
        listbox.selectItem(getDisplayValue(property.get()), true);
    }

    public void updateItems() {
        listbox.removeItems((short)0, Short.MAX_VALUE);
        if (property == null) { return; }
        options.clear();
        options.addAll(property.options());
        Collections.sort(options, this);
        short i = 0;
        for (T t : options) {
            listbox.addItem(getDisplayValue(t), i);
            i++;
        }
    }

    @Override
    public void itemStateChanged(ItemEvent event) {
        if (event.Source.equals(listbox)) {
            save();
        }
    }

    public void listenControl(boolean onOff) {
        if (onOff) {
            listbox.addItemListener(this);
        } else {
            listbox.removeItemListener(this);
        }
    }

    @Override
    public void propertyUpdated(PropertyEvent event) {
        if (event.getSource() == property) {
            listenControl(false);
            if (event.ContextChanged) { updateItems(); }
            update();
            if (event.ContextChanged) { updateProperties(); }
            listenControl(true);
        }
    }

    @Override
    public void link(OptionSetting<T> setting) {
        property = setting;
        listenProperty(property);
        listenControl(false);
        updateItems();
        update();
        updateProperties();
        listenControl(true);
    }
}
