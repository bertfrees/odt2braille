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

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.HashSet;

import be.docarch.odt2braille.setup.Configuration;
import be.docarch.odt2braille.setup.SpecialSymbol;

/**
 *
 * @author Bert Frees
 */
public class Volume {

    private enum FrontMatterMode { EXTENDED, BASIC, NONE }
    private enum TableOfContentMode { EXTENDED, BASIC, NONE }

    private String title;

    private final String identifier;
    private FrontMatterMode frontMatterMode;
    private TableOfContentMode tableOfContentMode;
    private final boolean transcribersNotesPageEnabled;
    private final boolean specialSymbolListEnabled;

    private int braillePagesStart;
    private int numberOfBraillePages;
    private int numberOfPreliminaryPages;
    private String firstPrintPage;
    private String lastPrintPage;
    
    private List<SpecialSymbol> specialSymbols;
    private List<String> transcribersNotes;

    public Volume(Configuration.Volume settings) {

        title = settings.getTitle();
        identifier = createUniqueIdentifier();

        frontMatterMode = settings.getFrontMatter() ? FrontMatterMode.BASIC : FrontMatterMode.NONE;
        tableOfContentMode = settings.getTableOfContent() ? TableOfContentMode.BASIC : TableOfContentMode.NONE;
        transcribersNotesPageEnabled = settings.getTranscribersNotesPage();
        specialSymbolListEnabled = settings.getSpecialSymbolList();

        braillePagesStart = 1;
        numberOfBraillePages = 0;
        numberOfPreliminaryPages = 0;
        firstPrintPage = null;
        lastPrintPage = null;
        
    }

    public void setTitle                    (String value)              { title = value; }
    public void setBraillePagesStart        (int value)                 { braillePagesStart = value; }
    public void setNumberOfBraillePages     (int value)                 { numberOfBraillePages = value; }
    public void setNumberOfPreliminaryPages (int value)                 { numberOfPreliminaryPages = value; }
    public void setFirstPrintPage           (String value)              { firstPrintPage = value; }
    public void setLastPrintPage            (String value)              { lastPrintPage = value; }
    public void setSpecialSymbols           (List<SpecialSymbol> value) { specialSymbols = value; }
    public void setTranscribersNotes        (List<String> value)        { transcribersNotes = value; }

    public void setExtendedFrontMatter(boolean value) { 
        if (value && frontMatterMode == FrontMatterMode.BASIC) {
            frontMatterMode = FrontMatterMode.EXTENDED;
        } else if (!value && frontMatterMode == FrontMatterMode.EXTENDED) {
            frontMatterMode = FrontMatterMode.BASIC;
        }
    }

    public void setExtendedTableOfContent(boolean value) {
        if (value && tableOfContentMode == TableOfContentMode.BASIC) {
            tableOfContentMode = TableOfContentMode.EXTENDED;
        } else if (!value && tableOfContentMode == TableOfContentMode.EXTENDED) {
            tableOfContentMode = TableOfContentMode.BASIC;
        }
    }

    public String              getTitle()                        { return title; }
    public String              getIdentifier()                   { return identifier; }
    public boolean             getFrontMatter()                  { return frontMatterMode != FrontMatterMode.NONE; }
    public boolean             getExtendedFrontMatter()          { return frontMatterMode == FrontMatterMode.EXTENDED; }
    public boolean             getTableOfContent()               { return tableOfContentMode != TableOfContentMode.NONE; }
    public boolean             getExtendedTableOfContent()       { return tableOfContentMode == TableOfContentMode.EXTENDED; }
    public boolean             getTranscribersNotesPageEnabled() { return transcribersNotesPageEnabled; }
    public boolean             getSpecialSymbolListEnabled()     { return specialSymbolListEnabled; }
    public int                 getFirstBraillePage()             { return braillePagesStart; }
    public int                 getLastBraillePage()              { return braillePagesStart + numberOfBraillePages - 1; }
    public int                 getNumberOfPreliminaryPages()     { return numberOfPreliminaryPages; }
    public String              getFirstPrintPage()               { return firstPrintPage; }
    public String              getLastPrintPage()                { return lastPrintPage; }
    public List<SpecialSymbol> getSpecialSymbols()               { return specialSymbols; }
    public List<String>        getTranscribersNotes()            { return transcribersNotes; }

    private static final Set<String> uniqueIDs = new HashSet<String>();

    private static String createUniqueIdentifier() {

        Random random = new Random();
        String s = "abcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder builder = new StringBuilder();
        for (int i=0; i<4; i++) {
            int index = random.nextInt(36);
            builder.append(s.charAt(index));
        }
        String id = builder.toString();
        if (uniqueIDs.contains(id)) {
            return createUniqueIdentifier();
        } else {
            uniqueIDs.add(id);
            return id;
        }
    }
}
