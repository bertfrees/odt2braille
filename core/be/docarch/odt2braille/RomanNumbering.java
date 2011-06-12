/**
 *  odt2daisy - OpenDocument to DAISY XML/Audio
 *
 *  (c) Copyright 2008 - 2009 by Vincent Spiewak, All Rights Reserved.
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Lesser Public License as published by
 *  the Free Software Foundation; either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License along
 *  with this program; if not, write to the Free Software Foundation, Inc.,
 *  51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */
package be.docarch.odt2braille;

/**
 * 
 * @author Vincent Spiewak
 */
public class RomanNumbering {

    private static final String[] ROMAN_NUMS = {"m", "cm", "d", "cd", "c", "xc", "l",
        "xl", "x", "ix", "v", "iv", "i"
    };
    private static final int[] NUMRERAL_NUMS = {1000, 900, 500, 400, 100, 90, 50,
        40, 10, 9, 5, 4, 1
    };

    public static String toRoman(int number) {
        if (number <= 0 || number >= 4000) {
            throw new NumberFormatException("Value outside roman numeral range.");
        }

        // Roman notation will be accumualated here.
        String roman = "";

        for (int i = 0; i < ROMAN_NUMS.length; i++) {
            while (number >= NUMRERAL_NUMS[i]) {
                number -= NUMRERAL_NUMS[i];
                roman += ROMAN_NUMS[i];
            }
        }
        return roman;
    }
}
