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

package be.docarch.odt2braille.utils;

/**
 *
 * @author Bert Frees
 */
public class NumberFormatter {
    
    public static String format(int number, String numberFormat) {
        if ("i".equals(numberFormat)) {
            return RomanNumbering.toRoman(number);
        } else if ("I".equals(numberFormat)) {
            return RomanNumbering.toRoman(number).toUpperCase();
        } else if ("a".equals(numberFormat)) {
            return LetterNumbering.toLetter(number);
        } else if ("A".equals(numberFormat)) {
            return LetterNumbering.toLetter(number).toUpperCase();
        } else {
            return String.valueOf(number);
        }
    }
}