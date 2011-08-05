<?xml version="1.0" encoding="UTF-8"?>

    <!--

    odt2braille - Braille authoring in OpenOffice.org.

    Copyright (c) 2010 by DocArch <http://www.docarch.be>.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Lesser General Public License as
    published by the Free Software Foundation, either version 3 of the
    License, or (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
    GNU Lesser General Public License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this program.  If not, see <http://www.gnu.org/licenses/>.

    -->

<xsl:stylesheet version="2.0"

        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
        xmlns:ns1="http://www.docarch.be/accessibility/properties#"
        xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
        xmlns:xlink="http://www.w3.org/1999/xlink"
        xmlns:dc="http://purl.org/dc/elements/1.1/"
        xmlns:math="http://www.w3.org/1998/Math/MathML"
        xmlns:dom="http://www.w3.org/2001/xml-events"
        xmlns:xforms="http://www.w3.org/2002/xforms"
        xmlns:xsd="http://www.w3.org/2001/XMLSchema"
        xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
        xmlns:exsl="http://exslt.org/common"
        xmlns:xalan="http://xml.apache.org/xslt"
        xmlns:office="urn:oasis:names:tc:opendocument:xmlns:office:1.0"
        xmlns:style="urn:oasis:names:tc:opendocument:xmlns:style:1.0"
        xmlns:text="urn:oasis:names:tc:opendocument:xmlns:text:1.0"
        xmlns:table="urn:oasis:names:tc:opendocument:xmlns:table:1.0"
        xmlns:draw="urn:oasis:names:tc:opendocument:xmlns:drawing:1.0"
        xmlns:fo="urn:oasis:names:tc:opendocument:xmlns:xsl-fo-compatible:1.0"
        xmlns:meta="urn:oasis:names:tc:opendocument:xmlns:meta:1.0"
        xmlns:number="urn:oasis:names:tc:opendocument:xmlns:datastyle:1.0"
        xmlns:svg="urn:oasis:names:tc:opendocument:xmlns:svg-compatible:1.0"
        xmlns:chart="urn:oasis:names:tc:opendocument:xmlns:chart:1.0"
        xmlns:dr3d="urn:oasis:names:tc:opendocument:xmlns:dr3d:1.0"
        xmlns:form="urn:oasis:names:tc:opendocument:xmlns:form:1.0"
        xmlns:script="urn:oasis:names:tc:opendocument:xmlns:script:1.0"
        xmlns:o2b="http://odt2braille.sf.net"

        exclude-result-prefixes="o2b dtb exsl office style dom xforms xsi xsd text table draw fo
                                 xlink number svg chart dr3d math form script dc meta xalan rdf ns1" >

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"
                    xalan:indent-amount="3" />

        <!-- XSLT Parameters  -->

        <xsl:param name="paramBodyMatterMode"             as="xsd:string"   select="'SINGLE'"  />
        <xsl:param name="paramRearMatterMode"             as="xsd:string"   select="'SINGLE'"  />

        <xsl:param name="paramColumnDelimiter"            as="xsd:string"   select='"&#x2830;"' />
        <xsl:param name="paramTableHeadingSuffix"         as="xsd:string"   select='"&#x2812;"' />
        <xsl:param name="paramEllipsis"			  as="xsd:string"   select="'&#x2810;&#x2810;&#x2810;'" />
        <xsl:param name="paramDoubleDash"		  as="xsd:string"   select="'&#x2824;&#x2824;&#x2824;&#x2824;'" />
        <xsl:param name="paramEllipsisDots"		  as="xsd:string"   select="'(5, 5, 5)'" />
        <xsl:param name="paramDoubleDashDots"		  as="xsd:string"   select="'(36, 36, 36, 36)'" />
        <xsl:param name="paramPictureDescriptionPrefix"   as="xsd:string"   select="'Picture description:'"/>
        <xsl:param name="paramPictureOpeningMark"         as="xsd:string"   select="'&#x2820;&#x2804;'"/>
        <xsl:param name="paramPictureClosingMark"         as="xsd:string"   select="'&#x2820;&#x2804;'"/>

        <xsl:param name="paramConfiguredParagraphStyles"  as="xsd:string*"  />
        <xsl:param name="paramKeepEmptyParagraphStyles"   as="xsd:string*"  />
        <xsl:param name="paramNoterefNumberPrefixes"      as="xsd:string*"  />
        <xsl:param name="paramNoterefNumberFormats"       as="xsd:string*"  />

        <xsl:param name="paramTocUptoLevel"               as="xsd:integer"  select="2" />

        <xsl:param name="paramHyphenationEnabled"         as="xsd:boolean"  select="false()" />
        <xsl:param name="paramKeepHardPageBreaks"         as="xsd:boolean"  select="false()" />
        <xsl:param name="paramNoterefSpaceBefore"         as="xsd:boolean"  select="false()" />
        <xsl:param name="paramNoterefSpaceAfter"          as="xsd:boolean"  select="false()" />
        <xsl:param name="paramStairstepTableEnabled"      as="xsd:boolean"  select="false()" />
        <xsl:param name="paramColumnHeadings"             as="xsd:boolean"  select="false()" />
        <xsl:param name="paramRowHeadings"                as="xsd:boolean"  select="false()" />
        <xsl:param name="paramRepeatTableHeading"         as="xsd:boolean"  select="false()" />
        <xsl:param name="paramMirrorTable"                as="xsd:boolean"  select="false()" />
        <xsl:param name="paramTableUpperBorder"           as="xsd:boolean"  select="false()" />
        <xsl:param name="paramTableLowerBorder"           as="xsd:boolean"  select="false()" />
        <xsl:param name="paramFrameUpperBorder"           as="xsd:boolean"  select="false()" />
        <xsl:param name="paramFrameLowerBorder"           as="xsd:boolean"  select="false()" />
        <xsl:param name="paramHeadingUpperBorder"         as="xsd:boolean*" select="(false(),false(),false(),false(),false(),false(),false(),false(),false(),false())" />
        <xsl:param name="paramHeadingLowerBorder"         as="xsd:boolean*" select="(false(),false(),false(),false(),false(),false(),false(),false(),false(),false())" />

        <!-- Global variables  -->

        <xsl:param    name="controller-url"   as="xsd:string" />
        <xsl:param    name="styles-url"       as="xsd:string" />

        <xsl:variable name="controller"       select="doc($controller-url)" />
        <xsl:variable name="styles"           select="doc($styles-url)/office:document-styles/office:styles" />
        <xsl:variable name="automatic-styles" select="/office:document-content/office:automatic-styles" />
        <xsl:variable name="body"             select="/office:document-content/office:body" />
        <xsl:variable name="stylesheet"       select="document('')/xsl:stylesheet" />

        <xsl:variable name="main-language">
            <xsl:value-of select="$styles/style:default-style[@style:family='paragraph'][1]
                                  /style:text-properties/@fo:language" />
            <xsl:text>-</xsl:text>
            <xsl:value-of select="$styles/style:default-style[@style:family='paragraph'][1]
                                  /style:text-properties/@fo:country" />
        </xsl:variable>

        <!-- Strip Space -->
        <xsl:strip-space elements="text:section text:p text:h text:a text:list text:list-item text:note text:note-body
                                   table:table table:table-row table:table-cell draw:frame draw:image office:annotation
                                   math:math math:annotation math:semantics math:mo math:mi math:mrow math:msup" />

        <xsl:include href="common-templates.xsl" />

    <!--
=============
DOCUMENT ROOT
=============
    -->

    <xsl:template match="/">

        <!-- DTBOOK -->

        <dtb:dtbook>
            <xsl:if test="//math:math">
                <xsl:copy-of select="$stylesheet/namespace::math" />
            </xsl:if>
            <xsl:attribute name="version">
                <xsl:value-of select="'2005-3'" />
            </xsl:attribute>
            <xsl:attribute name="xml:lang" select="$main-language" />

            <!-- BOOK -->
            <dtb:book>

                <!-- FRONTMATTER -->
                <dtb:frontmatter>                    
                    <xsl:variable name="frontmatter-section" as="xsd:string">
                        <xsl:call-template name="get-frontmatter-section" />
                    </xsl:variable>
                    <xsl:if test="string-length($frontmatter-section)>0">
                        <xsl:apply-templates mode="toplevel"
                                             select="$body/office:text//text:section[@text:name=$frontmatter-section][1]">
                            <xsl:with-param name="frontmatter-mode" select="true()" />
                        </xsl:apply-templates>
                    </xsl:if>
                </dtb:frontmatter>

                <!-- BODYMATTER -->
                <dtb:bodymatter>
                    <xsl:choose>
                        <xsl:when test="$paramBodyMatterMode='SINGLE' or
                                        $paramBodyMatterMode='AUTOMATIC'">
                            <dtb:volume>
                                <xsl:apply-templates mode="toplevel"
                                                     select="$body/office:text/text:sequence-decls/following-sibling::*"/>
                            </dtb:volume>
                        </xsl:when>
                        <xsl:when test="$paramBodyMatterMode='MANUAL'">
                            <xsl:apply-templates mode="toplevel"
                                                 select="$body/office:text/text:sequence-decls/following-sibling::*"/>
                        </xsl:when>
                        <xsl:otherwise />
                    </xsl:choose>
                </dtb:bodymatter>

                <!-- REARMATTER -->
                <dtb:rearmatter>
                    <xsl:variable name="rearmatter-section" as="xsd:string"
                                  select="substring-after(string($controller//ns1:rearmatter/@rdf:about), 'section:/')"/>
                    <xsl:if test="string-length($rearmatter-section)>0">
                        <xsl:choose>
                            <xsl:when test="$paramRearMatterMode='SINGLE'">
                                <dtb:volume>
                                    <xsl:apply-templates mode="toplevel"
                                                         select="$body/office:text//text:section[@text:name=$rearmatter-section][1]">
                                        <xsl:with-param name="rearmatter-mode" select="true()" />
                                    </xsl:apply-templates>
                                </dtb:volume>
                            </xsl:when>
                            <xsl:when test="$paramRearMatterMode='MANUAL'">
                                <xsl:apply-templates mode="toplevel"
                                                     select="$body/office:text//text:section[@text:name=$rearmatter-section][1]">
                                    <xsl:with-param name="rearmatter-mode" select="true()" />
                                </xsl:apply-templates>
                            </xsl:when>
                            <xsl:otherwise />
                        </xsl:choose>
                    </xsl:if>
                </dtb:rearmatter>
            </dtb:book>
        </dtb:dtbook>

        <xsl:value-of select="'&#10;'" />
    </xsl:template>


    <!--
=========
TOP LEVEL
=========
    -->

    <xsl:template match="*|@*" name="toplevel" mode="toplevel">
        <xsl:param name="frontmatter-mode" as="xsd:boolean" select="false()" />
        <xsl:param name="rearmatter-mode"  as="xsd:boolean" select="false()" />
        <xsl:param name="omissions"        as="xsd:boolean" select="true()"  />
        <xsl:param name="pagenumbers"      as="xsd:boolean" select="true()"  />

        <xsl:choose>

            <!-- PAGENUMBER -->
            <xsl:when test="$pagenumbers and name(current())='pagenum'">
                <xsl:call-template name="pagenumbering" />
            </xsl:when>

            <!-- HEADING -->
            <xsl:when test="name(current())='text:h'">
                <xsl:call-template name="heading" />
            </xsl:when>

            <!-- PARAGRAPH -->
            <xsl:when test="name(current())='text:p'">
                <xsl:call-template name="para" />
            </xsl:when>

            <!-- LIST -->
            <xsl:when test="name(current())='text:list'">
                <xsl:call-template name="list"/>
            </xsl:when>

            <!-- TABLE -->
            <xsl:when test="name(current())='table:table'">
                <xsl:call-template name="table"/>
            </xsl:when>
            
            <!-- BIBLIOGRAPHY -->
            <xsl:when test="name(current())='text:bibliography'">
                <xsl:call-template name="bibliography" />
            </xsl:when>

            <!-- TABLE OF CONTENT -->
            <xsl:when test="name(current())='text:table-of-content'">
                <xsl:if test="$omissions">
                    <dtb:div class="toc" />
                </xsl:if>
                <xsl:if test="$pagenumbers">
                    <xsl:apply-templates select="current()/descendant::pagenum" />
                </xsl:if>
            </xsl:when>

            <!-- SECTIONS -->
            <xsl:when test="name(current())='text:section'">                
                <xsl:variable name="section-name" select="@text:name" />
                <xsl:variable name="frontmatter-section" as="xsd:string">
                    <xsl:call-template name="get-frontmatter-section"/>
                </xsl:variable>
                <xsl:choose>
                    
                    <!-- NOT RENDERED FRONTMATTER -->
                    <xsl:when test="$section-name=$frontmatter-section and not($frontmatter-mode)">
                        <xsl:if test="$pagenumbers">
                            <xsl:apply-templates select="current()/descendant::pagenum" />
                        </xsl:if>
                    </xsl:when>

                    <!-- REPEATED FRONTMATTER = TITLE PAGE -->
                    <xsl:when test="$frontmatter-mode and
                                    $controller//ns1:repeat-frontmatter[@rdf:about=concat('section:/', $section-name)] and
                                    $controller//ns1:titlepage         [@rdf:about=concat('section:/', $section-name)]">
                        <dtb:repeat-frontmatter>
                            <dtb:titlepage>
                                <xsl:call-template name="section">
                                    <xsl:with-param name="frontmatter-mode"    select="true()"   />
                                    <xsl:with-param name="without-notesection" select="true()"  />
                                </xsl:call-template>
                            </dtb:titlepage>
                        </dtb:repeat-frontmatter>
                    </xsl:when>

                    <!-- REPEATED FRONTMATTER -->
                    <xsl:when test="$frontmatter-mode and
                                    $controller//ns1:repeat-frontmatter[@rdf:about=concat('section:/', $section-name)]">
                        <dtb:repeat-frontmatter>
                            <xsl:call-template name="section">
                                <xsl:with-param name="frontmatter-mode"    select="true()"  />
                                <xsl:with-param name="without-notesection" select="true()"  />
                            </xsl:call-template>
                        </dtb:repeat-frontmatter>
                    </xsl:when>

                    <!-- TITLE PAGE -->
                    <xsl:when test="$frontmatter-mode and
                                    $controller//ns1:titlepage[@rdf:about=concat('section:/', $section-name)]">
                        <dtb:titlepage>
                            <xsl:call-template name="section">
                                <xsl:with-param name="frontmatter-mode" select="true()"   />
                            </xsl:call-template>
                        </dtb:titlepage>
                    </xsl:when>

                    <!-- FRONTMATTER -->
                    <xsl:when test="$frontmatter-mode and
                                    $controller//ns1:frontmatter[@rdf:about=concat('section:/', $section-name)]">
                        <xsl:call-template name="section">
                            <xsl:with-param name="frontmatter-mode"    select="true()"   />
                            <xsl:with-param name="without-notesection" select="true()"  />
                        </xsl:call-template>
                    </xsl:when>

                    <!-- NOT RENDERED REARMATTER -->
                    <xsl:when test="$controller//ns1:rearmatter[@rdf:about=concat('section:/', $section-name)]
                                    and not($rearmatter-mode)"/>

                    <!-- REARMATTER -->
                    <xsl:when test="$controller//ns1:rearmatter[@rdf:about=concat('section:/', $section-name)]">
                        <xsl:call-template name="section">
                            <xsl:with-param name="rearmatter-mode"  select="true()" />
                        </xsl:call-template>
                    </xsl:when>

                    <!-- VOLUME IN REARMATTER -->
                    <xsl:when test="$rearmatter-mode and
                                    $paramRearMatterMode='MANUAL' and
                                    $controller//ns1:volume[@rdf:about=concat('section:/', $section-name)]">
                        <dtb:volume>
                            <xsl:attribute name="id" select="$section-name" />
                            <xsl:call-template name="section">
                                <xsl:with-param name="without-notesection" select="true()" />
                            </xsl:call-template>
                        </dtb:volume>
                    </xsl:when>

                    <!-- VOLUME IN BODYMATTER -->
                    <xsl:when test="$paramBodyMatterMode='MANUAL' and
                                    $controller//ns1:volume[@rdf:about=concat('section:/', $section-name)]">
                        <dtb:volume>
                            <xsl:attribute name="id" select="$section-name" />
                            <xsl:call-template name="section">
                                <xsl:with-param name="without-notesection" select="true()" />
                            </xsl:call-template>
                        </dtb:volume>
                    </xsl:when>

                    <!-- OTHER SECTION -->
                    <xsl:otherwise>
                        <xsl:call-template name="section">
                            <xsl:with-param name="frontmatter-mode" select="$frontmatter-mode" />
                            <xsl:with-param name="rearmatter-mode"  select="$rearmatter-mode" />
                        </xsl:call-template>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>

            <!-- OMISSIONS -->
            <xsl:otherwise>
                <xsl:if test="$omissions">
                    <dtb:div class="omission" />
                </xsl:if>
                <xsl:if test="$pagenumbers">
                    <xsl:apply-templates select="current()/descendant::pagenum" />
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!-- SECTIONS -->

    <xsl:template name="section">
        <xsl:param    name="frontmatter-mode"    select="false()" as="xsd:boolean" />
        <xsl:param    name="rearmatter-mode"     select="false()" as="xsd:boolean" />
        <xsl:param    name="without-notesection" select="false()" as="xsd:boolean" />

        <xsl:variable name="style-name"    select="@text:style-name" />
        <xsl:variable name="section-name"  select="@text:name"       />
        <xsl:variable name="frontmatter-section" as="xsd:string">
            <xsl:call-template name="get-frontmatter-section"/>
        </xsl:variable>

        <xsl:if test="not(string-length($frontmatter-section)>0 and
                          $section-name=$frontmatter-section    and
                          not($frontmatter-mode))">
            <xsl:apply-templates mode="toplevel" select="child::*">
                <xsl:with-param name="frontmatter-mode" select="$frontmatter-mode" />
                <xsl:with-param name="rearmatter-mode"  select="$rearmatter-mode" />
            </xsl:apply-templates>
        </xsl:if>

        <!-- Section endnotes -->
        <xsl:if test="not($without-notesection) and
                      $automatic-styles/style:style[@style:name=($style-name)]
                      /style:section-properties/text:notes-configuration[@text:note-class='endnote']">
            <dtb:note-section>
                <xsl:attribute name="section-name" select="$section-name" />
            </dtb:note-section>
        </xsl:if>
    </xsl:template>
    

    <!--
===============
INLINE ELEMENTS
===============
    -->

    <!-- TEXTNODES -->

    <xsl:template match="text()">
        <xsl:if test="not(contains(., '&#xa;'))">
            <dtb:text>
                <xsl:call-template name="soft-hyphen" />
            </dtb:text>
        </xsl:if>
    </xsl:template>

    <xsl:template match="text()" mode="convert-breaks">
        <dtb:text>
            <xsl:call-template name="breaks" />
        </dtb:text>
    </xsl:template>

    <!-- BREAKS -->

    <xsl:template name="breaks">
        <xsl:param name="text"          select="." />
        <xsl:choose>
            <xsl:when test="contains($text, '&#xa;')">
                <xsl:call-template name="soft-hyphen">
                    <xsl:with-param name="text" select="substring-before($text, '&#xa;')" />
                </xsl:call-template>
                <dtb:br />
                <xsl:call-template name="breaks">
                    <xsl:with-param name="text" select="substring-after($text,'&#xa;')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="soft-hyphen">
                    <xsl:with-param name="text" select="$text" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- SOFT HYPHENS -->

    <xsl:template name="soft-hyphen">
        <xsl:param name="text" select="."/>
        <xsl:choose>
            <xsl:when test="not($paramHyphenationEnabled) and contains($text, '&#xad;')">
                <xsl:value-of select="substring-before($text, '&#xad;')"/>
                <xsl:call-template name="soft-hyphen">
                    <xsl:with-param name="text" select="substring-after($text,'&#xad;')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$text"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- LINEBREAKS -->

    <xsl:template match="text:line-break">
        <dtb:br />
    </xsl:template>

    <!-- SPACES -->

    <xsl:template match="text:s">
        <dtb:text>
            <xsl:choose>
                <xsl:when test="@text:c">
                    <xsl:call-template name="insert-space">
                        <xsl:with-param name="amount" select="@text:c" />
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="insert-space" />
                </xsl:otherwise>
            </xsl:choose>
        </dtb:text>
    </xsl:template>

    <xsl:template name="insert-space">
        <xsl:param name="amount" select="1"  />
        <xsl:param name="spaces" select="''" />
        <xsl:choose>
            <xsl:when test="$amount > 0">
                <xsl:call-template name="insert-space">
                    <xsl:with-param name="amount" select="$amount - 1" />
                    <xsl:with-param name="spaces" select="concat($spaces,'&#x20;')" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$spaces" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- LANGUAGE -->

    <xsl:template match="*|@*" mode="language">
        <xsl:param name="pagenum"          as="xsd:boolean" select="true()" />
        <xsl:param name="specialtypeface"  as="xsd:boolean" select="true()" />

        <xsl:if test="(name(current())='text:p')
                   or (name(current())='text:h')
                   or (name(current())='text:span')
                   or (name(current())='text:a')">

            <xsl:variable name="style-name">
                <xsl:choose>
                    <xsl:when test="name(current())='text:a'">
                        <xsl:value-of select="'Internet_20_link'" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="current()/@text:style-name" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>

            <xsl:variable name="language">
                <xsl:if test="$automatic-styles/style:style[@style:name=($style-name)]
                              /style:text-properties/@fo:language">
                    <xsl:value-of select="$automatic-styles/style:style[@style:name=($style-name)]
                                          /style:text-properties/@fo:language" />
                    <xsl:if test="$automatic-styles/style:style[@style:name=($style-name)]
                                  /style:text-properties/@fo:country">
                        <xsl:text>-</xsl:text>
                        <xsl:value-of select="$automatic-styles/style:style[@style:name=($style-name)]
                                              /style:text-properties/@fo:country"  />
                    </xsl:if>
                </xsl:if>
            </xsl:variable>

            <xsl:choose>
                <xsl:when test="not($language='') and (name(current())='text:span' or not($language=$main-language))">
                    <dtb:span>
                        <xsl:attribute name="lang" select="$language" />
                        <xsl:apply-templates select="current()"    mode="typeface">
                            <xsl:with-param name="pagenum"         select="$pagenum" />
                            <xsl:with-param name="specialtypeface" select="$specialtypeface" />
                        </xsl:apply-templates>
                    </dtb:span>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="current()"    mode="typeface" >
                        <xsl:with-param name="pagenum"         select="$pagenum" />
                        <xsl:with-param name="specialtypeface" select="$specialtypeface" />
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!-- TYPEFACE -->

    <xsl:template match="*|@*" mode="typeface">

        <xsl:param name="pagenum"          as="xsd:boolean" select="true()" />
        <xsl:param name="specialtypeface"  as="xsd:boolean" select="true()" />

        <xsl:if test="(name(current())='text:p')
                   or (name(current())='text:h')
                   or (name(current())='text:span')
                   or (name(current())='text:a')">

            <xsl:variable name="style-name">
                <xsl:choose>
                    <xsl:when test="name(current())='text:a'">
                        <xsl:value-of select="'Internet_20_link'" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="current()/@text:style-name" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:variable name="family">
                <xsl:choose>
                    <xsl:when test="name(current())='text:p' or name(current())='text:h'">
                        <xsl:value-of select="'paragraph'" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="'text'" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not($specialtypeface)">
                    <xsl:apply-templates>
                        <xsl:with-param name="pagenum"         select="$pagenum" />
                        <xsl:with-param name="specialtypeface" select="false()" />
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="font-style">
                        <xsl:call-template name="get-font-style">
                            <xsl:with-param name="style-name" select="$style-name" />
                            <xsl:with-param name="family"     select="$family"     />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="font-weight">
                        <xsl:call-template name="get-font-weight">
                            <xsl:with-param name="style-name" select="$style-name" />
                            <xsl:with-param name="family"     select="$family"     />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="underline-style">
                        <xsl:call-template name="get-underline-style">
                            <xsl:with-param name="style-name" select="$style-name" />
                            <xsl:with-param name="family"     select="$family"     />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="font-variant">
                        <xsl:call-template name="get-font-variant">
                            <xsl:with-param name="style-name" select="$style-name" />
                            <xsl:with-param name="family"     select="$family"     />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="text-transform">
                        <xsl:call-template name="get-text-transform">
                            <xsl:with-param name="style-name" select="$style-name" />
                            <xsl:with-param name="family"     select="$family"     />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:choose>
                        <xsl:when test="not($font-weight=''
                                        and $font-style=''
                                        and $underline-style=''
                                        and $font-variant=''
                                        and $text-transform='')">
                            <dtb:span>
                                <xsl:if test="not($font-style='')">
                                    <xsl:attribute name="font-style"      select="$font-style" />
                                </xsl:if>
                                <xsl:if test="not($font-weight='')">
                                    <xsl:attribute name="font-weight"     select="$font-weight" />
                                </xsl:if>
                                <xsl:if test="not($underline-style='')">
                                    <xsl:attribute name="underline-style" select="$underline-style" />
                                </xsl:if>
                                <xsl:if test="not($font-variant='')">
                                    <xsl:attribute name="font-variant"    select="$font-variant" />
                                </xsl:if>
                                <xsl:if test="not($text-transform='')">
                                    <xsl:attribute name="text-transform"  select="$text-transform" />
                                </xsl:if>
                                <xsl:apply-templates>
                                    <xsl:with-param name="pagenum" select="$pagenum" />
                                </xsl:apply-templates>
                            </dtb:span>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:apply-templates>
                                <xsl:with-param name="pagenum" select="$pagenum" />
                            </xsl:apply-templates>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!-- SPAN ELEMENTS -->

    <xsl:template match="text:span">
        <xsl:param name="specialtypeface" as="xsd:boolean" select="true()" />
        <xsl:param name="pagenum"         as="xsd:boolean" select="true()" />

        <xsl:if test="string(.) or count(./*) > 0">

            <xsl:variable name="character-style-name">
                <xsl:call-template name="get-character-style-name">
                    <xsl:with-param name="style-name" select="current()/@text:style-name" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$specialtypeface and not($character-style-name='')">
                    <dtb:span>
                        <xsl:attribute name="style">
                            <xsl:value-of select="$character-style-name" />
                        </xsl:attribute>
                        <xsl:apply-templates select="current()" mode="language">
                            <xsl:with-param name="pagenum"         select="$pagenum" />
                            <xsl:with-param name="specialtypeface" select="$specialtypeface" />
                        </xsl:apply-templates>
                    </dtb:span>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:apply-templates select="current()" mode="language">
                        <xsl:with-param name="pagenum"         select="$pagenum" />
                        <xsl:with-param name="specialtypeface" select="$specialtypeface" />
                    </xsl:apply-templates>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>

    <!-- HYPERLINKS -->

    <xsl:template match="text:a">
        <xsl:param name="specialtypeface" as="xsd:boolean" select="true()" />
        <xsl:param name="pagenum"         as="xsd:boolean" select="true()" />

        <xsl:variable name="protocol"     select="substring-before(@xlink:href,':')" />

        <xsl:choose>
            <xsl:when test="$protocol = 'http' or $protocol = 'mailto'">
                <dtb:a href="{@xlink:href}" external="true">
                    <dtb:span style="Internet_20_link">
                        <xsl:choose>
                            <xsl:when test="string(.) or count(./*) > 0">
                                <xsl:apply-templates select="current()"     mode="language">
                                    <xsl:with-param  name="pagenum"         select="$pagenum" />
                                    <xsl:with-param  name="specialtypeface" select="$specialtypeface" />
                                </xsl:apply-templates>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="@xlink:href" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </dtb:span>
                </dtb:a>
            </xsl:when>
            <xsl:otherwise>
                <dtb:a>
                    <xsl:if test="string(.) or count(./*) > 0">
                        <dtb:span style="Internet_20_link">
                            <xsl:apply-templates select="current()"     mode="language">
                                <xsl:with-param  name="pagenum"         select="$pagenum" />
                                <xsl:with-param  name="specialtypeface" select="$specialtypeface" />
                            </xsl:apply-templates>
                        </dtb:span>
                    </xsl:if>
                </dtb:a>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- PAGENUMBERS -->

    <xsl:template match="pagenum" name="pagenumbering">
        <xsl:param name="pagenum" as="xsd:boolean" select="true()" />

        <xsl:if test="$pagenum">
            <xsl:if test="(@type = 'hard' and $paramKeepHardPageBreaks) or
                          (@type = 'new-braille-page')">
                <dtb:pagebreak />
            </xsl:if>
            <xsl:if test="@num and @enum and @render and @value">
                <xsl:variable name="attrPage">
                    <xsl:choose>
                        <xsl:when test="@enum='1'">
                            <xsl:value-of select="'normal'" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="'special'" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="@render='true'">
                        <dtb:pagenum>
                            <xsl:attribute name="page">
                                <xsl:value-of select="$attrPage" />
                            </xsl:attribute>
                            <xsl:value-of select="@value" />
                        </dtb:pagenum>
                    </xsl:when>
                    <xsl:otherwise>
                        <dtb:pagenum />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <!-- LISTNUMBERS -->

    <xsl:template match="num" name="numbering">

        <dtb:span class="unspaced">
            <xsl:value-of select="@value" />
        </dtb:span>
    </xsl:template>

    <!-- NOTE REFERENCES -->

    <xsl:template name="noteref">
        <xsl:param name="space-before" select="false()" />
        <xsl:param name="space-after"  select="true()"  />
        <dtb:noteref>
            <xsl:if test="$space-before">
                <xsl:attribute name="space-before" select="'true'" />
            </xsl:if>
            <xsl:if test="$space-after">
                <xsl:attribute name="space-after"  select="'true'" />
            </xsl:if>
            <xsl:variable name="note-class" select="@text:note-class" />
            <xsl:choose>
                <xsl:when test="text:note-citation[not(@text:label)]">
                    <xsl:variable name="num-format" as="xsd:string">
                        <xsl:call-template name="get-note-num-format">
                            <xsl:with-param name="section"    select="ancestor::text:section[1]" />
                            <xsl:with-param name="note-class" select="$note-class" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:variable name="num-letter-sync" as="xsd:boolean">
                        <xsl:call-template name="get-note-num-letter-sync">
                            <xsl:with-param name="section"    select="ancestor::text:section[1]" />
                            <xsl:with-param name="note-class" select="$note-class" />
                        </xsl:call-template>
                    </xsl:variable>
                    <dtb:span class="unspaced">
                        <xsl:call-template name="get-noteref-prefix">
                            <xsl:with-param name="num-format" select="$num-format" />
                        </xsl:call-template>
                    </dtb:span>
                    <xsl:call-template name="format-number">
                        <xsl:with-param name="num-in"              select="text:note-citation/text()" />
                        <xsl:with-param name="num-format-out"      select="$num-format" />
                        <xsl:with-param name="num-letter-sync-out" select="$num-letter-sync" />
                        <xsl:with-param name="num-format-in">
                            <xsl:call-template name="get-default-note-num-format">
                                <xsl:with-param name="note-class" select="$note-class" />
                            </xsl:call-template>
                        </xsl:with-param>
                        <xsl:with-param name="num-letter-sync-in">
                            <xsl:call-template name="get-default-note-num-letter-sync">
                                <xsl:with-param name="note-class" select="$note-class" />
                            </xsl:call-template>
                        </xsl:with-param>
                    </xsl:call-template>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="text:note-citation/text()" />
                </xsl:otherwise>
            </xsl:choose>
        </dtb:noteref>
    </xsl:template>


    <xsl:template match="text:note">
        <xsl:call-template name="noteref">
            <xsl:with-param name="space-before" select="$paramNoterefSpaceBefore" />
            <xsl:with-param name="space-after"  select="$paramNoterefSpaceAfter"  />
        </xsl:call-template>
        <xsl:variable name="note-class" select="@text:note-class" />
        <xsl:if test="$note-class='endnote'">
            <xsl:call-template name="endnote" />
        </xsl:if>
    </xsl:template>


   <!--
==============
BLOCK ELEMENTS
==============
    -->

    <!-- HEADINGS -->

    <xsl:template match="text:h">
        <xsl:param name="specialtypeface"  as="xsd:boolean" select="false()" />
        <xsl:param name="notes"            as="xsd:boolean" select="true()"  />
        <xsl:param name="frames"           as="xsd:boolean" select="true()"  />
        <xsl:param name="omit-frames"      as="xsd:boolean" select="false()" />
        <xsl:param name="transpose-frames" as="xsd:boolean" select="false()" />

        <xsl:call-template name="heading">
            <xsl:with-param name="specialtypeface"  select="$specialtypeface"  />
            <xsl:with-param name="notes"            select="$notes"            />
            <xsl:with-param name="frames"           select="$frames"           />
            <xsl:with-param name="omit-frames"      select="$omit-frames"      />
            <xsl:with-param name="transpose-frames" select="$transpose-frames" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="heading">
        <xsl:param name="specialtypeface"  as="xsd:boolean" select="false()" />
        <xsl:param name="notes"            as="xsd:boolean" select="true()"  />
        <xsl:param name="frames"           as="xsd:boolean" select="true()"  />
        <xsl:param name="omit-frames"      as="xsd:boolean" select="false()" />
        <xsl:param name="transpose-frames" as="xsd:boolean" select="false()" />

        <xsl:variable name="outline-level" as="xsd:integer" select="current()/@text:outline-level" />
        <xsl:variable name="upper-border" as="xsd:boolean">
            <xsl:value-of select="$paramHeadingUpperBorder[$outline-level]" />
        </xsl:variable>
        <xsl:variable name="lower-border" as="xsd:boolean">
            <xsl:value-of select="$paramHeadingLowerBorder[$outline-level]" />
        </xsl:variable>

        <dtb:heading>

            <!-- Top boxline -->
            <xsl:if test="$upper-border">
                <dtb:hr/>
            </xsl:if>

            <dtb:div class="border">
                <xsl:if test="current()/child::* | current()/text()">
                    <xsl:element name="dtb:h{$outline-level}">
                        <xsl:if test="$outline-level>$paramTocUptoLevel or ./ancestor::draw:frame">
                            <xsl:attribute name="class">
                                <xsl:value-of select="'dummy'" />
                            </xsl:attribute>
                        </xsl:if>
                        <xsl:apply-templates select="current()" mode="language">
                            <xsl:with-param name="specialtypeface" select="$specialtypeface" />
                        </xsl:apply-templates>
                    </xsl:element>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="$omit-frames">

                        <!-- Omit frames -->
                        <xsl:for-each select="current()/draw:frame[not(draw:object/math or draw:object/math:math)] |
                                              current()/draw:a/draw:frame[not(draw:object/math or draw:object/math:math)]">
                            <dtb:div class="omission">
                                <dtb:flag class="double-dash" />
                                <dtb:span class="spaced">
                                    <dtb:value-of select="$paramDoubleDash" />
                                </dtb:span>
                            </dtb:div>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:when test="$transpose-frames">

                        <!-- Transpose frames -->
                        <xsl:for-each select="current()/draw:frame[not(draw:object/math or draw:object/math:math)] |
                                              current()/draw:a/draw:frame[not(draw:object/math or draw:object/math:math)]">
                            <dtb:div class="transposition">
                                <dtb:flag class="double-dash" />
                                <dtb:span class="spaced">
                                    <dtb:value-of select="$paramDoubleDash" />
                                </dtb:span>
                            </dtb:div>
                        </xsl:for-each>
                    </xsl:when>
                </xsl:choose>
                <xsl:if test="$notes">

                    <!-- Footnotes -->
                    <xsl:if test="current()//text:note[@text:note-class='footnote']">
                        <xsl:call-template name="footnote">
                            <xsl:with-param name="notes" select="current()//text:note[@text:note-class='footnote']" />
                        </xsl:call-template>
                    </xsl:if>
                </xsl:if>
            </dtb:div>

            <!-- Bottom boxline -->
            <xsl:if test="$lower-border">
                <dtb:hr/>
            </xsl:if>
        </dtb:heading>

        <!-- Frames -->
        <xsl:if test="$frames and not($transpose-frames) and not($omit-frames)">
            <xsl:apply-templates select="current()/draw:frame | current()/draw:a" mode="frame"/>
        </xsl:if>
    </xsl:template>

    <!-- PARAGRAPHS -->

    <xsl:template match="text:p">
        <xsl:param name="specialtypeface"  as="xsd:boolean" select="true()" />
        <xsl:param name="caption"          as="xsd:boolean" select="false()" />
        <xsl:param name="notes"            as="xsd:boolean" select="true()"  />
        <xsl:param name="frames"           as="xsd:boolean" select="true()"  />
        <xsl:param name="omit-frames"      as="xsd:boolean" select="false()" />
        <xsl:param name="transpose-frames" as="xsd:boolean" select="false()" />

        <xsl:call-template name="para">
            <xsl:with-param name="specialtypeface"   select="$specialtypeface" />
            <xsl:with-param name="caption"           select="$caption"          />
            <xsl:with-param name="notes"             select="$notes"            />
            <xsl:with-param name="frames"            select="$frames"           />
            <xsl:with-param name="omit-frames"       select="$omit-frames"      />
            <xsl:with-param name="transpose-frames"  select="$transpose-frames" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="para">
        <xsl:param name="specialtypeface"  as="xsd:boolean" select="true()" />
        <xsl:param name="caption"          as="xsd:boolean" select="false()" />
        <xsl:param name="notes"            as="xsd:boolean" select="true()"  />
        <xsl:param name="frames"           as="xsd:boolean" select="true()"  />
        <xsl:param name="omit-frames"      as="xsd:boolean" select="false()" />
        <xsl:param name="transpose-frames" as="xsd:boolean" select="false()" />

        <xsl:variable name="style-name" select="@text:style-name" />
        <xsl:variable name="is-paragraph" as="xsd:boolean">
            <xsl:call-template name="is-paragraph">
                <xsl:with-param name="node" select="." />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="is-caption" as="xsd:boolean">
            <xsl:call-template name="is-caption">
                <xsl:with-param name="style-name" select="$style-name" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="is-empty" as="xsd:boolean">
            <xsl:call-template name="is-empty">
                <xsl:with-param name="node" select="." />
            </xsl:call-template>
        </xsl:variable>
        
        <xsl:choose>

            <!-- CAPTION -->
            <xsl:when test="$caption">
                <dtb:caption>
                    <xsl:apply-templates select="current()" mode="language">
                        <xsl:with-param name="pagenum" select="false()" />
                        <xsl:with-param name="specialtypeface" select="$specialtypeface" />
                    </xsl:apply-templates>
                </dtb:caption>

                <!-- Footnotes -->
                <xsl:if test="$notes">
                    <xsl:if test="current()//text:note[@text:note-class='footnote']">
                        <xsl:call-template name="footnote">
                            <xsl:with-param name="notes" select="current()//text:note[@text:note-class='footnote']" />
                        </xsl:call-template>
                    </xsl:if>
                </xsl:if>
            </xsl:when>

            <!-- PARAGRAPH -->
            <xsl:otherwise>
                <xsl:variable name="configured-style-name" as="xsd:string">
                    <xsl:call-template name="get-configured-paragraph-style">
                        <xsl:with-param name="style-name" select="$style-name"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$is-caption">
                        <xsl:apply-templates select="pagenum" />
                    </xsl:when>
                    <xsl:when test="$is-paragraph and $is-empty">

                        <!-- Empty paragraph -->
                        <xsl:apply-templates select="pagenum" />
                        <xsl:variable name="keep-empty" as="xsd:boolean">
                            <xsl:call-template name="get-keep-empty-para-style">
                                <xsl:with-param name="style-name" select="$configured-style-name" />
                            </xsl:call-template>
                        </xsl:variable>                        
                        <xsl:if test="$keep-empty">
                            <dtb:br />
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <dtb:paragraph>
                            <xsl:if test="$is-paragraph">
                                <xsl:attribute name="style" select="$configured-style-name" />
                            </xsl:if>
                            <dtb:p>
                                <xsl:apply-templates select="current()" mode="language">
                                    <xsl:with-param name="specialtypeface" select="$specialtypeface" />
                                </xsl:apply-templates>
                            </dtb:p>
                            <xsl:choose>
                                <xsl:when test="$omit-frames">

                                    <!-- Omit frames -->
                                    <xsl:for-each select="./draw:frame[not(draw:object/math or draw:object/math:math)] |
                                                          ./draw:a/draw:frame[not(draw:object/math or draw:object/math:math)]">
                                        <dtb:div class="omission">
                                            <dtb:flag class="double-dash" />
                                            <dtb:span class="spaced">
                                                <xsl:value-of select="$paramDoubleDash" />
                                            </dtb:span>
                                        </dtb:div>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:when test="$transpose-frames">

                                    <!-- Transpose frames -->
                                    <xsl:for-each select="./draw:frame[not(draw:object/math or draw:object/math:math)] |
                                                          ./draw:a/draw:frame[not(draw:object/math or draw:object/math:math)]">
                                        <dtb:div class="transposition">
                                            <dtb:flag class="double-dash" />
                                            <dtb:span class="spaced">
                                                <dtb:value-of select="$paramDoubleDash" />
                                            </dtb:span>
                                        </dtb:div>
                                    </xsl:for-each>
                                </xsl:when>
                            </xsl:choose>

                            <!-- Footnotes -->
                            <xsl:if test="$notes">
                                <xsl:if test="current()//text:note[@text:note-class='footnote']">
                                    <xsl:call-template name="footnote">
                                        <xsl:with-param name="notes" select="current()//text:note[@text:note-class='footnote']" />
                                    </xsl:call-template>
                                </xsl:if>
                            </xsl:if>
                        </dtb:paragraph>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>

        <!-- Frames -->
        <xsl:if test="$frames and not($transpose-frames) and not($omit-frames)">
            <xsl:apply-templates select="current()/draw:frame | current()/draw:a" mode="frame"/>
        </xsl:if>
    </xsl:template>

    <!-- FOOTNOTES -->

    <xsl:template name="footnote">
        <xsl:param name="notes" />

        <xsl:for-each select="$notes">
            <dtb:note>
                <xsl:attribute name="class">
                    <xsl:value-of select="'footnote'" />
                </xsl:attribute>
                <xsl:call-template name="noteref" />
                <xsl:apply-templates select="(current()/text:note-body/text:p)
                                            |(current()/text:note-body/text:h)">
                    <xsl:with-param name="frames"          select="false()" />
                    <xsl:with-param name="notes"           select="false()" />
                    <xsl:with-param name="specialtypeface" select="true()"  />
                    <xsl:with-param name="omit-frames"     select="true()"  />
                </xsl:apply-templates>
            </dtb:note>
        </xsl:for-each>
    </xsl:template>

    <!-- ENDNOTE -->

    <xsl:template name="endnote" >

        <xsl:variable name="end-of-section">
            <xsl:call-template name="get-endnote-section">
                <xsl:with-param name="start-section" select="./ancestor::text:section[1]" />
            </xsl:call-template>
        </xsl:variable>
        <dtb:note>
            <xsl:attribute name="class" select="'endnote'" />
            <xsl:if test="string-length($end-of-section)>0">
                <xsl:attribute name="end-of-section" select="$end-of-section" />
            </xsl:if>
            <xsl:call-template name="noteref" />
            <xsl:apply-templates select="(current()/text:note-body/text:p)
                                        |(current()/text:note-body/text:h)">
                <xsl:with-param name="frames"           select="false()" />
                <xsl:with-param name="notes"            select="false()" />
                <xsl:with-param name="specialtypeface"  select="true()"  />
                <xsl:with-param name="omit-frames"      select="true()"  />
            </xsl:apply-templates>
        </dtb:note>
    </xsl:template>

    <!-- LISTS -->

    <xsl:template name="list" match="text:list">

        <xsl:param name="listlevel"       as="xsd:integer" select="0" />
        <xsl:param name="transposed"      as="xsd:boolean" select="false()" />
        <xsl:param name="transpose-lists" as="xsd:boolean" select="false()" />

        <xsl:choose>
            <xsl:when test="$transpose-lists">
                <dtb:div class="transposition">
                    <dtb:flag class="double-dash" />
                    <dtb:span class="spaced">
                        <dtb:value-of select="$paramDoubleDash" />
                    </dtb:span>
                </dtb:div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$transposed">
                    <dtb:div class="tn">
                        <dtb:note>
                            <xsl:value-of select='"The list below was transposed from it&apos;s original location."' />
                        </dtb:note>
                    </dtb:div>
                </xsl:if>
                <xsl:choose>
                    <xsl:when test="current()/@xml:id">
                        <dtb:list>
                            <xsl:apply-templates>
                                <xsl:with-param name="listlevel" select="$listlevel + 1" />
                            </xsl:apply-templates>
                        </dtb:list>
                        <xsl:apply-templates select="(current()//text:list-item/*/draw:frame)
                                                    |(current()//text:list-item/*/draw:a)
                                                    |(current()//text:list-header/*/draw:frame)
                                                    |(current()//text:list-header/*/draw:a)"
                                             mode="frame"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <dtb:list>
                            <xsl:apply-templates>
                                <xsl:with-param name="listlevel" select="$listlevel + 1" />
                            </xsl:apply-templates>
                        </dtb:list>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- LISTITEMS, LISTHEADERS -->

    <xsl:template match="text:list-item | text:list-header">

        <xsl:param name="listlevel" as="xsd:integer" select="0" />
        <dtb:li>
            <xsl:apply-templates select="current()/child::*">
                <xsl:with-param name="specialtypeface"  select="true()"     />
                <xsl:with-param name="notes"            select="true()"     />
                <xsl:with-param name="frames"           select="false()"    />
                <xsl:with-param name="listlevel"        select="$listlevel" />
            </xsl:apply-templates>
        </dtb:li>
    </xsl:template>

    <!-- TABLES -->

    <xsl:template match="table:table" name="table">

        <xsl:param name="transposed"       as="xsd:boolean" select="false()" />
        <xsl:param name="transpose-tables" as="xsd:boolean" select="false()" />

        <xsl:variable name="styleName"  select="current()/@table:style-name" />
        <xsl:variable name="tableName" select="@table:name" />
        <xsl:variable name="border">
            <xsl:choose>
                <xsl:when test="$automatic-styles/style:style
                                [substring-before(@style:name,'.')=$styleName]
                                /style:table-cell-properties[not(@fo:border='none')]">
                    <xsl:value-of select="''" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'none'" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="upper-border" as="xsd:boolean">
            <xsl:value-of select="$paramTableUpperBorder" />
        </xsl:variable>
        <xsl:variable name="lower-border" as="xsd:boolean">
            <xsl:value-of select="$paramTableLowerBorder" />
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$transpose-tables">
                <dtb:div class="transposition">
                    <dtb:flag class="double-dash" />
                    <dtb:span class="spaced">
                        <dtb:value-of select="$paramDoubleDash" />
                    </dtb:span>
                </dtb:div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="transposedContent"
                              select="current()/descendant::*[self::table:table
                                                           or self::text:list
                                                           or (self::draw:frame and not(child::draw:object[math or math:math]))]" />
                <xsl:variable name="emptyCells"
                              select="current()/table:table-row/table:table-cell[not(string(self::*))]
                                   or current()/table:table-header-rows/table:table-row/table:table-cell[not(string(self::*))]" />
                
                <xsl:if test="$transposedContent or $transposed or $emptyCells">
                    <dtb:div class="tn">
                        <xsl:if test="$transposed">
                            <dtb:note>
                                <xsl:value-of select='"The table below was transposed from it&apos;s original location."' />
                            </dtb:note>
                        </xsl:if>
                        <xsl:if test="$transposedContent">
                            <dtb:note>
                                <xsl:value-of select="'Some material inside the table below was moved outside.'" />
                            </dtb:note>
                            <dtb:note>
                                <dtb:span class="spaced"><xsl:value-of select="'These transpositions are indicated with a double dash: '" /></dtb:span>
                                <dtb:span class="spaced"><dtb:value-of select="$paramDoubleDash" /></dtb:span>
                                <dtb:span class="spaced"><dtb:value-of select="$paramDoubleDashDots" /></dtb:span>
                            </dtb:note>
                        </xsl:if>
                        <xsl:if test="$emptyCells">
                            <dtb:note>
                                <dtb:span class="spaced"><xsl:value-of select="'Empty table cells are indicated with an ellipsis:'" /></dtb:span>
                                <dtb:span class="spaced"><xsl:value-of select="$paramEllipsis" /></dtb:span>
                                <dtb:span class="spaced"><xsl:value-of select="$paramEllipsisDots" /></dtb:span>
                            </dtb:note>
                        </xsl:if>
                    </dtb:div>
                </xsl:if>
                <dtb:table>

                    <!-- Top boxline -->
                    <xsl:if test="$upper-border">
                        <dtb:hr/>
                    </xsl:if>
                    <dtb:div class="border">

                        <!-- Table caption -->
                        <xsl:variable name="table-caption-id">
                            <xsl:call-template name="get-table-caption-id">
                                <xsl:with-param name="table-name" select="$tableName" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:if test="$table-caption-id">
                            <xsl:variable name="caption" select="$body//text:p[@xml:id=$table-caption-id]" />
                            <xsl:if test="$caption" >
                                <dtb:div class="caption">
                                    <xsl:apply-templates select="$caption">
                                        <xsl:with-param name="caption" select="true()" />
                                        <xsl:with-param name="frames"  select="false()" />
                                        <xsl:with-param name="specialtypeface" select="false()" />
                                    </xsl:apply-templates>
                                </dtb:div>
                            </xsl:if>
                        </xsl:if>

                        <!-- Flatten table -->
                        <xsl:variable name="flat-table" as="element()">
                            <xsl:call-template name="flatten-table-header-rows">
                                <xsl:with-param name="table" select="."/>
                            </xsl:call-template>
                        </xsl:variable>

                        <!-- (Mirror table) -->
                        <xsl:variable name="table" as="element()">
                            <xsl:choose>
                                <xsl:when test="$paramMirrorTable">
                                    <xsl:call-template name="mirror-table">
                                        <xsl:with-param name="table" select="$flat-table"/>
                                    </xsl:call-template>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:sequence select="$flat-table"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:variable>
                        <xsl:choose>

                            <!-- Table with headings -->
                            <xsl:when test="($paramColumnHeadings and not($paramMirrorTable)) or
                                            ($paramRowHeadings and $paramMirrorTable)">

                                <!-- Table heading -->
                                <xsl:variable name="table-heading" as="element()">
                                    <xsl:sequence select="$table/table:table-row[1]"/>
                                </xsl:variable>

                                <!-- Table body -->
                                <xsl:variable name="table-body" as="element()*">
                                    <xsl:sequence select="$table/table:table-row[position()>1]"/>
                                </xsl:variable>

                                <dtb:thead>
                                    <xsl:apply-templates select="$table-heading" mode="table-heading"/>
                                </dtb:thead>
                                <dtb:hr />
                                <dtb:tbody>
                                    <xsl:apply-templates select="$table-body">
                                        <xsl:with-param name="heading-row" select="$table-heading"/>
                                    </xsl:apply-templates>
                                </dtb:tbody>

                            </xsl:when>

                            <!-- Table without headings -->
                            <xsl:otherwise>

                                <!-- Table body -->
                                <xsl:variable name="table-body" as="element()*">
                                    <xsl:sequence select="$table/table:table-row"/>
                                </xsl:variable>

                                <xsl:apply-templates select="$table-body" />
                            </xsl:otherwise>
                        </xsl:choose>

                        <!-- Table footnotes  -->
                        <xsl:variable name="depth" select="count(current()/ancestor::*)" />
                        <xsl:variable name="notes" select="current()//text:note[@text:note-class='footnote'
                                                                           and (count(ancestor::table:table[1]/ancestor::*) = $depth)
                                                                           and (count(ancestor::text:list[1]/ancestor::*) &lt; $depth)
                                                                           and (count(ancestor::draw:frame[1]/ancestor::*) &lt; $depth) ]" />
                        <xsl:if test="$notes">
                            <xsl:call-template name="footnote">
                                <xsl:with-param name="notes" select="$notes" />
                            </xsl:call-template>
                        </xsl:if>
                    </dtb:div>

                    <!-- Bottom boxline -->
                    <xsl:if test="$lower-border">
                        <dtb:hr/>
                    </xsl:if>
                </dtb:table>

                <!-- Page breaks -->
                <xsl:if test="not($transposed)">
                    <xsl:apply-templates select="descendant::pagenum" />
                </xsl:if>

                <!-- Transposed tables, lists & frames -->
                <xsl:apply-templates select="(current()/table:table-row/table:table-cell/*/draw:frame)
                                            |(current()/table:table-row/table:table-cell/*/draw:a)
                                            |(current()/table:table-row/table:table-cell/table:table)
                                            |(current()/table:table-row/table:table-cell/text:list)
                                            |(current()/table:table-header-rows/table:table-row/table:table-cell/*/draw:frame)
                                            |(current()/table:table-header-rows/table:table-row/table:table-cell/*/draw:a)
                                            |(current()/table:table-header-rows/table:table-row/table:table-cell/table:table)
                                            |(current()/table:table-header-rows/table:table-row/table:table-cell/text:list)"
                                     mode="frame"/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- TABLE ROW  -->

    <xsl:template match="table:table-row" mode="table-heading">
        <dtb:tr>
            <xsl:for-each select="table:table-cell | table:covered-table-cell">
                <xsl:apply-templates select="." mode="table-heading">
                    <xsl:with-param name="column-delimiter"
                                    select="not($paramStairstepTableEnabled) and following-sibling::table:table-cell"/>
                </xsl:apply-templates>
            </xsl:for-each>
        </dtb:tr>
    </xsl:template>
    
    <xsl:template match="table:table-row">
        <xsl:param name="heading-row" as="element()*" />
        <dtb:tr>
            <xsl:for-each select="table:table-cell | table:covered-table-cell">
                <xsl:variable name="i" select="position()" />
                <xsl:apply-templates select=".">
                    <xsl:with-param name="heading-cell"
                                    select="$heading-row[1]/*[self::table:table-cell or self::table:covered-table-cell][$i]"/>
                    <xsl:with-param name="column-delimiter"
                                    select="not($paramStairstepTableEnabled) and following-sibling::table:table-cell"/>
                    <xsl:with-param name="row-heading"
                                    select="($i=1) and (($paramRowHeadings and not($paramMirrorTable)) or
                                                        ($paramColumnHeadings and $paramMirrorTable))"/>
                </xsl:apply-templates>
            </xsl:for-each>
        </dtb:tr>
    </xsl:template>

    <!-- TABLE HEADING CELL -->

    <xsl:template match="table:table-cell" mode="table-heading">
        <xsl:param name="column-delimiter" as="xsd:boolean" select="false()" />
        <dtb:th>
            <xsl:choose>
                <xsl:when test="string(.)">
                    <xsl:apply-templates>
                        <xsl:with-param name="frames"           select="false()" />
                        <xsl:with-param name="notes"            select="false()" />
                        <xsl:with-param name="specialtypeface"  select="false()"  />
                        <xsl:with-param name="transpose-frames" select="true()"  />
                        <xsl:with-param name="transpose-tables" select="true()"  />
                        <xsl:with-param name="transpose-lists"  select="true()"  />
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <dtb:p>
                        <dtb:flag class="ellipsis" />
                        <xsl:value-of select="$paramEllipsis" />
                    </dtb:p>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="$column-delimiter">
                <dtb:span class="unspaced">
                    <xsl:value-of select="$paramColumnDelimiter" />
                </dtb:span>
            </xsl:if>
        </dtb:th>
    </xsl:template>

    <xsl:template match="table:covered-table-cell" mode="table-heading">
        <dtb:th />
    </xsl:template>

    <!-- TABLE CELL  -->

    <xsl:template match="table:table-cell">
        <xsl:param name="heading-cell"     as="element()*" />
        <xsl:param name="column-delimiter" as="xsd:boolean" select="false()" />
        <xsl:param name="row-heading"      as="xsd:boolean" select="false()" />
        <dtb:td>
            <xsl:choose>
                <xsl:when test="string(.)">
                    <xsl:if test="$paramRepeatTableHeading
                                  and not($row-heading)
                                  and (($paramColumnHeadings and not($paramMirrorTable)) or
                                       ($paramRowHeadings and $paramMirrorTable))">
                        <xsl:if test="string($heading-cell/child::*[self::text:p or self::text:h])">
                            <xsl:apply-templates select="$heading-cell/child::*[self::text:p or self::text:h]">
                                <xsl:with-param name="frames"           select="false()" />
                                <xsl:with-param name="notes"            select="false()" />
                                <xsl:with-param name="specialtypeface"  select="false()"  />
                            </xsl:apply-templates>
                            <dtb:span class="unspaced">
                                <xsl:value-of select="$paramTableHeadingSuffix" />
                            </dtb:span>
                        </xsl:if>
                    </xsl:if>
                    <xsl:apply-templates>
                        <xsl:with-param name="frames"           select="false()" />
                        <xsl:with-param name="notes"            select="false()" />
                        <xsl:with-param name="specialtypeface"  select="not($row-heading)"  />
                        <xsl:with-param name="transpose-frames" select="true()"  />
                        <xsl:with-param name="transpose-tables" select="true()"  />
                        <xsl:with-param name="transpose-lists"  select="true()"  />
                    </xsl:apply-templates>
                </xsl:when>
                <xsl:otherwise>
                    <dtb:p>
                        <dtb:flag class="ellipsis" />
                        <xsl:value-of select="$paramEllipsis" />
                    </dtb:p>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:if test="$column-delimiter">
                <dtb:span class="unspaced">
                    <xsl:value-of select="$paramColumnDelimiter" />
                </dtb:span>
            </xsl:if>
        </dtb:td>
    </xsl:template>

    <xsl:template match="table:covered-table-cell">
        <dtb:td />
    </xsl:template>

    <!-- TABLE OF CONTENTS -->

    <xsl:template match="text:table-of-content">
        <dtb:div class="toc" />
        <xsl:apply-templates select="current()/descendant::pagenum" />
    </xsl:template>

    <!-- FRAMES -->

    <xsl:template match="draw:a">
        <xsl:param name="transposed" as="xsd:boolean" select="false()" />
        <xsl:apply-templates select="draw:frame">
            <xsl:with-param name="transposed" select="$transposed" />
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="draw:frame">
        <xsl:param name="transposed" as="xsd:boolean" select="false()" />
        
        <xsl:variable name="is-formula" as="xsd:boolean">
            <xsl:call-template name="is-formula">
                <xsl:with-param name="node" select="." />
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$is-formula">
                <xsl:apply-templates select="." mode="formula"/>
            </xsl:when>
            <xsl:when test="$transposed">
                <xsl:apply-templates select="." mode="frame">
                    <xsl:with-param name="transposed" select="true()"/>
                </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise/>
        </xsl:choose>
    </xsl:template>

    <!-- FORMULAS -->

    <xsl:template match="draw:a" mode="formula">
        <xsl:apply-templates select="draw:frame" mode="formula" />
    </xsl:template>

    <xsl:template match="draw:frame" mode="formula">
        <xsl:apply-templates select="draw:object/*" mode="math" />
    </xsl:template>

    <!-- TEXTBOXES & IMAGES -->

    <xsl:template match="draw:a" mode="frame">
        <xsl:param name="transposed" as="xsd:boolean" select="false()" />
        <xsl:apply-templates select="draw:frame" mode="frame">
            <xsl:with-param name="transposed" select="$transposed" />
        </xsl:apply-templates>
    </xsl:template>

    <xsl:template match="draw:frame" mode="frame">
        <xsl:param name="transposed" as="xsd:boolean" select="false()" />

        <xsl:variable name="caption-id">
            <xsl:call-template name="get-frame-caption-id">
                <xsl:with-param name="frame-name" select="@draw:name" />
            </xsl:call-template>
        </xsl:variable>

        <xsl:variable name="is-formula" as="xsd:boolean">
            <xsl:call-template name="is-formula">
                <xsl:with-param name="node" select="." />
            </xsl:call-template>
        </xsl:variable>
        
        <xsl:choose>

            <!-- Formula -->
            <xsl:when test="$is-formula"/>

            <!-- Image -->
            <xsl:when test="current()/child::draw:image">

                <xsl:variable name="caption" as="element()*">
                    <xsl:if test="$caption-id">
                        <xsl:apply-templates select="$body//text:p[@xml:id=$caption-id][1]">
                            <xsl:with-param name="caption" select="true()" />
                            <xsl:with-param name="frames" select="false()" />
                            <xsl:with-param name="notes" select="false()" />
                            <xsl:with-param name="specialtypeface" select="false()" />
                        </xsl:apply-templates>
                    </xsl:if>
                </xsl:variable>

                <xsl:variable name="description" as="element()*">
                    <xsl:if test="svg:title">
                        <dtb:span>
                            <xsl:apply-templates select="svg:title/text()" mode="convert-breaks" />
                        </dtb:span>
                    </xsl:if>
                    <xsl:if test="svg:desc">
                        <dtb:span>
                            <xsl:apply-templates select="svg:desc/text()" mode="convert-breaks" />
                        </dtb:span>
                    </xsl:if>
                </xsl:variable>

                <!-- Ignore images without a caption or description -->
                <xsl:if test="$caption or $description">

                    <!-- Collect notes -->
                    <xsl:variable name="notes" as="element()*">

                        <!-- Transposition notification -->
                        <xsl:if test="$transposed">
                            <dtb:note>
                                <dtb:span>
                                    <xsl:value-of select='"The picture below was transposed from it&apos;s original location."' />
                                </dtb:span>
                            </dtb:note>
                        </xsl:if>

                        <!-- Caption -->
                        <xsl:if test="$caption">
                            <dtb:note>
                                <xsl:sequence select="$caption"/>
                            </dtb:note>
                        </xsl:if>

                        <!-- Description -->
                        <xsl:for-each select="$description">
                            <xsl:variable name="i" select="position()"/>
                            <dtb:note>
                                <xsl:if test="$i=1 and string-length($paramPictureDescriptionPrefix)>0">
                                    <dtb:span class="unspaced">
                                        <xsl:value-of select="$paramPictureDescriptionPrefix" />
                                    </dtb:span>
                                </xsl:if>
                                <xsl:sequence select="."/>
                            </dtb:note>
                        </xsl:for-each>
                    </xsl:variable>

                    <dtb:div class="image">
                        <dtb:div class="tn">
                            <xsl:for-each select="$notes">
                                <xsl:copy>

                                    <!-- Add opening mark -->
                                    <xsl:if test="position() = 1">
                                        <dtb:span class="unspaced">
                                            <xsl:value-of select="$paramPictureOpeningMark" />
                                        </dtb:span>
                                    </xsl:if>
                                    <xsl:sequence select="child::*"/>

                                    <!-- Add closing mark -->
                                    <xsl:if test="position() = last()">
                                        <dtb:span class="unspaced">
                                            <xsl:value-of select="$paramPictureClosingMark" />
                                        </dtb:span>
                                    </xsl:if>
                                </xsl:copy>
                            </xsl:for-each>
                        </dtb:div>
                    </dtb:div>
                </xsl:if>

                <!-- Footnotes in caption -->
                <xsl:if test="$caption-id">
                    <xsl:variable name="caption" select="$body//text:p[@xml:id=$caption-id][1]" />
                    <xsl:if test="$caption//text:note[@text:note-class='footnote']">
                        <xsl:call-template name="footnote">
                            <xsl:with-param name="notes" select="$caption//text:note[@text:note-class='footnote']" />
                        </xsl:call-template>
                    </xsl:if>
                </xsl:if>
            </xsl:when>

            <!-- Textbox -->
            <xsl:when test="current()/child::draw:text-box">

                <xsl:variable name="style-name" select="@draw:style-name"/>
                <xsl:variable name="border"
                              select="$automatic-styles/style:style[@style:name=$style-name]
                                      /style:graphic-properties/@fo:border" />
                <xsl:variable name="border-top"
                              select="$automatic-styles/style:style[@style:name=$style-name]
                                      /style:graphic-properties/@fo:border-top" />
                <xsl:variable name="border-bottom"
                              select="$automatic-styles/style:style[@style:name=$style-name]
                                      /style:graphic-properties/@fo:border-bottom" />
                <xsl:variable name="upper-border" as="xsd:boolean">
                    <xsl:value-of select="$paramFrameUpperBorder and (($border and not($border='none')) or
                                                                      ($border-top and not($border-top='none')))" />
                </xsl:variable>
                <xsl:variable name="lower-border" as="xsd:boolean">
                    <xsl:value-of select="$paramFrameLowerBorder and (($border and not($border='none')) or
                                                                      ($border-bottom and not($border-bottom='none')))" />
                </xsl:variable>

                <!-- Transposition notification -->
                <xsl:if test="$transposed">
                    <dtb:div class="tn">
                        <dtb:note>
                            <xsl:value-of select='"The textbox below was transposed from it&apos;s original location."' />
                        </dtb:note>
                    </dtb:div>
                </xsl:if>

                <dtb:div class="frame">

                    <!-- Top boxline -->
                    <xsl:if test="$upper-border">
                        <dtb:hr/>
                    </xsl:if>
                    <dtb:div class="border">

                        <!-- Caption -->
                        <xsl:if test="$caption-id">
                            <xsl:variable name="caption" select="$body//text:p[@xml:id=$caption-id]" />
                            <xsl:if test="$caption" >
                                <dtb:div class="caption">
                                    <xsl:apply-templates select="$caption">
                                        <xsl:with-param name="caption" select="true()" />
                                        <xsl:with-param name="frames"  select="false()" />
                                        <xsl:with-param name="specialtypeface" select="false()" />
                                    </xsl:apply-templates>
                                </dtb:div>
                            </xsl:if>
                        </xsl:if>

                        <!-- Box content -->
                        <xsl:apply-templates select="./draw:text-box/draw:frame | ./draw:text-box/draw:a" mode="formula"/>
                        <xsl:apply-templates select="./draw:text-box/draw:frame | ./draw:text-box/draw:a" mode="frame"/>
                        <xsl:apply-templates select="current()/draw:text-box/*" />
                    </dtb:div>

                    <!-- Bottom boxline -->
                    <xsl:if test="$lower-border">
                        <dtb:hr/>
                    </xsl:if>
                </dtb:div>
            </xsl:when>
        </xsl:choose>
    </xsl:template>

    <!-- MATH -->

    <xsl:template match="*"
                  mode="math">
        <xsl:element name="math:{local-name()}">
            <xsl:apply-templates select="@*|node()"
                                 mode="math"/>
        </xsl:element>
    </xsl:template>
    
    <xsl:template match="@*"
                  mode="math">
        <xsl:attribute name="{local-name()}">
            <xsl:value-of select="." />
        </xsl:attribute>
    </xsl:template>

    <xsl:template match="mmultiscripts | math:mmultiscripts"
                  mode="math">
        <xsl:choose>
            <xsl:when test="count(child::*) = 6 and local-name(child::*[4]) = 'mprescripts'">
                <xsl:element name="math:mmultiscripts">
                    <xsl:apply-templates select="@*" mode="math"/>
                    <xsl:element name="math:msubsup">
                        <xsl:apply-templates select="child::*[position() &lt;= 3]" mode="math"/>
                    </xsl:element>
                    <xsl:apply-templates select="child::*[position() > 3]" mode="math"/>
                </xsl:element>
            </xsl:when>
            <xsl:otherwise>
                <xsl:element name="math:mmultiscripts">
                    <xsl:apply-templates select="@*|node()" mode="math"/>
                </xsl:element>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="annotation | math:annotation"
                  mode="math">
        <!-- skip -->
    </xsl:template>

    <!-- BIBLIOGRAPHY -->

    <xsl:template match="text:bibliography" name="bibliography">

        <dtb:bibliography>
            <dtb:heading>
                <dtb:h1 class="dummy">
                    <xsl:apply-templates select="current()/text:index-body/text:index-title/text:p" mode="language">
                        <xsl:with-param name="specialtypeface" select="false()" />
                    </xsl:apply-templates>
                </dtb:h1>
            </dtb:heading>
            <dtb:list>
                <xsl:for-each select="current()/text:index-body/text:p">
                    <dtb:li>
                        <dtb:p>
                            <xsl:apply-templates select="current()" mode="language">
                                <xsl:with-param name="pagenum" select="false()" />
                            </xsl:apply-templates>
                        </dtb:p>
                    </dtb:li>
                </xsl:for-each>
            </dtb:list>
        </dtb:bibliography>
        <xsl:apply-templates select="current()/descendant::pagenum" />
    </xsl:template>


    <!--
================
SKIP / OMISSIONS
================
    -->

    <!-- SKIP -->

    <xsl:template match="text:soft-page-break" />
    <xsl:template match="math:math" />
    <xsl:template match="svg:title" />
    <xsl:template match="text:toc-mark-start" />
    <xsl:template match="text:toc-mark-end" />
    <xsl:template match="text:toc-mark" />
    <xsl:template match="text:bookmark-ref" />
    <xsl:template match="text:bookmark-start" />
    <xsl:template match="text:bookmark-end" />
    <xsl:template match="text:bookmark" />
    <xsl:template match="text:bibliography-source" />
    <xsl:template match="text:change" />
    <xsl:template match="text:change-start" />
    <xsl:template match="text:change-end" />

    <!-- OMISSIONS -->


    <!-- NOTHING -->

    <xsl:template match="text:bibliography-mark">
        <xsl:apply-templates />
    </xsl:template>


    <!--
==============
HELP TEMPLATES
==============
    -->

    <xsl:template name="get-endnote-section">
        <xsl:param name="start-section" />
        <xsl:variable name="style-name"   select="$start-section/@text:style-name" />
        <xsl:choose>
            <xsl:when test="not($start-section)">
                <xsl:value-of select="''" />
            </xsl:when>
            <xsl:when test="$automatic-styles/style:style[@style:name=($style-name)]
                            /style:section-properties/text:notes-configuration[@text:note-class='endnote']">
                <xsl:value-of select="$start-section/@text:name" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:call-template name="get-endnote-section">
                    <xsl:with-param name="start-section" select="$start-section/ancestor::text:section[1]" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="get-configured-paragraph-style">
        <xsl:param name="style-name" as="xsd:string"/>
        <xsl:variable name="contains" as="xsd:boolean">
            <xsl:call-template name="array-contains-value">
                <xsl:with-param name="array" select="$paramConfiguredParagraphStyles"/>
                <xsl:with-param name="value" select="$style-name"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$contains">
                <xsl:value-of select="$style-name" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parent-style-name">
                    <xsl:call-template name="get-parent-style-name">
                        <xsl:with-param name="style-name" select="$style-name"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($parent-style-name='')">
                        <xsl:call-template name="get-configured-paragraph-style">
                            <xsl:with-param name="style-name" select="$parent-style-name"/>
                        </xsl:call-template>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="'Standard'" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="get-keep-empty-para-style">
        <xsl:param name="style-name" as="xsd:string"/>
        <xsl:call-template name="array-contains-value">
            <xsl:with-param name="array" select="$paramKeepEmptyParagraphStyles"/>
            <xsl:with-param name="value" select="$style-name"/>
        </xsl:call-template>
    </xsl:template>


    <xsl:template name="get-noteref-prefix">
        <xsl:param name="num-format" as="xsd:string" />
        <xsl:variable name="index" as="xsd:integer">
            <xsl:call-template name="first-index-of">
                <xsl:with-param name="array" select="$paramNoterefNumberFormats"/>
                <xsl:with-param name="value" select="$num-format"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$index = -1">
                <xsl:value-of select="''" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$paramNoterefNumberPrefixes[$index]" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="get-table-caption-id">
        <xsl:param name="table-name" />
        <xsl:value-of select="$controller//ns1:table[@rdf:about=concat('table:/',$table-name)]/ns1:hasCaption[1]/@rdf:resource" />
    </xsl:template>


    <xsl:template name="get-frame-caption-id">
        <xsl:param name="frame-name" />
        <xsl:value-of select="$controller//ns1:frame[@rdf:about=concat('frame:/',$frame-name)]/ns1:hasCaption[1]/@rdf:resource" />
    </xsl:template>


    <xsl:template name="get-frontmatter-section">
        <xsl:choose>
            <xsl:when test="$controller//ns1:frontmatter">
                <xsl:value-of select="substring-after(string($controller//ns1:frontmatter/@rdf:about), 'section:/')" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="''" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    

    <xsl:template name="flatten-table-header-rows">
        <xsl:param name="table"/>
        <xsl:if test="name($table)='table:table'">
            <table:table>
                <xsl:for-each select="$table/table:table-header-rows/table:table-row">
                    <xsl:sequence select="."/>
                </xsl:for-each>
                <xsl:for-each select="$table/table:table-row">
                    <xsl:sequence select="."/>
                </xsl:for-each>
            </table:table>
        </xsl:if>
    </xsl:template>

    <xsl:template name="mirror-table">
        <xsl:param name="table"/>
        <xsl:if test="name($table)='table:table'">
            <table:table>
                <xsl:for-each select="$table/table:table-row[1]/*[self::table:table-cell or self::table:covered-table-cell]">
                    <xsl:variable name="i" select="position()" />
                    <table:table-row>
                        <xsl:for-each select="$table/table:table-row">
                            <xsl:sequence select="child::*[self::table:table-cell or self::table:covered-table-cell][$i]"/>
                        </xsl:for-each>
                    </table:table-row>
                </xsl:for-each>
            </table:table>
        </xsl:if>
    </xsl:template>

</xsl:stylesheet>
