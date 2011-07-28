package be.docarch.odt2braille.setup;

import java.util.*;
import java.beans.DefaultPersistenceDelegate;
import java.beans.Expression;
import java.beans.Encoder;
import java.beans.PropertyDescriptor;
import java.beans.Statement;
import java.beans.IntrospectionException;
import java.lang.reflect.Method;

import be.docarch.odt2braille.setup.Configuration.SectionVolume;
import be.docarch.odt2braille.setup.Configuration.Volume;
import be.docarch.odt2braille.setup.style.*;
import be.docarch.odt2braille.setup.style.TocStyle.TocLevelStyle;


public class PersistenceDelegate extends DefaultPersistenceDelegate {

    @Override
    protected Expression instantiate(Object oldInstance, Encoder out) {

        System.out.println("instantiate " + oldInstance.getClass());

        if (oldInstance instanceof Configuration) {

            return new Expression(oldInstance, Configuration.class, "newInstance", new Object[] {});

        } else if (oldInstance instanceof Locale) {

            Locale oldLocale = (Locale)oldInstance;
            if (oldLocale.getCountry().length() > 0) {
                return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[]{oldLocale.getLanguage(), oldLocale.getCountry()});
            } else {
                return new Expression(oldInstance, oldInstance.getClass(), "new", new Object[]{oldLocale.getLanguage()});
            }
        }

        return super.instantiate(oldInstance, out);
    }

    @Override
    protected void initialize(Class type, Object oldInstance, Object newInstance, Encoder out) {

        System.out.println("initialize " + type);

        try {

            PropertyDescriptor[] descriptors = propertyDescriptorsMap.get(type);

            if (descriptors != null) {

                initialize(type.getSuperclass(), oldInstance, newInstance, out);

                for (PropertyDescriptor d : descriptors) {
                    Method getter = d.getReadMethod();
                    Method setter = d.getWriteMethod();
                    if (getter != null) {
                        Expression oldGetExp = new Expression(oldInstance, getter.getName(), new Object[]{});
                        Object oldValue = oldGetExp.getValue();
                        out.writeExpression(oldGetExp);
                        if (setter != null) {
                            Expression newGetExp = new Expression(newInstance, getter.getName(), new Object[]{});
                            Object newValue = newGetExp.getValue();
                            if (!equals(newValue, out.get(oldValue))) {
                                out.writeStatement(new Statement(oldInstance, setter.getName(), new Object[]{oldValue}));
                            }
                        }
                    }
                }

            } else if (type == SettingMap.class) {

                SettingMap oldMap = (SettingMap)oldInstance;
                SettingMap newMap = (SettingMap)newInstance;

                Object oldValue, newValue;

                for (Object key : oldMap.keys()) {

                    oldValue = oldMap.get(key);
                    newValue = newMap.get(key);

                    if (!oldValue.equals(newValue)) {
                        if (!(key instanceof String || key instanceof Integer)) {
                            out.writeExpression(instantiate(key, out));
                        }
                        out.writeExpression(new Expression(oldValue, oldInstance, "get", new Object[]{key}));
                    }
                }

            } else if (type == SettingList.class) {

                SettingList oldSettingList = (SettingList)oldInstance;
                SettingList newSettingList = (SettingList)newInstance;
                
                List oldList = oldSettingList.values();
                List newList = newSettingList.values();

                if (!oldList.equals(newList)) {
                    if (newList.size() > 0) {
                        out.writeStatement(new Statement(oldInstance, "clear", new Object[]{}));
                    }
                    for (Object oldObject : oldList) {
                        out.writeExpression(new Expression(oldObject, oldInstance, "add", new Object[]{}));
                    }
                }
            }
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected boolean mutatesTo(Object oldInstance, Object newInstance) {
        return (newInstance != null && oldInstance != null &&
                oldInstance.getClass() == newInstance.getClass());
    }
    
    private static boolean equals(Object o1, Object o2) {
        return (o1 == null) ? (o2 == null) : o1.equals(o2);
    }

    private static Map<Class,PropertyDescriptor[]> propertyDescriptorsMap = new HashMap<Class,PropertyDescriptor[]>();
    static {
        try {

            propertyDescriptorsMap.put(Configuration.class, new PropertyDescriptor[] {

                new PropertyDescriptor("translationTables",           Configuration.class, "getTranslationTables",   null),
                new PropertyDescriptor("embossConfiguration",         Configuration.class, "getEmbossConfiguration", null),
                new PropertyDescriptor("exportConfiguration",         Configuration.class, "getExportConfiguration", null),
                new PropertyDescriptor("mathCode",                    Configuration.class),
                new PropertyDescriptor("printPageNumbers",            Configuration.class),
                new PropertyDescriptor("braillePageNumbers",          Configuration.class),
                new PropertyDescriptor("continuePages",               Configuration.class),
                new PropertyDescriptor("printPageNumberRange",        Configuration.class),
                new PropertyDescriptor("pageSeparator",               Configuration.class),
                new PropertyDescriptor("pageSeparatorNumber",         Configuration.class),
                new PropertyDescriptor("ignoreEmptyPages",            Configuration.class),
                new PropertyDescriptor("mergeUnnumberedPages",        Configuration.class),
                new PropertyDescriptor("printPageNumberPosition",     Configuration.class),
                new PropertyDescriptor("braillePageNumberPosition",   Configuration.class),
                new PropertyDescriptor("pageNumberLineAtTop",         Configuration.class),
                new PropertyDescriptor("pageNumberLineAtBottom",      Configuration.class),
                new PropertyDescriptor("preliminaryPageNumberFormat", Configuration.class),
                new PropertyDescriptor("beginningBraillePageNumber",  Configuration.class),
                new PropertyDescriptor("paragraphStyles",             Configuration.class, "getParagraphStyles",     null),
                new PropertyDescriptor("characterStyles",             Configuration.class, "getCharacterStyles",     null),
                new PropertyDescriptor("headingStyles",               Configuration.class, "getHeadingStyles",       null),
                new PropertyDescriptor("tableStyles",                 Configuration.class, "getTableStyles",         null),
                new PropertyDescriptor("listStyles",                  Configuration.class, "getListStyles",          null),
                new PropertyDescriptor("tocStyle",                    Configuration.class, "getTocStyle",            null),
                new PropertyDescriptor("frameStyle",                  Configuration.class, "getFrameStyle",          null),
                new PropertyDescriptor("footnoteStyle",               Configuration.class, "getFootnoteStyle",       null),
                new PropertyDescriptor("pictureStyle",                Configuration.class, "getPictureStyle",        null),
                new PropertyDescriptor("hardPageBreaks",              Configuration.class),
                new PropertyDescriptor("creator",                     Configuration.class),
                new PropertyDescriptor("hyphenate",                   Configuration.class),
                new PropertyDescriptor("minSyllableLength",           Configuration.class),
                new PropertyDescriptor("transcribersNotesPageTitle",  Configuration.class),
                new PropertyDescriptor("specialSymbolListTitle",      Configuration.class),
                new PropertyDescriptor("frontMatterSection",          Configuration.class),
                new PropertyDescriptor("repeatFrontMatterSection",    Configuration.class),
                new PropertyDescriptor("titlePageSection",            Configuration.class),
                new PropertyDescriptor("rearMatterSection",           Configuration.class),
                new PropertyDescriptor("bodyMatterMode",              Configuration.class),
                new PropertyDescriptor("rearMatterMode",              Configuration.class),
                new PropertyDescriptor("preliminaryVolumeEnabled",    Configuration.class),
                new PropertyDescriptor("volumeInfoEnabled",           Configuration.class),
                new PropertyDescriptor("transcriptionInfoEnabled",    Configuration.class),
                new PropertyDescriptor("volumeInfoStyle",             Configuration.class),
                new PropertyDescriptor("transcriptionInfoStyle",      Configuration.class),
                new PropertyDescriptor("preliminaryVolume",           Configuration.class, "getPreliminaryVolume",   null),
                new PropertyDescriptor("bodyMatterVolume",            Configuration.class, "getBodyMatterVolume",    null),
                new PropertyDescriptor("rearMatterVolume",            Configuration.class, "getRearMatterVolume",    null),
                new PropertyDescriptor("sectionVolumeList",           Configuration.class, "getSectionVolumeList",   null),
                new PropertyDescriptor("specialSymbolList",           Configuration.class, "getSpecialSymbolList",   null)
            });

            propertyDescriptorsMap.put(EmbossConfiguration.class, new PropertyDescriptor[] {

                new PropertyDescriptor("embosserType",  EmbossConfiguration.class),
                new PropertyDescriptor("duplex",        EmbossConfiguration.class),
                new PropertyDescriptor("eightDots",     EmbossConfiguration.class),
                new PropertyDescriptor("charSetType",   EmbossConfiguration.class),
                new PropertyDescriptor("saddleStitch",  EmbossConfiguration.class),
                new PropertyDescriptor("zFolding",      EmbossConfiguration.class),
                new PropertyDescriptor("paperType",     EmbossConfiguration.class),
                new PropertyDescriptor("paperWidth",    EmbossConfiguration.class),
                new PropertyDescriptor("paperHeight",   EmbossConfiguration.class),
                new PropertyDescriptor("margins",       EmbossConfiguration.class,  "getMargins",  null)
            });

            propertyDescriptorsMap.put(ExportConfiguration.class, new PropertyDescriptor[] {

                new PropertyDescriptor("fileFormatType", ExportConfiguration.class),
                new PropertyDescriptor("duplex",         ExportConfiguration.class),
                new PropertyDescriptor("eightDots",      ExportConfiguration.class),
                new PropertyDescriptor("charSetType",    ExportConfiguration.class),
                new PropertyDescriptor("multipleFiles",  ExportConfiguration.class),
                new PropertyDescriptor("columns",        ExportConfiguration.class),
                new PropertyDescriptor("rows",           ExportConfiguration.class)                        
            });

            propertyDescriptorsMap.put(ParagraphStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("inherit",              ParagraphStyle.class),
                new PropertyDescriptor("alignment",            ParagraphStyle.class),
                new PropertyDescriptor("firstLine",            ParagraphStyle.class),
                new PropertyDescriptor("runovers",             ParagraphStyle.class),
                new PropertyDescriptor("marginLeftRight",      ParagraphStyle.class),
                new PropertyDescriptor("linesAbove",           ParagraphStyle.class),
                new PropertyDescriptor("linesBelow",           ParagraphStyle.class),
                new PropertyDescriptor("keepEmptyParagraphs",  ParagraphStyle.class),
                new PropertyDescriptor("keepWithNext",         ParagraphStyle.class),
                new PropertyDescriptor("dontSplit",            ParagraphStyle.class),
                new PropertyDescriptor("widowControlEnabled",  ParagraphStyle.class),
                new PropertyDescriptor("orphanControlEnabled", ParagraphStyle.class),
                new PropertyDescriptor("widowControl",         ParagraphStyle.class),
                new PropertyDescriptor("orphanControl",        ParagraphStyle.class)
            });

            propertyDescriptorsMap.put(HeadingStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("alignment",          HeadingStyle.class),
                new PropertyDescriptor("firstLine",          HeadingStyle.class),
                new PropertyDescriptor("runovers",           HeadingStyle.class),
                new PropertyDescriptor("marginLeftRight",    HeadingStyle.class),
                new PropertyDescriptor("linesAbove",         HeadingStyle.class),
                new PropertyDescriptor("linesBelow",         HeadingStyle.class),
                new PropertyDescriptor("newBraillePage",     HeadingStyle.class),
                new PropertyDescriptor("keepWithNext",       HeadingStyle.class),
                new PropertyDescriptor("dontSplit",          HeadingStyle.class),
                new PropertyDescriptor("upperBorderEnabled", HeadingStyle.class),
                new PropertyDescriptor("lowerBorderEnabled", HeadingStyle.class),
                new PropertyDescriptor("paddingAbove",       HeadingStyle.class),
                new PropertyDescriptor("paddingBelow",       HeadingStyle.class),
                new PropertyDescriptor("upperBorderStyle",   HeadingStyle.class),
                new PropertyDescriptor("lowerBorderStyle",   HeadingStyle.class)
            });

            propertyDescriptorsMap.put(CharacterStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("inherit",    CharacterStyle.class),
                new PropertyDescriptor("italic",     CharacterStyle.class),
                new PropertyDescriptor("boldface",   CharacterStyle.class),
                new PropertyDescriptor("underline",  CharacterStyle.class),
                new PropertyDescriptor("capitals",   CharacterStyle.class)
            });

            propertyDescriptorsMap.put(ListStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("firstLine",      ListStyle.class),
                new PropertyDescriptor("runovers",       ListStyle.class),
                new PropertyDescriptor("linesAbove",     ListStyle.class),
                new PropertyDescriptor("linesBelow",     ListStyle.class),
                new PropertyDescriptor("linesBetween",   ListStyle.class),
                new PropertyDescriptor("dontSplit",      ListStyle.class),
                new PropertyDescriptor("dontSplitItems", ListStyle.class),
                new PropertyDescriptor("prefix",         ListStyle.class)
            });

            propertyDescriptorsMap.put(TableStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("firstLine",          TableStyle.class),
                new PropertyDescriptor("runovers",           TableStyle.class),
                new PropertyDescriptor("linesAbove",         TableStyle.class),
                new PropertyDescriptor("linesBelow",         TableStyle.class),
                new PropertyDescriptor("linesBetween",       TableStyle.class),
                new PropertyDescriptor("stairstepEnabled",   TableStyle.class),
                new PropertyDescriptor("columnDelimiter",    TableStyle.class),
                new PropertyDescriptor("indentPerColumn",    TableStyle.class),
                new PropertyDescriptor("dontSplit",          TableStyle.class),
                new PropertyDescriptor("dontSplitRows",      TableStyle.class),
                new PropertyDescriptor("mirrorTable",        TableStyle.class),
                new PropertyDescriptor("columnHeadings",     TableStyle.class),
                new PropertyDescriptor("rowHeadings",        TableStyle.class),
                new PropertyDescriptor("repeatHeading",      TableStyle.class),
                new PropertyDescriptor("headingSuffix",      TableStyle.class),
                new PropertyDescriptor("upperBorderEnabled", TableStyle.class),
                new PropertyDescriptor("lowerBorderEnabled", TableStyle.class),
                new PropertyDescriptor("paddingAbove",       TableStyle.class),
                new PropertyDescriptor("paddingBelow",       TableStyle.class),
                new PropertyDescriptor("upperBorderStyle",   TableStyle.class),
                new PropertyDescriptor("lowerBorderStyle",   TableStyle.class),
                new PropertyDescriptor("headingBorderStyle", TableStyle.class)
            });

            propertyDescriptorsMap.put(TocStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("levels",             TocStyle.class, "getLevels", null),
                new PropertyDescriptor("title",              TocStyle.class),
                new PropertyDescriptor("printPageNumbers",   TocStyle.class),
                new PropertyDescriptor("braillePageNumbers", TocStyle.class),
                new PropertyDescriptor("linesBetween",       TocStyle.class),
                new PropertyDescriptor("lineFillSymbol",     TocStyle.class),
                new PropertyDescriptor("evaluateUptoLevel",  TocStyle.class)
            });

            propertyDescriptorsMap.put(TocLevelStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("firstLine", TocLevelStyle.class),
                new PropertyDescriptor("runovers",  TocLevelStyle.class)
            });

            propertyDescriptorsMap.put(FootnoteStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("alignment",       FootnoteStyle.class),
                new PropertyDescriptor("firstLine",       FootnoteStyle.class),
                new PropertyDescriptor("runovers",        FootnoteStyle.class),
                new PropertyDescriptor("marginLeftRight", FootnoteStyle.class),
                new PropertyDescriptor("linesAbove",      FootnoteStyle.class),
                new PropertyDescriptor("linesBelow",      FootnoteStyle.class)
            });

            propertyDescriptorsMap.put(FrameStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("linesAbove",         FrameStyle.class),
                new PropertyDescriptor("linesBelow",         FrameStyle.class),
                new PropertyDescriptor("upperBorderEnabled", FrameStyle.class),
                new PropertyDescriptor("lowerBorderEnabled", FrameStyle.class),
                new PropertyDescriptor("paddingAbove",       FrameStyle.class),
                new PropertyDescriptor("paddingBelow",       FrameStyle.class),
                new PropertyDescriptor("upperBorderStyle",   FrameStyle.class),
                new PropertyDescriptor("lowerBorderStyle",   FrameStyle.class)
            });

            propertyDescriptorsMap.put(PictureStyle.class, new PropertyDescriptor[] {

                new PropertyDescriptor("firstLine",         PictureStyle.class),
                new PropertyDescriptor("runovers",          PictureStyle.class),
                new PropertyDescriptor("linesAbove",        PictureStyle.class),
                new PropertyDescriptor("linesBelow",        PictureStyle.class),
                new PropertyDescriptor("openingMark",       PictureStyle.class),
                new PropertyDescriptor("closingMark",       PictureStyle.class),
                new PropertyDescriptor("descriptionPrefix", PictureStyle.class)
            });

            propertyDescriptorsMap.put(Volume.class, new PropertyDescriptor[] {

                new PropertyDescriptor("title",                 Volume.class),
                new PropertyDescriptor("frontMatter",           Volume.class),
                new PropertyDescriptor("tableOfContent",        Volume.class),
                new PropertyDescriptor("transcribersNotesPage", Volume.class),
                new PropertyDescriptor("specialSymbolList",     Volume.class)
            });


            propertyDescriptorsMap.put(SectionVolume.class, new PropertyDescriptor[] {

                new PropertyDescriptor("section", SectionVolume.class)
            });

            propertyDescriptorsMap.put(SpecialSymbol.class, new PropertyDescriptor[] {

                new PropertyDescriptor("type",        SpecialSymbol.class),
                new PropertyDescriptor("symbol",      SpecialSymbol.class),
                new PropertyDescriptor("description", SpecialSymbol.class),
                new PropertyDescriptor("mode",        SpecialSymbol.class)
            });

            propertyDescriptorsMap.put(TranslationTable.class, new PropertyDescriptor[] {

                new PropertyDescriptor("id", TranslationTable.class, "getID", "setID")
            });
            
        } catch (IntrospectionException e) {
            e.printStackTrace();
        }
    }
}
