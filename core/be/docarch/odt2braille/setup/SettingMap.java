package be.docarch.odt2braille.setup;

import java.util.Collection;
import java.io.Serializable;

public abstract class SettingMap<K,V> implements Serializable {
    
    public abstract V get(K key);
    
    public abstract Collection<V> values();
    
    public abstract Collection<K> keys();
    
    protected abstract void add(K key);
    
  //protected abstract void remove(K key);
    
}