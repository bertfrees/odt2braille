package be.docarch.odt2braille.setup;

import java.util.EventListener;

public interface SettingMapListener extends EventListener {
    
    public void mapUpdated(SettingMap<?,?> map);
    
}
