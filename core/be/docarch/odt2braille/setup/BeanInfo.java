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
    
    private static final Map<Class,PropertyDescriptors> propertyDescriptors
               = new HashMap<Class,PropertyDescriptors>();
    
    public static PropertyDescriptor[] getPropertyDescriptors(Class type) {
        if (!propertyDescriptors.containsKey(type)) { return null; }
        return propertyDescriptors.get(type).list;
    }

    public static PropertyDescriptor getPropertyDescriptor(Class type, String property) {
        if (!propertyDescriptors.containsKey(type)) { return null; }
        return propertyDescriptors.get(type).get(property);
    }

    private static class PropertyDescriptors {

        public final PropertyDescriptor[] list;
        private final Map<String,PropertyDescriptor> map = new HashMap<String,PropertyDescriptor>();

        public PropertyDescriptors(PropertyDescriptor[] array) {
            list = array;
            for (PropertyDescriptor d : array) {
                map.put(d.getName(), d);
            }
        }

        public PropertyDescriptor[] values() {
            return list;
        }

        public PropertyDescriptor get(String property) {
            return map.get(property);
        }
    }

    static {
        
        Class type;
        
        try {
            
            /* Configuration */

            type = Configuration.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
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
                newReadOnlyPropertyDescriptor(type, "noteReferenceFormats"),
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
            }));
            
            /* ExportConfiguration */
            
            type = ExportConfiguration.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "fileFormatType"),
                newPropertyDescriptor(type, "duplex"),
                newPropertyDescriptor(type, "eightDots"),
                newPropertyDescriptor(type, "charSetType"),
                newPropertyDescriptor(type, "multipleFiles"),
                newPropertyDescriptor(type, "columns"),
                newPropertyDescriptor(type, "rows")
            }));
            
            /* EmbossConfiguration */
            
            type = EmbossConfiguration.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "embosserType"),
                newPropertyDescriptor(type, "duplex"),
                newPropertyDescriptor(type, "eightDots"),
                newPropertyDescriptor(type, "charSetType"),
                newPropertyDescriptor(type, "saddleStitch"),
                newPropertyDescriptor(type, "zFolding"),
                newPropertyDescriptor(type, "paperType"),
                newPropertyDescriptor(type, "pageOrientation"),
                newPropertyDescriptor(type, "pageWidth"),
                newPropertyDescriptor(type, "pageHeight"),
                newReadOnlyPropertyDescriptor(type, "margins")
            }));
            
            /* MarginSettings */
            
            type = MarginSettings.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "inner"),
                newPropertyDescriptor(type, "outer"),
                newPropertyDescriptor(type, "top"),
                newPropertyDescriptor(type, "bottom"),
            }));
            
            /* ParagraphStyle */
            
            type = ParagraphStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
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
            }));
            
            /* HeadingStyle */
            
            type = HeadingStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
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
            }));
            
            /* CharacterStyle */
            
            type = CharacterStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "inherit"),
                newPropertyDescriptor(type, "italic"),
                newPropertyDescriptor(type, "boldface"),
                newPropertyDescriptor(type, "underline"),
                newPropertyDescriptor(type, "capitals")
            }));
            
            /* ListStyle */
            
            type = ListStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "linesBetween"),
                newPropertyDescriptor(type, "dontSplit"),
                newPropertyDescriptor(type, "dontSplitItems"),
                newPropertyDescriptor(type, "prefix")
            }));
            
            /* TableStyle */
            
            type = TableStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
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
            }));
            
            /* TocStyle */
            
            type = TocStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newReadOnlyPropertyDescriptor(type, "levels"),
                newPropertyDescriptor(type, "title"),
                newPropertyDescriptor(type, "printPageNumbers"),
                newPropertyDescriptor(type, "braillePageNumbers"),
                newPropertyDescriptor(type, "linesBetween"),
                newPropertyDescriptor(type, "lineFillSymbol"),
                newPropertyDescriptor(type, "evaluateUptoLevel")
            }));
            
            /* TocLevelStyle */
            
            type = TocLevelStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers")
            }));
            
            /* FootnoteStyle */
            
            type = FootnoteStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "alignment"),
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "marginLeftRight"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow")
            }));
            
            /* FrameStyle */
            
            type = FrameStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "upperBorderEnabled"),
                newPropertyDescriptor(type, "lowerBorderEnabled"),
                newPropertyDescriptor(type, "paddingAbove"),
                newPropertyDescriptor(type, "paddingBelow"),
                newPropertyDescriptor(type, "upperBorderStyle"),
                newPropertyDescriptor(type, "lowerBorderStyle")
            }));
            
            /* PictureStyle */
            
            type = PictureStyle.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "firstLine"),
                newPropertyDescriptor(type, "runovers"),
                newPropertyDescriptor(type, "linesAbove"),
                newPropertyDescriptor(type, "linesBelow"),
                newPropertyDescriptor(type, "openingMark"),
                newPropertyDescriptor(type, "closingMark"),
                newPropertyDescriptor(type, "descriptionPrefix")
            }));
            
            /* Volume */
            
            type = Volume.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "title"),
                newPropertyDescriptor(type, "frontMatter"),
                newPropertyDescriptor(type, "tableOfContent"),
                newPropertyDescriptor(type, "transcribersNotesPage"),
                newPropertyDescriptor(type, "specialSymbolList")
            }));
            
            /* SectionVolume */
            
            type = SectionVolume.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "section")
            }));
            
            /* SpecialSymbol */
            
            type = SpecialSymbol.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "type"),
                newPropertyDescriptor(type, "symbol"),
                newPropertyDescriptor(type, "description"),
                newPropertyDescriptor(type, "mode")
            }));
            
            /* TranslationTable */
            
            type = TranslationTable.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                new PropertyDescriptor("id", type, "getID","setID")
            }));

            /* NoteReferenceFormat */

            type = NoteReferenceFormat.class;
            propertyDescriptors.put(type, new PropertyDescriptors(new PropertyDescriptor[] {
                newPropertyDescriptor(type, "spaceBefore"),
                newPropertyDescriptor(type, "spaceAfter"),
                newPropertyDescriptor(type, "prefix")
            }));

            
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
