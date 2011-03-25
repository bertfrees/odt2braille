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
import java.util.List;


/**
 *
 * @author freesb
 */
public class Volume {

    public enum Type { PRELIMINARY,
                       NORMAL,
                       SUPPLEMENTARY,
                       SINGLE };

    private static boolean frontMatterAvailable = false;
    private static boolean extFrontMatterAvailable = false;

    private static List<Volume> volumes = new ArrayList<Volume>();

    private Type type;
    private String id;
    private String title;
    private int braillePagesStart;
    private int numberOfBraillePages;
    private int numberOfPreliminaryPages;
    private boolean frontMatter;
    private boolean extFrontMatter;
    private boolean transcribersNotesPage;
    private boolean specialSymbolsList;
    private boolean toc;
    private boolean extToc;
    private String firstPrintPage;
    private String lastPrintPage;
    private List<Boolean> specialSymbolsPresent;
    private List<Boolean> transcribersNotesEnabled;

    public static void init() {
        volumes.clear();
    }

    public Volume(Volume copyVolume) {

        this.type = copyVolume.type;
        this.title = new String(copyVolume.title);
        this.braillePagesStart = copyVolume.braillePagesStart;
        this.numberOfBraillePages = copyVolume.numberOfBraillePages;
        this.numberOfPreliminaryPages = copyVolume.numberOfPreliminaryPages;
        this.frontMatter = copyVolume.frontMatter;
        this.extFrontMatter = copyVolume.extFrontMatter;
        this.transcribersNotesPage = copyVolume.transcribersNotesPage;
        this.specialSymbolsList = copyVolume.specialSymbolsList;
        this.toc = copyVolume.toc;
        this.extToc = copyVolume.extToc;
        this.firstPrintPage = null;
        this.lastPrintPage = null;
        this.specialSymbolsPresent = null;
        this.transcribersNotesEnabled = null;

        if (copyVolume.id != null)                       { this.id = new String(copyVolume.id); }
        if (copyVolume.firstPrintPage != null)           { this.firstPrintPage = new String(copyVolume.firstPrintPage); }
        if (copyVolume.lastPrintPage != null)            { this.lastPrintPage = new String(copyVolume.lastPrintPage); }
        if (copyVolume.specialSymbolsPresent != null)    { this.specialSymbolsPresent = new ArrayList(copyVolume.specialSymbolsPresent); }
        if (copyVolume.transcribersNotesEnabled != null) { this.transcribersNotesEnabled = new ArrayList(copyVolume.transcribersNotesEnabled); }
        
        volumes.add(this);
    }

    public Volume(Type type,
                  String id) {

        this.type = type;

        this.title = "";
        this.braillePagesStart = 1;
        this.numberOfBraillePages = 0;
        this.numberOfPreliminaryPages = 0;
        this.frontMatter = false;
        this.extFrontMatter = false;
        this.toc = false;
        this.extToc = false;
        this.transcribersNotesPage = false;
        this.specialSymbolsList = false;
        this.firstPrintPage = null;
        this.lastPrintPage = null;
        this.specialSymbolsPresent = null;
        this.transcribersNotesEnabled = null;

        setFrontMatter(true);
        setToc(true);

        switch (type) {
            case PRELIMINARY:
                setExtFrontMatter(true);
                setExtToc(true);
                this.id = "";
                break;
            case SINGLE:
                this.id = "";
                break;
            default:
                if (id==null) {
                    this.id = "";
                } else {
                    this.id = id;
                }
        }

        volumes.add(this);
    }

    public static void setFrontMatterAvailable(boolean frontMatterAvailable) {

        Volume.frontMatterAvailable = frontMatterAvailable;
        for (Volume volume : volumes) {
            volume.setFrontMatter(volume.frontMatter);
        }
        setExtFrontMatterAvailable(extFrontMatterAvailable);
    }

    public static void setExtFrontMatterAvailable(boolean extFrontMatterAvailable) {

        Volume.extFrontMatterAvailable = extFrontMatterAvailable && frontMatterAvailable;
        for (Volume volume : volumes) {
            volume.setExtFrontMatter(volume.extFrontMatter);
        }
    }

    public void setTitle                    (String title)                  { this.title = title; }
    public void setFrontMatter              (boolean frontMatter)           { this.frontMatter = (frontMatterAvailable && frontMatter) || (type == Type.PRELIMINARY);
                                                                              setExtFrontMatter(extFrontMatter); }
    public void setExtFrontMatter           (boolean extFrontMatter)        { this.extFrontMatter = extFrontMatterAvailable && extFrontMatter && frontMatter; }
    public void setTranscribersNotesPage    (boolean transcribersNotesPage) { this.transcribersNotesPage = transcribersNotesPage; }
    public void setSpecialSymbolsList       (boolean specialSymbolsList)    { this.specialSymbolsList = specialSymbolsList; }
    public void setToc                      (boolean toc)                   { this.toc = toc;
                                                                              setExtToc(extToc); }
    public void setExtToc                   (boolean extToc)                { this.extToc = extToc && toc; }
    public void setBraillePagesStart        (int braillePagesStart)         { this.braillePagesStart = braillePagesStart; }
    public void setNumberOfBraillePages     (int numberOfBraillePages)      { this.numberOfBraillePages = numberOfBraillePages; }
    public void setNumberOfPreliminaryPages (int number)                    { this.numberOfPreliminaryPages = number; }
    public void setFirstPrintPage           (String firstPrintPage)         { this.firstPrintPage = firstPrintPage; }
    public void setLastPrintPage            (String lastPrintPage)          { this.lastPrintPage = lastPrintPage; }

    public void setSpecialSymbolsPresent(List<Boolean> specialSymbolsPresent) {
        this.specialSymbolsPresent = new ArrayList(specialSymbolsPresent);
    }

    public void setTranscribersNotesEnabled(List<Boolean> transcribersNotesEnabled) {
        this.transcribersNotesEnabled = new ArrayList(transcribersNotesEnabled);
    }

    public Type            getType()                      { return type; }
    public String          getIdentifier()                { return id; }
    public String          getTitle()                     { return title; }
    public boolean         getFrontMatter()               { return frontMatter; }
    public boolean         getExtFrontMatter()            { return extFrontMatter; }
    public boolean         getToc()                       { return toc; }
    public boolean         getExtToc()                    { return extToc; }
    public boolean         getTranscribersNotesPage()     { return transcribersNotesPage; }
    public boolean         getSpecialSymbolsList()        { return specialSymbolsList; }
    public int             getFirstBraillePage()          { return braillePagesStart; }
    public int             getLastBraillePage()           { return braillePagesStart + numberOfBraillePages - 1; }
    public int             getNumberOfPreliminaryPages()  { return numberOfPreliminaryPages; }
    public String          getFirstPrintPage()            { return firstPrintPage; }
    public String          getLastPrintPage()             { return lastPrintPage; }
    public List<Boolean>   getSpecialSymbolsPresent()     { return specialSymbolsPresent; }
    public List<Boolean>   getTranscribersNotesEnabled()  { return transcribersNotesEnabled; }

}
