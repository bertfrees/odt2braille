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

import be.docarch.odt2braille.setup.style.*;
import be.docarch.odt2braille.setup.style.TocStyle.TocLevelStyle;
import be.docarch.odt2braille.setup.Configuration.SectionVolume;
import be.docarch.odt2braille.setup.Configuration.SplittableVolume;
import be.docarch.odt2braille.setup.Configuration.Volume;
import be.docarch.odt2braille.setup.EmbossConfiguration.MarginSettings;

import java.util.Map;
import java.util.HashMap;
import java.beans.PropertyDescriptor;
import java.beans.IntrospectionException;

public class BeanInfo {
    
    private static final Map<Class,PropertyDescriptor[]> propertyDescriptorsMap
               = new HashMap<Class,PropertyDescriptor[]>();
    
    public static PropertyDescriptor[] getPropertyDescriptors(Class beanClass) {
        if (!propertyDescriptorsMap.containsKey(beanClass)) { return null; }
        return propertyDescriptorsMap.get(beanClass);
    }

    public static PropertyDescriptor getPropertyDescriptor(Class beanClass, String property) {
        PropertyDescriptor[] propertyDescriptors = getPropertyDescriptors(beanClass);
        while (propertyDescriptors != null) {
            for (int i=0; i<propertyDescriptors.length; i++) {
                if (propertyDescriptors[i].getName().equals(property)) {
                    return propertyDescriptors[i];
                }
            }
            beanClass = beanClass.getSuperclass();
            propertyDescriptors = getPropertyDescriptors(beanClass);
        }
        return null;
    }

    static {
        
        Class beanClass;
        
        try {
            
            /* Configuration */

            beanClass = Configuration.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newReadOnlyPropertyDescriptor(beanClass, "translationTables"),
                newPropertyDescriptor(beanClass, "mathCode"),
                newPropertyDescriptor(beanClass, "printPageNumbers"),
                newPropertyDescriptor(beanClass, "braillePageNumbers"),
                newPropertyDescriptor(beanClass, "continuePages"),
                newPropertyDescriptor(beanClass, "printPageNumberRange"),
                newPropertyDescriptor(beanClass, "pageSeparator"),
                newPropertyDescriptor(beanClass, "pageSeparatorNumber"),
                newPropertyDescriptor(beanClass, "ignoreEmptyPages"),
                newPropertyDescriptor(beanClass, "mergeUnnumberedPages"),
                newPropertyDescriptor(beanClass, "printPageNumberPosition"),
                newPropertyDescriptor(beanClass, "braillePageNumberPosition"),
                newPropertyDescriptor(beanClass, "pageNumberLineAtTop"),
                newPropertyDescriptor(beanClass, "pageNumberLineAtBottom"),
                newPropertyDescriptor(beanClass, "preliminaryPageNumberFormat"),
                newPropertyDescriptor(beanClass, "beginningBraillePageNumber"),
                newReadOnlyPropertyDescriptor(beanClass, "paragraphStyles"),
                newReadOnlyPropertyDescriptor(beanClass, "characterStyles"),
                newReadOnlyPropertyDescriptor(beanClass, "headingStyles"),
                newReadOnlyPropertyDescriptor(beanClass, "tableStyles"),
                newReadOnlyPropertyDescriptor(beanClass, "listStyles"),
                newReadOnlyPropertyDescriptor(beanClass, "tocStyle"),
                newReadOnlyPropertyDescriptor(beanClass, "frameStyle"),
                newReadOnlyPropertyDescriptor(beanClass, "footnoteStyle"),
                newReadOnlyPropertyDescriptor(beanClass, "pictureStyle"),
                newReadOnlyPropertyDescriptor(beanClass, "noteReferenceFormats"),
                newPropertyDescriptor(beanClass, "creator"),
                newPropertyDescriptor(beanClass, "hyphenate"),
                newPropertyDescriptor(beanClass, "minSyllableLength"),
                newPropertyDescriptor(beanClass, "transcribersNotesPageTitle"),
                newPropertyDescriptor(beanClass, "specialSymbolListTitle"),
                newPropertyDescriptor(beanClass, "endNotesPageTitle"),
                newPropertyDescriptor(beanClass, "frontMatterSection"),
                newPropertyDescriptor(beanClass, "repeatFrontMatterSection"),
                newPropertyDescriptor(beanClass, "titlePageSection"),
                newPropertyDescriptor(beanClass, "rearMatterSection"),
                newPropertyDescriptor(beanClass, "bodyMatterMode"),
                newPropertyDescriptor(beanClass, "rearMatterMode"),
                newPropertyDescriptor(beanClass, "preliminaryVolumeEnabled"),
                newPropertyDescriptor(beanClass, "volumeInfoEnabled"),
                newPropertyDescriptor(beanClass, "transcriptionInfoEnabled"),
                newPropertyDescriptor(beanClass, "volumeInfoStyleID"),
                newPropertyDescriptor(beanClass, "transcriptionInfoStyleID"),
                newReadOnlyPropertyDescriptor(beanClass, "preliminaryVolume"),
                newReadOnlyPropertyDescriptor(beanClass, "bodyMatterVolume"),
                newReadOnlyPropertyDescriptor(beanClass, "rearMatterVolume"),
                newReadOnlyPropertyDescriptor(beanClass, "sectionVolumeList"),
                newReadOnlyPropertyDescriptor(beanClass, "specialSymbolList")
            });
            
            /* ExportConfiguration */
            
            beanClass = ExportConfiguration.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "fileFormatType"),
                newPropertyDescriptor(beanClass, "duplex"),
                newPropertyDescriptor(beanClass, "eightDots"),
                newPropertyDescriptor(beanClass, "charSetType"),
                newPropertyDescriptor(beanClass, "multipleFiles"),
                newPropertyDescriptor(beanClass, "columns"),
                newPropertyDescriptor(beanClass, "rows")
            });
            
            /* EmbossConfiguration */
            
            beanClass = EmbossConfiguration.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "embosserType"),
                newPropertyDescriptor(beanClass, "duplex"),
                newPropertyDescriptor(beanClass, "eightDots"),
                newPropertyDescriptor(beanClass, "charSetType"),
                newPropertyDescriptor(beanClass, "magazineMode"),
                newPropertyDescriptor(beanClass, "zFolding"),
                newPropertyDescriptor(beanClass, "paperType"),
                newPropertyDescriptor(beanClass, "pageOrientation"),
                newPropertyDescriptor(beanClass, "pageWidth"),
                newPropertyDescriptor(beanClass, "pageHeight"),
                newReadOnlyPropertyDescriptor(beanClass, "margins")
            });
            
            /* MarginSettings */
            
            beanClass = MarginSettings.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "inner"),
                newPropertyDescriptor(beanClass, "outer"),
                newPropertyDescriptor(beanClass, "top"),
                newPropertyDescriptor(beanClass, "bottom"),
            });
            
            /* ParagraphStyle */
            
            beanClass = ParagraphStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "inherit"),
                newPropertyDescriptor(beanClass, "alignment"),
                newPropertyDescriptor(beanClass, "firstLine"),
                newPropertyDescriptor(beanClass, "runovers"),
                newPropertyDescriptor(beanClass, "marginLeftRight"),
                newPropertyDescriptor(beanClass, "linesAbove"),
                newPropertyDescriptor(beanClass, "linesBelow"),
                newPropertyDescriptor(beanClass, "emptyParagraphs"),
                newPropertyDescriptor(beanClass, "hardPageBreaks"),
                newPropertyDescriptor(beanClass, "keepWithNext"),
                newPropertyDescriptor(beanClass, "dontSplit"),
                newPropertyDescriptor(beanClass, "widowControlEnabled"),
                newPropertyDescriptor(beanClass, "orphanControlEnabled"),
                newPropertyDescriptor(beanClass, "widowControl"),
                newPropertyDescriptor(beanClass, "orphanControl")
            });
            
            /* HeadingStyle */
            
            beanClass = HeadingStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "alignment"),
                newPropertyDescriptor(beanClass, "firstLine"),
                newPropertyDescriptor(beanClass, "runovers"),
                newPropertyDescriptor(beanClass, "marginLeftRight"),
                newPropertyDescriptor(beanClass, "linesAbove"),
                newPropertyDescriptor(beanClass, "linesBelow"),
                newPropertyDescriptor(beanClass, "newBraillePage"),
                newPropertyDescriptor(beanClass, "keepWithNext"),
                newPropertyDescriptor(beanClass, "dontSplit"),
                newPropertyDescriptor(beanClass, "upperBorderEnabled"),
                newPropertyDescriptor(beanClass, "lowerBorderEnabled"),
                newPropertyDescriptor(beanClass, "paddingAbove"),
                newPropertyDescriptor(beanClass, "paddingBelow"),
                newPropertyDescriptor(beanClass, "upperBorderStyle"),
                newPropertyDescriptor(beanClass, "lowerBorderStyle")
            });
            
            /* CharacterStyle */
            
            beanClass = CharacterStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "inherit"),
                newPropertyDescriptor(beanClass, "italic"),
                newPropertyDescriptor(beanClass, "boldface"),
                newPropertyDescriptor(beanClass, "underline"),
                newPropertyDescriptor(beanClass, "capitals")
            });
            
            /* ListStyle */
            
            beanClass = ListStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "firstLine"),
                newPropertyDescriptor(beanClass, "runovers"),
                newPropertyDescriptor(beanClass, "linesAbove"),
                newPropertyDescriptor(beanClass, "linesBelow"),
                newPropertyDescriptor(beanClass, "linesBetween"),
                newPropertyDescriptor(beanClass, "dontSplit"),
                newPropertyDescriptor(beanClass, "dontSplitItems"),
                newPropertyDescriptor(beanClass, "prefix")
            });
            
            /* TableStyle */
            
            beanClass = TableStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "firstLine"),
                newPropertyDescriptor(beanClass, "runovers"),
                newPropertyDescriptor(beanClass, "linesAbove"),
                newPropertyDescriptor(beanClass, "linesBelow"),
                newPropertyDescriptor(beanClass, "linesBetween"),
                newPropertyDescriptor(beanClass, "stairstepEnabled"),
                newPropertyDescriptor(beanClass, "columnDelimiter"),
                newPropertyDescriptor(beanClass, "indentPerColumn"),
                newPropertyDescriptor(beanClass, "dontSplit"),
                newPropertyDescriptor(beanClass, "dontSplitRows"),
                newPropertyDescriptor(beanClass, "mirrorTable"),
                newPropertyDescriptor(beanClass, "columnHeadings"),
                newPropertyDescriptor(beanClass, "rowHeadings"),
                newPropertyDescriptor(beanClass, "repeatHeading"),
                newPropertyDescriptor(beanClass, "headingSuffix"),
                newPropertyDescriptor(beanClass, "upperBorderEnabled"),
                newPropertyDescriptor(beanClass, "lowerBorderEnabled"),
                newPropertyDescriptor(beanClass, "paddingAbove"),
                newPropertyDescriptor(beanClass, "paddingBelow"),
                newPropertyDescriptor(beanClass, "upperBorderStyle"),
                newPropertyDescriptor(beanClass, "lowerBorderStyle"),
                newPropertyDescriptor(beanClass, "headingBorderStyle")
            });
            
            /* TocStyle */
            
            beanClass = TocStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newReadOnlyPropertyDescriptor(beanClass, "levels"),
                newPropertyDescriptor(beanClass, "title"),
                newPropertyDescriptor(beanClass, "printPageNumbers"),
                newPropertyDescriptor(beanClass, "braillePageNumbers"),
                newPropertyDescriptor(beanClass, "linesBetween"),
                newPropertyDescriptor(beanClass, "lineFillSymbol"),
                newPropertyDescriptor(beanClass, "evaluateUptoLevel")
            });
            
            /* TocLevelStyle */
            
            beanClass = TocLevelStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "firstLine"),
                newPropertyDescriptor(beanClass, "runovers")
            });
            
            /* FootnoteStyle */
            
            beanClass = FootnoteStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "alignment"),
                newPropertyDescriptor(beanClass, "firstLine"),
                newPropertyDescriptor(beanClass, "runovers"),
                newPropertyDescriptor(beanClass, "marginLeftRight"),
                newPropertyDescriptor(beanClass, "linesAbove"),
                newPropertyDescriptor(beanClass, "linesBelow")
            });
            
            /* FrameStyle */
            
            beanClass = FrameStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "linesAbove"),
                newPropertyDescriptor(beanClass, "linesBelow"),
                newPropertyDescriptor(beanClass, "upperBorderEnabled"),
                newPropertyDescriptor(beanClass, "lowerBorderEnabled"),
                newPropertyDescriptor(beanClass, "paddingAbove"),
                newPropertyDescriptor(beanClass, "paddingBelow"),
                newPropertyDescriptor(beanClass, "upperBorderStyle"),
                newPropertyDescriptor(beanClass, "lowerBorderStyle")
            });
            
            /* PictureStyle */
            
            beanClass = PictureStyle.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "firstLine"),
                newPropertyDescriptor(beanClass, "runovers"),
                newPropertyDescriptor(beanClass, "linesAbove"),
                newPropertyDescriptor(beanClass, "linesBelow"),
                newPropertyDescriptor(beanClass, "openingMark"),
                newPropertyDescriptor(beanClass, "closingMark"),
                newPropertyDescriptor(beanClass, "descriptionPrefix")
            });
            
            /* Volume */
            
            beanClass = Volume.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "title"),
                newPropertyDescriptor(beanClass, "frontMatter"),
                newPropertyDescriptor(beanClass, "tableOfContent"),
                newPropertyDescriptor(beanClass, "transcribersNotesPage"),
                newPropertyDescriptor(beanClass, "specialSymbolList")
            });
            
            /* SectionVolume */
            
            beanClass = SectionVolume.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "section")
            });

            /* SplittableVolume */
            beanClass = SplittableVolume.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "preferredVolumeSize"),
                newPropertyDescriptor(beanClass, "maxVolumeSize"),
                newPropertyDescriptor(beanClass, "minVolumeSize"),
                newPropertyDescriptor(beanClass, "minLastVolumeSize")
            });
            
            /* SpecialSymbol */
            
            beanClass = SpecialSymbol.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "type"),
                newPropertyDescriptor(beanClass, "symbol"),
                newPropertyDescriptor(beanClass, "description"),
                newPropertyDescriptor(beanClass, "mode")
            });
            
            /* TranslationTable */
            
            beanClass = TranslationTable.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                new PropertyDescriptor("id", beanClass, "getID","setID")
            });

            /* NoteReferenceFormat */

            beanClass = NoteReferenceFormat.class;
            propertyDescriptorsMap.put(beanClass, new PropertyDescriptor[] {
                newPropertyDescriptor(beanClass, "spaceBefore"),
                newPropertyDescriptor(beanClass, "spaceAfter"),
                newPropertyDescriptor(beanClass, "prefix")
            });

            
        } catch (IntrospectionException e) {
        }
    }
    
    private static PropertyDescriptor newPropertyDescriptor(Class beanClass,
                                                            String property) 
                                                     throws IntrospectionException {
        return new PropertyDescriptor(property, beanClass);
    }
    
    private static PropertyDescriptor newReadOnlyPropertyDescriptor(Class beanClass,
                                                                    String property)
                                                             throws IntrospectionException {
        return new PropertyDescriptor(property, beanClass, 
                "get" + property.substring(0,1).toUpperCase() + property.substring(1).toLowerCase(), null);
    }
}
