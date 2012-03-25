package be.docarch.odt2braille.setup;

import java.util.EventListener;

public interface SettingListListener extends EventListener {
    
    public void listUpdated(SettingList<?> list);
    
}
