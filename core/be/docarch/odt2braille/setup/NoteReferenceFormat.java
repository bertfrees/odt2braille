/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
 
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
