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

package be.docarch.odt2braille.paper;

import be.docarch.odt2braille.paper.CustomPaperProvider.PaperType;

import org.daisy.braille.tools.Length;
import org.daisy.paper.SheetPaper;

public class CustomSheetPaper extends SheetPaper {

    private Length width;
    private Length height;

    public CustomSheetPaper(String name, String desc) {
        this(name, desc, Length.newMillimeterValue(210d), Length.newMillimeterValue(297d));
    }

    public CustomSheetPaper(String name, String desc, Length width, Length height) {
        super(name, desc, PaperType.SHEET, width, height);
        this.width = width;
        this.height = height;
    }

    public void setPageWidth(Length width) {
        this.width = width;
    }

    public void setPageHeight(Length height) {
        this.height = height;
    }

    @Override
    public Length getPageHeight() {
        return height;
    }

    @Override
    public Length getPageWidth() {
        return width;
    }
}