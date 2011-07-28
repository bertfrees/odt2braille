package be.docarch.odt2braille.setup;

import java.util.List;

public abstract class SettingList<V> {

    public abstract V get(int index);

    public abstract List<V> values();

    public abstract V add();

    public abstract V remove(int index);

    public abstract void clear();

    public abstract boolean canAdd();

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof SettingList)) { return false; }
        try {
            SettingList<V> that = (SettingList<V>)object;
            return this.values().equals(that.values());
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() { return values().hashCode(); }

}
