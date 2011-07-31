package be.docarch.odt2braille.setup;

import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyDescriptor;

import java.beans.IntrospectionException;

import be.docarch.odt2braille.setup.style.*;
import be.docarch.odt2braille.setup.style.TocStyle.TocLevelStyle;
import be.docarch.odt2braille.setup.Configuration.SectionVolume;
import be.docarch.odt2braille.setup.Configuration.Volume;
import be.docarch.odt2braille.setup.EmbossConfiguration.MarginSettings;

public class BeanInfo {
    
    private static final Map<Class,Map<String,PropertyDescriptor>> propertyDescriptors
            = new HashMap<Class,Map<String,PropertyDescriptor>>();
    
    public static Map<String,PropertyDescriptor> getPropertyDescriptors(Class type) {
        if (!propertyDescriptors.containsKey(type)) { return null; }
        return propertyDescriptors.get(type);
    }
    
    static {
        
        Class type;
        PropertyDescriptor[] descriptors;
        Map<String,PropertyDescriptor> map;
        
        try {
            
            /* Configuration */
            
            type = Configuration.class;
            descriptors = new PropertyDescriptor[] {
                newReadOnlyPropertyDescriptor(type, "translationTables"),
                newPropertyDescriptor(type, "mathCode"),
                newPropertyDescriptor(type, "printPageNumbers"),
                newPropertyDescriptor(type, "braillePageNumbers"),
                newPropertyDescriptor(type, "continuePages"),
                newPropertyDescriptor(type, "printPageNumberRange"),
                newPropertyDescriptor(type, "pageSeparator"),
                newPropertyDescriptor(type, "pageSeparatorNumber"),
                newPropertyDescriptor(type, "ignoreEmptyPages"),
                newPropertyDescriptor(type, "mergeUnnumberedPages"),
                newPropertyDescriptor(type, "printPageNumberPosition"),
                newPropertyDescriptor(type, "braillePageNumberPosition"),
                newPropertyDescriptor(type, "pageNumberLineAtTop"),
                newPropertyDescriptor(type, "pageNumberLineAtBottom"),
                newPropertyDescriptor(type, "preliminaryPageNumberFormat"),
                newPropertyDescriptor(type, "beginningBraillePageNumber"),
                newReadOnlyPropertyDescriptor(type, "paragraphStyles"),
                newReadOnlyPropertyDescriptor(type, "characterStyles"),
                newReadOnlyPropertyDescriptor(type, "headingStyles"),
                newReadOnlyPropertyDescriptor(type, "tableStyles"),
                newReadOnlyPropertyDescriptor(type, "listStyles"),
                newReadOnlyPropertyDescriptor(type, "tocStyle"),
                newReadOnlyPropertyDescriptor(type, "frameStyle"),
                newReadOnlyPropertyDescriptor(type, "footnoteStyle"),
                newReadOnlyPropertyDescriptor(type, "pictureStyle"),
                newPropertyDescriptor(type, "hardPageBreaks"),
                newPropertyDescriptor(type, "creator"),
                newPropertyDescriptor(type, "hyphenate"),
                newPropertyDescriptor(type, "minSyllableLength"),
                newPropertyDescriptor(type, "transcribersNotesPageTitle"),
                newPropertyDescriptor(type, "specialSymbolListTitle"),
                newPropertyDescriptor(type, "frontMatterSection"),
                newPropertyDescriptor(type, "repeatFrontMatterSection"),
                newPropertyDescriptor(type, "titlePageSection"),
                newPropertyDescriptor(type, "rearMatterSection"),
                newPropertyDescriptor(type, "bodyMatterMode"),
                newPropertyDescriptor(type, "rearMatterMode"),
                newPropertyDescriptor(type, "preliminaryVolumeEnabled"),
                newPropertyDescriptor(type, "volumeInfoEnabled"),
                newPropertyDescriptor(type, "transcriptionInfoEnabled"),
                newPropertyDescriptor(type, "volumeInfoStyle"),
                newPropertyDescriptor(type, "transcriptionInfoStyle"),
                newReadOnlyPropertyDescriptor(type, "preliminaryVolume"),
                newReadOnlyPropertyDescriptor(type, "bodyMatterVolume"),
                newReadOnlyPropertyDescriptor(type, "rearMatterVolume"),
                newReadOnlyPropertyDescriptor(type, "sectionVolumeList"),
                newReadOnlyPropertyDescriptor(type, "specialSymbolList")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* ExportConfiguration */
            
            type = ExportConfiguration.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "fileFormatType"),
                newPropertyDescriptor(type, "duplex"),
                newPropertyDescriptor(type, "eightDots"),
                newPropertyDescriptor(type, "charSetType"),
                newPropertyDescriptor(type, "multipleFiles"),
                newPropertyDescriptor(type, "columns"),
                newPropertyDescriptor(type, "rows")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* EmbossConfiguration */
            
            type = EmbossConfiguration.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "embosserType"),
                newPropertyDescriptor(type, "duplex"),
                newPropertyDescriptor(type, "eightDots"),
                newPropertyDescriptor(type, "charSetType"),
                newPropertyDescriptor(type, "saddleStitch"),
                newPropertyDescriptor(type, "zFolding"),
                newPropertyDescriptor(type, "paperType"),
                newPropertyDescriptor(type, "paperWidth"),
                newPropertyDescriptor(type, "paperHeight"),
                newReadOnlyPropertyDescriptor(type, "margins")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* MarginSettings */
            
            type = MarginSettings.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "inner"),
                newPropertyDescriptor(type, "outer"),
                newPropertyDescriptor(type, "top"),
                newPropertyDescriptor(type, "bottom"),
            };
            
            /* ParagraphStyle */
            
            type = ParagraphStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "inherit"),
                newPropertyDescriptor(type, "alignment"),
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "marginLeftRight"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "keepEmptyParagraphs"),
                newPropertyDescriptor(type, "keepWithNext"),
                newPropertyDescriptor(type, "dontSplit"),
                newPropertyDescriptor(type, "widowControlEnabled"),
                newPropertyDescriptor(type, "orphanControlEnabled"),
                newPropertyDescriptor(type, "widowControl"),
                newPropertyDescriptor(type, "orphanControl")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* HeadingStyle */
            
            type = HeadingStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "alignment"),
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "marginLeftRight"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "newBraillePage"),
                newPropertyDescriptor(type, "keepWithNext"),
                newPropertyDescriptor(type, "dontSplit"),
                newPropertyDescriptor(type, "upperBorderEnabled"),
                newPropertyDescriptor(type, "lowerBorderEnabled"),
                newPropertyDescriptor(type, "paddingAbove"),
                newPropertyDescriptor(type, "paddingBelow"),
                newPropertyDescriptor(type, "upperBorderStyle"),
                newPropertyDescriptor(type, "lowerBorderStyle")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* CharacterStyle */
            
            type = CharacterStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "inherit"),
                newPropertyDescriptor(type, "italic"),
                newPropertyDescriptor(type, "boldface"),
                newPropertyDescriptor(type, "underline"),
                newPropertyDescriptor(type, "capitals")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* ListStyle */
            
            type = ListStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "linesBetween"),
                newPropertyDescriptor(type, "dontSplit"),
                newPropertyDescriptor(type, "dontSplitItems"),
                newPropertyDescriptor(type, "prefix")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* TableStyle */
            
            type = TableStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "linesBetween"),
                newPropertyDescriptor(type, "stairstepEnabled"),
                newPropertyDescriptor(type, "columnDelimiter"),
                newPropertyDescriptor(type, "indentPerColumn"),
                newPropertyDescriptor(type, "dontSplit"),
                newPropertyDescriptor(type, "dontSplitRows"),
                newPropertyDescriptor(type, "mirrorTable"),
                newPropertyDescriptor(type, "columnHeadings"),
                newPropertyDescriptor(type, "rowHeadings"),
                newPropertyDescriptor(type, "repeatHeading"),
                newPropertyDescriptor(type, "headingSuffix"),
                newPropertyDescriptor(type, "upperBorderEnabled"),
                newPropertyDescriptor(type, "lowerBorderEnabled"),
                newPropertyDescriptor(type, "paddingAbove"),
                newPropertyDescriptor(type, "paddingBelow"),
                newPropertyDescriptor(type, "upperBorderStyle"),
                newPropertyDescriptor(type, "lowerBorderStyle"),
                newPropertyDescriptor(type, "headingBorderStyle")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* TocStyle */
            
            type = TocStyle.class;
                descriptors = new PropertyDescriptor[] {
                newReadOnlyPropertyDescriptor(type, "levels"),
                newPropertyDescriptor(type, "title"),
                newPropertyDescriptor(type, "printPageNumbers"),
                newPropertyDescriptor(type, "braillePageNumbers"),
                newPropertyDescriptor(type, "linesBetween"),
                newPropertyDescriptor(type, "lineFillSymbol"),
                newPropertyDescriptor(type, "evaluateUptoLevel")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* TocLevelStyle */
            
            type = TocLevelStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* FootnoteStyle */
            
            type = FootnoteStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "alignment"),
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "marginLeftRight"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* FrameStyle */
            
            type = FrameStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "upperBorderEnabled"),
                newPropertyDescriptor(type, "lowerBorderEnabled"),
                newPropertyDescriptor(type, "paddingAbove"),
                newPropertyDescriptor(type, "paddingBelow"),
                newPropertyDescriptor(type, "upperBorderStyle"),
                newPropertyDescriptor(type, "lowerBorderStyle")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* PictureStyle */
            
            type = PictureStyle.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "openingMark"),
                newPropertyDescriptor(type, "closingMark"),
                newPropertyDescriptor(type, "descriptionPrefix")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* Volume */
            
            type = Volume.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "title"),
                newPropertyDescriptor(type, "frontMatter"),
                newPropertyDescriptor(type, "tableOfContent"),
                newPropertyDescriptor(type, "transcribersNotesPage"),
                newPropertyDescriptor(type, "specialSymbolList")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* SectionVolume */
            
            type = SectionVolume.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "section")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* SpecialSymbol */
            
            type = SpecialSymbol.class;
            descriptors = new PropertyDescriptor[] {
                newPropertyDescriptor(type, "type"),
                newPropertyDescriptor(type, "symbol"),
                newPropertyDescriptor(type, "description"),
                newPropertyDescriptor(type, "mode")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
            /* TranslationTable */
            
            type = TranslationTable.class;
            descriptors = new PropertyDescriptor[] {
                new PropertyDescriptor("id", type, "getID","setID")
            };
            map = new HashMap<String,PropertyDescriptor>();
            for (PropertyDescriptor d : descriptors) { map.put(d.getName(), d); }
            propertyDescriptors.put(type, map);
            
        } catch (IntrospectionException e) {
        }
    }
    
    private static PropertyDescriptor newPropertyDescriptor(Class type,
                                                            String property) 
                                                     throws IntrospectionException {
        return new PropertyDescriptor(property, type);
    }
    
    private static PropertyDescriptor newReadOnlyPropertyDescriptor(Class type,
                                                                    String property)
                                                             throws IntrospectionException {
        return new PropertyDescriptor(property, type, 
                "get" + property.substring(0,1).toUpperCase() + property.substring(1).toLowerCase(), null);
    }
}
