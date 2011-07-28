package be.docarch.odt2braille.ooo.dialog;

import com.sun.star.lang.XEventListener;

public interface Control extends Field,
                                 XEventListener {

    public void save();
    public void updateProperties();

}
