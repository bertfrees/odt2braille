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

package be.docarch.odt2braille;

import org.daisy.dotify.api.paper.Length;
import org.daisy.dotify.api.paper.RollPaper;

import be.docarch.odt2braille.CustomPaperProvider.PaperType;

public class CustomRollPaper extends RollPaper {

    private Length across;

    public CustomRollPaper(String name, String desc) {
        super(name, desc, PaperType.ROLL.toString(), Length.newMillimeterValue(297d));
        across = super.getLengthAcrossFeed();
    }

    public void setLengthAcrossFeed(Length across) {
        this.across = across;
    }

    @Override
    public Length getLengthAcrossFeed() {
        return across;
    }
}