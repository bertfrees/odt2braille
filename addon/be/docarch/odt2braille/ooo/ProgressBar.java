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

package be.docarch.odt2braille.ooo;

import java.util.Locale;

import com.sun.star.task.XStatusIndicator;
import com.sun.star.task.XStatusIndicatorFactory;
import com.sun.star.uno.UnoRuntime;
import com.sun.star.frame.XFrame;

import be.docarch.odt2braille.StatusIndicator;

/**
 * Extension of <code>be.docarch.odt2braille.StatusIndicator</code> that visualizes the progress in a progress bar.
 * The bar is displayed at the bottom of the main OpenOffice.org screen.
 *
 * @see         StatusIndicator
 * @author      Bert Frees
 */
public class ProgressBar extends StatusIndicator {

    private XStatusIndicatorFactory xStatusIndicatorFactory = null;
    private XStatusIndicator xStatusIndicator = null;
    private Locale locale = null;

    /**
     * An <code>XStatusIndicator</code> element is created.
     *
     * @param   xFrame
     * @see     XStatusIndicator
     */
    public ProgressBar (XFrame xFrame,
                        Locale locale) {
        super();

        this.locale = locale;
        // Init Status Indicator
        xStatusIndicatorFactory = (XStatusIndicatorFactory) UnoRuntime.queryInterface(XStatusIndicatorFactory.class, xFrame);
        xStatusIndicator = xStatusIndicatorFactory.createStatusIndicator();

    }

    /**
     * The progress is started.
     * The progress bar is displayed, with progress 0% and no description.
     *
     */
    @Override
    public void start() {

        super.start();
        xStatusIndicator.start("", 100);
    
    }

    /**
     * Update the progress description. This text is displayed at the bottom left corner.
     *
     * @param  status  The new progress description.
     */
    @Override
    public void setStatus(String status) {

        super.setStatus(status);
        xStatusIndicator.setText(status);

    }

    /**
     * Update the progress value.
     *
     * @param value   The new progress value, between 0 and 100.
     */
    @Override
    public void setValue(double value) {

        super.setValue(value);
        xStatusIndicator.setValue((int)this.value);

    }

    /**
     * Reset the status indicator. Clears progress value and description.
     *
     */
    @Override
    public void reset() {

        super.reset();
        xStatusIndicator.reset();

    }

    /**
     * Stop the progress. Only <code>init()</code> can reactivate the status indicator.
     *
     * @return  <code>true</true>
     */
    @Override
    public boolean close() {

        super.close();
        xStatusIndicator.end();
        return true;

    }

    @Override
    public Locale getPreferredLocale() {

        if (locale != null) {
            return locale;
        } else {
            return super.getPreferredLocale();
        }
    }
}
