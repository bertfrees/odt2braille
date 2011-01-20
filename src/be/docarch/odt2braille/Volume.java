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

import java.util.ArrayList;


/**
 *
 * @author freesb
 */
public class Volume {

    public enum VolumeType { NORMAL,
                             PRELIMINARY,
                             SUPPLEMENTARY };

    private VolumeType type;
    private int number;
    private int firstBraillePage;
    private int lastBraillePage;
    private int numberOfPreliminaryPages;
    private String firstPrintPage;
    private String lastPrintPage;
    private ArrayList<Boolean> specialSymbolsPresent;
    private ArrayList<Boolean> transcribersNotesEnabled;

    public Volume(VolumeType type,
                  int number) {

        this.type = type;
        this.number = number;
        this.firstBraillePage = 0;
        this.lastBraillePage = 0;
        this.numberOfPreliminaryPages = 0;
        this.firstPrintPage = null;
        this.lastPrintPage = null;
        this.specialSymbolsPresent = null;
        this.transcribersNotesEnabled = null;

    }

    public VolumeType         getType()                      { return type; }
    public int                getNumber()                    { return number; }
    public int                getFirstBraillePage()          { return firstBraillePage; }
    public int                getLastBraillePage()           { return lastBraillePage; }
    public int                getNumberOfPreliminaryPages()  { return numberOfPreliminaryPages; }
    public String             getFirstPrintPage()            { return firstPrintPage; }
    public String             getLastPrintPage()             { return lastPrintPage; }
    public ArrayList<Boolean> getSpecialSymbolsPresent()     { return specialSymbolsPresent; }
    public ArrayList<Boolean> getTranscribersNotesEnabled()  { return transcribersNotesEnabled; }

    public void setFirstBraillePage         (int firstBraillePage)  { this.firstBraillePage = firstBraillePage; }
    public void setLastBraillePage          (int lastBraillePage)   { this.lastBraillePage = lastBraillePage; }
    public void setNumberOfPreliminaryPages (int number)            { this.numberOfPreliminaryPages = number; }
    public void setFirstPrintPage           (String firstPrintPage) { this.firstPrintPage = firstPrintPage; }
    public void setLastPrintPage            (String lastPrintPage)  { this.lastPrintPage = lastPrintPage; }
    public void setSpecialSymbolsPresent    (ArrayList<Boolean> specialSymbolsPresent) {
        this.specialSymbolsPresent = new ArrayList(specialSymbolsPresent);
    }
    public void setTranscribersNotesEnabled (ArrayList<Boolean> transcribersNotesEnabled) {
        this.transcribersNotesEnabled = new ArrayList(transcribersNotesEnabled);
    }
}
