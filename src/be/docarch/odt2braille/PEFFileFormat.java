package be.docarch.odt2braille;

import java.io.OutputStream;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.FileFormat;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableFilter;

/**
 *
 * @author Bert Frees
 */
public class PEFFileFormat implements FileFormat {

    private TableFilter tableFilter;
    private String identifier = "be.docarch.odt2braille.PEFFileFormat";
    private static String displayName = "PEF (Portable Embosser Format)";
    private static String description = "";

    public PEFFileFormat() {

        this.tableFilter = new TableFilter() {
            @Override
            public boolean accept(Table object) {
                return false;
            }
        };
    }

    public TableFilter getTableFilter() {
        return tableFilter;
    }

    public boolean supportsTable(Table table) {
        return getTableFilter().accept(table);
    }

    public boolean supports8dot() {
        return true;
    }

    public boolean supportsDuplex() {
        return true;
    }

    public EmbosserWriter newEmbosserWriter(OutputStream os) {
        throw new UnsupportedOperationException("Not supported because convertion of PEF to PEF is trivial.");
    }

    public String getFileExtension() {
        return ".pef";
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
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
