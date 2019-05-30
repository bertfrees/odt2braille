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
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.liblouis.CompilationException;
import org.liblouis.Louis;
import org.liblouis.Table;
import org.liblouis.TableInfo;
import org.liblouis.TableResolver;

public class TranslationTable implements TranslationTableProperties, Serializable {

    /*************
    /* SETTINGS */
    /************/

    public final OptionSetting<String> locale;
    public final DependentOptionSetting<Integer> grade;
    public final DependentOptionSetting<Dots> dots;

    private String fileName;

    /* GETTERS */
    
    public String getLocale()   { return locale.get(); }
    public int    getGrade()    { return grade.get(); }
    public Dots   getDots()     { return dots.get(); }

    public String getFileName() {
        if (fileName == null) {
            Table table = null;
            for (Table option : TranslationTable.options) {
                TranslationTableProperties props = fromTableInfo(option.getInfo());
                if (props.getLocale().equals(locale.get()) && props.getGrade() == grade.get() && props.getDots() == dots.get()) {
                    table = option;
                }
            }
            if (table == null)
                throw new RuntimeException(); // should not happen
            try {
                fileName = table.getTranslator().getTable();
            } catch (CompilationException e) {
                throw new RuntimeException(e);
            }
            if (!"yes".equals(table.getInfo().get("has-nocross"))) {
                final Locale locale = Locale.forLanguageTag(TranslationTable.this.locale.get());
                if ("cs".equals(locale.getLanguage()))
                    fileName += ",_hyphenation_cs.dic"; // hyph_cs_CZ.dic
                else if ("da".equals(locale.getLanguage()))
                    fileName += ",hyph_da_DK.dic";
                else if ("de".equals(locale.getLanguage()))
                    fileName += ",hyph_de_DE.dic";
                else if ("en".equals(locale.getLanguage()))
                    fileName += ",hyph_en_US.dic";
                else if ("eo".equals(locale.getLanguage()))
                    fileName += ",hyph_eo.dic";
                else if ("es".equals(locale.getLanguage()))
                    fileName += ",hyph_es_ES.dic";
                else if ("et".equals(locale.getLanguage()))
                    ; // fileName += ",_hyphenation_et.dic";
                else if ("fr".equals(locale.getLanguage()))
                    fileName += ",hyph_fr_FR.dic";
                else if ("ga".equals(locale.getLanguage()))
                    fileName += ",_hyphenation_ga.dic";
                else if ("hr".equals(locale.getLanguage()))
                    fileName += ",_hyphenation_hr.dic";
                else if ("hu".equals(locale.getLanguage()))
                    fileName += ",hyph_hu_HU.dic";
                else if ("is".equals(locale.getLanguage()))
                    fileName += ",_hyphenation_is.dic";
                else if ("it".equals(locale.getLanguage()))
                    fileName += ",hyph_it_IT.dic";
                else if ("nl".equals(locale.getLanguage())) {
                    if ("BE".equals(locale.getCountry()))
                        fileName += ",_hyphenation_nl-BE.dic"; // hyph_nl_NL.dic
                    else
                        fileName += ",hyph_nl_NL.dic";
                } else if ("no".equals(locale.getLanguage()))
                    fileName += ",_hyphenation_no.dic"; // hyph_nb_NO.dic / hyph_nn_NO.dic
                else if ("pl".equals(locale.getLanguage()))
                    fileName += ",hyph_pl_PL.dic";
                else if ("pt".equals(locale.getLanguage()))
                    fileName += ",hyph_pt_PT.dic";
                else if ("ru".equals(locale.getLanguage()))
                    fileName += ",hyph_ru.dic";
                else if ("sv".equals(locale.getLanguage()))
                    fileName += ",_hyphenation_sv.dic"; // hyph_sv_SE.dic
                else if ("tr".equals(locale.getLanguage()))
                    ; // fileName += ",_hyphenation_tr.dic";
            }
        }
        return fileName;
    }

    /****************************/
    /* PRIVATE STATIC CONSTANTS */
    /****************************/

    private static final String DEFAULT_LOCALE = "en-US";

    private static final Collection<Table> options = new HashSet<Table>();
    private static final Collection<String> localeOptions = new HashSet<String>();

    private static boolean initialized = false;

    static void setLiblouisFolder(File folder) throws Exception {
        if (initialized) return;
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            if (System.getProperty("os.arch").contains("64"))
                Louis.setLibraryPath(new File(folder, "bin" + File.separator + "64" + File.separator + "liblouis.dll"));
            else
                Louis.setLibraryPath(new File(folder, "bin" + File.separator + "liblouis.dll"));
        } else if (System.getProperty("os.name").toLowerCase().contains("mac"))
            Louis.setLibraryPath(new File(folder, "bin" + File.separator + "liblouis.dylib"));
        else
            Louis.setLibraryPath(new File(folder, "bin" + File.separator + "liblouis.so"));
        setTablesFolder(new File(folder, "files"));
        initialized = true;
    }

    private static void setTablesFolder(final File folder) throws Exception {
        if (!folder.exists()) {
            throw new Exception("Folder doesn't exist");
        }
        final Set<String> list = new HashSet<String>();
        for (String f : folder.list()) list.add(f);
        Louis.setTableResolver(new TableResolver() {
                public URL resolve(String table, URL base) {
                    File f = new File(folder, table);
                    if (f.exists())
                        try {
                            return f.toURI().toURL();
                        } catch (MalformedURLException e) {
                            throw new RuntimeException(e); // should not happen
                        }
                    return null;
                }
                public Set<String> list() {
                    return list;
                }
            }
        );
        for (Table t : Louis.listTables()) {
            TranslationTableProperties props = fromTableInfo(t.getInfo());
            if (props != null) {
                options.add(t);
                localeOptions.add(props.getLocale());
            }
        }
        if (options.size() == 0) {
            throw new Exception("Folder doesn't contain any tables");
        }
    }
   

    TranslationTable(Locale loc) {

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
        return toID(this);
    }

    public void setID(String value) {
        if (value.equals(getID())) { return; }
        TranslationTableProperties newProps = fromID(value);
        for (Table option : TranslationTable.options) {
            TranslationTableProperties props = fromTableInfo(option.getInfo());
            if (props.getLocale().equals(newProps.getLocale())
                && props.getGrade() == newProps.getGrade()
                && props.getDots() == newProps.getDots()) {
                locale.update(props.getLocale());
                grade.update(props.getGrade());
                dots.update(props.getDots());
                return;
            }
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
            fileName = null;
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
            fileName = null;
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
            for (Table option : TranslationTable.options) {
                TranslationTableProperties props = fromTableInfo(option.getInfo());
                if (props.getLocale().equals(locale))
                    options.add(props.getGrade());
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
            fileName = null;
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
            for (Table option : TranslationTable.options) {
                TranslationTableProperties props = fromTableInfo(option.getInfo());
                if (props.getLocale().equals(locale) && props.getGrade() == grade)
                    options.add(props.getDots());
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

    private static String toID(TranslationTableProperties table) {
        return table.getLocale() + "-g" + table.getGrade() + (table.getDots()==Dots.EIGHTDOTS?"-8d":"");
    }

    private static String toQuery(TranslationTableProperties table) {
        return "locale:" + table.getLocale() + " grade:" + table.getGrade() + " dots:" + (table.getDots()==Dots.EIGHTDOTS?"8":"6");
    }

    private static TranslationTableProperties fromID(String value) {
        final String locale = value.substring(0,value.lastIndexOf("-g"));
        final int grade = value.endsWith("-8d")
            ? Integer.parseInt(value.substring(value.lastIndexOf("-g")+2),value.lastIndexOf("-8d"))
            : Integer.parseInt(value.substring(value.lastIndexOf("-g")+2));
        final Dots dots = value.endsWith("-8d")
            ? Dots.EIGHTDOTS
            : Dots.SIXDOTS;
        return new TranslationTableProperties() {
            public String getLocale() { return locale; }
            public int    getGrade()  { return grade;  }
            public Dots   getDots()   { return dots;   }
        };
    }

    private static Map<TableInfo,TranslationTableProperties> fromTableInfo = new IdentityHashMap<TableInfo,TranslationTableProperties>();

    private static TranslationTableProperties fromTableInfo(TableInfo meta) {
        if (fromTableInfo.containsKey(meta))
            return fromTableInfo.get(meta);
        final String locale = meta.get("locale");
        if (locale == null) return null;
        final int grade; {
            try {
                String g = meta.get("grade");
                if (g == null) return null;
                if (!g.matches("^[0-9]$")) return null;
                grade = Integer.parseInt(g);
            } catch(NumberFormatException e) {
                return null;
            }
        }
        final Dots dots; {
            String d = meta.get("dots");
            if (d == null || d.equals("6"))
                dots = Dots.SIXDOTS;
            else if (d.equals("8"))
                dots = Dots.EIGHTDOTS;
            else return null;
        }
        TranslationTableProperties props = new TranslationTableProperties() {
            public String getLocale() { return locale; }
            public int    getGrade()  { return grade;  }
            public Dots   getDots()   { return dots;   }
        };
        fromTableInfo.put(meta, props);
        return props;
    }
}
