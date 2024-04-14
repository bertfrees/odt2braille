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

import java.io.Serializable;
import java.nio.charset.UnsupportedCharsetException;
import java.util.Collection;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.NoSuchElementException;

import be.docarch.odt2braille.Constants;
import be.docarch.odt2braille.FileFormatCatalog;

import org.daisy.dotify.api.embosser.FileFormat;
import org.daisy.dotify.api.embosser.EmbosserFeatures;
import org.daisy.dotify.api.factory.FactoryProperties;
import org.daisy.dotify.api.table.Table;
import org.daisy.dotify.api.table.BrailleConverter;
import org.daisy.dotify.api.table.TableCatalog;

public class ExportConfiguration implements Serializable,
                                            PEFConfiguration {
    
    /************/
    /* SETTINGS */
    /************/
    
    public final FileFormatSetting      fileFormat;
    public final CharacterSetSetting    charSet;
    public final DependentYesNoSetting  duplex;
    public final Setting<Boolean>       doubleLineSpacing;
    public final DependentYesNoSetting  eightDots;
    public final Setting<Boolean>       multipleFiles;
    public final Setting<Integer>       columns;
    public final Setting<Integer>       rows;
    
    
    /* GETTERS */
    
    public FileFormat getFileFormat()      { return fileFormat.get(); }
    public Table      getCharSet()         { return charSet.get(); }
    
    public String   getFileFormatType()    { return fileFormat.getType(); }
    public String   getCharSetType()       { return charSet.getType(); }
    public boolean  getDuplex()            { return duplex.get(); }
    public boolean  getDoubleLineSpacing() { return doubleLineSpacing.get(); }
    public boolean  getEightDots()         { return eightDots.get(); }
    public int      getColumns()           { return columns.get(); }
    public int      getRows()              { return rows.get(); }
    public boolean  getMultipleFiles()     { return multipleFiles.get(); }
    
    
    /* SETTERS */
    
    public void setFileFormatType    (String value)  { fileFormat.setType(value); }
    public void setCharSetType       (String value)  { charSet.setType(value); }
    public void setDuplex            (boolean value) { duplex.set(value); }
    public void setDoubleLineSpacing (boolean value) { doubleLineSpacing.set(value); }
    public void setEightDots         (boolean value) { eightDots.set(value); }
    public void setColumns           (int value)     { columns.set(value); }
    public void setRows              (int value)     { rows.set(value); }
    public void setMultipleFiles     (boolean value) { multipleFiles.set(value); }
    
    
    /***************************/
    /* PUBLIC STATIC CONSTANTS */
    /***************************/
    
    public static final String PEF = "be.docarch.odt2braille.PEFFileFormat";
    public static final String BRF = "org_daisy.BrailleEditorsFileFormatProvider.FileType.BRF";
    public static final String BRL = "org_daisy.BrailleEditorsFileFormatProvider.FileType.BRL";
    public static final String BRA = "org_daisy.BrailleEditorsFileFormatProvider.FileType.BRA";

    /****************************/
    /* PRIVATE STATIC CONSTANTS */
    /****************************/

    private static final Logger logger = Constants.getLogger();
    
    /****************************/
    /* PRIVATE CONSTANTS */
    /****************************/

    private final FileFormatCatalog fileFormatCatalog;
    private final TableCatalog charSetCatalog;
    private final Collection<String> fileFormatOptions = new ArrayList<String>();
    
    
    public ExportConfiguration() {

        logger.entering("ExportConfiguration","<init>");

        /************
           CATALOGS
         ************/

        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        Thread.currentThread().setContextClassLoader(Configuration.class.getClassLoader()); {

            fileFormatCatalog = new FileFormatCatalog();
            charSetCatalog = TableCatalog.newInstance();

        } Thread.currentThread().setContextClassLoader(cl);

        for (FileFormat f : fileFormatCatalog.list()) {
            fileFormatOptions.add(f.getIdentifier());
        }
        

        /***********************
           SETTING DECLARATION
         ***********************/

        fileFormat = new FileFormatSetting();
        charSet = new CharacterSetSetting();
        duplex = new DuplexSetting();
        doubleLineSpacing = new YesNoSetting();
        eightDots = new EightDotsSetting();
        multipleFiles = new MultipleFilesSetting();

        columns = new NumberSetting() {
            @Override
            public boolean accept(Integer value) { return value > 0; }
        };

        rows = new NumberSetting() {
            @Override
            public boolean accept(Integer value) { return value > 0; }
        };

        
        /**************************
           SETTING INITIALIZATION
         **************************/
        
        duplex.set(true);
        doubleLineSpacing.set(false);
        eightDots.set(false);
        multipleFiles.set(false);
        columns.set(40);
        rows.set(25);
        
        duplex.refresh();
        eightDots.refresh();
        charSet.refresh();
        

        /*******************
           SETTING LINKING
         *******************/

        fileFormat.addListener(duplex);
        fileFormat.addListener(eightDots);
        fileFormat.addListener(charSet);
        eightDots.addListener(charSet);

        logger.exiting("ExportConfiguration","<init>");
        
    }
    
    
    /*****************/
    /* INNER CLASSES */
    /*****************/
    
    public class FileFormatSetting extends OptionSetting<FileFormat> {

        private static final String DEFAULT_FILEFORMAT = BRF;
        private FileFormat format = fileFormatCatalog.get(DEFAULT_FILEFORMAT);

        public Collection<FileFormat> options() {
            return fileFormatCatalog.list();
        }

        @Override
        public boolean accept(FileFormat value) {
            return fileFormatOptions.contains(value.getIdentifier());
        }

        protected boolean update(FileFormat value) {
            if (value.getIdentifier().equals(format.getIdentifier())) { return false; }
            format = value;
            return true;
        }

        public FileFormat get() {
            return format;
        }
        
        public void setType(String type) {
            FileFormat f = fileFormatCatalog.get(type);
            if (f != null) { set(f); }
        }
        
        public String getType() {
            if (format == null) { return null; }
            return format.getIdentifier();
        }
    }
    
    public class CharacterSetSetting extends DependentOptionSetting<Table> {

        private static final String DEFAULT_CHARSET = "org_daisy.EmbosserTableProvider.TableType.MIT";
        private Table charSet = charSetCatalog.get(DEFAULT_CHARSET);
        
        @Override
        public boolean accept(Table value) {
            if (value == null) { return false; }
            try {
                BrailleConverter converter = value.newBrailleConverter();
                if (getEightDots() ^ converter.supportsEightDot()) { return false; }
                return fileFormat.get().supportsTable(value);
            } catch (UnsupportedCharsetException e) {
                return false;
            }
        }
        
        public Collection<Table> options() {
            Collection<Table> options = new ArrayList();
            for (FactoryProperties p : charSetCatalog.list()) {
                Table t = charSetCatalog.get(p.getIdentifier());
                if (accept(t)) {
                    options.add(t);
                }
            }
            return options;
        }

        protected boolean update(Table value) {
            charSet = value;
            if (value != null) {
                try {
                    getFileFormat().setFeature(EmbosserFeatures.TABLE, value);
                } catch (IllegalArgumentException e) {}
            }
            return true;
        }

        public Table get() {
            return charSet;
        }

        public boolean refresh() {
            if (accept(charSet)) {
                return update(charSet);
            } else {
                try {
                    return update(options().iterator().next());
                } catch (NoSuchElementException e) {
                    return update(null);
                }
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
    
    private class DuplexSetting extends DependentYesNoSetting {

        @Override
        public boolean refresh() {
            return update(accept(yesNo) ? yesNo : !yesNo);
        }

        @Override
        protected boolean update(Boolean value) {
            try {
                getFileFormat().setFeature(EmbosserFeatures.DUPLEX, value);
            } catch (IllegalArgumentException e) {
            }
            return super.update(value);
        }
        
        public boolean accept(Boolean value) {
            return getFileFormat().supportsDuplex() ? true : !value;
        }
    }
    
    private class EightDotsSetting extends DependentYesNoSetting {

        public boolean accept(Boolean value) {
            return getFileFormat().supports8dot() ? true : !value;
        }
    }

    private class MultipleFilesSetting extends YesNoSetting {

        @Override
        public boolean accept(Boolean value) {
          //return System.getProperty("os.name").toLowerCase().contains("mac os") ? !value : true;
            return true;
        }
    }
}
