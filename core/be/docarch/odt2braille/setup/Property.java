package be.docarch.odt2braille.setup;

import java.util.Collection;
import java.util.ArrayList;

public abstract class Property<T> {

    private Collection<PropertyListener> listeners;
    
    public abstract T get();
    
    public void addListener(PropertyListener listener) {
        
        if (listeners == null) { listeners = new ArrayList<PropertyListener>(); }
        listeners.add(listener);
    }
    
    protected void fireEvent(boolean valueChanged, boolean contextChanged) {
        if (listeners == null) { return; }
        if (valueChanged || contextChanged) {
            PropertyEvent event = new PropertyEvent(this, valueChanged, contextChanged);
            for (PropertyListener listener : listeners) {
                listener.propertyUpdated(event);
            }
        }
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof Property)) { return false; }
        try {
            Property<T> that = (Property<T>)object;
            return this.get() == null ? that.get() == null : this.get().equals(that.get());
        } catch (ClassCastException e) {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return get().hashCode();
    }
}
