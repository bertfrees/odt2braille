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

import java.util.ArrayList;
import java.util.Collection;
import org.daisy.paper.Paper;
import org.daisy.paper.PaperProvider;

/**
 *
 * @author Bert Frees
 */
public class CustomPaperProvider implements PaperProvider {

    enum PaperType {
        SHEET,
        TRACTOR,
        ROLL
        // FIXME: partly reverted r186
        //, ROWS_COLUMNS
    };

    private final ArrayList<Paper> papers;

    public CustomPaperProvider() {
        papers = new ArrayList<Paper>();
        papers.add(new CustomSheetPaper("Custom sheet...", "Sheet paper with adjustable width and height"));
        papers.add(new CustomTractorPaper("Custom tractor paper...", "Tractor paper with adjustable width and height"));
        papers.add(new CustomRollPaper("Custom roll...", "Roll paper with adjustable width"));
        // FIXME: partly reverted r186
        // papers.add(new CustomRowsColumnsPaper("Custom ...", "Paper with adjustable rows and columns (intended for generic embosser)"));
    }

    public Collection<Paper> list() {
        return papers;
    }
}
