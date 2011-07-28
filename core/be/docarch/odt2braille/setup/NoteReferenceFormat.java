package be.docarch.odt2braille.setup;

import java.io.Serializable;

public class NoteReferenceFormat implements Serializable {

    /*************
    /* SETTINGS */
    /************/

    public final Setting<Boolean> spaceBefore;
    public final Setting<Boolean> spaceAfter;
    public final Setting<String> prefix;

    /* GETTERS */

    public boolean getSpaceBefore() { return spaceBefore.get(); }
    public boolean getSpaceAfter()  { return spaceAfter.get(); }
    public String  getPrefix()      { return prefix.get(); }

    /* SETTERS */

    public void setSpaceBefore (boolean value) { spaceBefore.set(value); }
    public void setSpaceAfter  (boolean value) { spaceAfter.set(value); }
    public void setPrefix      (String value)  { prefix.set(value); }


    protected NoteReferenceFormat() {

        /***********************
           SETTING DECLARATION
         ***********************/

        spaceBefore = new YesNoSetting();
        spaceAfter = new YesNoSetting();

        prefix = new TextSetting() {
            @Override
            public boolean accept(String value) {
                return value.matches("[\\p{InBraille_Patterns}]+");
            }
        };
    }
}
