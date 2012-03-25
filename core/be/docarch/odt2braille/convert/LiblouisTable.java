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

package be.docarch.odt2braille.convert;

import java.nio.charset.Charset;
import org.daisy.braille.table.AbstractTable;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;

/**
 *
 * @author Bert Frees
 */
public class LiblouisTable extends AbstractTable {

    public LiblouisTable() {
        super("Liblouis", "Liblouis", "be.docarch.odt2braille.LiblouisTable.TableType.LIBLOUIS");
    }

    public BrailleConverter newBrailleConverter() {
        StringBuilder sb = new StringBuilder(" a1b'k2l`cif/msp\"e3h9o6r~djg>ntq,*5<-u8v.%{$+x!&;:4|0z7(_?w}#y)=");
        for (int i=64; i<256; i++) {
                sb.append((char)(0x2800+i));
        }
        return new EmbosserBrailleConverter(sb.toString(), Charset.forName("UTF-8"), EightDotFallbackMethod.values()[0], '\u2800', false);
    }

    public Object getProperty(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Object getFeature(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setFeature(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
