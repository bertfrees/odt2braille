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

package be.docarch.odt2braille.ooo.check;

import java.util.Collection;
import java.util.HashSet;

import be.docarch.odt2braille.check.Check;
//import be.docarch.accessibility.Check;
//import be.docarch.accessibility.Issue;
//import be.docarch.accessibility.Repairer;

/**
 *
 * @author Bert Frees
 */
public class BrailleRepairer /* implements Repairer */ {

    private Collection<Check> supportedChecks;

    public BrailleRepairer() {

        supportedChecks = new HashSet<Check>();
        // ...
    }

 /* @Override
    public String getIdentifier() {
        return "be.docarch.odt2braille.ooo.checker.BrailleRepairer";
    }

    @Override
    public boolean supports(Check check) {
        return supportedChecks.contains(check);
    }

    @Override
    public RepairMode getRepairMode(Check check)
                             throws IllegalArgumentException {

        if (supports(check)) {
            // ...
        }

        throw new java.lang.IllegalArgumentException("Check is not supported");
    }

    @Override
    public boolean repair(Issue issue) {

        // ...

        return false;
    } */
}
