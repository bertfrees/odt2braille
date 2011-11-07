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

import org.daisy.braille.tools.Length;
import org.daisy.paper.TractorPaper;
import be.docarch.odt2braille.CustomPaperProvider.PaperType;

public class CustomTractorPaper extends TractorPaper {

    private Length across;
    private Length along;

    public CustomTractorPaper(String name, String desc) {
        super(name, desc, PaperType.TRACTOR, Length.newMillimeterValue(210d), Length.newInchValue(11d));
        across = super.getLengthAcrossFeed();
        along = super.getLengthAlongFeed();
    }

    public void setLengthAcrossFeed(Length across) {
        this.across = across;
    }

    public void setLengthAlongFeed(Length along) {
        this.along = along;
    }

    @Override
    public Length getLengthAcrossFeed() {
        return across;
    }

    @Override
    public Length getLengthAlongFeed() {
        return along;
    }
}