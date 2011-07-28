package be.docarch.odt2braille.setup;

import java.util.EventListener;

public interface PropertyListener extends EventListener {
    
    public void propertyUpdated(PropertyEvent event);
    
}
