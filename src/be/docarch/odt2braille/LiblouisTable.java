package be.docarch.odt2braille;

import java.nio.charset.Charset;

import org.daisy.braille.table.Table;
import org.daisy.braille.table.BrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter;
import org.daisy.braille.table.EmbosserBrailleConverter.EightDotFallbackMethod;

/**
 *
 * @author Bert Frees
 */
public class LiblouisTable implements Table {

    private static final String identifier = "be.docarch.odt2braille.LiblouisTable.TableType.LIBLOUIS";

    public BrailleConverter newBrailleConverter() {
        StringBuilder sb = new StringBuilder(" a1b'k2l`cif/msp\"e3h9o6r~djg>ntq,*5<-u8v.%{$+x!&;:4|0z7(_?w}#y)=");
        for (int i=64; i<256; i++) {
                sb.append((char)(0x2800+i));
        }
        return new EmbosserBrailleConverter(sb.toString(), Charset.forName("UTF-8"), EightDotFallbackMethod.values()[0], '\u2800', false);
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplayName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public String getDescription() {
        throw new UnsupportedOperationException("Not supported yet.");
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
