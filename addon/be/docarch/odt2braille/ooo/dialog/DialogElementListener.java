package be.docarch.odt2braille.ooo.dialog;

import java.util.EventListener;
import java.util.EventObject;

public interface DialogElementListener extends EventListener {

    public void dialogElementUpdated(EventObject event);

}
