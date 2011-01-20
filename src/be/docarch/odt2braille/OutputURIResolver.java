/**
 *  odt2braille - Braille authoring in OpenOffice.org.
 *
 *  Copyright (c) 2010 by DocArch <http://www.docarch.be>.
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

import javax.xml.transform.Result;
import net.sf.saxon.event.StandardOutputResolver;
import net.sf.saxon.trans.XPathException;


/**
 *
 * @author Bert Frees
 */
public class OutputURIResolver implements net.sf.saxon.OutputURIResolver {

    private StandardOutputResolver resolver;

    public OutputURIResolver () {
        resolver = StandardOutputResolver.getInstance();
    }

    @Override
    public Result resolve(String href,
                          String base)
                   throws XPathException {

        if (href.contains("#")) {
            href = href.substring(0, href.lastIndexOf("#"));
        }
        return resolver.resolve(href, base);
    }

    @Override
    public void close(Result result)
               throws XPathException {

        resolver.close(result);
    }
}
