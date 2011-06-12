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

/**
 * Indicate the status and progress of a process.
 * Known extension: <code>be.docarch.odt2braille.ooo.ProgressBar</code>
 *
 * @author      Bert Frees
 */
public class StatusIndicator {

    protected double value;
    private double increment;

    /**
     * Creates a new <code>StatusIndicator</code> instance.
     *
     */
    public StatusIndicator() {
        
        this.value = 0;
        this.increment = 0;

    }

    public void start() {
        reset();
    }

    /**
     * Specify the total number of steps the process will go through.
     *
     * @param steps     Number of steps.
     */
    public void setSteps(int steps) {

        if (steps!=0) {
            increment = (100d/(double)steps);
        } else {
            increment = 0;
        }
    }

    /**
     * Update the progress description.
     *
     * @param status    The description.
     */
    public void setStatus(String status) {}

    /**
     * Update the progress value.
     *
     * @param value    The new value, a number between 0 and 100. 
     */
    protected void setValue(double value) {
        this.value = Math.min(100,Math.max(0,value));
    }

    /**
     * Proceed one step in the process. The progress value is adjusted accordingly.
     *
     */
    public void increment() {
        setValue(value+increment);
    }

    /**
     * Reset the progress value.
     *
     */
    public void reset() {
        setValue(0);
    }

    /**
     * Finish the process. The progress value is set to 100.
     *
     */
    public void finish(boolean succes) {

        if (succes) {
            setValue(100);
        }
    }

    /**
     * @return  <code>true</code>
     */
    public boolean close() {
        return true;
    }
}
