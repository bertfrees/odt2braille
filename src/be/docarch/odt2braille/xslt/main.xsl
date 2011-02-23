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
                                 xlink number svg chart dr3d math form script dc meta xalan" >

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"
                    xalan:indent-amount="3" />

        <!-- XSLT Parameters  -->

        <xsl:param name="paramNoteSectionTitle"           as="xsd:string"   select="'NOTES'"    />
        <xsl:param name="paramColumnDelimiter"            as="xsd:string"   select='"&#x2806;"' />

        <xsl:param name="paramStairstepTableEnabled"      as="xsd:boolean"  select="false()"  />
        <xsl:param name="paramHyphenationEnabled"         as="xsd:boolean"  select="false()"  />
        <xsl:param name="paramKeepHardPageBreaks"         as="xsd:boolean"  select="false()"  />

        <xsl:param name="paramEllipsis"			  as="xsd:string"   select="'&#x2810;&#x2810;&#x2810;'" />
        <xsl:param name="paramDoubleDash"		  as="xsd:string"   select="'&#x2824;&#x2824;&#x2824;&#x2824;'" />
        <xsl:param name="paramEllipsisDots"		  as="xsd:string"   select="'(5, 5, 5)'" />
        <xsl:param name="paramDoubleDashDots"		  as="xsd:string"   select="'(36, 36, 36, 36)'" />

        <xsl:param name="paramKeepEmptyParagraphStyles"   as="xsd:string*"  />
        <xsl:param name="paramNoterefNumberPrefixes"      as="xsd:string*"  />
        <xsl:param name="paramNoterefNumberFormats"       as="xsd:string*"  />

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
                <xsl:if test="$body/office:text/text:section[@text:name='PreliminaryPages']">
                    <dtb:frontmatter>
                        <xsl:apply-templates mode="toplevel"
                                             select="$body/office:text/text:section[@text:name='PreliminaryPages'][1]" />
                    </dtb:frontmatter>
                </xsl:if>

                <!-- BODYMATTER -->
                <dtb:bodymatter>
                    <xsl:choose>
                        <xsl:when test="$body/office:text/text:section[@text:name='PreliminaryPages']">
                            <xsl:call-template name="bodymatter">
                                <xsl:with-param name="currentNode" select="$body/office:text/text:section[@text:name='PreliminaryPages']" />
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:call-template name="bodymatter" />
                        </xsl:otherwise>
                    </xsl:choose>
                </dtb:bodymatter>
            </dtb:book>
        </dtb:dtbook>

        <xsl:value-of select="'&#10;'" />
    </xsl:template>

    <!--
==========
BODYMATTER
==========
    -->

    <xsl:template name="bodymatter">
        <xsl:param name="currentNode" select="$body/office:text/text:sequence-decls[1]" />
        <xsl:param name="supplement"  select="false()" as="xsd:boolean" />
        <xsl:param name="volumeNr"    select="1"       as="xsd:integer" />

        <xsl:variable name="sectionName">
            <xsl:choose>
                <xsl:when test="$supplement">
                    <xsl:value-of select="concat('Supplement',$volumeNr)" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="concat('Volume',$volumeNr)" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$currentNode/following-sibling::text:section[@text:name=$sectionName]">

                <!-- This volume -->
                <dtb:div>
                    <xsl:attribute name="class">
                        <xsl:choose>
                            <xsl:when test="$supplement">
                                <xsl:value-of select="'supplement'" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="'volume'" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:attribute>

                    <xsl:attribute name="id" select="lower-case($sectionName)" />
                    
                    <!-- Omitted pages before this volume -->
                    <xsl:if test="$currentNode[@text:name='PreliminaryPages']
                               or $currentNode/following-sibling::*[following-sibling::text:section[@text:name=$sectionName]]">
                        <dtb:div class="not-in-volume">
                            <xsl:if test="$currentNode[@text:name='PreliminaryPages']">
                                <xsl:apply-templates mode="toplevel"
                                                     select="$currentNode/following-sibling::*[1]/preceding-sibling::*[not(self::text:sequence-decls)]">
                                    <xsl:with-param name="render" select="false()" />
                                </xsl:apply-templates>
                            </xsl:if>
                            <xsl:apply-templates mode="toplevel"
                                                 select="$currentNode/following-sibling::*[following-sibling::text:section[@text:name=$sectionName]]">
                                <xsl:with-param name="render" select="false()" />
                            </xsl:apply-templates>
                        </dtb:div>
                    </xsl:if>
                    
                    <!-- Body -->
                    <xsl:apply-templates mode="toplevel"
                                         select="$currentNode/following-sibling::text:section[@text:name=$sectionName]/child::*">
                    </xsl:apply-templates>

                    <!-- Notesection -->
                    <xsl:call-template name="notesection">
                        <xsl:with-param name="sectionName" select="$sectionName" />
                        <xsl:with-param name="endnotes"    select="$currentNode/following-sibling::text:section[@text:name=$sectionName]
                                                                               //text:note[@text:note-class='endnote']" />
                    </xsl:call-template>

                    <!-- Omitted pages after this volume -->
                    <xsl:if test="(    $supplement  and not($currentNode/following-sibling::text:section[@text:name=concat('Supplement',$volumeNr+1)]))
                               or (not($supplement) and not($currentNode/following-sibling::text:section[@text:name=concat('Volume',    $volumeNr+1)]) 
                                                    and not($currentNode/following-sibling::text:section[@text:name='Supplement1']))">
                        <dtb:div class="not-in-volume">
                            <xsl:apply-templates mode="toplevel"
                                                 select="$currentNode/following-sibling::text:section[@text:name=$sectionName]/following-sibling::*">
                                <xsl:with-param name="render"    select="false()" />
                            </xsl:apply-templates>
                        </dtb:div>
                    </xsl:if>
                </dtb:div>
                
                <!-- Next volume -->
                <xsl:call-template name="bodymatter">
                    <xsl:with-param name="currentNode" select="$currentNode/following-sibling::text:section[@text:name=$sectionName]"/>
                    <xsl:with-param name="supplement"  select="$supplement" />
                    <xsl:with-param name="volumeNr"    select="$volumeNr+1" />
                </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="not($supplement)">
                    <xsl:choose>
                        <xsl:when test="$volumeNr=1">

                            <!-- This volume -->
                            <dtb:div class="volume" id="volume1">

                                <!-- Omitted pages before this volume -->
                                <xsl:if test="$currentNode[@text:name='PreliminaryPages']">
                                    <dtb:div class="not-in-volume">
                                        <xsl:apply-templates mode="toplevel"
                                                             select="$currentNode/following-sibling::*[1]/preceding-sibling::*[not(self::text:sequence-decls)]">
                                            <xsl:with-param name="render" select="false()" />
                                        </xsl:apply-templates>
                                    </dtb:div>
                                </xsl:if>
                                <xsl:choose>
                                    <xsl:when test="$currentNode/following-sibling::text:section[@text:name='Supplement1']">

                                        <!-- Body -->
                                        <xsl:apply-templates mode="toplevel"
                                                             select="$currentNode/following-sibling::*[following-sibling::text:section[@text:name='Supplement1']]">
                                        </xsl:apply-templates>

                                        <!-- Notesection -->
                                        <xsl:call-template name="notesection">
                                            <xsl:with-param name="endnotes"
                                                            select="$currentNode/following-sibling::*[following-sibling::text:section[@text:name='Supplement1']]
                                                                                /descendant-or-self::text:note[@text:note-class='endnote']" />
                                        </xsl:call-template>
                                    </xsl:when>
                                    <xsl:otherwise>

                                        <!-- Body -->
                                        <xsl:apply-templates mode="toplevel"
                                                             select="$currentNode/following-sibling::*">
                                        </xsl:apply-templates>

                                        <!-- Notesection -->
                                        <xsl:call-template name="notesection">
                                            <xsl:with-param name="endnotes"
                                                            select="$currentNode/following-sibling::*/descendant-or-self::text:note[@text:note-class='endnote']" />
                                        </xsl:call-template>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </dtb:div>

                            <!-- Next volume -->
                            <xsl:if test="$currentNode/following-sibling::text:section[@text:name='Supplement1']">
                                <xsl:call-template name="bodymatter">
                                    <xsl:with-param name="currentNode" select="$currentNode/following-sibling::*[following-sibling::text:section[@text:name='Supplement1']][last()]"/>
                                    <xsl:with-param name="supplement"  select="true()" />
                                    <xsl:with-param name="volumeNr"    select="1" />
                                </xsl:call-template>
                            </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>

                            <!-- Next volume -->
                            <xsl:call-template name="bodymatter">
                                <xsl:with-param name="currentNode" select="$currentNode"/>
                                <xsl:with-param name="supplement"  select="true()" />
                                <xsl:with-param name="volumeNr"    select="1" />
                            </xsl:call-template>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <!--
=========
TOP LEVEL
=========
    -->

    <xsl:template match="*|@*" name="toplevel" mode="toplevel">
        <xsl:param name="render"      as="xsd:boolean" select="true()"  />
        <xsl:param name="omissions"   as="xsd:boolean" select="true()"  />
        <xsl:param name="pagenumbers" as="xsd:boolean" select="true()"  />

        <xsl:choose>

            <!-- PAGENUMBER -->
            <xsl:when test="$pagenumbers and name(current())='pagenum'">
                <xsl:call-template name="pagenumbering" />
            </xsl:when>

            <!-- HEADING -->
            <xsl:when test="$render and name(current())='text:h'">
                <xsl:call-template name="heading" />
            </xsl:when>

            <!-- PARAGRAPH -->
            <xsl:when test="$render and name(current())='text:p'">
                <xsl:call-template name="para" />
            </xsl:when>

            <!-- TITLE PAGE -->
            <xsl:when test="$render and name(current())='text:section' and @text:name='TitlePage'">
                <xsl:call-template name="titlepage" />
            </xsl:when>

            <!-- LIST -->
            <xsl:when test="$render and name(current())='text:list'">
                <xsl:call-template name="list"/>
            </xsl:when>

            <!-- TABLE -->
            <xsl:when test="$render and name(current())='table:table'">
                <xsl:call-template name="table"/>
            </xsl:when>

            <!-- SECTION -->
            <xsl:when test="$render and name(current())='text:section'">
                <xsl:call-template name="section" />
            </xsl:when>

            <!-- BIBLIOGRAPHY -->
            <xsl:when test="$render and name(current())='text:bibliography'">
                <xsl:call-template name="bibliography" />
            </xsl:when>

            <!-- NOT RENDERED PRELIMINARY PAGES -->
            <xsl:when test="not($render) and current()/self::text:section[@text:name='PreliminaryPages']">
                <xsl:apply-templates select="current()/child::*" mode="toplevel">
                    <xsl:with-param name="render"      select="false()"      />
                    <xsl:with-param name="omissions"   select="false()"      />
                    <xsl:with-param name="pagenumbers" select="$pagenumbers" />
                </xsl:apply-templates>
            </xsl:when>

            <!-- NOT RENDERED SECTION -->
            <xsl:when test="not($render) and name(current())='text:section'">
                <xsl:apply-templates select="current()/child::*" mode="toplevel">
                    <xsl:with-param name="render"      select="false()"      />
                    <xsl:with-param name="omissions"   select="$omissions"   />
                    <xsl:with-param name="pagenumbers" select="$pagenumbers" />
                </xsl:apply-templates>
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

            <!-- OMIT EMPTY PARAGRAPH -->
            <xsl:when test="name(current())='text:p' and not(string(current()) or count(current()/node()) > 0)">
                <!-- no omission flag -->
            </xsl:when>

            <!-- OTHER OMISSIONS -->
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
        <xsl:param name="pagenum"          select="'true'" />
        <xsl:param name="specialtypeface"  select="'true'" />

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

        <xsl:param name="pagenum"          select="'true'" />
        <xsl:param name="specialtypeface"  select="'true'" />

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
                <xsl:when test="$specialtypeface='false'">
                    <xsl:apply-templates>
                        <xsl:with-param name="pagenum"         select="$pagenum" />
                        <xsl:with-param name="specialtypeface" select="'false'" />
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
        <xsl:param name="specialtypeface" select="'true'" />
        <xsl:param name="pagenum"         select="'true'" />

        <xsl:if test="string(.) or count(./*) > 0">

            <xsl:variable name="character-style-name">
                <xsl:call-template name="get-character-style-name">
                    <xsl:with-param name="style-name" select="current()/@text:style-name" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$specialtypeface='true' and not($character-style-name='')">
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
        <xsl:param name="specialtypeface" select="'true'" />
        <xsl:param name="pagenum"         select="'true'" />

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

        <xsl:param name="pagenum" select="'true'" />

        <xsl:if test="$pagenum='true'">
            <xsl:if test="(@type = 'hard' and $paramKeepHardPageBreaks) or
                          (@type = 'new-braille-page')">
                <dtb:pagebreak />
            </xsl:if>
            <xsl:if test="@num and @enum and @render and @value">
                <xsl:variable name="attrPage">
                    <xsl:choose>
                        <xsl:when test="@enum = '1'">
                            <xsl:value-of select="'normal'" />
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="'special'" />
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="@render = 'true'">
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

    <xsl:template match="text:note">
        
        <dtb:noteref>
            <xsl:variable name="note-class" select="@text:note-class" />
            <xsl:choose>
                <xsl:when test="text:note-citation[not(@text:label)]">
                    <xsl:variable name="num-format" as="xsd:string">
                        <xsl:call-template name="get-note-format">
                            <xsl:with-param name="section"    select="ancestor::text:section[1]" />
                            <xsl:with-param name="note-class" select="$note-class" />
                        </xsl:call-template>
                    </xsl:variable>
                    <dtb:span class="unspaced">
                        <xsl:call-template name="get-noteref-prefix">
                            <xsl:with-param name="num-format" select="$num-format" />
                        </xsl:call-template>
                    </dtb:span>                    
                    <xsl:choose>
                        <xsl:when test="$note-class='endnote' and $num-format='i'">
                            <xsl:value-of select="text:note-citation/text()" />
                        </xsl:when>
                        <xsl:when test="$note-class='endnote' and $num-format='I'">
                            <xsl:value-of select="upper-case(./text:note-citation/text())" />
                        </xsl:when>
                        <xsl:when test="$note-class='endnote'">
                            <xsl:call-template name="format-number">
                                <xsl:with-param name="integer">
                                    <xsl:call-template name="roman-to-integer">
                                        <xsl:with-param name="roman" select="text:note-citation/text()" />
                                    </xsl:call-template>
                                </xsl:with-param>
                                <xsl:with-param name="num-format" select="$num-format" />
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:when test="$note-class='footnote'">
                            <xsl:call-template name="format-number">
                                <xsl:with-param name="integer"    select="text:note-citation/text()" />
                                <xsl:with-param name="num-format" select="$num-format" />
                            </xsl:call-template>
                        </xsl:when>
                        <xsl:otherwise />
                    </xsl:choose>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="text:note-citation/text()" />
                </xsl:otherwise>
            </xsl:choose>
        </dtb:noteref>
    </xsl:template>


   <!--
==============
BLOCK ELEMENTS
==============
    -->

    <!-- HEADINGS -->

    <xsl:template match="text:h">
        <xsl:param name="specialtypeface"  select="'false'" />
        <xsl:param name="notes"            select="'true'"  />
        <xsl:param name="frames"           select="'true'"  />
        <xsl:param name="omitFrames"       select="'false'" />
        <xsl:param name="transposeFrames"  select="'false'" />

        <xsl:call-template name="heading">
            <xsl:with-param name="specialtypeface"  select="$specialtypeface" />
            <xsl:with-param name="notes"            select="$notes"           />
            <xsl:with-param name="frames"           select="$frames"          />
            <xsl:with-param name="omitFrames"       select="$omitFrames"      />
            <xsl:with-param name="transposeFrames"  select="$transposeFrames" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="heading">
        <xsl:param name="specialtypeface"  select="'false'" />
        <xsl:param name="notes"            select="'true'"  />
        <xsl:param name="frames"           select="'true'"  />
        <xsl:param name="omitFrames"       select="'false'" />
        <xsl:param name="transposeFrames"  select="'false'" />

        <xsl:variable name="outline-level" as="xsd:integer" select="current()/@text:outline-level" />

        <dtb:heading>
            <xsl:if test="current()/child::* | current()/text()">
                <xsl:element name="dtb:h{$outline-level}">
                    <xsl:apply-templates select="current()" mode="language">
                        <xsl:with-param name="specialtypeface" select="$specialtypeface" />
                    </xsl:apply-templates>
                </xsl:element>
            </xsl:if>
            <xsl:choose>
                <xsl:when test="$omitFrames='true'">

                    <!-- Omit frames -->
                    <xsl:for-each select="current()/draw:frame | current()/draw:a">
                        <dtb:div class="omission">
                            <dtb:flag class="double-dash" />
                            <dtb:span class="spaced">
                                <dtb:value-of select="$paramDoubleDash" />
                            </dtb:span>
                        </dtb:div>
                    </xsl:for-each>
                </xsl:when>
                <xsl:when test="$transposeFrames='true'">

                    <!-- Transpose frames -->
                    <xsl:for-each select="current()/draw:frame | current()/draw:a">
                        <dtb:div class="transposition">
                            <dtb:flag class="double-dash" />
                            <dtb:span class="spaced">
                                <dtb:value-of select="$paramDoubleDash" />
                            </dtb:span>
                        </dtb:div>
                    </xsl:for-each>
                </xsl:when>
            </xsl:choose>
            <xsl:if test="$notes = 'true'">

                <!-- Footnotes -->
                <xsl:if test="current()//text:note[@text:note-class='footnote']">
                    <xsl:call-template name="footnote">
                        <xsl:with-param name="notes" select="current()//text:note[@text:note-class='footnote']" />
                    </xsl:call-template>
                </xsl:if>
            </xsl:if>
        </dtb:heading>

        <!-- Frames -->
        <xsl:if test="$frames='true' and $transposeFrames='false' and $omitFrames='false'">
            <xsl:apply-templates select="current()/draw:frame | current()/draw:a">
                <xsl:with-param name="render" select="'true'" />
            </xsl:apply-templates>
        </xsl:if>
    </xsl:template>

    <!-- PARAGRAPHS -->

    <xsl:template match="text:p">
        <xsl:param name="caption"           select="'false'" />
        <xsl:param name="notes"             select="'true'"  />
        <xsl:param name="frames"            select="'true'"  />
        <xsl:param name="omitFrames"        select="'false'" />
        <xsl:param name="transposeFrames"   select="'false'" />

        <xsl:call-template name="para">
            <xsl:with-param name="caption"           select="$caption"         />
            <xsl:with-param name="notes"             select="$notes"           />
            <xsl:with-param name="frames"            select="$frames"          />
            <xsl:with-param name="omitFrames"        select="$omitFrames"      />
            <xsl:with-param name="transposeFrames"   select="$transposeFrames" />
        </xsl:call-template>
    </xsl:template>

    <xsl:template name="para">
        <xsl:param name="caption"           select="'false'" />
        <xsl:param name="notes"             select="'true'"  />
        <xsl:param name="frames"            select="'true'"  />
        <xsl:param name="omitFrames"        select="'false'" />
        <xsl:param name="transposeFrames"   select="'false'" />

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
            <xsl:when test="$caption='true'">
                <dtb:caption>
                    <xsl:apply-templates select="current()" mode="language">
                        <xsl:with-param name="pagenum" select="'false'" />
                    </xsl:apply-templates>
                </dtb:caption>

                <!-- Footnotes -->
                <xsl:if test="$notes='true'">
                    <xsl:if test="current()//text:note[@text:note-class='footnote']">
                        <xsl:call-template name="footnote">
                            <xsl:with-param name="notes" select="current()//text:note[@text:note-class='footnote']" />
                        </xsl:call-template>
                    </xsl:if>
                </xsl:if>
            </xsl:when>

            <!-- PARAGRAPH -->
            <xsl:otherwise>
                <xsl:choose>
                    <xsl:when test="$is-caption">
                        <xsl:apply-templates select="pagenum" />
                    </xsl:when>
                    <xsl:when test="$is-paragraph and $is-empty">

                        <!-- Empty paragraph -->
                        <xsl:apply-templates select="pagenum" />
                        <xsl:variable name="keep-empty" as="xsd:boolean">
                            <xsl:call-template name="get-keep-empty-para-style">
                                <xsl:with-param name="style" select="$style-name" />
                            </xsl:call-template>
                        </xsl:variable>                        
                        <xsl:if test="$keep-empty">
                            <dtb:br />
                        </xsl:if>
                    </xsl:when>
                    <xsl:otherwise>
                        <dtb:paragraph>
                            <xsl:if test="$is-paragraph">
                                <xsl:attribute name="style" select="$style-name" />
                            </xsl:if>
                            <xsl:if test="current()/child::*[not(self::draw:a or self::draw:frame)]
                                        | current()/text()">
                                <dtb:p>
                                    <xsl:apply-templates select="current()" mode="language" />
                                </dtb:p>
                            </xsl:if>
                            <xsl:choose>
                                <xsl:when test="$omitFrames='true'">

                                    <!-- Omit frames -->
                                    <xsl:for-each select="./draw:frame | ./draw:a">
                                        <dtb:div class="omission">
                                            <dtb:flag class="double-dash" />
                                            <dtb:span class="spaced">
                                                <xsl:value-of select="$paramDoubleDash" />
                                            </dtb:span>
                                        </dtb:div>
                                    </xsl:for-each>
                                </xsl:when>
                                <xsl:when test="$transposeFrames='true'">

                                    <!-- Transpose frames -->
                                    <xsl:for-each select="current()/draw:frame | current()/draw:a">
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
                            <xsl:if test="$notes='true'">
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
        <xsl:if test="$frames='true' and $transposeFrames='false' and $omitFrames='false'">
            <xsl:apply-templates select="current()/draw:frame | current()/draw:a">
                <xsl:with-param name="render" select="'true'" />
            </xsl:apply-templates>
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
                <xsl:apply-templates select="current()" />
                <xsl:apply-templates select="(current()/text:note-body/text:p)
                                            |(current()/text:note-body/text:h)">
                    <xsl:with-param name="frames"           select="'false'" />
                    <xsl:with-param name="notes"            select="'false'" />
                    <xsl:with-param name="specialtypeface"  select="'true'"  />
                    <xsl:with-param name="omitFrames"       select="'true'"  />
                </xsl:apply-templates>
            </dtb:note>
        </xsl:for-each>
    </xsl:template>

    <!-- TITLE PAGE -->

    <xsl:template name="titlepage">

        <dtb:titlepage newpage="yes">
            <xsl:apply-templates mode="toplevel" select="child::*" />
        </dtb:titlepage>
    </xsl:template>

    <!-- SECTIONS -->

    <xsl:template name="section">
        <xsl:variable name="styleName"      select="@text:style-name" />
        <xsl:variable name="sectionName"    select="@text:name"       />

        <xsl:apply-templates mode="toplevel" select="child::*" />

        <!-- Section endnotes -->
        <xsl:if test="$automatic-styles/style:style[@style:name=($styleName)]
                      /style:section-properties/text:notes-configuration[@text:note-class='endnote']" >

            <xsl:call-template name="notesection">
                <xsl:with-param name="sectionName" select="$sectionName" />
                <xsl:with-param name="endnotes" select="current()//text:note[@text:note-class='endnote']" />
            </xsl:call-template>
        </xsl:if>
    </xsl:template>

    <!-- ENDNOTE SECTIONS -->

    <xsl:template name="notesection" >
        <xsl:param name="sectionName" select="''"/>
        <xsl:param name="endnotes" />

        <xsl:if test="$endnotes">
            <dtb:notesection newpage="yes">
                <dtb:heading>
                    <dtb:h1 class="dummy">
                        <xsl:value-of select="$paramNoteSectionTitle" />
                    </dtb:h1>
                </dtb:heading>
                <xsl:for-each select="$endnotes">
                    <xsl:call-template name="endnote">
                        <xsl:with-param name="sectionName" select="$sectionName" />
                    </xsl:call-template>
                </xsl:for-each>
            </dtb:notesection>
        </xsl:if>
    </xsl:template>

    <!-- ENDNOTE -->

    <xsl:template name="endnote" >
        <xsl:param name="sectionName" />

        <xsl:variable name="hasAlreadyBeenRendered">
            <xsl:call-template name="endnoteBeenRendered">
                <xsl:with-param name="parentSection" select="current()/ancestor::text:section[1]" />
                <xsl:with-param name="topSectionName" select="$sectionName" />
            </xsl:call-template>
        </xsl:variable>
        
        <xsl:if test="$hasAlreadyBeenRendered='false'">

            <dtb:note>
                <xsl:attribute name="class">
                    <xsl:value-of select="'endnote'" />
                </xsl:attribute>
                <xsl:apply-templates select="current()" />
                <xsl:apply-templates select="(current()/text:note-body/text:p)
                                            |(current()/text:note-body/text:h)">
                    <xsl:with-param name="frames"           select="'false'" />
                    <xsl:with-param name="notes"            select="'false'" />
                    <xsl:with-param name="specialtypeface"  select="'true'"  />
                    <xsl:with-param name="omitFrames"       select="'true'"  />
                </xsl:apply-templates>
            </dtb:note>
        </xsl:if>
    </xsl:template>

    <!-- LISTS -->

    <xsl:template name="list" match="text:list">

        <xsl:param name="listlevel"       select="0" />
        <xsl:param name="transposed"      select="'false'" />
        <xsl:param name="transposeLists"  select="'false'" />

        <xsl:choose>
            <xsl:when test="$transposeLists='true'">
                <dtb:div class="transposition">
                    <dtb:flag class="double-dash" />
                    <dtb:span class="spaced">
                        <dtb:value-of select="$paramDoubleDash" />
                    </dtb:span>
                </dtb:div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:if test="$transposed='true'">
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
                                                    |(current()//text:list-header/*/draw:a)">

                            <xsl:with-param name="render" select="'true'" />
                        </xsl:apply-templates>
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

        <xsl:param name="listlevel" select="0" />
        <dtb:li>
            <xsl:apply-templates select="current()/child::*">
                <xsl:with-param name="specialtypeface"  select="'true'"     />
                <xsl:with-param name="notes"            select="'true'"     />
                <xsl:with-param name="frames"           select="'false'"    />
                <xsl:with-param name="listlevel"        select="$listlevel" />
            </xsl:apply-templates>
        </dtb:li>
    </xsl:template>

    <!-- TABLES -->

    <xsl:template match="table:table" name="table">

        <xsl:param name="transposed"       select="'false'" />
        <xsl:param name="transposeTables"  select="'false'" />

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
        <xsl:choose>
            <xsl:when test="$transposeTables='true'">
                <dtb:div class="transposition">
                    <dtb:flag class="double-dash" />
                    <dtb:span class="spaced">
                        <dtb:value-of select="$paramDoubleDash" />
                    </dtb:span>
                </dtb:div>
            </xsl:when>
            <xsl:otherwise>
                <xsl:variable name="parentStyleNameFirstCell"
                              select="$automatic-styles
                                      /style:style[@style:name=(current()/table:table-row[1]/table:table-cell/text:p/@text:style-name)]
                                      /@style:parent-style-name" />
                <xsl:variable name="styleNameFirstCell"
                              select="current()/table:table-row[1]/table:table-cell/text:p/@text:style-name"/>
                <xsl:variable name="transposedContent" select="current()/descendant::*[name()='table:table'
                                                                                    or name()='text:list'
                                                                                    or name()='draw:frame']" />
                <xsl:variable name="emptyCells" select="current()/table:table-row/table:table-cell
                                                            [not(string(self::*) or count(self::*/*) > 0)]
                                                     or current()/table:table-header-rows/table:table-row/table:table-cell
                                                            [not(string(self::*) or count(self::*/*) > 0)]" />
                <xsl:if test="$transposedContent or ($transposed='true')">
                    <dtb:div class="tn">
                        <xsl:if test="$transposed='true'">
                            <dtb:note>
                                <xsl:value-of select='"The table below was transposed from it&apos;s original location."' />
                            </dtb:note>
                        </xsl:if>
                        <xsl:if test="$transposedContent">
                            <dtb:note>
                                <xsl:value-of select="'Some material inside the table below was moved outside.'" />
                            </dtb:note>
                            <dtb:note>
                                <dtb:span class="spaced"><xsl:value-of select="'These transpositions are indicated by a double dash: '" /></dtb:span>
                                <dtb:span class="spaced"><dtb:value-of select="$paramDoubleDash" /></dtb:span>
                                <dtb:span class="spaced"><dtb:value-of select="$paramDoubleDashDots" /></dtb:span>
                            </dtb:note>
                        </xsl:if>
                        <xsl:if test="$emptyCells">
                            <dtb:note>
                                <dtb:span class="spaced"><xsl:value-of select="'Empty table cells are indicated by an ellipsis:'" /></dtb:span>
                                <dtb:span class="spaced"><xsl:value-of select="$paramEllipsis" /></dtb:span>
                                <dtb:span class="spaced"><xsl:value-of select="$paramEllipsisDots" /></dtb:span>
                            </dtb:note>
                        </xsl:if>
                    </dtb:div>
                </xsl:if>

                <!-- Top boxline -->
                <xsl:if test="not($border='none')">
                    <dtb:hr />
                </xsl:if>

                <dtb:table>
                    <!-- No border -->
                    <xsl:if test="$border='none'">
                        <xsl:attribute name="border">
                            <xsl:value-of select="'none'" />
                        </xsl:attribute>
                    </xsl:if>

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
                                    <xsl:with-param name="caption" select="'true'" />
                                    <xsl:with-param name="frames"  select="'false'" />
                                </xsl:apply-templates>
                            </dtb:div>
                        </xsl:if>
                    </xsl:if>
                    <xsl:choose>

                        <!-- Table with headings -->
                        <xsl:when test="$parentStyleNameFirstCell = 'Table_20_Heading' or $styleNameFirstCell = 'Table_20_Heading'">

                            <dtb:thead>
                                <xsl:apply-templates select="current()/table:table-row[ position() = 1 ]">
                                    <xsl:with-param name="heading" select="true()" />
                                </xsl:apply-templates>
                            </dtb:thead>

                            <dtb:hr />

                            <dtb:tbody>
                                <xsl:apply-templates select="current()/table:table-row[ position() > 1 ] |
                                                             current()/child::*[position() &lt; last() and name()='pagenum']" />
                            </dtb:tbody>
                        </xsl:when>
                        <xsl:when test="current()/table:table-header-rows">

                            <dtb:thead>
                                <xsl:apply-templates select="current()/table:table-header-rows/table:table-row" >
                                    <xsl:with-param name="heading" select="true()" />
                                </xsl:apply-templates>
                                <xsl:apply-templates select="current()/table:table-header-rows/pagenum" />
                            </dtb:thead>

                            <dtb:hr />

                            <dtb:tbody>
                                <xsl:apply-templates select="current()/table:table-row |
                                                             current()/child::*[position() &lt; last() and name()='pagenum']" />
                            </dtb:tbody>
                        </xsl:when>

                        <!-- Table without headings -->
                        <xsl:otherwise>
                            <xsl:apply-templates select="current()/table:table-row |
                                                         current()/child::*[position() &lt; last() and name()='pagenum']" />
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
                </dtb:table>

                <!-- Bottom boxline -->
                <xsl:if test="not($border='none')">
                    <dtb:hr />
                </xsl:if>

                <xsl:apply-templates select="current()/child::*[position()=last() and name()='pagenum']" />

                <!-- Transposed tables, lists & frames -->
                <xsl:apply-templates select="(current()/table:table-row/table:table-cell/*/draw:frame)
                                            |(current()/table:table-row/table:table-cell/*/draw:a)
                                            |(current()/table:table-row/table:table-cell/table:table)
                                            |(current()/table:table-row/table:table-cell/text:list)
                                            |(current()/table:table-header-rows/table:table-row/table:table-cell/*/draw:frame)
                                            |(current()/table:table-header-rows/table:table-row/table:table-cell/*/draw:a)
                                            |(current()/table:table-header-rows/table:table-row/table:table-cell/table:table)
                                            |(current()/table:table-header-rows/table:table-row/table:table-cell/text:list)">

                    <xsl:with-param name="transposed" select="'true'" />
                </xsl:apply-templates>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- TABLE ROW  -->

    <xsl:template match="table:table-row" name="table-row">
        <xsl:param name="heading" select="false()" />

        <dtb:tr>
            <xsl:apply-templates>
                <xsl:with-param name="heading" select="$heading" />
            </xsl:apply-templates>
        </dtb:tr>
    </xsl:template>

    <!-- TABLE CELL  -->

    <xsl:template match="table:table-cell" name="table-cell">
        <xsl:param name="heading" select="false()" />

        <xsl:choose>
            <xsl:when test="$heading">
                <dtb:th>
                    <xsl:choose>
                        <xsl:when test="string(.) or count(./*) > 0">
                            <xsl:apply-templates>
                                <xsl:with-param name="frames"           select="'false'" />
                                <xsl:with-param name="notes"            select="'false'" />
                                <xsl:with-param name="specialtypeface"  select="'true'"  />
                                <xsl:with-param name="transposeFrames"  select="'true'"  />
                                <xsl:with-param name="transposeTables"  select="'true'"  />
                                <xsl:with-param name="transposeLists"   select="'true'"  />
                            </xsl:apply-templates>
                            <xsl:if test="not($paramStairstepTableEnabled) and (position() &lt; last())">
                                <dtb:span class="unspaced">
                                    <xsl:value-of select="$paramColumnDelimiter" />
                                </dtb:span>
                            </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                            <dtb:p>
                                <dtb:flag class="ellipsis" />
                                <xsl:value-of select="$paramEllipsis" />
                            </dtb:p>
                        </xsl:otherwise>
                    </xsl:choose>
                </dtb:th>
            </xsl:when>
            <xsl:otherwise>
                <dtb:td>
                    <xsl:choose>
                        <xsl:when test="string(.) or count(./*) > 0">
                            <xsl:apply-templates>
                                <xsl:with-param name="frames"           select="'false'" />
                                <xsl:with-param name="notes"            select="'false'" />
                                <xsl:with-param name="specialtypeface"  select="'true'"  />
                                <xsl:with-param name="transposeFrames"  select="'true'"  />
                                <xsl:with-param name="transposeTables"  select="'true'"  />
                                <xsl:with-param name="transposeLists"   select="'true'"  />
                            </xsl:apply-templates>
                            <xsl:if test="not($paramStairstepTableEnabled) and (position() &lt; last())">
                                <dtb:span class="unspaced">
                                    <xsl:value-of select="$paramColumnDelimiter" />
                                </dtb:span>
                            </xsl:if>
                        </xsl:when>
                        <xsl:otherwise>
                            <dtb:p>
                                <dtb:flag class="ellipsis" />
                                <xsl:value-of select="$paramEllipsis" />
                            </dtb:p>
                        </xsl:otherwise>
                    </xsl:choose>
                </dtb:td>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template match="table:covered-table-cell">
        <xsl:param name="heading" select="false()" />

        <xsl:choose>
            <xsl:when test="$heading">
                <dtb:th />
            </xsl:when>
            <xsl:otherwise>
                <dtb:td />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <!-- TABLE OF CONTENTS -->
    <xsl:template match="text:table-of-content">
        <dtb:div class="toc" />
        <xsl:apply-templates select="current()/descendant::pagenum" />
    </xsl:template>
   
    <!-- FRAMES -->

    <xsl:template match="draw:frame">
        <xsl:param name="render"     select="'false'" />
        <xsl:param name="transposed" select="'false'" />

        <xsl:variable name="caption-id">
            <xsl:call-template name="get-frame-caption-id">
                <xsl:with-param name="frame-name" select="@draw:name" />
            </xsl:call-template>
        </xsl:variable>

        <xsl:if test="$render='true' or $transposed='true'">
            <xsl:choose>

                <!-- Formula -->
                <xsl:when test="current()/child::draw:object
                            and $automatic-styles/style:style[@style:name=(current()/@draw:style-name)]
                                /@style:parent-style-name='Formula'">
                    <xsl:apply-templates select="draw:object/*" mode="math" />
                </xsl:when>

                <!-- Image -->
                <xsl:when test="current()/child::draw:image">
                    <dtb:div class="image">
                        <dtb:div class="tn">

                            <!-- Transposition notification -->
                            <xsl:if test="$transposed='true'">
                                <dtb:note>
                                    <xsl:value-of select='"The image below was transposed from it&apos;s original location."' />
                                </dtb:note>
                            </xsl:if>

                            <!-- Caption -->
                            <xsl:if test="$caption-id">
                                <xsl:variable name="caption" select="$body//text:p[@xml:id=$caption-id][1]" />
                                <xsl:if test="$caption" >
                                    <dtb:note>
                                        <xsl:apply-templates select="$caption">
                                            <xsl:with-param name="caption" select="'true'" />
                                            <xsl:with-param name="frames"  select="'false'" />
                                            <xsl:with-param name="notes"   select="'false'" />
                                        </xsl:apply-templates>
                                    </dtb:note>
                                </xsl:if>
                            </xsl:if>

                            <!-- Not reproduced note -->
                            <dtb:note>
                                <xsl:value-of select="'This image is not reproduced in Braille.'" />
                            </dtb:note>

                            <!-- Title -->
                            <xsl:if test="svg:title">
                                <dtb:note>
                                    <dtb:span class="spaced">
                                        <xsl:value-of select="'Title:'" />
                                    </dtb:span>
                                    <xsl:apply-templates select="svg:title/text()" mode="convert-breaks" />
                                </dtb:note>
                            </xsl:if>

                            <!-- Description -->
                            <xsl:if test="svg:desc">
                                <dtb:note>
                                    <dtb:span class="spaced">
                                        <xsl:value-of select="'Description:'" />
                                    </dtb:span>
                                    <xsl:apply-templates select="svg:desc/text()" mode="convert-breaks" />
                                </dtb:note>
                            </xsl:if>
                        </dtb:div>
                    </dtb:div>

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

                    <xsl:variable name="border"
                                  select="$automatic-styles/style:style[@style:name=(current()/@draw:style-name)]
                                          /style:graphic-properties/@fo:border" />
                    <xsl:variable name="border-bottom"
                                  select="$automatic-styles/style:style[@style:name=(current()/@draw:style-name)]
                                          /style:graphic-properties/@fo:border-bottom" />
                    <xsl:variable name="border-top"
                                  select="$automatic-styles/style:style[@style:name=(current()/@draw:style-name)]
                                          /style:graphic-properties/@fo:border-top" />

                    <!-- Transposition notification -->
                    <xsl:if test="$transposed='true'">
                        <dtb:div class="tn">
                            <dtb:note>
                                <xsl:value-of select='"The textbox below was transposed from it&apos;s original location."' />
                            </dtb:note>
                        </dtb:div>
                    </xsl:if>

                    <!-- Top boxline -->
                    <xsl:if test="not($border='none')">
                        <dtb:hr />
                    </xsl:if>

                    <dtb:div class="frame">

                        <!-- No border -->
                        <xsl:if test="$border='none'">
                            <xsl:attribute name="border">
                                <xsl:value-of select="'none'" />
                            </xsl:attribute>
                        </xsl:if>

                        <!-- Caption -->
                        <xsl:if test="$caption-id">
                            <xsl:variable name="caption" select="$body//text:p[@xml:id=$caption-id]" />
                            <xsl:if test="$caption" >
                                <dtb:div class="caption">
                                    <xsl:apply-templates select="$caption">
                                        <xsl:with-param name="caption" select="'true'" />
                                        <xsl:with-param name="frames"  select="'false'" />
                                    </xsl:apply-templates>
                                </dtb:div>
                            </xsl:if>
                        </xsl:if>

                        <!-- Box content -->
                        <xsl:apply-templates select="current()/draw:text-box/draw:frame | current()/draw:text-box/draw:a">
                            <xsl:with-param name="render" select="'true'" />
                        </xsl:apply-templates>
                        <xsl:apply-templates select="current()/draw:text-box/*" />
                    </dtb:div>

                    <!-- Bottom boxline -->
                    <xsl:if test="not($border='none')">
                        <dtb:hr />
                    </xsl:if>
                </xsl:when>
            </xsl:choose>
        </xsl:if>
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
    
    <!-- FRAME LINKS -->

    <xsl:template match="draw:a">
        <xsl:param name="render"     select="'false'" />
        <xsl:param name="transposed" select="'false'" />

        <xsl:apply-templates select="draw:frame">
            <xsl:with-param name="render" select="$render" />
            <xsl:with-param name="transposed" select="$transposed" />
        </xsl:apply-templates>
    </xsl:template>

    <!-- BIBLIOGRAPHY -->

    <xsl:template match="text:bibliography" name="bibliography">

        <dtb:bibliography newpage="yes">
            <dtb:heading>
                <dtb:h1 class="dummy">
                    <xsl:apply-templates select="current()/text:index-body/text:index-title/text:p" mode="language">
                        <xsl:with-param name="specialtypeface" select="'false'" />
                    </xsl:apply-templates>
                </dtb:h1>
            </dtb:heading>
            <dtb:list>
                <xsl:for-each select="current()/text:index-body/text:p">
                    <dtb:li>
                        <dtb:p>
                            <xsl:apply-templates select="current()" mode="language">
                                <xsl:with-param name="pagenum" select="'false'" />
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

    <xsl:template name="endnoteBeenRendered">
        <xsl:param name="parentSection" />
        <xsl:param name="topSectionName" />

        <xsl:variable name="styleName" select="$parentSection/@text:style-name" />

        <xsl:choose>
            <xsl:when test="not($parentSection)">
                <xsl:value-of select="'false'" />
            </xsl:when>

            <xsl:when test="$parentSection/@text:name=($topSectionName)">
                <xsl:value-of select="'false'" />
            </xsl:when>

            <xsl:when test="$automatic-styles/style:style[@style:name=($styleName)]
                            /style:section-properties/text:notes-configuration[@text:note-class='endnote']">
                <xsl:value-of select="'true'" />
            </xsl:when>
            
            <xsl:otherwise>
                <xsl:call-template name="endnoteBeenRendered">
                    <xsl:with-param name="parentSection"  select="$parentSection/ancestor::text:section[1]" />
                    <xsl:with-param name="topSectionName" select="$topSectionName" />
                </xsl:call-template>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    
    <xsl:template name="get-keep-empty-para-style">
        <xsl:param name="style" />
        <xsl:variable name="occurences" as="xsd:integer*">
            <xsl:for-each select="$paramKeepEmptyParagraphStyles">
                <xsl:variable name="i" select="position()" />
                <xsl:if test="$paramKeepEmptyParagraphStyles[$i]=$style">
                    <xsl:sequence select="$i" />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$occurences[1]">
                <xsl:value-of select="true()" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="false()" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="get-noteref-prefix">
        <xsl:param name="num-format" as="xsd:string" />
        <xsl:variable name="occurences" as="xsd:integer*">
            <xsl:for-each select="$paramNoterefNumberFormats">
                <xsl:if test=".=$num-format">
                    <xsl:sequence select="position()" />
                </xsl:if>
            </xsl:for-each>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$occurences[1]">
                <xsl:variable name="i" select="$occurences[1]" />
                <xsl:value-of select="$paramNoterefNumberPrefixes[$i]" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="''" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="get-table-caption-id">
        <xsl:param name="table-name" />
        <xsl:value-of select="$controller//ns1:table[@rdf:about=$table-name]/ns1:hasCaption[1]/@rdf:resource" />
    </xsl:template>


    <xsl:template name="get-frame-caption-id">
        <xsl:param name="frame-name" />
        <xsl:value-of select="$controller//ns1:frame[@rdf:about=$frame-name]/ns1:hasCaption[1]/@rdf:resource" />
    </xsl:template>

</xsl:stylesheet>
