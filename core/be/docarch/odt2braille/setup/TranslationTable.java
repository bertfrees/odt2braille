package be.docarch.odt2braille.setup;

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

    private static final Collection<String> options = new HashSet<String>();
    private static final Collection<String> localeOptions = new HashSet<String>();

    private static final String[] optionsArray = {

          /* "ar-g0",     */  "ar-g1",
             "as-g0",
             "awa-g0",
             "bg-g0",
             "bh-g0",
             "bn-g0",
             "bo-g0",
             "bra-g0",
                              "ca-g1",
                              "ckb-g1",
          /* "cs-g0",     */  "cs-g1",
          /* "cy-g0",     */  "cy-g1",      "cy-g2",
          /* "da-g0-8d",  */  "da-g1-8d",   "da-g2-8d",
             "de-DE-g0",      "de-DE-g1",   "de-DE-g2",
             "de-CH-g0",      "de-CH-g1",   "de-CH-g2",
             "dra-g0",
          /* "el-GR-g0",  */  "el-GR-g1",
             "el-LLX-g0",
          /* "en-US-g0",  */  "en-US-g1",   "en-US-g2",
          /* "en-GB-g0",  */  "en-GB-g1",   "en-GB-g2",
             "en-CA-g0",
          /* "en-UEB-g0", */  "en-UEB-g1",  "en-UEB-g2",
             "eo-g0",
                              "es-g1",
             "et-g0",
             "fi-g0-8d",
                              "fr-BFU-g1",  "fr-BFU-g2",
                              "fr-BFU-g1-8d",
          /* "fr-FR-g0",      "fr-FR-g1",   "fr-FR-g2", */
          /* "fr-CA-g0",      "fr-CA-g1",   "fr-CA-g2", */
             "ga-g0",
             "gd-g0",
                              "gez-g1",
             "gon-g0",
             "gu-g0",
             "he-g0",
          /* "hi-g0",     */  "hi-g1",
             "hr-g0",
             "hu-g0-8d",
             "hy-g0",
          /* "is-g0",     */  "is-g1",
          /* "it-g0",     */  "it-g1",
             "kha-g0",
             "kn-g0",
             "kok-g0",
             "kru-g0",
             "lt-g0",
          /* "lv-g0",     */  "lv-g1",
             "ml-g0",
             "mni-g0",
             "mr-g0",
             "mt-g0",
             "mun-g0",
             "mwr-g0",
             "ne-g0",
             "new-g0",
          /* "nl-NL-g0",  */  "nl-NL-g1",
          /* "nl-BE-g0",  */  "nl-BE-g1",
             "no-g0",         "no-g1",      "no-g2",       "no-g3",
             "or-g0",
             "pa-g0",
             "pi-g0",
          /* "pl-g0",     */  "pl-g1",
          /* "pt-g0",     */  "pt-g1",      "pt-g2",
             "ro-g0",
          /* "ru-g0",     */  "ru-g1",
             "sa-g0",
             "sat-g0",
             "sd-g0",
          /* "sk-g0",     */  "sk-g1",
          /* "sl-g0",     */  "sl-g1",
                              "sr-g1",
          /* "sv-g0",     */  "sv-g1",      "sv-g2",
             "ta-g0",
             "te-g0",
             "tr-g0",
             "vi-g0",
             "zh-HK-g0",
             "zh-TW-g0"   };

    static {
        for (String option : optionsArray) {
            if (option.matches("[a-z]+(-[A-Z]+)?-g[0-9](-8d)?")) {
                options.add(option);
                localeOptions.add(option.substring(0,option.lastIndexOf("-g")));
            }
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
            for (String option : optionsArray) {
                if (option.matches(locale + "-g[0-9](-8d)?")) {
                    int i = option.lastIndexOf("-g") + 2;
                    options.add(Integer.parseInt(option.substring(i,i+1)));
                }
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
            for (String option : optionsArray) {
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
}
