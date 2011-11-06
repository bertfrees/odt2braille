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
 
 package be.docarch.odt2braille.setup;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Collections;
import java.io.Serializable;

public class TranslationTable implements Serializable {

    /*************
    /* SETTINGS */
    /************/

    public final OptionSetting<String> locale;
    public final DependentOptionSetting<Integer> grade;
    public final DependentOptionSetting<Dots> dots;
    
    /* GETTERS */
    
    public String getLocale() { return locale.get(); }
    public int    getGrade()  { return grade.get(); }
    public Dots   getDots()   { return dots.get(); }

    
    /***************************/
    /* PUBLIC STATIC CONSTANTS */
    /***************************/

    public static enum Dots { SIXDOTS, EIGHTDOTS };


    /****************************/
    /* PRIVATE STATIC CONSTANTS */
    /****************************/

    private static final String DEFAULT_LOCALE = "en-US";

    private static final FilenameFilter filter = new TranslationTableFilter();

    private static final Collection<String> options = new HashSet<String>();
    private static final Collection<String> localeOptions = new HashSet<String>();

    protected static void setTablesFolder(File folder) throws Exception {
        if (!folder.exists()) {
            throw new Exception("Folder doesn't exist");
        }
        for (String table : folder.list(filter)) {
            if (table.matches("__[a-z]+(-[A-Z]+)?-g[0-9](-8d)?\\.ctb")) {
                options.add(table.substring(2, table.lastIndexOf(".ctb")));
                localeOptions.add(table.substring(2,table.lastIndexOf("-g")));
            }
        }
        if (options.size() == 0) {
            throw new Exception("Folder doesn't contain any tables");
        }
    }
   

    protected TranslationTable(Locale loc) {

        /***********************
           SETTING DECLARATION
         ***********************/

        locale = new LocaleSetting();
        grade = new GradeSetting();
        dots = new DotsSetting();

        /**************************
           SETTING INITIALIZATION
         **************************/

        locale.set(computeLocale(loc));
        grade.refresh();
        dots.refresh();

        /*******************
           SETTING LINKING
         *******************/
        
        locale.addListener(grade);
        locale.addListener(dots);
        grade.addListener(dots);
        
    }

    public String getID() {
        return locale.get() + "-g" + grade.get() + (dots.get()==Dots.EIGHTDOTS?"-8d":"");
    }
    
    public void setID(String value) {
        if (!options.contains(value)) { return; }
        if (value.equals(getID())) { return; }
        locale.set(value.substring(0,value.lastIndexOf("-g")));
        if (value.endsWith("-8d")) {
            grade.set(Integer.parseInt(value.substring(value.lastIndexOf("-g")+2),value.lastIndexOf("-8d")));
            dots.set(Dots.EIGHTDOTS);
        } else {
            grade.set(Integer.parseInt(value.substring(value.lastIndexOf("-g")+2)));
            dots.set(Dots.SIXDOTS);
        }
    }

    /*****************/
    /* INNER CLASSES */
    /*****************/

    private class LocaleSetting extends OptionSetting<String> {

        private String locale = DEFAULT_LOCALE;

        public Collection options() {
            return localeOptions;
        }

        public String get() {
            return locale;
        }

        protected boolean update(String value) {
            if (value.equals(locale)) { return false; }
            locale = value;
            return true;
        }
    };

    private class GradeSetting extends DependentOptionSetting<Integer> {

        private int grade = 0;
        private Collection<Integer> options = new HashSet<Integer>();

        public Collection<Integer> options() {
            return new HashSet<Integer>(options);
        }

        public Integer get() {
            return grade;
        }

        protected boolean update(Integer value) {
            if (value==grade) { return false; }
            grade = value;
            return true;
        }

        public boolean refresh() {
            refreshOptions();
            if (accept(grade)) { return false; }
            grade = Collections.max(options());
            return true;
        }

        public void refreshOptions() {
            options.clear();
            String locale = getLocale();
            for (String option : TranslationTable.options) {
                if (option.matches(locale + "-g[0-9](-8d)?")) {
                    int i = option.lastIndexOf("-g") + 2;
                    options.add(Integer.parseInt(option.substring(i,i+1)));
                }
            }
            if (options.size() > 1 && !TranslationTable.options.contains(locale + "-g0-8d")) {
                options.remove(0);
            }
        }
    };

    private class DotsSetting extends DependentOptionSetting<Dots> {

        private Dots dots = Dots.SIXDOTS;
        private Collection<Dots> options = new HashSet<Dots>();

        public Collection<Dots> options() {
            return new HashSet<Dots>(options);
        }

        public Dots get() {
            return dots;
        }

        protected boolean update(Dots value) {
            if (value==dots) { return false; }
            dots = value;
            return true;
        }

        public boolean refresh() {
            refreshOptions();
            if (accept(dots)) { return false; }
            dots = options().iterator().next();
            return true;
        }

        public void refreshOptions() {
            options.clear();
            String locale = getLocale();
            int grade = getGrade();
            for (String option : TranslationTable.options) {
                if (option.equals(locale + "-g" + grade)) {
                    options.add(Dots.SIXDOTS);
                } else if (option.equals(locale + "-g" + grade + "-8d")) {
                    options.add(Dots.EIGHTDOTS);
                }
            }
        }
    };

    
    @Override
    public boolean equals(Object object) {
        if (this == object) { return true; }
        if (!(object instanceof TranslationTable)) { return false; }
        TranslationTable that = (TranslationTable)object;
        return this.getID().equals(that.getID());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 11 * hash + getID().hashCode();
        return hash;
    }

    /********************/
    /* HELPER FUNCTIONS */
    /********************/
    
    public static String computeLocale(Locale locale) {

        String language = locale.getLanguage();
        String country = locale.getCountry();

        if (localeOptions.contains(language + "-" + country)) {
            return language + "-" + country;
        } else {
            for (String s : localeOptions) {
                if ((s + "-").indexOf(language + "-") == 0) {
                    return s;
                }
            }
        }
        return DEFAULT_LOCALE;
    }
    
    /******************/
    /* FILENAMEFILTER */
    /******************/

    private static class TranslationTableFilter implements FilenameFilter {

        private static final String regex = "__[a-z]+(-[A-Z]+)?-g[0-9](-8d)?\\.ctb";

        public boolean accept(File directory,
                              String filename) {

            return filename.matches(regex);
        }
    }
}
