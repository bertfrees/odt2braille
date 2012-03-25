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
 * @author Vincent Spiewak
 */
public class LetterNumbering {

    private static final String[] LETTER_NUMS = { "a", "b", "c", "d", "e", "f", "g", "h", "i",
        "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"
    };


    /**
     * Convert number to letter in base 26
     *
     * ex: A, B, ..., Z,
     *     AA, AB, ..., AZ,
     *     BA, BB, BC ... BZ,
     *     ...,
     *     ZA, ZB, ZC ... ZZ,
     *     AAA, AAB,..., AAZ,
     *     ABA, ABB, ABC, ... ABZ,
     *     ...
     *
     * @param number (MUST be > 1)
     * @return letter
     *
     */
    public static String toLetter(int n){
        if(n<1) 
            return null;
        else
            return toLetterSub(n-1);
    }

    private static String toLetterSub(int n) {

        int r = n % 26;
        String result = "";

        if (n - r == 0) {
            result = LETTER_NUMS[n];
        } else {
            result = toLetterSub(((n - r) - 1) / 26) + LETTER_NUMS[r];
        }

        return result;
    }
}
