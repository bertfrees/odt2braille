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

//import be.docarch.odt2braille.setup.Configuration.PageNumberPosition;
//import be.docarch.odt2braille.setup.Configuration.PageNumberFormat;
//import be.docarch.odt2braille.setup.style.ParagraphStyle;
//import be.docarch.odt2braille.setup.style.CharacterStyle;
//import be.docarch.odt2braille.setup.style.HeadingStyle;
//import be.docarch.odt2braille.setup.style.TableStyle;
//import be.docarch.odt2braille.setup.style.ListStyle;
//import be.docarch.odt2braille.setup.style.TocStyle;
//import be.docarch.odt2braille.setup.style.FrameStyle;
//import be.docarch.odt2braille.setup.style.FootnoteStyle;
//import be.docarch.odt2braille.setup.style.PictureStyle;
//import be.docarch.odt2braille.setup.style.Style.Alignment;

public class UEBCFormattingRules implements FormattingRules {

    public String getName() {
        return "UEBC (ICEB)";
    }

    public String getDescription() {
        return "Unified English Braille Code (International Council on English Braille)";
    };

    public void applyTo(Configuration configuration) {

        
    }
}
