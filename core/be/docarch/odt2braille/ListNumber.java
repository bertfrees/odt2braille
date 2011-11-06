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
 * A <code>ListNumber</code> instance keeps track of the numbering of an OpenOffice.org list (or the sequence of OpenOffice.org headings).
 *
 * The current level as well as the current number in each level is kept and
 * updated whenever <code>reset</code>, <code>restart</code> or <code>update</code> is called.
 *
 * @author freesb
 */
public class ListNumber {

    private int level;
    private int[] num;
    private int[] startValue;
    private ListStyleProperties properties;

    /**
     * Creates and initializes a new <code>ListNumber</code> instance.
     * The level is set to zero and the number in each level is set to the corresponding start value minus 1.
     *
     * @param properties   The properties of the list.
     */
    public ListNumber(ListStyleProperties properties) {

        level = 0;
        num = new int[10];
        this.properties = properties;
        startValue = properties.getStartValue().clone();

        for (int i=1;i<=10;i++) {
            num[i-1] = startValue[i-1] - 1;
        }
    }

    /**
     * The current level is set to <code>newLevel</code> and
     * the numbers of all higher levels are set to the corresponding start value minus 1.
     *
     * @param newLevel   The new level.
     */
    public void reset(int newLevel) {

        level = newLevel;
        for (int i=level+1;i<=10;i++) {
            num[i-1] = startValue[i-1] - 1;
        }
    }

    /**
     * The numbering of a certain level is restarted.
     *
     * @param   level
     * @param   num     If <code>num</code> is equal to <code>-1</code>, the number of the specified level is set to the corresponding start value minus 1.
     *                  Else, the number is set to <code>num - 1</code>.
     */
    public void restart(int level,
                        int num) {

        if (num == -1) {
            this.num[level-1] = startValue[level-1] - 1;
        } else {
            this.num[level-1] = num - 1;
        }
    }

    /**
     * The current level is set to <code>newLevel</code> and the numbering is updated accordingly.
     *
     * @param newLevel   The new level.
     */
    public void update(int newLevel) {

        for (int i=level+1;i<newLevel;i++) {
            num[i-1]++;
        }
        num[newLevel-1]++;
        reset(newLevel);

    }

    /**
     * @return   The current level.
     */
    public int getLevel() {
        return level;
    }

    /**
     * @param     level
     * @return    The current numbers in the specified level.
     */
    public int getNumber(int level) {
        return num[level-1];
    }

    /**
     * @return   The properties of the list.
     */
    public ListStyleProperties getProperties() {
        return properties;
    }
}
