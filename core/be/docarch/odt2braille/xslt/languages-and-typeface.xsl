<?xml version="1.0" encoding="UTF-8"?>

    <!--

    odt2braille - Braille authoring in OpenOffice.org.

    Copyright (c) 2010-2011 by DocArch <http://www.docarch.be>.

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
                xmlns:dtb="http://www.daisy.org/z3986/2005/dtbook/"
                xmlns:math="http://www.w3.org/1998/Math/MathML"
                xmlns:xalan="http://xml.apache.org/xslt"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                
                exclude-result-prefixes="dtb math xalan xsd" >

        <xsl:output method="xml"
                    encoding="UTF-8"
                    media-type="text/xml"
                    indent="yes"
                    omit-xml-declaration="no"
                    xalan:indent-amount="3" />

        <xsl:param name="paramLocales"                  as="xsd:string*"   />
        <xsl:param name="paramTranslationTables"        as="xsd:string*"   />
        <xsl:param name="paramMainTranslationTable"     as="xsd:string"    />

        <xsl:param name="paramMathCode"                 as="xsd:string"    />

        <xsl:param name="paramKeepCapsStyles"           as="xsd:string*"  />
        <xsl:param name="paramKeepItalicStyles"         as="xsd:string*"  />
        <xsl:param name="paramKeepBoldfaceStyles"       as="xsd:string*"  />
        <xsl:param name="paramKeepUnderlineStyles"      as="xsd:string*"  />

        <xsl:variable name="main-lang" select="$paramMainTranslationTable" />

        <xsl:strip-space elements="dtb:strong dtb:em dtb:paragraph dtb:p dtb:div dtb:a dtb:note dtb:noteref
                                   dtb:span dtb:list dtb:li dtb:caption dtb:table dtb:tr dtb:td dtb:th dtb:thead dtb:tbody
                                   dtb:heading dtb:h1 dtb:h2 dtb:h3 dtb:h4 dtb:h5 dtb:h6 dtb:h7 dtb:h8 dtb:h9 dtb:h10" />

        <xsl:include href="common-templates.xsl" />
        

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>


    <xsl:template match="node()" mode="text">
        <xsl:variable name="is-caps" as="xsd:boolean">
            <xsl:call-template name="get-caps">
                <xsl:with-param name="node" select="." />
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$is-caps">
                <xsl:value-of select="upper-case(text())" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="text()" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template match="math:math">
        <dtb:span>
            <xsl:attribute name="lang">
                <xsl:text>__</xsl:text>
                <xsl:value-of select="$paramMathCode" />
                <xsl:text>.ctb</xsl:text>
            </xsl:attribute>
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </dtb:span>
    </xsl:template>


    <xsl:template match="dtb:paragraph[@style]">
        <xsl:if test="string(.)">
            <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
            </xsl:copy>
        </xsl:if>
    </xsl:template>


    <xsl:template match="dtb:span[@lang or @style or @font-style
                                                  or @font-weight
                                                  or @font-variant
                                                  or @text-transform
                                                  or @underline-style]">
        <xsl:apply-templates select="node()" />
    </xsl:template>


    <xsl:template match="dtb:text">
        <xsl:call-template name="lang" >
            <xsl:with-param name="node" select="current()" />
        </xsl:call-template>
    </xsl:template>


    <xsl:template name="lang">
        <xsl:param name="node" />
        <xsl:variable name="lang-is-new" as="xsd:boolean">
            <xsl:call-template name="lang-is-new">
                <xsl:with-param name="node" select="$node" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$lang-is-new">
            <xsl:variable name="lang">
                <xsl:call-template name="get-lang">
                    <xsl:with-param name="node" select="$node" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="not($lang=$main-lang)">
                    <dtb:span>
                        <xsl:attribute name="lang">
                            <xsl:text>__</xsl:text>
                            <xsl:value-of select="$lang" />
                            <xsl:text>.ctb</xsl:text>
                        </xsl:attribute>
                        <dtb:span class="lang-wrap">
                            <xsl:call-template name="scan-lang">
                                <xsl:with-param name="node" select="$node" />
                            </xsl:call-template>
                        </dtb:span>
                    </dtb:span>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="scan-lang">
                        <xsl:with-param name="node" select="$node" />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>


    <xsl:template name="scan-lang">
        <xsl:param name="node" />
        <xsl:param name="first" as="xsd:boolean" select="true()" />
        <xsl:variable name="continue-scanning" as="xsd:boolean">
            <xsl:choose>
                <xsl:when test="$first">
                    <xsl:value-of select="true()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="lang-is-new" as="xsd:boolean">
                        <xsl:call-template name="lang-is-new">
                            <xsl:with-param name="node" select="$node" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:value-of select="not($lang-is-new)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="$continue-scanning">
            <xsl:call-template name="typeface">
                <xsl:with-param name="node" select="$node" />
            </xsl:call-template>
            <xsl:variable name="next" as="element()*">
                <xsl:call-template name="get-following-text">
                    <xsl:with-param name="current" select="$node"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$next">
                <xsl:call-template name="scan-lang">
                    <xsl:with-param name="node" select="$next" />
                    <xsl:with-param name="first" select="false()" />
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
    </xsl:template>


    <xsl:template name="typeface">
        <xsl:param name="node" />
        <xsl:variable name="typeface-is-new" as="xsd:boolean">
            <xsl:call-template name="typeface-is-new">
                <xsl:with-param name="node" select="$node" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$typeface-is-new">
            <xsl:variable name="typeface" as="xsd:string">
                <xsl:call-template name="get-typeface">
                    <xsl:with-param name="node" select="$node" />
                </xsl:call-template>
            </xsl:variable>
            <xsl:choose>
                <xsl:when test="$typeface='strong'">
                    <dtb:strong>
                        <xsl:call-template name="scan-typeface">
                            <xsl:with-param name="node" select="$node" />
                        </xsl:call-template>
                    </dtb:strong>
                </xsl:when>
                <xsl:when test="$typeface='em'">
                    <dtb:em>
                        <xsl:call-template name="scan-typeface">
                            <xsl:with-param name="node" select="$node" />
                        </xsl:call-template>
                    </dtb:em>
                </xsl:when>
                <xsl:when test="$typeface='underline'">
                    <dtb:em>
                        <xsl:call-template name="scan-typeface">
                            <xsl:with-param name="node" select="$node" />
                        </xsl:call-template>
                    </dtb:em>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:call-template name="scan-typeface">
                        <xsl:with-param name="node" select="$node" />
                    </xsl:call-template>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:template>


    <xsl:template name="scan-typeface">
        <xsl:param name="node" />
        <xsl:param name="first" as="xsd:boolean" select="true()" />
        <xsl:variable name="continue-scanning" as="xsd:boolean">
            <xsl:choose>
                <xsl:when test="$first">
                    <xsl:value-of select="true()"/>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:variable name="typeface-is-new" as="xsd:boolean">
                        <xsl:call-template name="typeface-is-new">
                            <xsl:with-param name="node" select="$node" />
                        </xsl:call-template>
                    </xsl:variable>
                    <xsl:value-of select="not($typeface-is-new)"/>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test="$continue-scanning">
            <xsl:apply-templates select="$node" mode="text" />
            <xsl:variable name="next" as="element()*">
                <xsl:call-template name="get-following-text">
                    <xsl:with-param name="current" select="$node"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$next">
                <xsl:call-template name="scan-typeface">
                    <xsl:with-param name="node" select="$next" />
                    <xsl:with-param name="first" select="false()" />
                </xsl:call-template>
            </xsl:if>
        </xsl:if>
    </xsl:template>


    <xsl:template name="get-lang">
        <xsl:param name="node" />
        <xsl:choose>
            <xsl:when test="count($node/ancestor::dtb:span[@lang]) > 0 ">
                <xsl:variable name="index" as="xsd:integer">
                    <xsl:call-template name="get-locale-index">
                        <xsl:with-param name="locale" select="$node/ancestor::dtb:span[@lang][1]/@lang" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$index>0">
                        <xsl:value-of select="$paramTranslationTables[$index]" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="$main-lang" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="$main-lang" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    

    <xsl:template name="get-locale-index">
        <xsl:param name="locale" />
        <xsl:call-template name="first-index-of">
            <xsl:with-param name="array" select="$paramLocales"/>
            <xsl:with-param name="value" select="$locale"/>
            <xsl:with-param name="case-sensitive" select="false()"/>
        </xsl:call-template>
    </xsl:template>


    <xsl:template name="lang-is-new">
        <xsl:param name="node" />
        <xsl:variable name="next" as="element()*">
            <xsl:call-template name="get-following-text">
                <xsl:with-param name="current" select="$node/preceding::dtb:text[1]"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$next">
                <xsl:variable name="current-lang">
                    <xsl:call-template name="get-lang">
                        <xsl:with-param name="node" select="$node" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:variable name="preceding-lang">
                    <xsl:call-template name="get-lang">
                        <xsl:with-param name="node" select="$node/preceding::dtb:text[1]" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$current-lang = $preceding-lang">
                        <xsl:value-of select="false()" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="true()" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="true()" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>

    <xsl:template name="get-typeface">
        <xsl:param name="node" />
        <xsl:variable name="style" as="xsd:string">
            <xsl:choose>
                <xsl:when test="$node/ancestor::dtb:span[@style]">
                    <xsl:value-of select="$node/ancestor::dtb:span[@style][1]/@style" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="'Default'" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="keep-boldface" as="xsd:boolean">
            <xsl:call-template name="get-keep-boldface">
                <xsl:with-param name="style-name" select="$style" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="keep-italic" as="xsd:boolean">
            <xsl:call-template name="get-keep-italic">
                <xsl:with-param name="style-name" select="$style" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:variable name="keep-underline" as="xsd:boolean">
            <xsl:call-template name="get-keep-underline">
                <xsl:with-param name="style-name" select="$style" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$keep-boldface
                        and (count($node/ancestor::dtb:span[@font-weight='bold'  ][1]/ancestor::*)
                           > count($node/ancestor::dtb:span[@font-weight='normal'][1]/ancestor::*))">
                <xsl:value-of select="'strong'" />
            </xsl:when>
            <xsl:when test="$keep-italic
                        and (count($node/ancestor::dtb:span[@font-style='italic'][1]/ancestor::*)
                           > count($node/ancestor::dtb:span[@font-style='normal'][1]/ancestor::*))">
                <xsl:value-of select="'em'" />
            </xsl:when>
            <xsl:when test="$keep-underline
                        and (count($node/ancestor::dtb:span[@underline-style][not(@underline-style='none')][1]/ancestor::*)
                           > count($node/ancestor::dtb:span                  [    @underline-style='none' ][1]/ancestor::*))">
                <xsl:value-of select="'em'" />
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="'none'" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    

    <xsl:template name="get-caps">
        <xsl:param name="node" />
        <xsl:variable name="style" as="xsd:string">
            <xsl:choose>
                <xsl:when test="$node/ancestor::dtb:span[@style]">
                    <xsl:value-of select="$node/ancestor::dtb:span[@style][1]/@style" />
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="''" />
                </xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:variable name="keep-caps" as="xsd:boolean">
            <xsl:call-template name="get-keep-caps">
                <xsl:with-param name="style-name" select="$style" />
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>            
            <xsl:when test="$keep-caps">
                <xsl:choose>
                    <xsl:when test="count($node/ancestor::dtb:span[                     @text-transform='uppercase' ][1]/ancestor::*)
                                  > count($node/ancestor::dtb:span[@text-transform][not(@text-transform='uppercase')][1]/ancestor::*)">
                        <xsl:value-of select="true()" />
                    </xsl:when>
                    <xsl:when test="count($node/ancestor::dtb:span[                   @font-variant='small-caps' ][1]/ancestor::*)
                                  > count($node/ancestor::dtb:span[@font-variant][not(@font-variant='small-caps')][1]/ancestor::*)">
                        <xsl:value-of select="true()" />
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="false()" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="false()" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="get-keep-boldface">
        <xsl:param name="style-name" as="xsd:string"/>
        <xsl:call-template name="array-contains-value">
            <xsl:with-param name="array" select="$paramKeepBoldfaceStyles"/>
            <xsl:with-param name="value" select="$style-name"/>
        </xsl:call-template>
    </xsl:template>
    
    
    <xsl:template name="get-keep-italic">
        <xsl:param name="style-name" as="xsd:string"/>
        <xsl:call-template name="array-contains-value">
            <xsl:with-param name="array" select="$paramKeepItalicStyles"/>
            <xsl:with-param name="value" select="$style-name"/>
        </xsl:call-template>
    </xsl:template>
    
    
    <xsl:template name="get-keep-underline">
        <xsl:param name="style-name" as="xsd:string"/>
        <xsl:call-template name="array-contains-value">
            <xsl:with-param name="array" select="$paramKeepUnderlineStyles"/>
            <xsl:with-param name="value" select="$style-name"/>
        </xsl:call-template>
    </xsl:template>
    
    
    <xsl:template name="get-keep-caps">
        <xsl:param name="style-name" as="xsd:string"/>
        <xsl:call-template name="array-contains-value">
            <xsl:with-param name="array" select="$paramKeepCapsStyles"/>
            <xsl:with-param name="value" select="$style-name"/>
        </xsl:call-template>
    </xsl:template>


    <xsl:template name="typeface-is-new">
        <xsl:param name="node" />
        <xsl:variable name="next" as="element()*">
            <xsl:call-template name="get-following-text">
                <xsl:with-param name="current" select="$node/preceding::dtb:text[1]"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:choose>
            <xsl:when test="$next">
                <xsl:variable name="lang-is-new" as="xsd:boolean">
                    <xsl:call-template name="lang-is-new">
                        <xsl:with-param name="node" select="$node" />
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="not($lang-is-new)">
                        <xsl:variable name="current-typeface" as="xsd:string">
                            <xsl:call-template name="get-typeface">
                                <xsl:with-param name="node" select="$node" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:variable name="preceding-typeface" as="xsd:string">
                            <xsl:call-template name="get-typeface">
                                <xsl:with-param name="node" select="$node/preceding::dtb:text[1]" />
                            </xsl:call-template>
                        </xsl:variable>
                        <xsl:choose>
                            <xsl:when test="$current-typeface = $preceding-typeface">
                                <xsl:value-of select="false()" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="true()" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="true()" />
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select="true()" />
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>


    <xsl:template name="get-following-text">
        <xsl:param name="current" />
        <xsl:param name="next" as="element()*">
            <xsl:sequence select="$current/following::*
                               [not(self::dtb:span[@lang or
                                                   @style or
                                                   @font-style or
                                                   @font-weight or
                                                   @font-variant or
                                                   @text-transform or
                                                   @underline-style])][1][self::dtb:text]"/>
        </xsl:param>
        <xsl:if test="$next">
            <xsl:variable name="current-level" as="xsd:integer">
                <xsl:call-template name="get-level">
                    <xsl:with-param name="node" select="$current"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="next-level" as="xsd:integer">
                <xsl:call-template name="get-level">
                    <xsl:with-param name="node" select="$next"/>
                </xsl:call-template>
            </xsl:variable>
            <xsl:if test="$current-level = $next-level">
                <xsl:sequence select="$next"/>
            </xsl:if>
        </xsl:if>
    </xsl:template>

    <xsl:template name="get-level">
        <xsl:param name="node" />
        <xsl:value-of select="count($node/ancestor::*[not(self::dtb:span[@lang or
                                                                         @style or
                                                                         @font-style or
                                                                         @font-weight or
                                                                         @font-variant or
                                                                         @text-transform or
                                                                         @underline-style])][1]/ancestor::*)"/>
    </xsl:template>

</xsl:stylesheet>
