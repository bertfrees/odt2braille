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

/**
 * Exception that is called when the <code>xml2brl</code> program returns an error or doesn't terminate correctly.
 *
 * @author  Bert Frees
 */
public class LiblouisXMLException extends Exception {

    private String error = null;

    public LiblouisXMLException() {
        super();
        this.error = "liblouis exception";
    }

    public LiblouisXMLException(String error) {
        super(error);
        this.error = error;
    }

    public String getError() {
        return error;
    }
}
