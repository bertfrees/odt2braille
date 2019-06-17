/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
        options.addAll(map.keys());
        Collections.sort(options, this);
        key = options.iterator().next();
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
