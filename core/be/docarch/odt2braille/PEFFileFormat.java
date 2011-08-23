package be.docarch.odt2braille;

import java.io.OutputStream;
import org.daisy.braille.embosser.EmbosserWriter;
import org.daisy.braille.embosser.FileFormat;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableFilter;
import org.daisy.factory.Factory;

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

    @Override
    public TableFilter getTableFilter() {
        return tableFilter;
    }

    @Override
    public boolean supportsTable(Table table) {
        return getTableFilter().accept(table);
    }

    @Override
    public boolean supports8dot() {
        return true;
    }

    @Override
    public boolean supportsDuplex() {
        return true;
    }

    @Override
    public EmbosserWriter newEmbosserWriter(OutputStream os) {
        throw new UnsupportedOperationException("Not supported because convertion of PEF to PEF is trivial.");
    }

    @Override
    public String getFileExtension() {
        return ".pef";
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setFeature(String key, Object value) {
        throw new IllegalArgumentException("Unsupported feature " + key);
    }

    @Override
    public Object getFeature(String key) {
        throw new IllegalArgumentException("Unsupported feature " + key);
    }

    @Override
    public Object getProperty(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public int compareTo(Factory o) {
        if (this.equals(o)) {
            return 0;
        } else {
            return this.getDisplayName().compareTo(o.getDisplayName());
        }
    }
}
