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
