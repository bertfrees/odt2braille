package be.docarch.odt2braille.setup;

import java.io.Serializable;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Logger;

import java.util.NoSuchElementException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.CustomPaper;
import be.docarch.odt2braille.Dimensions;

import org.daisy.braille.embosser.Embosser;
import org.daisy.braille.embosser.EmbosserFeatures;
import org.daisy.braille.embosser.EmbosserCatalog;
import org.daisy.braille.table.Table;
import org.daisy.braille.table.TableCatalog;
import org.daisy.paper.Paper;
import org.daisy.paper.PageFormat;
import org.daisy.paper.Area;
import org.daisy.paper.PrintPage;
import org.daisy.paper.PaperCatalog;

public class EmbossConfiguration implements Serializable,
                                            PEFConfiguration {
    
    /**************/
    /* PROPERTIES */
    /**************/
    
    public final ColumnsProperty columns;
    public final RowsProperty    rows;
    
    
    /* GETTERS */
    
    public int getColumns() { return columns.get(); }
    public int getRows()    { return rows.get(); }  
    
    
    /************/
    /* SETTINGS */
    /************/
    
    public final EmbosserSetting           embosser;
    public final CharacterSetSetting       charSet;
    public final PaperSetting              paper;
    public final DependentYesNoSetting     duplex;
    public final DependentYesNoSetting     eightDots;
    public final DependentYesNoSetting     zFolding;
    public final DependentYesNoSetting     saddleStitch;
    public final DependentNumberSetting    sheetsPerQuire;
    
    
    /* GETTERS */
    
    public Embosser getEmbosser()      { return embosser.get(); }
    public Table    getCharSet()       { return charSet.get(); }
    public Paper    getPaper()         { return paper.get(); }
            
    public String   getEmbosserType()  { return embosser.getType(); }
    public String   getCharSetType()   { return charSet.getType(); }
    public String   getPaperType()     { return paper.getType(); }
    public double   getPaperWidth()    { return paper.getWidth(); }
    public double   getPaperHeight()   { return paper.getHeight(); }
    public boolean  getDuplex()        { return duplex.get(); }
    public boolean  getEightDots()     { return eightDots.get(); }
    public boolean  getZFolding()      { return zFolding.get(); }
    public boolean  getSaddleStitch()  { return saddleStitch.get(); }

    
    /* SETTERS */
            
    public void setEmbosserType   (String value)  { embosser.setType(value); }
    public void setCharSetType    (String value)  { charSet.setType(value); }
    public void setPaperType      (String value)  { paper.setType(value); }
    public void setPaperWidth     (double value)  { paper.setWidth(value); }
    public void setPaperHeight    (double value)  { paper.setHeight(value); }
    public void setDuplex         (boolean value) { duplex.set(value); }
    public void setEightDots      (boolean value) { eightDots.set(value); }
    public void setZFolding       (boolean value) { zFolding.set(value); }
    public void setSaddleStitch   (boolean value) { saddleStitch.set(value); }
    
    
    /***************************/
    /* SUBLEVEL CONFIGURATIONS */
    /***************************/
    
    public final MarginSettings margins;
    
    
    /* GETTERS */
    
    public MarginSettings getMargins() { return margins; }
    
    
    /***************************/
    /* PUBLIC STATIC CONSTANTS */
    /***************************/    
    
    public static final String INTERPOINT =              "be_interpoint";
    public static final String INDEX_BRAILLE =           "com_indexbraille";
    public static final String BRAILLO =                 "com_braillo";
    public static final String CIDAT =                   "es_once_cidat";
    public static final String ENABLING_TECHNOLOGIES =   "com_brailler";
    public static final String HARPO =                   "pl_com_harpo";
    public static final String VIEWPLUS =                "com_viewplus";

    public static final String GENERIC_EMBOSSER =        "org_daisy.GenericEmbosserProvider.EmbosserType.NONE";
    public static final String INTERPOINT_55 =           "be_interpoint.InterpointEmbosserProvider.EmbosserType.INTERPOINT_55";
    public static final String INDEX_BASIC_BLUE_BAR =    "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_BLUE_BAR";
    public static final String INDEX_EVEREST_S_V1 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_S_V1";
    public static final String INDEX_EVEREST_D_V1 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V1";
    public static final String INDEX_BASIC_S_V2 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_S_V2";
    public static final String INDEX_BASIC_D_V2 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_D_V2";
    public static final String INDEX_EVEREST_D_V2 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V2";
    public static final String INDEX_4X4_PRO_V2 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_4X4_PRO_V2";
    public static final String INDEX_BASIC_S_V3 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_S_V3";
    public static final String INDEX_BASIC_D_V3 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_D_V3";
    public static final String INDEX_EVEREST_D_V3 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V3";
    public static final String INDEX_4X4_PRO_V3 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_4X4_PRO_V3";
    public static final String INDEX_4WAVES_PRO_V3 =     "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_4WAVES_PRO_V3";
    public static final String INDEX_BASIC_D_V4 =        "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BASIC_D_V4";
    public static final String INDEX_EVEREST_D_V4 =      "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_EVEREST_D_V4";
    public static final String INDEX_BRAILLE_BOX_V4 =    "com_indexbraille.IndexEmbosserProvider.EmbosserType.INDEX_BRAILLE_BOX_V4";
    public static final String BRAILLO_200 =             "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_200";
    public static final String BRAILLO_270 =             "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_270";
    public static final String BRAILLO_400_S =           "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_400_S";
    public static final String BRAILLO_400_SR =          "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_400_SR";
    public static final String BRAILLO_440_SW_2P =       "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_440_SW_2P";
    public static final String BRAILLO_440_SW_4P =       "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_440_SW_4P";
    public static final String BRAILLO_440_SWSF =        "com_braillo.BrailloEmbosserProvider.EmbosserType.BRAILLO_440_SWSF";
    public static final String IMPACTO_600 =             "es_once_cidat.CidatEmbosserProvider.EmbosserType.IMPACTO_600";
    public static final String IMPACTO_TEXTO =           "es_once_cidat.CidatEmbosserProvider.EmbosserType.IMPACTO_TEXTO";
    public static final String PORTATHIEL_BLUE =         "es_once_cidat.CidatEmbosserProvider.EmbosserType.PORTATHIEL_BLUE";
    public static final String ROMEO_ATTACHE =           "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_ATTACHE";
    public static final String ROMEO_ATTACHE_PRO =       "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_ATTACHE_PRO";
    public static final String ROMEO_25 =                "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.OMEO_25";
    public static final String ROMEO_PRO_50 =            "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_50";
    public static final String ROMEO_PRO_LE_NARROW =     "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_LE_NARROW";
    public static final String ROMEO_PRO_LE_WIDE =       "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ROMEO_PRO_LE_WIDE";
    public static final String THOMAS =                  "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.THOMAS";
    public static final String THOMAS_PRO =              "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.THOMAS_PRO";
    public static final String MARATHON =                "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.MARATHON";
    public static final String ET =                      "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.ET";
    public static final String JULIET_PRO =              "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_PRO";
    public static final String JULIET_PRO_60 =           "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_PRO_60";
    public static final String JULIET_CLASSIC =          "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.JULIET_CLASSIC";
    public static final String BOOKMAKER =               "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BOOKMAKER";
    public static final String BRAILLE_EXPRESS_100 =     "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_EXPRESS_100";
    public static final String BRAILLE_EXPRESS_150 =     "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_EXPRESS_150";
    public static final String BRAILLE_PLACE =           "com_brailler.EnablingTechnologiesEmbosserProvider.EmbosserType.BRAILLE_PLACE";
    public static final String MOUNTBATTEN_LS =          "pl_com_harpo.HarpoEmbosserProvider.EmbosserType.MOUNTBATTEN_LS";
    public static final String MOUNTBATTEN_PRO =         "pl_com_harpo.HarpoEmbosserProvider.EmbosserType.MOUNTBATTEN_PRO";
    public static final String MOUNTBATTEN_WRITER_PLUS = "pl_com_harpo.HarpoEmbosserProvider.EmbosserType.MOUNTBATTEN_WRITER_PLUS";
    public static final String PREMIER_80 =              "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.PREMIER_80";
    public static final String PREMIER_100 =             "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.PREMIER_100";
    public static final String ELITE_150 =               "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.ELITE_150";
    public static final String ELITE_200 =               "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.ELITE_200";
    public static final String PRO_GEN_II =              "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.PRO_GEN_II";
    public static final String CUB_JR =                  "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.CUB_JR";
    public static final String CUB =                     "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.CUB";
    public static final String MAX =                     "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.MAX";
    public static final String EMFUSE =                  "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.EMFUSE";
    public static final String EMPRINT_SPOTDOT =         "com_viewplus.ViewPlusEmbosserProvider.EmbosserType.EMPRINT_SPOTDOT";

    public static final String CUSTOM_PAPER =            "be.docarch.odt2braille.CustomPaperProvider.PaperSize.CUSTOM";

    /****************************/
    /* PRIVATE STATIC CONSTANTS */
    /****************************/

    private static final Logger logger = Logger.getLogger(Constants.LOGGER_NAME);
    
    /*********************/
    /* PRIVATE CONSTANTS */
    /*********************/
    
    private final EmbosserCatalog embosserCatalog;
    private final TableCatalog charSetCatalog;
    private final PaperCatalog paperCatalog;
    private final Collection<String> embosserOptions = new ArrayList<String>();

    
    public EmbossConfiguration() {

        logger.entering("EmbossConfiguration","<init>");

        /************
           CATALOGS
         ************/

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Configuration.class.getClassLoader()); {

            charSetCatalog = TableCatalog.newInstance();
            embosserCatalog = EmbosserCatalog.newInstance();
            paperCatalog = PaperCatalog.newInstance();

        } Thread.currentThread().setContextClassLoader(cl);

        for (Embosser e : embosserCatalog.list()) {
            embosserOptions.add(e.getIdentifier());
        }


        /***********************
           SETTING DECLARATION
         ***********************/

        embosser = new EmbosserSetting();
        charSet = new CharacterSetSetting();
        paper = new PaperSetting();
        duplex = new DuplexSetting();
        eightDots = new EightDotsSetting();
        zFolding = new ZFoldingSetting();
        saddleStitch = new SaddleStitchSetting();
        sheetsPerQuire = new SheetsPerQuireSetting();
        columns = new ColumnsProperty();
        rows = new RowsProperty();
        margins = new MarginSettings();
        

        /**************************
           SETTING INITIALIZATION
         **************************/
        
        duplex.set(true);
        eightDots.set(false);
        saddleStitch.set(false);
        zFolding.set(false);
        sheetsPerQuire.set(1);
        
        duplex.refresh();
        eightDots.refresh();
        charSet.refresh();
        sheetsPerQuire.refresh();
        saddleStitch.refresh();
        zFolding.refresh();
        paper.refresh();
        margins.refresh();
        columns.refresh();
        rows.refresh();
        

        /*******************
           SETTING LINKING
         *******************/

        embosser.addListener(duplex);
        embosser.addListener(eightDots);
        embosser.addListener(charSet);
        embosser.addListener(sheetsPerQuire);
        embosser.addListener(saddleStitch);
        embosser.addListener(zFolding);
        embosser.addListener(paper);
        embosser.addListener(margins);
        embosser.addListener(columns);
        embosser.addListener(rows);
        eightDots.addListener(charSet);
        saddleStitch.addListener(paper);
        saddleStitch.addListener(margins);
        saddleStitch.addListener(sheetsPerQuire);
        paper.addListener(margins);
        paper.addListener(columns);
        paper.addListener(rows);

        logger.exiting("EmbossConfiguration","<init>");
        
    }
    
    
    /*****************/
    /* INNER CLASSES */
    /*****************/
    
    public class EmbosserSetting extends OptionSetting<Embosser> {

        private static final String DEFAULT_EMBOSSER = GENERIC_EMBOSSER;
        private Embosser embosser = embosserCatalog.get(DEFAULT_EMBOSSER);

        public Collection<Embosser> options() {
            return embosserCatalog.list();
        }

        @Override
        public boolean accept(Embosser value) {
            return embosserOptions.contains(value.getIdentifier());
        }

        protected boolean update(Embosser value) {
            if (value.getIdentifier().equals(embosser.getIdentifier())) { return false; }
            embosser = value;
            return true;
        }

        public Embosser get() {
            return embosser;
        }
        
        public void setType(String type) {
            Embosser e = embosserCatalog.get(type);
            if (e != null) { set(e); }
        }
        
        public String getType() {
            if (embosser == null) { return null; }
            return embosser.getIdentifier();
        }
    }
    
    public class CharacterSetSetting extends DependentOptionSetting<Table> {

        private static final String DEFAULT_CHARSET = "org_daisy.EmbosserTableProvider.TableType.MIT";
        private Table charSet = charSetCatalog.get(DEFAULT_CHARSET);
        
        @Override
        public boolean accept(Table value) {
            if (value == null) { return false; }
            if (getEightDots() ^ value.newBrailleConverter().supportsEightDot()) { return false; }
            return getEmbosser().supportsTable(value);
        }
        
        public Collection<Table> options() {
            Collection<Table> options = new ArrayList();
            for (Table tab : charSetCatalog.list()) {
                if (accept(tab)) {
                    options.add(tab);
                }
            }
            return options;
        }

        protected boolean update(Table value) {
            charSet = value;
            if (value != null) {
                try {
                    getEmbosser().setFeature(EmbosserFeatures.TABLE, value);
                } catch (IllegalArgumentException e) {}
            }
            return true;
        }

        public Table get() {
            return charSet;
        }

        public boolean refresh() {
            if (accept(charSet)) { return false; }
            try {
                return update(options().iterator().next());
            } catch (NoSuchElementException e) {
                return update(null);
            }
        }
        
        @Override
        public boolean enabled() {
            if (!super.enabled()) { return false; }
            return (options().size() > 0);
        }
        
        public void setType(String type) {
            Table t = charSetCatalog.get(type);
            if (t != null) { set(t); }
        }
        
        public String getType() {
            if (charSet == null) { return null; }
            return charSet.getIdentifier();
        }
    }
    
    public class PaperSetting extends DependentOptionSetting<Paper> {

        private static final String DEFAULT_PAPER = CUSTOM_PAPER;
        private Paper paper = paperCatalog.get(DEFAULT_PAPER);

        @Override
        public boolean accept(Paper value) {
            if (value == null) { return false; }
            String id = value.getIdentifier();   
            String emb = getEmbosser().getIdentifier();
            if (!(emb.equals(INDEX_BASIC_BLUE_BAR) ||
                  emb.equals(INDEX_BASIC_S_V2) ||
                  emb.equals(INDEX_BASIC_D_V2) ||
                  emb.equals(INDEX_BASIC_S_V3) ||
                  emb.equals(INDEX_BASIC_D_V3) ||
                  emb.equals(INDEX_BASIC_D_V4))
                && id.equals(CUSTOM_PAPER)) {
                return true;
            }
            return getEmbosser().supportsDimensions(value);
        }
        
        public Collection<Paper> options() {
            Collection<Paper> options = new ArrayList();
            for (Paper p : paperCatalog.list()) {
                if (accept(p)) { options.add(p); }
            }
            return options;
        }

        protected boolean update(Paper value) {
            if (value == null) { return false; }
            if (value instanceof CustomPaper && !getEmbosser().supportsDimensions(value)) {
                CustomPaper cp = (CustomPaper)value;
                for (Paper p : options()) {
                    if (getEmbosser().supportsDimensions(p)) {
                        cp.setWidth(p.getWidth());
                        cp.setHeight(p.getHeight());
                    }
                }
            } else if (paper.getIdentifier().equals(value.getIdentifier())) {
                return false;
            }
            paper = value;
            return true;
        }

        public Paper get() {
            return paper;
        }

        public boolean refresh() {
            if (accept(paper)) {
                return update(paper);
            } else {
                for (Paper p : options()) {
                    if (getEmbosser().supportsDimensions(p)) {
                        return update(p);
                    }
                }
            }
            return false;
        }
        
        @Override
        public boolean enabled() {
            if (!super.enabled()) { return false; }
            return (options().size() > 0);
        }
        
        public void setType(String type) {
            Paper p = paperCatalog.get(type);
            if (p != null) { set(p); }
        }
        
        public String getType() {
            if (paper == null) { return null; }
            return paper.getIdentifier();
        }
        
        public boolean setWidth(double width) {
            if (!enabled()) { return false; }
            if (paper == null) { return false; }
            if (width == getWidth()) { return true; }
            if (paper instanceof CustomPaper) {
                if (getEmbosser().supportsDimensions(new Dimensions(width, getHeight()))) {
                    ((CustomPaper)paper).setWidth(width);
                    fireEvent(true, false);
                    return true;
                }
            }
            return false;
        }
        
        public boolean setHeight(double height) {
            if (!enabled()) { return false; }
            if (paper == null) { return false; }
            if (height == getHeight()) { return true; }
            if (paper instanceof CustomPaper) {
                if (getEmbosser().supportsDimensions(new Dimensions(getWidth(), height))) {
                    ((CustomPaper)paper).setHeight(height);
                    fireEvent(true, false);
                    return true;
                }
            }
            return false;
        }
        
        public double getWidth() {
            if (paper==null) { return 0; }
            return paper.getWidth();
        }
            
        public double getHeight() {
            if (paper==null) { return 0; }
            return paper.getHeight();
        }
    }
    
    private class DuplexSetting extends DependentYesNoSetting {

        @Override
        public boolean refresh() {
            return update(accept(yesNo) ? yesNo : !yesNo);
        }

        @Override
        protected boolean update(Boolean value) {
            try {
                getEmbosser().setFeature(EmbosserFeatures.DUPLEX, value);
            } catch (IllegalArgumentException e) {
            }
            return super.update(value);
        }
        
        public boolean accept(Boolean value) {
            return getEmbosser().supportsDuplex() ? true : !value;
        }
    }
    
    private class EightDotsSetting extends DependentYesNoSetting {

        public boolean accept(Boolean value) {
            return getEmbosser().supports8dot() ? true : !value;
        }
    }

    private class ZFoldingSetting extends DependentYesNoSetting {

        @Override
        public boolean refresh() {
            return update(accept(yesNo) ? yesNo : !yesNo);
        }

        @Override
        protected boolean update(Boolean value) {
            try {
                getEmbosser().setFeature(EmbosserFeatures.Z_FOLDING, value);
            } catch (IllegalArgumentException e) {
            }
            return super.update(value);
        }
        
        public boolean accept(Boolean value) {
            return (getEmbosser().getFeature(EmbosserFeatures.Z_FOLDING) != null) ? true : !value;
        }
    }

    private class SaddleStitchSetting extends DependentYesNoSetting {

        @Override
        public boolean refresh() {
            return update(accept(yesNo) ? yesNo : !yesNo);
        }

        @Override
        protected boolean update(Boolean value) {
            try {
                getEmbosser().setFeature(EmbosserFeatures.SADDLE_STITCH, value);
            } catch (IllegalArgumentException e) {
            }
            return super.update(value);
        }
        
        public boolean accept(Boolean value) {
            return (getEmbosser().getFeature(EmbosserFeatures.SADDLE_STITCH) != null) ? true : !value;
        }
    }
    
    private class SheetsPerQuireSetting extends DependentNumberSetting {

        @Override
        public boolean refresh() {
            return update(accept(number) ? number : 1);
        }

        @Override
        public boolean accept(Integer value) { return value > 0; }

        @Override
        protected boolean update(Integer value) {
            try {
                getEmbosser().setFeature(EmbosserFeatures.PAGES_IN_QUIRE, value);
            } catch (IllegalArgumentException e) {
            }
            return super.update(value);
        }
        
        @Override
        public boolean enabled() {
            if (!super.enabled()) { return false; }
            return (getSaddleStitch() && 
                    getEmbosser().getFeature(EmbosserFeatures.PAGES_IN_QUIRE) != null);
        }
    }

    public abstract class MarginSetting extends DependentNumberSetting {
      //protected int min = 0;
      //protected int max = 0;
        protected double unprintable = 0;
        public abstract int getOffset();
        protected boolean enabled = false;
        @Override
        public boolean enabled() { return enabled; }
    }

    public class MarginSettings implements Dependent,
                                           Serializable {
        /************/
        /* SETTINGS */
        /************/

        public final MarginSetting inner;
        public final MarginSetting outer;
        public final MarginSetting top;
        public final MarginSetting bottom;
        
        /* GETTERS */
        
        public int getInner()        { return inner.get(); }
        public int getOuter()        { return outer.get(); }
        public int getTop()          { return top.get(); }
        public int getBottom()       { return bottom.get(); }
        
        /* SETTERS */
        
        public void setInner  (int value) { inner.set(value); }
        public void setOuter  (int value) { outer.set(value); }
        public void setTop    (int value) { top.set(value); }
        public void setBottom (int value) { bottom.set(value); }        
        
        /***********/
        /* PRIVATE */
        /***********/

        private PrintPage printPage;
        private Area printableArea;

        private int maxMarginInner = 0;
        private int maxMarginOuter = 0;
        private int maxMarginTop = 0;
        private int maxMarginBottom = 0;
        private int minMarginInner = 0;
        private int minMarginOuter = 0;
        private int minMarginTop = 0;
        private int minMarginBottom = 0;        
        
        private MarginSettings() {
        
            /***********************
               SETTING DECLARATION
             ***********************/

            inner = new InnerMarginSetting();
            outer = new OuterMarginSetting();
            top = new TopMarginSetting();
            bottom = new BottomMarginSetting();            

            /*******************
               SETTING LINKING
             *******************/
            
            inner.addListener(outer);
            outer.addListener(inner);
            top.addListener(bottom);
            bottom.addListener(top);
            
            inner.addListener(columns);
            outer.addListener(columns);
            top.addListener(rows);
            bottom.addListener(rows);
            
        }

        /*****************/
        /* INNER CLASSES */
        /*****************/
        
        private class InnerMarginSetting extends MarginSetting {
            public boolean accept(Integer value) {
                return (value >= minMarginInner && value <= maxMarginInner);
            }
            @Override
            public boolean refresh() {
                enabled = getEmbosser().supportsAligning();
                double newUnprintable = printableArea.getOffsetX();
                boolean update = update(Math.min(maxMarginInner, Math.max(minMarginInner, get())));
                boolean updateUnprintable = (unprintable != newUnprintable);
                unprintable = newUnprintable;
                return updateUnprintable || update;
            }
            public int getOffset() {
                return (int)Math.floor(unprintable / 6d);
            }
        };

        private class OuterMarginSetting extends MarginSetting {
            public boolean accept(Integer value) {
                return (value >= minMarginOuter && value <= maxMarginOuter);
            }
            @Override
            public boolean refresh() {
                enabled = getEmbosser().supportsAligning();
                double newUnprintable = printPage.getWidth() - printableArea.getWidth() - printableArea.getOffsetX();
                boolean update = update(Math.min(maxMarginOuter, Math.max(minMarginOuter, get())));
                boolean updateUnprintable = (unprintable != newUnprintable);
                unprintable = newUnprintable;
                return updateUnprintable || update;
            }
            public int getOffset() {
                return (int)Math.floor(unprintable / 6d);
            }
        };

        private class TopMarginSetting extends MarginSetting {
            public boolean accept(Integer value) {
                return (value >= minMarginTop && value <= maxMarginTop);
            }
            @Override
            public boolean refresh() {
                enabled = getEmbosser().supportsAligning();
                double newUnprintable = printableArea.getOffsetY();
                boolean update = update(Math.min(maxMarginTop, Math.max(minMarginTop, get())));
                boolean updateUnprintable = (unprintable != newUnprintable);
                unprintable = newUnprintable;
                return updateUnprintable || update;
            }

            public int getOffset() {
                return (int)Math.floor(unprintable / 10d);
            }
        };

        private class BottomMarginSetting extends MarginSetting {
            public boolean accept(Integer value) {
                return (value >= minMarginBottom && value <= maxMarginBottom);
            }
            @Override
            public boolean refresh() {
                enabled = getEmbosser().supportsAligning();
                double newUnprintable = printPage.getHeight() - printableArea.getHeight() - printableArea.getOffsetY();
                boolean update = update(Math.min(maxMarginBottom, Math.max(minMarginBottom, get())));
                boolean updateUnprintable = (unprintable != newUnprintable);
                unprintable = newUnprintable;
                return updateUnprintable || update;
            }
            public int getOffset() {
                return (int)Math.floor(unprintable / 10d);
            }
        };

        /*************/
        /* DEPENDENT */
        /*************/

        public boolean refresh() {
            
            maxMarginInner = 0;
            maxMarginTop = 0;
            maxMarginOuter = 0;
            maxMarginBottom = 0;

            Paper paper = getPaper();
            Embosser embosser = getEmbosser();

            PageFormat inputPage = new PageFormat(paper);
            printPage = embosser.getPrintPage(inputPage);
            printableArea = embosser.getPrintableArea(inputPage);

            int cellsInWidth = getEmbosser().getMaxWidth(inputPage);
            int linesInHeight = getEmbosser().getMaxHeight(inputPage);

            if (embosser.supportsAligning()) {

                maxMarginInner = cellsInWidth;
                maxMarginOuter = cellsInWidth;
                maxMarginTop = linesInHeight;
                maxMarginBottom = linesInHeight;
            }

            int tempMaxMarginInner = maxMarginInner;
            int tempMaxMarginOuter = maxMarginOuter;
            int tempMaxMarginTop = maxMarginTop;
            int tempMaxMarginBottom = maxMarginBottom;
            int tempMinCellsPerLine = 1;
            int tempMinLinesPerPage = 1;

            maxMarginInner =  (int)Math.min(tempMaxMarginInner,  cellsInWidth  - tempMinCellsPerLine);
            maxMarginOuter =  (int)Math.min(tempMaxMarginOuter,  cellsInWidth  - tempMinCellsPerLine);
            maxMarginTop =    (int)Math.min(tempMaxMarginTop,    linesInHeight - tempMinLinesPerPage);
            maxMarginBottom = (int)Math.min(tempMaxMarginBottom, linesInHeight - tempMinLinesPerPage);

            inner.fireEvent(inner.refresh(), true);
            outer.fireEvent(outer.refresh(), true);
            top.fireEvent(top.refresh(), true);
            bottom.fireEvent(bottom.refresh(), true);
            
            return true;
        }
        
        public void propertyUpdated(PropertyEvent event) {
            if (event.ValueChanged) { refresh(); }
        }
    }

    public class ColumnsProperty extends Property<Integer>
                                 implements Dependent {

        private int columns = 0;

        public boolean refresh() {
            PageFormat inputPage = new PageFormat(getPaper());
            int cellsInWidth = getEmbosser().getMaxWidth(inputPage);
            int newValue = cellsInWidth - margins.getInner() - margins.getOuter();
            if (newValue == columns) { return false; }
            columns = newValue;
            return true;
        }

        public Integer get() {
            return columns;
        }
        
        public void propertyUpdated(PropertyEvent event) {
            if (event.ValueChanged) {
                fireEvent(refresh(), true);
            }
        }
    }
    
    public class RowsProperty extends Property<Integer>
                              implements Dependent {
        
        private int rows = 0;

        public boolean refresh() {
            PageFormat inputPage = new PageFormat(getPaper());
            int linesInHeight = getEmbosser().getMaxHeight(inputPage);
            int newValue = linesInHeight - margins.getTop() - margins.getBottom();
            if (newValue == rows) { return false; }
            rows = newValue;
            return true;
        }

        public Integer get() { return rows; }
        
        public void propertyUpdated(PropertyEvent event) {
            if (event.ValueChanged) {
                fireEvent(refresh(), true);
            }
        }
    }
}
