package be.docarch.odt2braille.ooo.dialog;

import java.util.List;
import java.util.Collection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.EventObject;

import com.sun.star.awt.ItemEvent;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XControl;
import com.sun.star.awt.XListBox;
import com.sun.star.awt.XItemListener;

import be.docarch.odt2braille.setup.SettingMap;

public class MapListBox<K,V> implements DialogElement,
                                        XItemListener,
                                        Comparator<K> {

    private final XListBox listbox;
    protected final SettingMap<K,V> map;
    private final List<K> options = new ArrayList<K>();
    private K key;
    private Collection<DialogElementListener> listeners = new ArrayList<DialogElementListener>();

    public MapListBox(XControl control,
                      SettingMap<K,V> settingMap) {

        listbox = (XListBox)UnoRuntime.queryInterface(XListBox.class, control);
        map = settingMap;
        key = map.keys().iterator().next();
        update();
    }

    public V getSelectedItem() {
        return map.get(key);
    }

    public void update() {
        options.clear();
        options.addAll(map.keys());
        Collections.sort(options, this);
        listenControl(false);
        listbox.removeItems((short)0, Short.MAX_VALUE);
        short i = 0;
        for (K k : options) {
            listbox.addItem(getDisplayValue(k), i);
            i++;
        }
        listbox.selectItem(getDisplayValue(key), true);
        listenControl(true);
    }

    public void itemStateChanged(ItemEvent event) {
        if (event.Source.equals(listbox)) {
            key = options.get(listbox.getSelectedItemPos());
            EventObject eo = new EventObject(this);
            for (DialogElementListener listener : listeners) {
                listener.dialogElementUpdated(eo);
            }
        }
    }

    public void addListener(DialogElementListener listener) {
        listeners.add(listener);
    }

    protected String getDisplayValue(K key) {
        return String.valueOf(key);
    }

    public int compare(K key1, K key2) {
        return getDisplayValue(key1).compareTo(getDisplayValue(key2));
    }

    public void listenControl(boolean onOff) {
        if (onOff) {
            listbox.addItemListener(this);
        } else {
            listbox.removeItemListener(this);
        }
    }

    public void disposing(com.sun.star.lang.EventObject object) {}
}
