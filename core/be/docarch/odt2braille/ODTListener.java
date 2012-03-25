package be.docarch.odt2braille;

import java.util.EventListener;

/**
 *
 * @author Bert Frees
 */
public interface ODTListener extends EventListener {
    
    public void odtUpdated(ODT odt);
    
}
