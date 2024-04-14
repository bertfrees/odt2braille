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

import org.daisy.braille.utils.impl.provider.BrailleEditorsFileFormatProvider;
import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.factory.FactoryCatalog;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.TableCatalog;

/**
 *
 * @author Bert Frees
 */
public class FileFormatCatalog implements FactoryCatalog<FileFormat> {

    private Map<String, FileFormat> map;

    public FileFormatCatalog() {

        map = new HashMap<String, FileFormat>();

        FileFormat pef = new PEFFileFormat();
        BrailleEditorsFileFormatProvider provider = new BrailleEditorsFileFormatProvider();
        provider.setTableCatalog(TableCatalog.newInstance());

        map.put(pef.getIdentifier(), pef);
        for (FactoryProperties p : provider.list()) {
            map.put(p.getIdentifier(), provider.newFactory(p.getIdentifier()));
        }
    }

    @Override
    public FileFormat get(String identifier) {
        return map.get(identifier);
    }

    public Collection<FileFormat> list() {
        return map.values();
    }
}
