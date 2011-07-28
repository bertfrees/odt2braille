package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.uno.UnoRuntime;
import com.sun.star.awt.XFixedText;
import com.sun.star.awt.XControl;

public class Label implements DialogElement {
    
    public final XFixedText label;
    
    public Label(XControl control,
                 String text) {
        
        label = (XFixedText)UnoRuntime.queryInterface(XFixedText.class, control);
        label.setText(text);
    }
}
