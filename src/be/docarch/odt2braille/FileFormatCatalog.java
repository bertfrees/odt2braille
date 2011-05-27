package be.docarch.odt2braille;

import java.util.Collection;
import java.util.Map;
import java.util.HashMap;

import org.daisy.braille.embosser.FileFormat;
import org.daisy.braille.embosser.FileFormatProvider;
import org.daisy.factory.FactoryCatalog;
import org.daisy.factory.FactoryFilter;

import org_daisy.BrailleEditorsFileFormatProvider;

/**
 *
 * @author Bert Frees
 */
public class FileFormatCatalog implements FactoryCatalog<FileFormat> {

    private Map<String, FileFormat> map;

    public FileFormatCatalog() {

        map = new HashMap<String, FileFormat>();

        FileFormat pef = new PEFFileFormat();
        FileFormatProvider provider = new BrailleEditorsFileFormatProvider();

        map.put(pef.getIdentifier(), pef);
        for (FileFormat format : provider.list()) {
            map.put(format.getIdentifier(), format);
        }
    }

    @Override
    public Collection<FileFormat> list() {
        return map.values();
    }

    @Override
    public FileFormat get(String identifier) {
        return map.get(identifier);
    }

    @Override
    public Object getFeature(String key) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void setFeature(String key, Object value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<FileFormat> list(FactoryFilter<FileFormat> filter) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
